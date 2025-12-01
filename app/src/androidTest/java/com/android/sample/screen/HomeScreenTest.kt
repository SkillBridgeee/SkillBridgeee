package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.ui.HomePage.ExploreSubjects
import com.android.sample.ui.HomePage.GreetingSection
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.HomePage.ProposalsSection
import com.android.sample.ui.HomePage.SubjectCard
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
        ProposalsSection(
            proposals = proposals, onProposalClick = { p -> clickedProposalId = p.listingId })
      }
    }

    composeRule.onNodeWithTag(HomeScreenTestTags.TOP_TUTOR_SECTION).assertIsDisplayed()
    composeRule.onNodeWithTag(HomeScreenTestTags.TUTOR_LIST).assertIsDisplayed()
    composeRule.onAllNodesWithTag(HomeScreenTestTags.TUTOR_CARD).assertCountEquals(2)

    // Click the first proposal card
    composeRule.onAllNodesWithTag(HomeScreenTestTags.TUTOR_CARD)[0].performClick()
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

    composeRule.onNodeWithTag(HomeScreenTestTags.TOP_TUTOR_SECTION).assertIsDisplayed()
  }
}
