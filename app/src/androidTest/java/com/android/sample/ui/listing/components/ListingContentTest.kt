package com.android.sample.ui.listing.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
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
      listing: com.android.sample.model.listing.Listing = sampleListing,
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
      bookerProfiles: Map<String, Profile> = emptyMap(),
      hasExistingBooking: Boolean = false
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
        listingDeleted = false,
        hasExistingBooking = hasExistingBooking)
  }

  // ---------- Tests ----------

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
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

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
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

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
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
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
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
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
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
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
            isOwnListing = true,
            bookingsLoading = false,
            listingBookings = listOf(activeBooking),
        )

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
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
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
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
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
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
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
    compose.onNodeWithTag(ListingContentTestTags.DELETE_BUTTON).performClick()

    compose
        .onNodeWithText(
            "Are you sure you want to delete this listing? This action cannot be undone.")
        .assertExists()

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
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
    compose.onNodeWithTag(ListingContentTestTags.DELETE_BUTTON).performClick()
    compose.onNodeWithText("Delete").performClick()

    assert(deleteCalled)
  }

  @Test
  fun listingContent_clickEditButton_callsCallback() {
    var editClicked = false
    val state = uiState(isOwnListing = true)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = { editClicked = true },
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
    compose.onNodeWithTag(ListingContentTestTags.EDIT_BUTTON).performClick()

    assert(editClicked)
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
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag(ListingContentTestTags.EDIT_BUTTON).assertDoesNotExist()
    compose.onNodeWithTag(ListingContentTestTags.DELETE_BUTTON).assertDoesNotExist()
  }

  // ---------- Display Tests ----------

  @Test
  fun listingContent_displaysTitle() {
    val state = uiState()

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag(ListingScreenTestTags.TITLE).assertExists()
  }

  @Test
  fun listingContent_displaysDescription() {
    val state = uiState()

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag(ListingScreenTestTags.DESCRIPTION).assertExists()
    compose.onNodeWithText("Algebra tutoring for high school students").assertExists()
  }

  @Test
  fun listingContent_displaysDefaultDescription_whenEmpty() {
    val listingWithoutDescription = sampleListing.copy(description = "")
    val state = uiState(listing = listingWithoutDescription)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithText("This Listing has no Description.").assertExists()
  }

  @Test
  fun listingContent_displaysTypeBadge_proposal() {
    val state = uiState()

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag(ListingScreenTestTags.TYPE_BADGE).assertExists()
    compose.onNodeWithText("Offering to Teach").assertExists()
  }

  @Test
  fun listingContent_displaysTypeBadge_request() {
    val requestListing =
        com.android.sample.model.listing.Request(
            listingId = "request-1",
            creatorUserId = "creator-1",
            skill = sampleSkill,
            description = "Looking for algebra tutor",
            location = sampleLocation,
            hourlyRate = 40.0,
            createdAt = Date())
    val state = uiState(listing = requestListing)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag(ListingScreenTestTags.TYPE_BADGE).assertExists()
    compose.onNodeWithText("Looking for Tutor").assertExists()
  }

  @Test
  fun listingContent_displaysCreatorCard() {
    val state = uiState(creator = sampleCreator)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag(ListingScreenTestTags.CREATOR_NAME).assertExists()
    compose.onNodeWithText("Alice Tutor").assertExists()
    compose.onNodeWithText("Tap to view profile").assertExists()
  }

  @Test
  fun listingContent_creatorCard_clickable() {
    var clickedProfileId: String? = null
    val state = uiState(creator = sampleCreator)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = { clickedProfileId = it },
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag(ListingScreenTestTags.CREATOR_NAME).performClick()
    assert(clickedProfileId == "creator-1")
  }

  @Test
  fun listingContent_displaysSkillDetails() {
    val state = uiState()

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithText("Skill Details").assertExists()
    compose.onNodeWithText("Subject:").assertExists()
    compose.onNodeWithText("ACADEMICS").assertExists()
    compose.onNodeWithText("Skill:").assertExists()
    compose.onNodeWithTag(ListingScreenTestTags.SKILL).assertExists()
    compose.onNodeWithText("Algebra").assertExists()
    compose.onNodeWithText("Expertise:").assertExists()
    compose.onNodeWithTag(ListingScreenTestTags.EXPERTISE).assertExists()
    compose.onNodeWithText("INTERMEDIATE").assertExists()
  }

  @Test
  fun listingContent_displaysSkillDetails_withoutSkillName() {
    val skillWithoutName = sampleSkill.copy(skill = "")
    val listing = sampleListing.copy(skill = skillWithoutName)
    val state = uiState(listing = listing)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithText("Skill Details").assertExists()
    compose.onNodeWithText("Skill:").assertDoesNotExist()
  }

  @Test
  fun listingContent_displaysLocation() {
    val state = uiState()

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag(ListingScreenTestTags.LOCATION).assertExists()
    compose.onNodeWithText("Geneva").assertExists()
  }

  @Test
  fun listingContent_displaysHourlyRate() {
    val state = uiState()

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithText("Hourly Rate:").assertExists()
    compose.onNodeWithTag(ListingScreenTestTags.HOURLY_RATE).assertExists()
    compose.onNodeWithText("$42.50/hr").assertExists()
  }

  @Test
  fun listingContent_displaysCreatedDate() {
    val state = uiState()

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag(ListingScreenTestTags.CREATED_DATE).assertExists()
  }

  // ---------- Booking Dialog Tests ----------

  @Test
  fun listingContent_showsBookButton_whenNotOwnListing() {
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
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
    compose.onNodeWithTag(ListingScreenTestTags.BOOK_BUTTON).assertExists()
  }

  @Test
  fun listingContent_bookButton_opensDialog_noExistingBooking() {
    val state = uiState(isOwnListing = false, bookingInProgress = false)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
    compose.onNodeWithTag(ListingScreenTestTags.BOOK_BUTTON).performClick()

    compose.onNodeWithTag(ListingScreenTestTags.BOOKING_DIALOG).assertExists()
  }

  @Test
  fun listingContent_bookButton_showsDuplicateWarning_whenHasExistingBooking() {
    val state = uiState(isOwnListing = false, bookingInProgress = false, hasExistingBooking = true)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
    compose.onNodeWithTag(ListingScreenTestTags.BOOK_BUTTON).performClick()

    compose.onNodeWithTag(ListingScreenTestTags.DUPLICATE_BOOKING_DIALOG).assertExists()
    compose
        .onNodeWithText(
            "You already have a booking for this listing. Are you sure you want to create another booking?")
        .assertExists()
  }

  @Test
  fun listingContent_duplicateWarning_confirm_opensBookingDialog() {
    val state = uiState(isOwnListing = false, bookingInProgress = false, hasExistingBooking = true)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
    compose.onNodeWithTag(ListingScreenTestTags.BOOK_BUTTON).performClick()
    compose.onNodeWithTag(ListingScreenTestTags.DUPLICATE_BOOKING_CONFIRM).performClick()

    compose.onNodeWithTag(ListingScreenTestTags.BOOKING_DIALOG).assertExists()
  }

  @Test
  fun listingContent_duplicateWarning_cancel_closesDialog() {
    val state = uiState(isOwnListing = false, bookingInProgress = false, hasExistingBooking = true)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
    compose.onNodeWithTag(ListingScreenTestTags.BOOK_BUTTON).performClick()
    compose.onNodeWithTag(ListingScreenTestTags.DUPLICATE_BOOKING_CANCEL).performClick()

    compose.onNodeWithTag(ListingScreenTestTags.DUPLICATE_BOOKING_DIALOG).assertDoesNotExist()
  }

  @Test
  fun listingContent_bookButton_disabledWhileBookingInProgress() {
    val state = uiState(isOwnListing = false, bookingInProgress = true)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
    compose.onNodeWithTag(ListingScreenTestTags.BOOK_BUTTON).assertIsNotEnabled()
    compose.onNodeWithText("Creating Booking...").assertExists()
  }

  @Test
  fun listingContent_bookButton_showsBookNowText_whenNotInProgress() {
    val state = uiState(isOwnListing = false, bookingInProgress = false)

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
    compose.onNodeWithText("Book Now").assertExists()
  }

  // ---------- Bookings Section Tests (for owner) ----------

  @Test
  fun listingContent_showsBookingsSection_whenOwnListing() {
    val booking =
        com.android.sample.model.booking.Booking(
            bookingId = "b1",
            associatedListingId = "listing-1",
            listingCreatorId = "creator-1",
            bookerId = "booker-1",
            sessionStart = Date(),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            status = com.android.sample.model.booking.BookingStatus.PENDING,
            price = 42.5)

    val bookerProfile =
        Profile(
            userId = "booker-1",
            name = "Bob Student",
            email = "bob@example.com",
            description = "",
            location = sampleLocation)

    val state =
        uiState(
            isOwnListing = true,
            bookingsLoading = false,
            listingBookings = listOf(booking),
            bookerProfiles = mapOf("booker-1" to bookerProfile))

    compose.setContent {
      MaterialTheme {
        ListingContent(
            uiState = state,
            onBook = { _, _ -> },
            onApproveBooking = {},
            onRejectBooking = {},
            onDeleteListing = {},
            onEditListing = {},
            modifier = Modifier,
            onNavigateToProfile = {},
            autoFillDatesForTesting = false,
        )
      }
    }

    compose.onNodeWithTag("listingContentLazyColumn").performScrollToIndex(8)
    compose.onNodeWithText("Bookings").assertExists()
  }
}
