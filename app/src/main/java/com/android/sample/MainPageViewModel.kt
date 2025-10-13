package com.android.sample

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import com.android.sample.ui.theme.AccentBlue
import com.android.sample.ui.theme.AccentGreen
import com.android.sample.ui.theme.AccentPurple

/**
 * ViewModel for the HomeScreen.
 * Manages UI state such as skills, tutors, and user actions.
 */
class MainPageViewModel(
    private val tutorsRepository: TutorsRepository
) : ViewModel() {

    data class Skill(
        val title: String,
        val color: Color
    )

    data class Tutor(
        val name: String,
        val subject: String,
        val price: String,
        val reviews: Int,
        val rating: Int = 5
    )

    private val _skills = mutableStateListOf<Skill>()
    val skills: List<Skill> get() = _skills

    private val _tutors = mutableStateListOf<Tutor>()
    val tutors: List<Tutor> get() = _tutors

    private val _welcomeMessage = mutableStateOf("Welcome back, Ava!")
    val welcomeMessage: State<String> get() = _welcomeMessage

    init {
        loadMockData()
    }

    private fun loadMockData() {
        _skills.addAll(
            listOf(
                Skill("Academics", AccentBlue),
                Skill("Music", AccentPurple),
                Skill("Sports", AccentGreen)
            )
        )

        _tutors.addAll(
            listOf(
                Tutor("Liam P.", "Piano Lessons", "$25/hr", 23),
                Tutor("Maria G.", "Calculus & Algebra", "$30/hr", 41),
                Tutor("David C.", "Acoustic Guitar", "$20/hr", 18)
            )
        )
    }

    fun onBookTutorClicked(tutor: Tutor) {
        viewModelScope.launch {

        }
    }

    fun onAddTutorClicked() {
        viewModelScope.launch {

        }
    }
}
