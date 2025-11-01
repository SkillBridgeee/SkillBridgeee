package com.android.sample.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProposalCardTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private fun makeProposal(
      id: String = "proposal-123",
      creatorId: String = "user-42",
      description: String = "Math tutoring for high school students",
      hourlyRate: Double = 25.0,
      locationName: String = "Campus Library",
      isActive: Boolean = true,
      skill: Skill =
          Skill(
              mainSubject = MainSubject.ACADEMICS,
              skill = "Algebra",
              skillTime = 5.0,
              expertise = ExpertiseLevel.ADVANCED),
      createdAt: Date = Date()
  ): Proposal {
    return Proposal(
        listingId = id,
        creatorUserId = creatorId,
        skill = skill,
        description = description,
        location = Location(name = locationName),
        createdAt = createdAt,
        isActive = isActive,
        hourlyRate = hourlyRate)
  }

  @Test
  fun proposalCard_displaysAllCoreInfo() {
    val proposal = makeProposal()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    composeRule.setContent { MaterialTheme { ProposalCard(proposal = proposal, onClick = {}) } }

    // Wait for composition
    composeRule.waitForIdle()

    // Card is displayed
    composeRule.onNodeWithTag(ProposalCardTestTags.CARD).assertIsDisplayed()

    // Status badge shows "Active" - use useUnmergedTree to access child nodes
    composeRule
        .onNodeWithTag(ProposalCardTestTags.STATUS_BADGE, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule.onNodeWithText("Active").assertIsDisplayed()

    // Title displays description
    composeRule
        .onNodeWithTag(ProposalCardTestTags.TITLE, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule.onNodeWithText("Math tutoring for high school students").assertIsDisplayed()

    // Description is displayed
    composeRule
        .onNodeWithTag(ProposalCardTestTags.DESCRIPTION, useUnmergedTree = true)
        .assertIsDisplayed()

    // Location is displayed (without emoji)
    composeRule
        .onNodeWithTag(ProposalCardTestTags.LOCATION, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule.onNodeWithText("Campus Library", substring = true).assertIsDisplayed()

    // Created date is displayed (without emoji)
    composeRule
        .onNodeWithTag(ProposalCardTestTags.CREATED_DATE, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule
        .onNodeWithText(dateFormat.format(proposal.createdAt), substring = true)
        .assertIsDisplayed()

    // Hourly rate is displayed
    composeRule
        .onNodeWithTag(ProposalCardTestTags.HOURLY_RATE, useUnmergedTree = true)
        .assertIsDisplayed()
    val rateText = String.format(Locale.getDefault(), "$%.2f/hr", 25.0)
    composeRule.onNodeWithText(rateText).assertIsDisplayed()
  }

  @Test
  fun proposalCard_inactiveStatus_showsInactiveBadge() {
    val proposal = makeProposal(isActive = false)

    composeRule.setContent { MaterialTheme { ProposalCard(proposal = proposal, onClick = {}) } }

    composeRule
        .onNodeWithTag(ProposalCardTestTags.STATUS_BADGE, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule.onNodeWithText("Inactive").assertIsDisplayed()
  }

  @Test
  fun proposalCard_emptyDescription_hidesDescription() {
    val proposal = makeProposal(description = "")

    composeRule.setContent { MaterialTheme { ProposalCard(proposal = proposal, onClick = {}) } }

    // Title should use skill instead
    composeRule
        .onNodeWithTag(ProposalCardTestTags.TITLE, useUnmergedTree = true)
        .assertIsDisplayed()

    // Description tag should not exist when description is empty
    composeRule
        .onNodeWithTag(ProposalCardTestTags.DESCRIPTION, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun proposalCard_emptyLocation_showsNoLocation() {
    val proposal = makeProposal(locationName = "")

    composeRule.setContent { MaterialTheme { ProposalCard(proposal = proposal, onClick = {}) } }

    composeRule
        .onNodeWithTag(ProposalCardTestTags.LOCATION, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule.onNodeWithText("📍 No location", substring = true).assertIsDisplayed()
  }

  @Test
  fun proposalCard_click_invokesCallback() {
    val proposal = makeProposal(id = "proposal-xyz")
    var clickedId: String? = null

    composeRule.setContent {
      MaterialTheme { ProposalCard(proposal = proposal, onClick = { clickedId = it }) }
    }

    // Click the card
    composeRule.onNodeWithTag(ProposalCardTestTags.CARD).performClick()

    // Verify callback was called with correct ID
    assertEquals("proposal-xyz", clickedId)
  }

  @Test
  fun proposalCard_customTestTag_usesProvidedTag() {
    val proposal = makeProposal()
    val customTag = "customProposalCard"

    composeRule.setContent {
      MaterialTheme { ProposalCard(proposal = proposal, onClick = {}, testTag = customTag) }
    }

    composeRule.onNodeWithTag(customTag).assertIsDisplayed()
  }

  @Test
  fun proposalCard_displaysTitleFromSkill_whenDescriptionBlank() {
    val proposal =
        makeProposal(
            description = "",
            skill =
                Skill(
                    mainSubject = MainSubject.MUSIC,
                    skill = "Piano",
                    skillTime = 3.0,
                    expertise = ExpertiseLevel.INTERMEDIATE))

    composeRule.setContent { MaterialTheme { ProposalCard(proposal = proposal, onClick = {}) } }

    // Should display skill as title
    composeRule
        .onNodeWithTag(ProposalCardTestTags.TITLE, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule.onNodeWithText("Piano").assertIsDisplayed()
  }

  @Test
  fun proposalCard_displayRate_10dollars() {
    val proposal = makeProposal(hourlyRate = 10.0)
    composeRule.setContent { MaterialTheme { ProposalCard(proposal = proposal, onClick = {}) } }
    composeRule.onNodeWithText("$10.00/hr").assertIsDisplayed()
  }

  @Test
  fun proposalCard_displayRate_25dollars50cents() {
    val proposal = makeProposal(hourlyRate = 25.50)
    composeRule.setContent { MaterialTheme { ProposalCard(proposal = proposal, onClick = {}) } }
    composeRule.onNodeWithText("$25.50/hr").assertIsDisplayed()
  }

  @Test
  fun proposalCard_displayRate_100dollars99cents() {
    val proposal = makeProposal(hourlyRate = 100.99)
    composeRule.setContent { MaterialTheme { ProposalCard(proposal = proposal, onClick = {}) } }
    composeRule.onNodeWithText("$100.99/hr").assertIsDisplayed()
  }

  @Test
  fun proposalCard_displayRate_zeroDollars() {
    val proposal = makeProposal(hourlyRate = 0.0)
    composeRule.setContent { MaterialTheme { ProposalCard(proposal = proposal, onClick = {}) } }
    composeRule.onNodeWithText("$0.00/hr").assertIsDisplayed()
  }

  @Test
  fun proposalCard_multipleClicks_callsCallbackMultipleTimes() {
    val proposal = makeProposal(id = "proposal-multi")
    var clickCount = 0

    composeRule.setContent {
      MaterialTheme { ProposalCard(proposal = proposal, onClick = { clickCount++ }) }
    }

    // Click multiple times
    composeRule.onNodeWithTag(ProposalCardTestTags.CARD).performClick()
    composeRule.onNodeWithTag(ProposalCardTestTags.CARD).performClick()
    composeRule.onNodeWithTag(ProposalCardTestTags.CARD).performClick()

    assertEquals(3, clickCount)
  }
}
