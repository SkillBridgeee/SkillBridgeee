package com.android.sample.ui.listing.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
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
      listing: Proposal = sampleListing,
      creator: Profile? = sampleCreator,
      isLoading: Boolean = false,
      error: String? = null,
      isOwnListing: Boolean = false,
      bookingInProgress: Boolean = false,
      bookingError: String? = null,
      bookingSuccess: Boolean = false,
      tutorRatingPending: Boolean = false,
      bookingsLoading: Boolean = false,
      listingBookings: List<com.android.sample.model.booking.Booking> = emptyList(),
      bookerProfiles: Map<String, Profile> = emptyMap()
  ): ListingUiState {
    return ListingUiState(
        listing = listing,
        creator = creator,
        isLoading = isLoading,
        error = error,
        isOwnListing = isOwnListing,
        bookingInProgress = bookingInProgress,
        bookingError = bookingError,
        bookingSuccess = bookingSuccess,
        tutorRatingPending = tutorRatingPending,
        bookingsLoading = bookingsLoading,
        listingBookings = listingBookings,
        bookerProfiles = bookerProfiles,
        listingDeleted = false)
  }

  // ---------- Tests ----------

  //  @Test
  //  fun listingContent_showsTutorRatingSection_whenOwnListingAndPending() {
  //    val state = uiState(isOwnListing = true, tutorRatingPending = true)
  //
  //    compose.setContent {
  //      MaterialTheme {
  //        ListingContent(
  //            uiState = state,
  //            onBook = { _, _ -> },
  //            onApproveBooking = {},
  //            onRejectBooking = {},
  //            onSubmitTutorRating = {},
  //            onDeleteListing = {},
  //            onEditListing = {})
  //      }
  //    }
  //
  //    // Wait up to 5s for the node to appear in either the unmerged or merged semantics tree,
  //    // then pick the tree that contains it and perform the scroll.
  //    val tag = ListingScreenTestTags.TUTOR_RATING_SECTION
  //    compose.waitUntil(5000) {
  //      compose
  //          .onAllNodes(hasTestTag(tag), useUnmergedTree = true)
  //          .fetchSemanticsNodes()
  //          .isNotEmpty() ||
  //          compose
  //              .onAllNodes(hasTestTag(tag), useUnmergedTree = false)
  //              .fetchSemanticsNodes()
  //              .isNotEmpty()
  //    }
  //
  //    val node =
  //        if (compose
  //            .onAllNodes(hasTestTag(tag), useUnmergedTree = true)
  //            .fetchSemanticsNodes()
  //            .isNotEmpty()) {
  //          compose.onNodeWithTag(tag, useUnmergedTree = true)
  //        } else {
  //          compose.onNodeWithTag(tag, useUnmergedTree = false)
  //        }
  //
  //    node.performScrollTo()
  //    node.assertIsDisplayed()
  //  }

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

  @Test
  fun listingContent_showsEditButton_whenOwnListing() {
    val state = uiState(isOwnListing = true)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            onSubmitTutorRating = {})
      }
    }

    compose.onNodeWithTag(ListingContentTestTags.EDIT_BUTTON).assertExists()
  }

  @Test
  fun listingContent_editButtonEnabled_whenNoActiveBookings() {
    val state = uiState(isOwnListing = true, bookingsLoading = false)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            onSubmitTutorRating = {})
      }
    }

    compose.onNodeWithTag(ListingContentTestTags.EDIT_BUTTON).assertIsEnabled()
  }

  @Test
  fun listingContent_editButtonDisabled_whenBookingsLoading() {
    val state = uiState(isOwnListing = true, bookingsLoading = true)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            onSubmitTutorRating = {})
      }
    }

    compose.onNodeWithTag(ListingContentTestTags.EDIT_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun listingContent_editButtonDisabled_whenHasActiveBookings() {
    val activeBooking =
        com.android.sample.model.booking.Booking(
            bookingId = "b1",
            associatedListingId = "listing-1",
            listingCreatorId = "creator-1",
            bookerId = "booker-1",
            sessionStart = Date(),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            status = com.android.sample.model.booking.BookingStatus.PENDING,
            price = 42.5)

    val state =
        uiState(
            isOwnListing = true, bookingsLoading = false, listingBookings = listOf(activeBooking))

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

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(10)

    compose.onNodeWithTag(ListingContentTestTags.EDIT_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun listingContent_editButtonEnabled_whenOnlyCancelledBookings() {
    val cancelledBooking =
        com.android.sample.model.booking.Booking(
            bookingId = "b1",
            associatedListingId = "listing-1",
            listingCreatorId = "creator-1",
            bookerId = "booker-1",
            sessionStart = Date(),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            status = com.android.sample.model.booking.BookingStatus.CANCELLED,
            price = 42.5)

    val state =
        uiState(isOwnListing = true, bookingsLoading = false)
            .copy(listingBookings = listOf(cancelledBooking))

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            onSubmitTutorRating = {})
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(10)

    compose.onNodeWithTag(ListingContentTestTags.EDIT_BUTTON).assertIsEnabled()
  }

  @Test
  fun listingContent_showsDeleteButton_whenOwnListing() {
    val state = uiState(isOwnListing = true)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            onSubmitTutorRating = {})
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(10)

    compose.onNodeWithTag(ListingContentTestTags.DELETE_BUTTON).assertExists()
  }

  @Test
  fun listingContent_clickDeleteButton_showsConfirmationDialog() {
    val state = uiState(isOwnListing = true)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            onSubmitTutorRating = {})
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(10)

    compose.onNodeWithTag(ListingContentTestTags.DELETE_BUTTON).performClick()

    // Check for the dialog's body text instead (unique to the dialog)
    compose
        .onNodeWithText(
            "Are you sure you want to delete this listing? This action cannot be undone.")
        .assertExists()

    // Or check for both "Delete" and "Cancel" buttons in the dialog
    compose.onNodeWithText("Delete").assertExists()
    compose.onNodeWithText("Cancel").assertExists()
  }

  @Test
  fun listingContent_deleteDialogConfirm_callsCallback() {
    val state = uiState(isOwnListing = true)
    var deleteCalled = false

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = { deleteCalled = true },
            onEditListing = {},
            onSubmitTutorRating = {})
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(10)

    compose.onNodeWithTag(ListingContentTestTags.DELETE_BUTTON).performClick()
    compose.onNodeWithText("Delete").performClick()

    assert(deleteCalled)
  }

  @Test
  fun listingContent_clickEditButton_callsCallback() {
    val state = uiState(isOwnListing = true)
    var editCalled = false

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = { editCalled = true },
            onSubmitTutorRating = {})
      }
    }

    compose.onNodeWithTag(ListingContentTestTags.EDIT_BUTTON).performClick()

    assert(editCalled)
  }

  @Test
  fun listingContent_doesNotShowEditDeleteButtons_whenNotOwnListing() {
    val state = uiState(isOwnListing = false)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            onSubmitTutorRating = {})
      }
    }

    compose.onNodeWithTag(ListingContentTestTags.EDIT_BUTTON).assertDoesNotExist()
    compose.onNodeWithTag(ListingContentTestTags.DELETE_BUTTON).assertDoesNotExist()
  }
}
