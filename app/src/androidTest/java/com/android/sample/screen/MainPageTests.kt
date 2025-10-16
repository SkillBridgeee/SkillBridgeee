 package com.android.sample.screen

 import androidx.compose.ui.test.*
 import androidx.compose.ui.test.junit4.createComposeRule
 import com.android.sample.*
 import com.android.sample.HomeScreenTestTags.WELCOME_SECTION
 import kotlinx.coroutines.ExperimentalCoroutinesApi
 import kotlinx.coroutines.test.runTest
 import org.junit.Rule
 import org.junit.Test

 @OptIn(ExperimentalCoroutinesApi::class)
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
  fun fabAdd_isDisplayed_andClickable() {
    composeRule.setContent { HomeScreen() }

    composeRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertIsDisplayed().performClick()
  }

  @Test
  fun greetingSection_displaysWelcomeText() {
    composeRule.setContent { HomeScreen() }

    composeRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()
  }

  @Test
  fun exploreSkills_displaysSkillCards() {
    composeRule.setContent { HomeScreen() }

    composeRule.onAllNodesWithTag(HomeScreenTestTags.SKILL_CARD).onFirst().assertIsDisplayed()
  }

  @Test
  fun tutorList_displaysTutorCards_andBookButtons() {
    composeRule.setContent { HomeScreen() }

    composeRule.onAllNodesWithTag(HomeScreenTestTags.TUTOR_CARD).onFirst().assertIsDisplayed()
    composeRule
        .onAllNodesWithTag(HomeScreenTestTags.TUTOR_BOOK_BUTTON)
        .onFirst()
        .assertIsDisplayed()
        .performClick()
  }

  @Test
  fun tutorsSection_displaysTopRatedTutorsHeader() {
    composeRule.setContent { HomeScreen() }

    composeRule.onNodeWithText("Top-Rated Tutors").assertIsDisplayed()
  }

  @Test
  fun homeScreen_scrollsAndShowsAllSections() {
    composeRule.setContent { HomeScreen() }

    composeRule.onNodeWithTag(HomeScreenTestTags.TUTOR_LIST).performTouchInput { swipeUp() }

    composeRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertIsDisplayed()
  }

  @Test
  fun tutorCard_displaysCorrectData() {
    val tutorUi =
        TutorCardUi(
            name = "Alex Johnson",
            subject = "Mathematics",
            hourlyRate = 40.0,
            ratingStars = 4,
            ratingCount = 120)

    composeRule.setContent { TutorCard(tutorUi, onBookClick = {}) }

    composeRule.onNodeWithTag(HomeScreenTestTags.TUTOR_CARD).assertIsDisplayed()
  }

  @Test
  fun onBookTutorClicked_doesNotCrash() = runTest {
    val vm = MainPageViewModel()
    vm.onBookTutorClicked("Some Tutor Name")
  }
 }
