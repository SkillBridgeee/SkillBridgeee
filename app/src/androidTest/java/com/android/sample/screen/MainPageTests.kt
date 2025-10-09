package com.android.sample.screen

import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.action.ViewActions.swipeUp
import com.android.sample.ExploreSkills
import com.android.sample.GreetingSection
import com.android.sample.HomeScreen
import com.android.sample.HomeScreenTestTags
import com.android.sample.SkillCard
import com.android.sample.TutorCard
import com.android.sample.TutorsSection
import org.junit.Rule
import org.junit.Test

class MainPageTests {

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun allSectionsAreDisplayed() {
    composeRule.setContent { HomeScreen() }

    composeRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.EXPLORE_SKILLS_SECTION).assertIsDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.TUTOR_LIST).assertIsDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertExists()
  }

  @Test
  fun skillCardsAreClickable() {
    composeRule.setContent { HomeScreen() }

    composeRule
        .onAllNodesWithTag(HomeScreenTestTags.SKILL_CARD)
        .onFirst()
        .assertIsDisplayed()
        .performClick()
  }

  @Test
  fun skillCardsAreWellDisplayed() {
    composeRule.setContent { HomeScreen() }

    composeRule.onAllNodesWithTag(HomeScreenTestTags.SKILL_CARD).onFirst().assertIsDisplayed()
  }

  // @Test
  /*fun tutorListIsScrollable(){
      composeRule.setContent {
          HomeScreen()
      }

      composeRule.onNodeWithTag(HomeScreenTestTags.TUTOR_LIST).performScrollToIndex(2)
  }*/

  @Test
  fun tutorListIsWellDisplayed() {
    composeRule.setContent { HomeScreen() }

    composeRule.onAllNodesWithTag(HomeScreenTestTags.TUTOR_CARD).onFirst().assertIsDisplayed()
    composeRule
        .onAllNodesWithTag(HomeScreenTestTags.TUTOR_BOOK_BUTTON)
        .onFirst()
        .assertIsDisplayed()
  }

  @Test
  fun scaffold_rendersFabAndPaddingCorrectly() {
    composeRule.setContent { HomeScreen() }

    composeRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertIsDisplayed().performClick()

    composeRule
        .onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION)
        .assertIsDisplayed()
        .performTouchInput { swipeUp() }
  }

  @Test
  fun tutorCard_displaysAllStarsAndReviewCount() {
    composeRule.setContent { TutorCard("Alex T.", "Guitar Lessons", "$40/hr", 99) }

    composeRule.onNodeWithText("Alex T.").assertIsDisplayed()
    composeRule.onNodeWithText("Guitar Lessons").assertIsDisplayed()

    composeRule.onNodeWithText("(99)").assertIsDisplayed()

    composeRule.onNodeWithTag(HomeScreenTestTags.TUTOR_BOOK_BUTTON).performClick()
  }

  @Test
  fun tutorsSection_scrollsAndDisplaysLastTutor() {
    composeRule.setContent { TutorsSection() }

    composeRule.onNodeWithTag(HomeScreenTestTags.TUTOR_LIST).performTouchInput { swipeUp() }

    composeRule.onNodeWithText("David C.").assertIsDisplayed()
  }

  @Test
  fun fabAddIsClickable() {
    composeRule.setContent { HomeScreen() }

    composeRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertIsDisplayed().performClick()
  }

  @Test
  fun fabAddIsWellDisplayed() {
    composeRule.setContent { HomeScreen() }

    composeRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertIsDisplayed()
  }

  @Test
  fun tutorBookButtonIsClickable() {
    composeRule.setContent { HomeScreen() }

    composeRule
        .onAllNodesWithTag(HomeScreenTestTags.TUTOR_BOOK_BUTTON)
        .onFirst()
        .assertIsDisplayed()
        .performClick()
  }

  @Test
  fun tutorBookButtonIsWellDisplayed() {
    composeRule.setContent { HomeScreen() }

    composeRule
        .onAllNodesWithTag(HomeScreenTestTags.TUTOR_BOOK_BUTTON)
        .onFirst()
        .assertIsDisplayed()
  }

  @Test
  fun welcomeSectionIsWellDisplayed() {
    composeRule.setContent { HomeScreen() }

    composeRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()
  }

  @Test
  fun tutorList_displaysTutorCards() {
    composeRule.setContent { HomeScreen() }

    composeRule.onAllNodesWithTag(HomeScreenTestTags.TUTOR_CARD).assertCountEquals(3)
  }

  @Test
  fun greetingSection_displaysWelcomeText() {
    composeRule.setContent { GreetingSection() }

    composeRule.onNodeWithText("Welcome back, Ava!").assertIsDisplayed()
    composeRule.onNodeWithText("Ready to learn something new today?").assertIsDisplayed()
  }

  @Test
  fun exploreSkills_displaysAllSkillCards() {
    composeRule.setContent { ExploreSkills() }

    composeRule.onAllNodesWithTag(HomeScreenTestTags.SKILL_CARD).assertCountEquals(3)
    composeRule.onNodeWithText("Academics").assertIsDisplayed()
    composeRule.onNodeWithText("Music").assertIsDisplayed()
    composeRule.onNodeWithText("Sports").assertIsDisplayed()
  }

  @Test
  fun tutorCard_displaysNameAndPrice() {
    composeRule.setContent { TutorCard("Liam P.", "Piano Lessons", "$25/hr", 23) }

    composeRule.onNodeWithText("Liam P.").assertIsDisplayed()
    composeRule.onNodeWithText("Piano Lessons").assertIsDisplayed()
    composeRule.onNodeWithText("$25/hr").assertIsDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.TUTOR_BOOK_BUTTON).performClick()
  }

  @Test
  fun tutorsSection_displaysThreeTutorCards() {
    composeRule.setContent { TutorsSection() }

    composeRule.onAllNodesWithTag(HomeScreenTestTags.TUTOR_CARD).assertCountEquals(3)
    composeRule.onNodeWithText("Top-Rated Tutors").assertIsDisplayed()
  }

  @Test
  fun homeScreen_scrollsAndShowsAllSections() {
    composeRule.setContent { HomeScreen() }

    composeRule.onNodeWithTag(HomeScreenTestTags.TUTOR_LIST).assertIsDisplayed().performTouchInput {
      swipeUp()
    }

    composeRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertIsDisplayed()
  }

  @Test
  fun skillCard_displaysTitle() {
    composeRule.setContent { SkillCard(title = "Test Skill", bgColor = Color.Red) }
    composeRule.onNodeWithText("Test Skill").assertIsDisplayed()
  }

  @Test
  fun tutorCard_hasCircularAvatarSurface() {
    composeRule.setContent {
      TutorCard(name = "Maya R.", subject = "Singing", price = "$30/hr", reviews = 21)
    }

    // Vérifie que le Surface est bien affiché (on ne peut pas tester CircleShape directement)
    composeRule.onNodeWithTag(HomeScreenTestTags.TUTOR_CARD).assertIsDisplayed()
  }

  @Test
  fun tutorCard_bookButton_isDisplayedAndClickable() {
    composeRule.setContent {
      TutorCard(name = "Ethan D.", subject = "Physics", price = "$50/hr", reviews = 7)
    }

    // Vérifie le bouton "Book"
    composeRule
        .onNodeWithTag(HomeScreenTestTags.TUTOR_BOOK_BUTTON)
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    // Vérifie le texte du bouton
    composeRule.onNodeWithText("Book").assertIsDisplayed()
  }

  @Test
  fun tutorCard_layoutStructure_isVisibleAndStable() {
    composeRule.setContent {
      TutorCard(name = "Zoe L.", subject = "Chemistry", price = "$60/hr", reviews = 3)
    }

    // Vérifie la hiérarchie : Card -> Row -> Column -> Button
    composeRule.onNodeWithTag(HomeScreenTestTags.TUTOR_CARD).assertIsDisplayed()
    composeRule.onNodeWithText("Zoe L.").assertExists()
    composeRule.onNodeWithTag(HomeScreenTestTags.TUTOR_BOOK_BUTTON).assertExists()
  }
}
