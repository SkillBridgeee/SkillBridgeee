package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.*
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.user.Profile
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun greetingSection_displaysTexts() {
    composeRule.setContent { MaterialTheme { GreetingSection("Welcome John!") } }

    composeRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()
    composeRule.onNodeWithText("Welcome John!").assertIsDisplayed()
    composeRule.onNodeWithText("Ready to learn something new today?").assertIsDisplayed()
  }

  @Test
  fun exploreSubjects_displaysCardsAndHandlesClick() {
    var clickedSubject: MainSubject? = null
    val subjects = listOf(MainSubject.ACADEMICS, MainSubject.MUSIC)

    composeRule.setContent { ExploreSubjects(subjects) { clickedSubject = it } }

    composeRule.onNodeWithTag(HomeScreenTestTags.EXPLORE_SKILLS_SECTION).assertIsDisplayed()
    composeRule.onAllNodesWithTag(HomeScreenTestTags.SKILL_CARD).assertCountEquals(2)

    composeRule.onAllNodesWithTag(HomeScreenTestTags.SKILL_CARD)[0].performClick()
    assertEquals(MainSubject.ACADEMICS, clickedSubject)
  }

  @Test
  fun subjectCard_displaysSubjectNameAndRespondsToClick() {
    var clicked: MainSubject? = null

    composeRule.setContent {
      SubjectCard(
          subject = MainSubject.MUSIC, color = Color.Blue, onSubjectCardClicked = { clicked = it })
    }

    composeRule.onNodeWithTag(HomeScreenTestTags.SKILL_CARD).assertIsDisplayed()
    composeRule.onNodeWithText("MUSIC").assertIsDisplayed()

    composeRule.onNodeWithTag(HomeScreenTestTags.SKILL_CARD).performClick()
    assertEquals(MainSubject.MUSIC, clicked)
  }

  @Test
  fun tutorsSection_displaysTutorsAndCallsBookCallback() {
    var bookedTutor: String? = null

    val p1 =
        Profile(
            userId = "alice-id",
            name = "Alice",
            description = "Math tutor",
            location = Location(name = "CityA"),
            tutorRating = RatingInfo(averageRating = 5.0, totalRatings = 10))

    val p2 =
        Profile(
            userId = "bob-id",
            name = "Bob",
            description = "Music tutor",
            location = Location(name = "CityB"),
            tutorRating = RatingInfo(averageRating = 4.0, totalRatings = 5))

    val profiles = listOf(p1, p2)

    composeRule.setContent { TutorsSection(profiles, onBookClick = { bookedTutor = it }) }

    composeRule.onNodeWithTag(HomeScreenTestTags.TOP_TUTOR_SECTION).assertIsDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.TUTOR_LIST).assertIsDisplayed()
    composeRule.onAllNodesWithTag(HomeScreenTestTags.TUTOR_CARD).assertCountEquals(2)

    // Click the first tutor card (some UI implementations don't expose a separate "Book" button
    // tag)
    composeRule.onAllNodesWithTag(HomeScreenTestTags.TUTOR_CARD)[0].performClick()
    assertEquals(p1.userId, bookedTutor)
  }

  @Test
  fun exploreSubjects_handlesEmptyListGracefully() {
    composeRule.setContent { ExploreSubjects(emptyList(), {}) }

    composeRule.onNodeWithTag(HomeScreenTestTags.EXPLORE_SKILLS_SECTION).assertIsDisplayed()
  }

  @Test
  fun tutorsSection_handlesEmptyListGracefully() {
    composeRule.setContent { TutorsSection(emptyList()) {} }

    composeRule.onNodeWithTag(HomeScreenTestTags.TOP_TUTOR_SECTION).assertIsDisplayed()
  }
}
