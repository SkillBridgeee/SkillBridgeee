package com.android.sample.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.rating.RatingType
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileScreenUiState(
    val isLoading: Boolean = true,
    val profile: Profile? = null,
    val proposals: List<Proposal> = emptyList(),
    val requests: List<Request> = emptyList(),
    val tutorAvg: Double = 0.0,
    val tutorCount: Int = 0,
    val studentAvg: Double = 0.0,
    val studentCount: Int = 0,
    val errorMessage: String? = null
)

class ProfileScreenViewModel(
    private val profileRepository: ProfileRepository,
    private val listingRepository: ListingRepository,
    private val ratingRepository: RatingRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow(ProfileScreenUiState())
  val uiState: StateFlow<ProfileScreenUiState> = _uiState.asStateFlow()

  /**
   * Load profile and all their listings (proposals and requests).
   *
   * @param userId The ID of the user whose profile to load.
   */
  fun loadProfile(userId: String) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

      try {
        // Fetch profile
        val profile = profileRepository.getProfile(userId)

        if (profile == null) {
          _uiState.value =
              _uiState.value.copy(
                  isLoading = false, errorMessage = "Profile not found", profile = null)
          return@launch
        }

        // Fetch all listings by this user
        val listings = listingRepository.getListingsByUser(userId)

        // Separate proposals and requests
        val proposals = listings.filterIsInstance<Proposal>()
        val requests = listings.filterIsInstance<Request>()

        val allRatings = ratingRepository.getRatingsByToUser(userId)

        val tutorRatings = allRatings.filter { it.ratingType == RatingType.TUTOR }
        val studentRatings = allRatings.filter { it.ratingType == RatingType.STUDENT }

        fun avg(list: List<Rating>): Double =
            if (list.isEmpty()) 0.0
            else list.map { (it.starRating.ordinal + 1).toDouble() }.average()

        val tutorAvg = avg(tutorRatings)
        val studentAvg = avg(studentRatings)

        _uiState.value =
            _uiState.value.copy(
                isLoading = false,
                profile = profile,
                proposals = proposals,
                requests = requests,
                tutorAvg = tutorAvg,
                tutorCount = tutorRatings.size,
                studentAvg = studentAvg,
                studentCount = studentRatings.size,
                errorMessage = null)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(
                isLoading = false, errorMessage = "Failed to load profile: ${e.message}")
      }
    }
  }

  /** Refresh the profile data */
  fun refresh(userId: String) {
    loadProfile(userId)
  }

  companion object {
    fun provideFactory(
        profileRepository: ProfileRepository,
        listingRepository: ListingRepository,
        ratingRepository: RatingRepository
    ): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileScreenViewModel(profileRepository, listingRepository, ratingRepository)
                as T
          }
        }
  }
}
