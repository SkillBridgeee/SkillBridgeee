package com.android.sample.ui.communication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI state for the discussion screen. */
data class DiscussionUiState(
    val conversations: List<OverViewConversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Discussion screen.
 *
 * @param overViewConvRepository Repository for fetching conversation overviews.
 */
class DiscussionViewModel(private val overViewConvRepository: OverViewConvRepository) :
    ViewModel() {

  private val _uiState = MutableStateFlow(DiscussionUiState())
  val uiState: StateFlow<DiscussionUiState> = _uiState.asStateFlow()

  private val currentUserId: String?
    get() = UserSessionManager.getCurrentUserId()

  init {
    loadConversations()
  }

  /** Loads conversations for the current user. */
  private fun loadConversations() {
    val userId = currentUserId
    if (userId == null) {
      _uiState.update { it.copy(isLoading = false, error = "User not authenticated") }
      return
    }

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, error = null) }
      overViewConvRepository
          .listenOverView(userId)
          .catch { e ->
            val errorMessage =
                when (e) {
                  is java.io.IOException ->
                      "Unable to load conversations. Please check your internet connection and try again."
                  is IllegalArgumentException -> "Invalid data received. Please try again later."
                  is com.google.firebase.FirebaseNetworkException ->
                      "Network error. Please check your connection and try again."
                  else ->
                      "An unexpected error occurred while loading conversations. Please try again."
                }
            _uiState.update { it.copy(isLoading = false, error = errorMessage) }
          }
          .collect { conversations ->
            _uiState.update { it.copy(isLoading = false, conversations = conversations) }
          }
    }
  }

  /** Clears the error message. */
  fun clearError() {
    _uiState.update { it.copy(error = null) }
  }
}
