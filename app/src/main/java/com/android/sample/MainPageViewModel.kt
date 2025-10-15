package com.android.sample

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.listing.FakeListingRepository
import com.android.sample.model.listing.Listing
import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.Skill
import com.android.sample.model.skill.SkillsFakeRepository
import com.android.sample.model.tutor.FakeProfileRepository
import com.android.sample.model.user.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class HomeUiState(
    val welcomeMessage: String = "",
    val skills: List<Skill> = emptyList(),
    val tutors: List<TutorCardUi> = emptyList()
)

data class TutorCardUi(
    val name: String,
    val subject: String,
    val hourlyRate: Double,
    val ratingStars: Int,
    val ratingCount: Int
)

class MainPageViewModel : ViewModel() {

    private val skillRepository = SkillsFakeRepository()
    private val profileRepository = FakeProfileRepository()
    private val listingRepository = FakeListingRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    suspend fun load() {
        try {
            val skills = skillRepository.skills
            val listings = listingRepository.getFakeListings()
            val tutors = profileRepository.tutors

            val tutorCards = listings.mapNotNull { buildTutorCardSafely(it, tutors) }
            val userName = profileRepository.fakeUser.name

            _uiState.value = HomeUiState(
                welcomeMessage = "Welcome back, ${userName}!",
                skills = skills,
                tutors = tutorCards
            )
        } catch (e: Exception) {
            _uiState.value = HomeUiState(welcomeMessage = "Welcome back, Ava!")
        }
    }

    private fun buildTutorCardSafely(listing: Listing, tutors: List<Profile>): TutorCardUi? {
        return try {
            val tutor = tutors.find { it.userId == listing.creatorUserId } ?: return null

            TutorCardUi(
                name = tutor.name,
                subject = listing.skill.skill,
                hourlyRate = formatPrice(listing.hourlyRate),
                ratingStars = computeAvgStars(tutor.tutorRating),
                ratingCount = ratingCountFor(tutor.tutorRating)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun computeAvgStars(rating: RatingInfo): Int {
        if (rating.totalRatings == 0) return 0
        val avg = rating.averageRating
        return avg.roundToInt().coerceIn(0, 5)
    }

    private fun ratingCountFor(rating: RatingInfo): Int = rating.totalRatings

    private fun formatPrice(hourlyRate: Double): Double {
        return String.format("%.2f", hourlyRate).toDouble()
    }

    fun onBookTutorClicked(tutorName: String) {
        viewModelScope.launch {
            // TODO handle booking logic
        }
    }

    fun onAddTutorClicked() {
        viewModelScope.launch {
            // TODO handle add tutor
        }
    }
}
