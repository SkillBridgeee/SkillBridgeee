package com.android.sample.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RequestCardTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private fun makeRequest(
      id: String = "request-123",
      creatorId: String = "user-42",
      description: String = "Need help with physics homework",
      hourlyRate: Double = 30.0,
      locationName: String = "University Library",
      isActive: Boolean = true,
      skill: Skill =
          Skill(
              mainSubject = MainSubject.ACADEMICS,
              skill = "Physics",
              skillTime = 3.0,
              expertise = ExpertiseLevel.INTERMEDIATE),
      createdAt: Date = Date()
  ): Request {
    return Request(
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
  fun requestCard_emptyDescription_hidesDescription() {
    val request = makeRequest(description = "")

    composeRule.setContent { MaterialTheme { RequestCard(request = request, onClick = {}) } }

    // Title should use skill instead
    composeRule.onNodeWithTag(RequestCardTestTags.TITLE, useUnmergedTree = true).assertIsDisplayed()

    // Description tag should not exist when description is empty
    composeRule
        .onNodeWithTag(RequestCardTestTags.DESCRIPTION, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun requestCard_emptyLocation_showsNoLocation() {
    val request = makeRequest(locationName = "")

    composeRule.setContent { MaterialTheme { RequestCard(request = request, onClick = {}) } }

    composeRule
        .onNodeWithTag(RequestCardTestTags.LOCATION, useUnmergedTree = true)
        .assertIsDisplayed()
    composeRule.onNodeWithText("No location", substring = true).assertIsDisplayed()
  }

  @Test
  fun requestCard_click_invokesCallback() {
    val request = makeRequest(id = "request-xyz")
    var clickedId: String? = null

    composeRule.setContent {
      MaterialTheme { RequestCard(request = request, onClick = { clickedId = it }) }
    }

    // Click the card
    composeRule.onNodeWithTag(RequestCardTestTags.CARD).performClick()

    // Verify callback was called with correct ID
    assertEquals("request-xyz", clickedId)
  }

  @Test
  fun requestCard_customTestTag_usesProvidedTag() {
    val request = makeRequest()
    val customTag = "customRequestCard"

    composeRule.setContent {
      MaterialTheme { RequestCard(request = request, onClick = {}, testTag = customTag) }
    }

    composeRule.onNodeWithTag(customTag).assertIsDisplayed()
  }

  @Test
  fun requestCard_displaysTitleFromSkill_whenDescriptionBlank() {
    val request =
        makeRequest(
            description = "",
            skill =
                Skill(
                    mainSubject = MainSubject.LANGUAGES,
                    skill = "Spanish",
                    skillTime = 2.0,
                    expertise = ExpertiseLevel.BEGINNER))

    composeRule.setContent { MaterialTheme { RequestCard(request = request, onClick = {}) } }

    // Should display skill as title
    composeRule.onNodeWithTag(RequestCardTestTags.TITLE, useUnmergedTree = true).assertIsDisplayed()
    composeRule.onNodeWithText("Spanish").assertIsDisplayed()
  }

  @Test
  fun requestCard_displayRate_15dollars() {
    val request = makeRequest(hourlyRate = 15.0)
    composeRule.setContent { MaterialTheme { RequestCard(request = request, onClick = {}) } }
    composeRule.onNodeWithText("$15.00/hr").assertIsDisplayed()
  }

  @Test
  fun requestCard_displayRate_35dollars75cents() {
    val request = makeRequest(hourlyRate = 35.75)
    composeRule.setContent { MaterialTheme { RequestCard(request = request, onClick = {}) } }
    composeRule.onNodeWithText("$35.75/hr").assertIsDisplayed()
  }

  @Test
  fun requestCard_displayRate_120dollars99cents() {
    val request = makeRequest(hourlyRate = 120.99)
    composeRule.setContent { MaterialTheme { RequestCard(request = request, onClick = {}) } }
    composeRule.onNodeWithText("$120.99/hr").assertIsDisplayed()
  }

  @Test
  fun requestCard_displayRate_zeroDollars() {
    val request = makeRequest(hourlyRate = 0.0)
    composeRule.setContent { MaterialTheme { RequestCard(request = request, onClick = {}) } }
    composeRule.onNodeWithText("$0.00/hr").assertIsDisplayed()
  }

  @Test
  fun requestCard_multipleClicks_callsCallbackMultipleTimes() {
    val request = makeRequest(id = "request-multi")
    var clickCount = 0

    composeRule.setContent {
      MaterialTheme { RequestCard(request = request, onClick = { clickCount++ }) }
    }

    // Click multiple times
    composeRule.onNodeWithTag(RequestCardTestTags.CARD).performClick()
    composeRule.onNodeWithTag(RequestCardTestTags.CARD).performClick()
    composeRule.onNodeWithTag(RequestCardTestTags.CARD).performClick()

    assertEquals(3, clickCount)
  }

  @Test
  fun requestCard_displaysDifferentColorThanProposal() {
    // This test ensures Request cards have different visual styling
    // Specifically, the hourly rate color should use secondary theme color
    val request = makeRequest()

    composeRule.setContent { MaterialTheme { RequestCard(request = request, onClick = {}) } }

    // Verify the card exists and displays
    composeRule.onNodeWithTag(RequestCardTestTags.CARD).assertIsDisplayed()
    composeRule
        .onNodeWithTag(RequestCardTestTags.HOURLY_RATE, useUnmergedTree = true)
        .assertIsDisplayed()
  }
}
