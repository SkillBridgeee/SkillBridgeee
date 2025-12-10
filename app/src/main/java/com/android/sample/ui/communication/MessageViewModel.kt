package com.android.sample.ui.communication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.communication.ConversationManager
import com.android.sample.model.communication.ConversationManagerInter
import com.android.sample.model.communication.conversation.ConversationRepositoryProvider
import com.android.sample.model.communication.conversation.Message
import com.android.sample.model.communication.overViewConv.OverViewConvRepositoryProvider
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import java.util.Date
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConvUIState(
    val messages: List<Message> = emptyList(),
    val currentMessage: String = "",
    val currentUserId: String = "",
    val partnerName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false,
    val infoMessage: String? = null
)

class MessageViewModel(
    private val convManager: ConversationManagerInter =
        ConversationManager(
            convRepo = ConversationRepositoryProvider.repository,
            overViewRepo = OverViewConvRepositoryProvider.repository),
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(ConvUIState())
  val uiState: StateFlow<ConvUIState> = _uiState.asStateFlow()

  private var currentConvId: String? = null
  private var currentUserId: String? = null
  private var otherId: String? = null
  private var loadJob: Job? = null

  private val userError: String = "User not authenticated. Please log in to view messages."
  private val convNotFoundError: String = "Conversation not found"
  private val listenMsgError: String = "Failed to receive messages"
  private val sendMsgError: String = "Failed to send message"

  init {
    // Initialize current user ID on ViewModel creation
    currentUserId = UserSessionManager.getCurrentUserId()
    if (currentUserId != null) {
      _uiState.update { it.copy(currentUserId = currentUserId!!) }
    }
  }

  /** Start listening to real-time messages for a given conversation ID. */
  fun loadConversation(convId: String) {
    loadJob?.cancel()
    currentConvId = convId

    loadJob =
        viewModelScope.launch {
          var userId = currentUserId ?: UserSessionManager.getCurrentUserId()
          if (userId == null) {
            kotlinx.coroutines.delay(500)
            userId = UserSessionManager.getCurrentUserId()
          }
          if (userId == null) {
            _uiState.update { it.copy(error = userError) }
            return@launch
          }
          currentUserId = userId
          _uiState.update { it.copy(currentUserId = userId) }

          // Fetch the conversation to find the other user
          val conversation = convManager.getConv(convId)
          if (conversation != null) {
            otherId =
                if (conversation.convCreatorId == userId) conversation.otherPersonId
                else conversation.convCreatorId

            try {
              val partnerProfile = profileRepository.getProfile(otherId!!)
              _uiState.update { it.copy(partnerName = partnerProfile?.name ?: "User") }
            } catch (_: Exception) {
              _uiState.update { it.copy(partnerName = "User") }
            }
          } else {
            _uiState.update { it.copy(error = convNotFoundError) }
            return@launch
          }
          try {
            convManager.resetUnreadCount(convId = convId, userId = userId)
          } catch (_: Exception) {
            Log.d("MessageViewModel", "Failed to reset unread message count")
          }

          // Start listening to messages
          convManager
              .listenMessages(convId)
              .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
              .catch { _uiState.update { it.copy(isLoading = false, error = listenMsgError) } }
              .collect { messages ->
                // Conversation may have been deleted by the other user while we are on this screen
                val convStillExists = convManager.getConv(convId) != null
                if (!convStillExists) {
                  _uiState.update {
                    it.copy(
                        infoMessage = "This conversation was deleted by the other user.",
                        messages = emptyList(),
                        isLoading = false,
                        error = null)
                  }
                  return@collect
                }

                // Normal behaviour when conversation still exists
                convManager.resetUnreadCount(convId = convId, userId = userId)
                _uiState.update {
                  it.copy(
                      messages = messages.sortedBy { msg -> msg.createdAt ?: Date(0) },
                      isLoading = false)
                }
              }
        }
  }

  /** Send the current message and clear the input field. */
  fun sendMessage() {
    val convId = currentConvId ?: return
    val senderId = currentUserId ?: return
    val receiverId = otherId ?: return
    val content = _uiState.value.currentMessage.trim()
    if (content.isBlank()) return

    val message =
        Message(
            msgId = convManager.getMessageNewUid(),
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            createdAt = Date())

    viewModelScope.launch {
      try {
        convManager.sendMessage(convId, message)
        _uiState.update { it.copy(currentMessage = "") }
      } catch (_: Exception) {
        _uiState.update { it.copy(error = sendMsgError) }
      }
    }
  }

  /** Updates the text for the new message being composed. */
  fun onMessageChange(newMessage: String) {
    _uiState.update { it.copy(currentMessage = newMessage) }
  }

  /** Retry loading the conversation (useful after authentication issues). */
  fun retry() {
    currentConvId?.let { loadConversation(it) }
  }

  /** Deletes the current conversation along with its overviews. */
  fun deleteConversation() {
    val convId = currentConvId ?: return
    val userId = currentUserId ?: return

    loadJob?.cancel()
    loadJob = null

    viewModelScope.launch {
      try {
        convManager.deleteConvAndOverviews(convId, userId, otherId ?: "")

        currentConvId = null
        otherId = null

        _uiState.update { it.copy(isDeleted = true, messages = emptyList()) }
      } catch (e: Exception) {
        Log.e("MessageViewModel", "Failed to delete conversation", e)
        _uiState.update { it.copy(error = "Failed to delete conversation") }
      }
    }
  }

  /** Resets the deletion flag after the conversation has been handled. */
  fun resetDeletionFlag() {
    _uiState.update { it.copy(isDeleted = false) }
  }
}
