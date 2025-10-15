package com.android.sample

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.listing.FakeListingRepository
import com.android.sample.model.skill.Skill
import com.android.sample.model.skill.SkillsFakeRepository
import com.android.sample.model.tutor.FakeProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    viewModelScope.launch {
      val skills = skillRepository.skills
      val listings = listingRepository.getFakeListings()
      val tutors = profileRepository.tutors

      val tutorCards =
          listings.mapNotNull { listing ->
            val tutor = tutors.find { it.userId == listing.creatorUserId } ?: return@mapNotNull null
            val avgRating = tutor.tutorRating.averageRating

            TutorCardUi(
                name = tutor.name,
                subject = listing.skill.skill,
                hourlyRate = listing.hourlyRate,
                ratingStars = avgRating.toInt(),
                ratingCount = tutor.tutorRating.totalRatings)
          }

      _uiState.value =
          HomeUiState(welcomeMessage = "Welcome back, Ava!", skills = skills, tutors = tutorCards)
    }
  }

  fun onBookTutorClicked(tutorName: String) {
    viewModelScope.launch {
      // TODO: handle booking
    }
  }

  fun onAddTutorClicked() {
    viewModelScope.launch {
      // TODO: handle add tutor
    }
  }
}
