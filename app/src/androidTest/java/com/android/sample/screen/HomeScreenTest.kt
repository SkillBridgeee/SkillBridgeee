package com.android.sample.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModel
import com.android.sample.*
import com.android.sample.model.skill.MainSubject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FakeMainPageViewModel : ViewModel() {
    data class UiState(
        val welcomeMessage: String = "Welcome Test User!",
        val subjects: List<MainSubject> = listOf(MainSubject.ACADEMICS, MainSubject.MUSIC),
        val tutors: List<TutorCardUi> = listOf(
            TutorCardUi("Alice", "Math", 5.0, 12, 30),
            TutorCardUi("Bob", "Music", 4.0, 7, 25)
        )
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    val navigationEvent = MutableStateFlow<MainSubject?>(null)

    fun onAddTutorClicked(userId: String) {}
    fun onBookTutorClicked(name: String) {}
    fun onNavigationHandled() { navigationEvent.value = null }
}

class HomeScreenTest {



    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()


    @Test
    fun greetingSection_displaysTexts() {
        composeRule.setContent {
            MaterialTheme { GreetingSection("Welcome John!") }
        }

        composeRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()
        composeRule.onNodeWithText("Welcome John!").assertIsDisplayed()
        composeRule.onNodeWithText("Ready to learn something new today?").assertIsDisplayed()
    }

    @Test
    fun exploreSubjects_displaysCardsAndHandlesClick() {
        var clickedSubject: MainSubject? = null
        val subjects = listOf(MainSubject.ACADEMICS, MainSubject.MUSIC)

        composeRule.setContent {

            ExploreSubjects(subjects) { clickedSubject = it }

        }

        // Ensure section title and cards are visible
        composeRule.onNodeWithTag(HomeScreenTestTags.EXPLORE_SKILLS_SECTION).assertIsDisplayed()
        composeRule.onAllNodesWithTag(HomeScreenTestTags.SKILL_CARD).assertCountEquals(2)

        // Click on first subject card
        composeRule.onAllNodesWithTag(HomeScreenTestTags.SKILL_CARD)[0].performClick()
        assertEquals(MainSubject.ACADEMICS, clickedSubject)
    }


    @Test
    fun subjectCard_displaysSubjectNameAndRespondsToClick() {
        var clicked: MainSubject? = null

        composeRule.setContent {
            SubjectCard(
                subject = MainSubject.MUSIC,
                color = Color.Blue,
                onSubjectCardClicked = { clicked = it }
            )
        }

        composeRule.onNodeWithTag(HomeScreenTestTags.SKILL_CARD).assertIsDisplayed()
        composeRule.onNodeWithText("MUSIC").assertIsDisplayed()

        composeRule.onNodeWithTag(HomeScreenTestTags.SKILL_CARD).performClick()
        assertEquals(MainSubject.MUSIC, clicked)
    }


    @Test
    fun tutorsSection_displaysTutorsAndCallsBookCallback() {
        var bookedTutor: String? = null
        val tutors = listOf(
            TutorCardUi(name = "Alice", subject = "Math", ratingStars = 5, ratingCount = 10, hourlyRate = 30.0),
            TutorCardUi(name = "Bob", subject = "Music", ratingStars = 4, ratingCount = 5, hourlyRate = 25.0)
        )

        composeRule.setContent {
            TutorsSection(tutors, onBookClick = { bookedTutor = it })
        }

        composeRule.onNodeWithTag(HomeScreenTestTags.TOP_TUTOR_SECTION).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeScreenTestTags.TUTOR_LIST).assertIsDisplayed()
        composeRule.onAllNodesWithTag(HomeScreenTestTags.TUTOR_CARD).assertCountEquals(2)

        // Click "Book" button of first tutor
        composeRule.onAllNodesWithTag(HomeScreenTestTags.TUTOR_BOOK_BUTTON)[0].performClick()
        assertEquals("Alice", bookedTutor)
    }


    @Test
    fun exploreSubjects_handlesEmptyListGracefully() {
        composeRule.setContent {
            ExploreSubjects(emptyList(), {})
        }

        // Still shows section even if no subjects
        composeRule.onNodeWithTag(HomeScreenTestTags.EXPLORE_SKILLS_SECTION).assertIsDisplayed()
    }

    @Test
    fun tutorsSection_handlesEmptyListGracefully() {
        composeRule.setContent {
            TutorsSection(emptyList()) {}
        }

        composeRule.onNodeWithTag(HomeScreenTestTags.TOP_TUTOR_SECTION).assertIsDisplayed()
    }



}
