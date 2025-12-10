package com.android.sample.ui.communication

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

          val conversation = convManager.getConv(convId)
          if (conversation != null) {
            otherId =
                if (conversation.convCreatorId == userId) conversation.otherPersonId
                else conversation.convCreatorId

            val partner = profileRepository.getProfile(otherId!!)
            _uiState.update { it.copy(partnerName = partner?.name ?: "User", error = null) }
          } else {
            _uiState.update { it.copy(error = convNotFoundError) }
            return@launch
          }

          convManager
              .listenMessages(convId)
              .onStart { _uiState.update { it.copy(isLoading = true) } }
              .catch { _uiState.update { it.copy(isLoading = false, error = listenMsgError) } }
              .collect { messages ->
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

    _uiState.update { state ->
      state.copy(
          messages = (state.messages + message).sortedBy { it.createdAt }, currentMessage = "")
    }

    viewModelScope.launch {
      try {
        convManager.sendMessage(convId, message)
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
