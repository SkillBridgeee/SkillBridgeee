package com.android.sample.ui.communication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.communication.newImplementation.ConversationManager
import com.android.sample.model.communication.newImplementation.ConversationManagerInter
import com.android.sample.model.communication.newImplementation.conversation.ConversationRepositoryProvider
import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepositoryProvider
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
    val messages: List<MessageNew> = emptyList(),
    val currentMessage: String = "",
    val currentUserId: String = "",
    val partnerName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
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

          // Get current user id (try to refresh if null)
          var userId = currentUserId ?: UserSessionManager.getCurrentUserId()
          if (userId == null) {
            // Wait a bit and try again (Firebase might still be initializing)
            kotlinx.coroutines.delay(500)
            userId = UserSessionManager.getCurrentUserId()
          }

          if (userId == null) {
            _uiState.update { it.copy(error = userError, isLoading = false) }
            return@launch
          }
          currentUserId = userId
          _uiState.update { it.copy(currentUserId = userId) }

          // Fetch the conversation to find the other user
          val conversation = convManager.getConv(convId)
          if (conversation == null) {
            _uiState.update { it.copy(error = convNotFoundError) }
            return@launch
          }

          // Determine who is the other participant
          otherId =
              if (conversation.convCreatorId == userId) {
                conversation.otherPersonId
              } else {
                conversation.convCreatorId
              }

          // Fetch partner's profile name
          try {
            val partnerProfile = profileRepository.getProfile(otherId!!)
            _uiState.update { it.copy(partnerName = partnerProfile?.name ?: "User") }
          } catch (_: Exception) {
            _uiState.update { it.copy(partnerName = "User") }
          }

          // Reset unread message count when conversation is loaded
          try {
            convManager.resetUnreadCount(convId = convId, userId = userId)
          } catch (_: Exception) {}

          // Start listening to messages
          convManager
              .listenMessages(convId)
              .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
              .catch { _ -> _uiState.update { it.copy(isLoading = false, error = listenMsgError) } }
              .collect { messages ->
                _uiState.update {
                  it.copy(
                      messages = messages.sortedBy { msg -> msg.createdAt },
                      isLoading = false,
                      error = null)
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
        MessageNew(
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

  /** Clears the error message. */
  fun clearError() {
    _uiState.update { it.copy(error = null) }
  }
}
