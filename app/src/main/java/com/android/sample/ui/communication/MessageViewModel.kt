package com.android.sample.ui.communication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.communication.newImplementation.ConversationManager
import com.android.sample.model.communication.newImplementation.conversation.ConversationRepositoryProvider
import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepositoryProvider
import java.util.Date
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConvUIState(
    val messages: List<MessageNew> = emptyList(),
    val currentMessage: String = "",
    val currentUserId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class MessageViewModel(
    private val convManager: ConversationManager =
        ConversationManager(
            convRepo = ConversationRepositoryProvider.repository,
            overViewRepo = OverViewConvRepositoryProvider.repository)
) : ViewModel() {

  private val _uiState = MutableStateFlow(ConvUIState())
  val uiState: StateFlow<ConvUIState> = _uiState

  private var currentConvId: String? = null
  private var otherId: String? = null

  private var loadJob: Job? = null

  /** Start listening to real-time messages for a given conversation ID. */
  fun loadConversation(convId: String) {

    loadJob?.cancel()
    currentConvId = convId

    loadJob =
        viewModelScope.launch {

          // Get current user id
          val userId = UserSessionManager.getCurrentUserId()
          if (userId == null) {
            _uiState.value = _uiState.value.copy(error = "User not logged in")
            return@launch
          }

          _uiState.value = _uiState.value.copy(currentUserId = userId)

          // Fetch the conversation to find the other user
          val conversation = convManager.getConv(convId)
          if (conversation == null) {
            _uiState.value = _uiState.value.copy(error = "Conversation not found")
            return@launch
          }

          // Determine who is the other participant
          otherId =
              if (conversation.convCreatorId == userId) conversation.otherPersonId
              else conversation.convCreatorId

          // Start listening to messages
          convManager
              .listenMessages(convId)
              .onStart { _uiState.value = _uiState.value.copy(isLoading = true, error = null) }
              .catch { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
              }
              .collect { messages ->
                _uiState.value =
                    _uiState.value.copy(
                        messages = messages.sortedBy { it.createdAt },
                        isLoading = false,
                        error = null)
              }
        }
  }

  /** Send the current message and clear the input field. */
  fun sendMessage() {
    val convId = currentConvId ?: return
    val content = _uiState.value.currentMessage.trim()
    val receiver = otherId ?: return

    if (content.isBlank()) return

    val message =
        MessageNew(
            msgId = convManager.getMessageNewUid(),
            senderId = _uiState.value.currentUserId,
            receiverId = receiver,
            content = content,
            createdAt = Date())

    viewModelScope.launch {
      try {
        convManager.sendMessage(convId, message)
        _uiState.value = _uiState.value.copy(currentMessage = "")
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(error = e.message)
      }
    }
  }

  /** Updates the text for the new message being composed. */
  fun onMessageChange(newMessage: String) {
    _uiState.update { it.copy(currentMessage = newMessage) }
  }

  /** Clears the error message. */
  fun clearError() {
    _uiState.update { it.copy(error = null) }
  }
}
