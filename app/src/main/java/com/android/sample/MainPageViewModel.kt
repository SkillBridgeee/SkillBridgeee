package com.android.sample

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.ui.theme.AccentBlue
import com.android.sample.ui.theme.AccentGreen
import com.android.sample.ui.theme.AccentPurple

/**
 * ViewModel for the HomeScreen.
 * Manages UI state such as skills, tutors, and user actions.
 */
class MainPageViewModel(
    //private val tutorsRepository: TutorsRepository
) : ViewModel() {


    private val _skills = mutableStateListOf<Skill>()
    val skills: List<Skill> get() = _skills

    private val _tutors = mutableStateListOf<Profile>()
    val tutors: List<Profile> get() = _tutors

    private val _listings = mutableStateListOf<Listing>()
    val listings: List<Listing> get() = _listings



    private val _welcomeMessage = mutableStateOf("Welcome back, Ava!")
    val welcomeMessage: State<String> get() = _welcomeMessage

    init {
        loadMockData()
    }

    private fun loadMockData() {


        _tutors.addAll(
            listOf(
                Profile("12", "Liam P.", "Piano Lessons", null, "$25/hr", "", RatingInfo(4.8, 23)),
                Profile("13", "Maria G.", "Calculus & Algebra", null, "$30/hr",  "", RatingInfo(4.9, 41)),
                Profile("14", "David C.", "Acoustic Guitar", null, "$20/hr", "", RatingInfo(4.7, 18))
            )
        )

        _listings.addAll(
            listOf(
                Proposal(
                    "1",
                    "12",
                    Skill("Piano"),
                    "Experienced piano teacher",
                    Location(37.7749, -122.4194),
                    amount = 25.0
                ),
                Proposal("2",
                    "13",
                    Skill("Calculus"),
                    "Math tutor for high school students",
                    Location(34.0522, -118.2437),
                    amount = 30.0
                ),
                Proposal("3",
                    "14",
                    Skill("Guitar"),
                    "Learn acoustic guitar basics",
                    Location(40.7128, -74.0060),
                    amount = 20.0
                )
            )
        )
    }

    fun onBookTutorClicked(tutor: Profile) {
        viewModelScope.launch {

        }
    }

    fun onAddTutorClicked() {
        viewModelScope.launch {

        }
    }
    fun getTutorFromId(tutorId: String): Profile {
        return tutors.find { it.userId == tutorId }
            ?: Profile(userId = tutorId, name = "Unknown Tutor", description = "No description available")
    }

}
