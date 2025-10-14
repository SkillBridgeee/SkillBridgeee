package com.android.sample

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.listing.FakeListingRepository
import com.android.sample.model.tutor.FakeProfileRepository
import com.android.sample.model.listing.Listing
import com.android.sample.model.skill.Skill
import com.android.sample.model.skill.SkillsFakeRepository
import com.android.sample.model.user.Profile
import kotlinx.coroutines.launch

/** ViewModel for the HomeScreen. Manages UI state such as skills, tutors, and user actions. */
class MainPageViewModel(
    // private val tutorsRepository: TutorsRepository
) : ViewModel() {
    val skillRepository = SkillsFakeRepository()
    val profileRepository = FakeProfileRepository()
    val listingRepository = FakeListingRepository()

  private val _skills = skillRepository.skills
  val skills: List<Skill>
    get() = _skills

  private val _tutors = profileRepository.tutors
  val tutors: List<Profile>
    get() = _tutors

  private val _listings = listingRepository.listings
  val listings: List<Listing>
    get() = _listings

  private val _welcomeMessage = mutableStateOf("Welcome back, Ava!")
  val welcomeMessage: State<String>
    get() = _welcomeMessage






  fun onBookTutorClicked(tutor: Profile) {
    viewModelScope.launch {}
  }

  fun onAddTutorClicked() {
    viewModelScope.launch {}
  }

  fun getTutorFromId(tutorId: String): Profile {
    return tutors.find { it.userId == tutorId }
        ?: Profile(
            userId = tutorId, name = "Unknown Tutor", description = "No description available")
  }
}
