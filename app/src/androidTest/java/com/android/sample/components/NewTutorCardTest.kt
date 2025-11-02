package com.android.sample.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.user.Profile
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NewTutorCardTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  /** Helper to build a normal Profile for most tests. */
  private fun sampleProfile(
      name: String = "Alice Johnson",
      description: String = "Friendly math tutor",
      locationName: String = "Campus East",
      avgRating: Double = 4.0,
      totalRatings: Int = 27,
      userId: String = "tutor-123"
  ): Profile {
    return Profile(
        userId = userId,
        name = name,
        email = "alice@example.com",
        levelOfEducation = "BSc Math",
        location = Location(name = locationName),
        hourlyRate = "25",
        description = description,
        tutorRating = RatingInfo(averageRating = avgRating, totalRatings = totalRatings),
        studentRating = RatingInfo())
  }

  @Test
  fun newTutorCard_displaysNameSubtitleRatingAndLocation() {
    val profile =
        sampleProfile(
            name = "Alice Johnson",
            description = "Friendly math tutor",
            locationName = "Campus East",
            avgRating = 4.0,
            totalRatings = 27,
            userId = "tutor-123")

    composeRule.setContent {
      MaterialTheme {
        TutorCard(
            profile = profile,
            onOpenProfile = {},
        )
      }
    }

    // Card exists with test tag
    composeRule.onNodeWithTag(TutorCardTestTags.CARD).assertIsDisplayed()

    // Name is shown
    composeRule.onNodeWithText("Alice Johnson").assertIsDisplayed()

    // Subtitle from profile.description
    composeRule.onNodeWithText("Friendly math tutor").assertIsDisplayed()

    // Rating count "(27)"
    composeRule.onNodeWithText("(27)").assertIsDisplayed()

    // Location is rendered
    composeRule.onNodeWithText("Campus East").assertIsDisplayed()
  }

  @Test
  fun newTutorCard_usesLessonsFallbackWhenDescriptionBlank() {
    val profileNoDesc =
        sampleProfile(
            description = "",
            locationName = "Main Building",
            avgRating = 3.5,
            totalRatings = 12,
            userId = "tutor-456")

    composeRule.setContent {
      MaterialTheme {
        TutorCard(
            profile = profileNoDesc,
            onOpenProfile = {},
        )
      }
    }

    // When description is blank, card shows "Lessons"
    composeRule.onNodeWithText("Lessons").assertIsDisplayed()

    // Location still shows
    composeRule.onNodeWithText("Main Building").assertIsDisplayed()
  }

  @Test
  fun newTutorCard_callsOnOpenProfileWhenClicked() {
    val profile = sampleProfile(userId = "tutor-abc", avgRating = 4.5, totalRatings = 99)
    var clickedUserId: String? = null

    composeRule.setContent {
      MaterialTheme { TutorCard(profile = profile, onOpenProfile = { uid -> clickedUserId = uid }) }
    }

    // Click the whole card
    composeRule.onNodeWithTag(TutorCardTestTags.CARD).performClick()

    // Verify callback got called with correct id
    assertEquals("tutor-abc", clickedUserId)
  }

  @Test
  fun newTutorCard_allowsSecondaryTextOverride() {
    val profile =
        sampleProfile(
            description = "This will be overridden",
            avgRating = 5.0,
            totalRatings = 100,
            userId = "tutor-777")

    composeRule.setContent {
      MaterialTheme {
        TutorCard(
            profile = profile,
            secondaryText = "Custom subtitle override",
            onOpenProfile = {},
        )
      }
    }

    // Override subtitle is shown
    composeRule.onNodeWithText("Custom subtitle override").assertIsDisplayed()

    // And rating count still shows
    composeRule.onNodeWithText("(100)").assertIsDisplayed()
  }

  @Test
  fun newTutorCard_fallbacksWhenNameAndLocationMissing() {
    // Build a profile that triggers:
    // - name = null      -> card should show "Tutor"
    // - description = "" -> subtitle "Lessons"
    // - location.name="" -> "Unknown"
    // - totalRatings = 0 -> shows "(0)"
    val profileMissingStuff =
        Profile(
            userId = "anon-id",
            name = null,
            email = "no-name@example.com",
            levelOfEducation = "",
            location = Location(name = ""),
            hourlyRate = "0",
            description = "",
            tutorRating = RatingInfo(averageRating = 0.0, totalRatings = 0),
            studentRating = RatingInfo())

    composeRule.setContent {
      MaterialTheme {
        TutorCard(
            profile = profileMissingStuff,
            onOpenProfile = {},
        )
      }
    }

    // Fallback name
    composeRule.onNodeWithText("Tutor").assertIsDisplayed()

    // Fallback subtitle
    composeRule.onNodeWithText("Lessons").assertIsDisplayed()

    // Rating count fallback "(0)"
    composeRule.onNodeWithText("No ratings yet").assertIsDisplayed()

    // Fallback location "Unknown"
    composeRule.onNodeWithText("Unknown").assertIsDisplayed()
  }
}
