package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.ui.HomePage.ExploreSubjects
import com.android.sample.ui.HomePage.GreetingSection
import com.android.sample.ui.HomePage.HomeScreen
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.HomePage.MainPageViewModel
import com.android.sample.ui.HomePage.ProposalsSection
import com.android.sample.ui.HomePage.SubjectCard
import com.android.sample.utils.fakeRepo.fakeListing.FakeListingError
import com.android.sample.utils.fakeRepo.fakeListing.FakeListingWorking
import com.android.sample.utils.fakeRepo.fakeProfile.FakeProfileError
import com.android.sample.utils.fakeRepo.fakeProfile.FakeProfileWorking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun greetingSection_displaysTexts() {
    composeRule.setContent {
      MaterialTheme {
        GreetingSection(welcomeMessage = "Welcome John!", refresh = {}, enableRefresh = true)
      }
    }

    composeRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()
    composeRule.onNodeWithText("Welcome John!").assertIsDisplayed()
    composeRule.onNodeWithText("Ready to learn something new today?").assertIsDisplayed()

    composeRule.onNodeWithTag(HomeScreenTestTags.REFRESH_BUTTON).assertIsDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.REFRESH_BUTTON).performClick()
  }

  @Test
  fun whenNoError() {

    UserSessionManager.setCurrentUserId("creator_1")

    val vm =
        MainPageViewModel(
            profileRepository = FakeProfileWorking(), listingRepository = FakeListingWorking())

    composeRule.setContent {
      MaterialTheme {
        HomeScreen(
            mainPageViewModel = vm,
            onNavigateToSubjectList = {},
            onNavigateToAddNewListing = {},
        ) {}
      }
    }

    composeRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()

    composeRule.onNodeWithTag(HomeScreenTestTags.REFRESH_BUTTON).assertIsDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.REFRESH_BUTTON).performClick()

    composeRule.onNodeWithTag(HomeScreenTestTags.PROPOSAL_SECTION).assertIsDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.REQUEST_SECTION).assertIsDisplayed()

    composeRule.onNodeWithTag(HomeScreenTestTags.PROPOSAL_CARD).assertIsDisplayed()
  }

  @Test
  fun whenIdentificationError() {

    val vm =
        MainPageViewModel(
            profileRepository = FakeProfileWorking(), listingRepository = FakeListingWorking())
    composeRule.setContent {
      MaterialTheme {
        HomeScreen(
            mainPageViewModel = vm,
            onNavigateToSubjectList = {},
            onNavigateToAddNewListing = {},
        ) {}
      }
    }
    composeRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.REFRESH_BUTTON).assertIsDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.REFRESH_BUTTON).performClick()
    composeRule.onNodeWithTag(HomeScreenTestTags.PROPOSAL_SECTION).assertIsNotDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.REQUEST_SECTION).assertIsNotDisplayed()

    composeRule.onNodeWithTag(HomeScreenTestTags.ERROR_TEXT).assertIsDisplayed()
    composeRule.onNodeWithText("An error occurred during your identification.").assertIsDisplayed()
  }

  @Test
  fun whenListingError() {
    UserSessionManager.setCurrentUserId("creator_1")

    val vm =
        MainPageViewModel(
            profileRepository = FakeProfileWorking(), listingRepository = FakeListingError())
    composeRule.setContent {
      MaterialTheme {
        HomeScreen(
            mainPageViewModel = vm,
            onNavigateToSubjectList = {},
            onNavigateToAddNewListing = {},
        ) {}
      }
    }
    composeRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.REFRESH_BUTTON).assertIsDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.REFRESH_BUTTON).performClick()
    composeRule.onNodeWithTag(HomeScreenTestTags.PROPOSAL_SECTION).assertIsNotDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.REQUEST_SECTION).assertIsNotDisplayed()

    composeRule.onNodeWithTag(HomeScreenTestTags.ERROR_TEXT).assertIsDisplayed()
    composeRule
        .onNodeWithText("An error occurred while loading proposals and requests.")
        .assertIsDisplayed()
  }

  @Test
  fun whenBothError() {
    val vm =
        MainPageViewModel(
            profileRepository = FakeProfileError(), listingRepository = FakeListingError())
    composeRule.setContent {
      MaterialTheme {
        HomeScreen(
            mainPageViewModel = vm,
            onNavigateToSubjectList = {},
            onNavigateToAddNewListing = {},
        ) {}
      }
    }
    composeRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.REFRESH_BUTTON).assertIsDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.REFRESH_BUTTON).performClick()
    composeRule.onNodeWithTag(HomeScreenTestTags.PROPOSAL_SECTION).assertIsNotDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.REQUEST_SECTION).assertIsNotDisplayed()

    composeRule.onNodeWithTag(HomeScreenTestTags.ERROR_TEXT).assertIsDisplayed()
    composeRule
        .onNodeWithText(
            "An error occurred during your identification and while loading proposals and requests.")
        .assertIsDisplayed()
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
  fun proposalsSection_displaysProposalsAndCallsClickCallback() {
    var clickedProposalId: String? = null

    val proposal1 =
        Proposal(
            listingId = "proposal-alice",
            creatorUserId = "alice-id",
            description = "Math tutor",
            location = Location(name = "CityA"),
            skill = Skill())

    val proposal2 =
        Proposal(
            listingId = "proposal-bob",
            creatorUserId = "bob-id",
            description = "Music tutor",
            location = Location(name = "CityB"),
            skill = Skill())

    val proposals = listOf(proposal1, proposal2)

    composeRule.setContent {
      MaterialTheme {
        ProposalsSection(proposals = proposals, onProposalClick = { id -> clickedProposalId = id })
      }
    }

    composeRule.onNodeWithTag(HomeScreenTestTags.PROPOSAL_SECTION).assertIsDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.PROPOSAL_LIST).assertIsDisplayed()
    composeRule.onAllNodesWithTag(HomeScreenTestTags.PROPOSAL_CARD).assertCountEquals(2)

    // Click the first proposal card
    composeRule.onAllNodesWithTag(HomeScreenTestTags.PROPOSAL_CARD)[0].performClick()
    assertEquals(proposal1.listingId, clickedProposalId)
  }

  @Test
  fun exploreSubjects_handlesEmptyListGracefully() {
    composeRule.setContent { ExploreSubjects(emptyList(), {}) }

    composeRule.onNodeWithTag(HomeScreenTestTags.EXPLORE_SKILLS_SECTION).assertIsDisplayed()
  }

  @Test
  fun proposalsSection_handlesEmptyListGracefully() {
    composeRule.setContent { MaterialTheme { ProposalsSection(emptyList()) { /* no-op */} } }

    composeRule.onNodeWithTag(HomeScreenTestTags.PROPOSAL_SECTION).assertIsDisplayed()
  }
}
