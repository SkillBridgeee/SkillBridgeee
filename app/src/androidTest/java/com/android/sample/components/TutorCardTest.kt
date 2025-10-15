package com.android.sample.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.user.Profile
import com.android.sample.ui.components.TutorCard
import com.android.sample.ui.components.TutorCardTestTags
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TutorCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun fakeProfile(
      name: String = "Alice Martin",
      description: String = "Tutor 1",
      rating: RatingInfo = RatingInfo(averageRating = 4.5, totalRatings = 23)
  ) =
      Profile(
          userId = "tutor-1",
          name = name,
          email = "alice@epfl.ch",
          location = Location(0.0, 0.0, "EPFL"),
          description = description,
          tutorRating = rating)

  @Test
  fun card_showsNameSubtitlePriceAndButton() {
    val p = fakeProfile()

    composeTestRule.setContent {
      MaterialTheme {
        TutorCard(
            profile = p,
            pricePerHour = "$25/hr",
            onPrimaryAction = {},
        )
      }
    }

    composeTestRule.onNodeWithTag(TutorCardTestTags.CARD).assertIsDisplayed()
    composeTestRule.onNodeWithText("Alice Martin").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tutor 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("$25/hr").assertIsDisplayed()
    composeTestRule.onNodeWithTag(TutorCardTestTags.ACTION_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithText("Book").assertIsDisplayed()
    // rating count text e.g. "(23)"
    composeTestRule.onNodeWithText("(23)").assertIsDisplayed()
  }

  @Test
  fun card_usesPlaceholderPriceWhenNull() {
    val p = fakeProfile()

    composeTestRule.setContent {
      MaterialTheme {
        TutorCard(
            profile = p,
            pricePerHour = null,
            onPrimaryAction = {},
        )
      }
    }

    composeTestRule.onNodeWithText("—/hr").assertIsDisplayed()
  }

  @Test
  fun button_clickInvokesCallback() {
    val p = fakeProfile()
    var clicked = false

    composeTestRule.setContent {
      MaterialTheme {
        TutorCard(
            profile = p,
            onPrimaryAction = { clicked = true },
        )
      }
    }

    composeTestRule.onNodeWithTag(TutorCardTestTags.ACTION_BUTTON).performClick()
    composeTestRule.runOnIdle { assertTrue(clicked) }
  }

  @Test
  fun customTags_areApplied() {
    val p = fakeProfile()

    val customCardTag = "CustomCardTag"
    val customButtonTag = "CustomButtonTag"

    composeTestRule.setContent {
      MaterialTheme {
        TutorCard(
            profile = p,
            pricePerHour = "$10/hr",
            onPrimaryAction = {},
            cardTestTag = customCardTag,
            buttonTestTag = customButtonTag)
      }
    }

    composeTestRule.onNodeWithTag(customCardTag).assertIsDisplayed()
    composeTestRule.onNodeWithTag(customButtonTag).assertIsDisplayed()
  }

  @Test
  fun customButtonLabel_isShown() {
    val p = fakeProfile()

    composeTestRule.setContent {
      MaterialTheme {
        TutorCard(
            profile = p,
            pricePerHour = "$40/hr",
            buttonLabel = "Contact",
            onPrimaryAction = {},
        )
      }
    }

    composeTestRule.onNodeWithText("Contact").assertIsDisplayed()
  }
}
