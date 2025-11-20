package com.android.sample.ui.listing.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.ui.listing.ListingScreenTestTags
import com.android.sample.ui.listing.ListingUiState
import java.util.Date
import org.junit.Rule
import org.junit.Test

class ListingContentTest {

  @get:Rule val compose = createComposeRule()

  // ---------- Test data ----------

  private val sampleSkill =
      Skill(
          mainSubject = MainSubject.ACADEMICS,
          skill = "Algebra",
          skillTime = 2.0,
          expertise = ExpertiseLevel.INTERMEDIATE,
      )

  private val sampleLocation = Location(latitude = 0.0, longitude = 0.0, name = "Geneva")

  private val sampleListing =
      Proposal(
          listingId = "listing-1",
          creatorUserId = "creator-1",
          skill = sampleSkill,
          description = "Algebra tutoring for high school students",
          location = sampleLocation,
          hourlyRate = 42.5,
          createdAt = Date(),
      )

  private val sampleCreator =
      Profile(
          userId = "creator-1",
          name = "Alice Tutor",
          email = "alice@example.com",
          description = "Experienced math tutor",
          location = sampleLocation,
      )

  private fun uiState(
      isOwnListing: Boolean = false,
      tutorRatingPending: Boolean = false
  ): ListingUiState =
      ListingUiState(
          listing = sampleListing,
          creator = sampleCreator,
          isLoading = false,
          error = null,
          isOwnListing = isOwnListing,
          bookingInProgress = false,
          bookingError = null,
          bookingSuccess = false,
          listingBookings = emptyList(),
          bookingsLoading = false,
          bookerProfiles = emptyMap(),
          tutorRatingPending = tutorRatingPending,
      )

  // ---------- Tests ----------

  @Test
  fun listingContent_showsTutorRatingSection_whenOwnListingAndPending() {
    val state = uiState(isOwnListing = true, tutorRatingPending = true)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onSubmitTutorRating = {},
            onDeleteListing = {},
            onEditListing = {})
      }
    }

    // Wait up to 5s for the node to appear in either the unmerged or merged semantics tree,
    // then pick the tree that contains it and perform the scroll.
    val tag = ListingScreenTestTags.TUTOR_RATING_SECTION
    compose.waitUntil(5000) {
      compose
          .onAllNodes(hasTestTag(tag), useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty() ||
          compose
              .onAllNodes(hasTestTag(tag), useUnmergedTree = false)
              .fetchSemanticsNodes()
              .isNotEmpty()
    }

    val node =
        if (compose
            .onAllNodes(hasTestTag(tag), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()) {
          compose.onNodeWithTag(tag, useUnmergedTree = true)
        } else {
          compose.onNodeWithTag(tag, useUnmergedTree = false)
        }

    node.performScrollTo()
    node.assertIsDisplayed()
  }

  @Test
  fun listingContent_hidesTutorRatingSection_whenNotOwnListing() {
    val state = uiState(isOwnListing = false, tutorRatingPending = true)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onSubmitTutorRating = {},
            onEditListing = {},
            onDeleteListing = {})
      }
    }

    // Not own listing → section must not exist
    compose.onNodeWithTag(ListingScreenTestTags.TUTOR_RATING_SECTION).assertDoesNotExist()
  }

  @Test
  fun listingContent_hidesTutorRatingSection_whenNoRatingPending() {
    val state = uiState(isOwnListing = true, tutorRatingPending = false)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onSubmitTutorRating = {},
            onEditListing = {},
            onDeleteListing = {})
      }
    }

    // Own listing but no pending rating → section must not exist
    compose.onNodeWithTag(ListingScreenTestTags.TUTOR_RATING_SECTION).assertDoesNotExist()
  }
}
