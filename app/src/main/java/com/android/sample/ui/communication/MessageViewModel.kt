package com.android.sample.ui.communication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.communication.Message
import com.android.sample.model.communication.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI state for the message screen. */
data class MessageUiState(
    val messages: List<Message> = emptyList(),
    val currentMessage: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Message screen.
 *
 * @param messageRepository Repository for fetching and sending messages.
 * @param conversationId The ID of the conversation to display.
 * @param otherUserId The ID of the other user in the conversation.
 */
class MessageViewModel(
    private val messageRepository: MessageRepository,
    private val conversationId: String,
    private val otherUserId: String
) : ViewModel() {

  private val _uiState = MutableStateFlow(MessageUiState())
  val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()

  private val currentUserId: String?
    get() = UserSessionManager.getCurrentUserId()

  init {
    loadMessages()
  }

  /** Loads messages for the current conversation. */
  private fun loadMessages() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, error = null) }
      try {
        if (currentUserId == null) {
          _uiState.update { it.copy(isLoading = false, error = "User not authenticated") }
          return@launch
        }
        val messages = messageRepository.getMessagesInConversation(conversationId)
        _uiState.update { it.copy(isLoading = false, messages = messages) }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(isLoading = false, error = "Failed to load messages: ${e.message}")
        }
      }
    }
  }

  /** Updates the text for the new message being composed. */
  fun onMessageChange(newMessage: String) {
    _uiState.update { it.copy(currentMessage = newMessage) }
  }

  /** Sends the current message. */
  fun sendMessage() {
    val content = _uiState.value.currentMessage.trim()
    if (content.isEmpty()) return

    val userId = currentUserId
    if (userId == null) {
      _uiState.update { it.copy(error = "User not authenticated") }
      return
    }

    val message =
        Message(
            conversationId = conversationId,
            sentFrom = userId,
            sentTo = otherUserId,
            content = content)

    viewModelScope.launch {
      try {
        messageRepository.sendMessage(message)
        _uiState.update { it.copy(currentMessage = "") }
        // Refresh messages after sending
        loadMessages()
      } catch (e: Exception) {
        _uiState.update { it.copy(error = "Failed to send message: ${e.message}") }
      }
    }
  }

  /** Clears the error message. */
  fun clearError() {
    _uiState.update { it.copy(error = null) }
  }
}
