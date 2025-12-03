package com.android.sample.ui.communication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepositoryProvider
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI state for the discussion screen. */
data class DiscussionUiState(
    val conversations: List<OverViewConversation> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(), // Maps user ID to their name
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Discussion screen.
 *
 * @param overViewConvRepository Repository for fetching conversation overviews.
 */
class DiscussionViewModel(
    private val overViewConvRepository: OverViewConvRepository =
        OverViewConvRepositoryProvider.repository,
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(DiscussionUiState())
  val uiState: StateFlow<DiscussionUiState> = _uiState.asStateFlow()

  private val currentUserId: String?
    get() = UserSessionManager.getCurrentUserId()

  companion object {
    private const val TAG = "DiscussionViewModel"

    // Centralized error messages for consistency and future localization
    const val MSG_USER_NOT_AUTHENTICATED = "User not authenticated"
    const val MSG_NETWORK_ISSUE =
        "Unable to load conversations. Please check your internet connection and try again."
    const val MSG_INVALID_DATA = "Invalid data received. Please try again later."
    const val MSG_FIREBASE_NETWORK = "Network error. Please check your connection and try again."
    const val MSG_UNEXPECTED =
        "An unexpected error occurred while loading conversations. Please try again."
  }

  init {
    // Observe auth state to reload data on user change
    viewModelScope.launch {
      UserSessionManager.authState.collect { authState ->
        if (authState is com.android.sample.model.authentication.AuthState.Authenticated) {
          Log.d(TAG, "User authenticated, loading conversations for ${authState.userId}")
          loadConversations()
        } else {
          Log.d(TAG, "User logged out, clearing conversations.")
          _uiState.update { DiscussionUiState() } // Reset to initial state
        }
      }
    }
  }

  /** Public API to retry loading conversations. */
  fun retry() {
    loadConversations()
  }

  /** Loads conversations for the current user. */
  private fun loadConversations() {
    val userId = currentUserId
    if (userId == null) {
      Log.w(TAG, "User not authenticated when attempting to load conversations")
      _uiState.update { it.copy(isLoading = false, error = MSG_USER_NOT_AUTHENTICATED) }
      return
    }

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, error = null) }
      overViewConvRepository
          .listenOverView(userId)
          .catch { e ->
            Log.w(TAG, "Failed to load conversations", e)
            val errorMessage =
                when (e) {
                  is java.io.IOException -> MSG_NETWORK_ISSUE
                  is IllegalArgumentException -> MSG_INVALID_DATA
                  is com.google.firebase.FirebaseNetworkException -> MSG_FIREBASE_NETWORK
                  else -> MSG_UNEXPECTED
                }
            _uiState.update { it.copy(isLoading = false, error = errorMessage) }
          }
          .collect { conversations ->
            // Filter to ensure we only process conversations owned by the current user
            val myConversations = conversations.filter { it.overViewOwnerId == userId }
            Log.d(TAG, "Received ${myConversations.size} conversations owned by user $userId")

            _uiState.update { it.copy(isLoading = false, conversations = myConversations) }

            // Fetch names for the other participants
            val otherParticipantIds = myConversations.map { it.otherPersonId }.distinct()
            Log.d(
                TAG,
                "Fetching profiles for ${otherParticipantIds.size} unique participants: $otherParticipantIds")

            val participantNames = mutableMapOf<String, String>()
            otherParticipantIds.forEach { participantId ->
              try {
                val profile = profileRepository.getProfile(participantId)
                participantNames[participantId] = profile?.name ?: "Unknown User"
              } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch profile for $participantId", e)
                participantNames[participantId] = "Unknown User"
              }
            }

            _uiState.update { it.copy(participantNames = participantNames) }
          }
    }
  }

  /** Clears the error message. */
  fun clearError() {
    _uiState.update { it.copy(error = null) }
  }
}
