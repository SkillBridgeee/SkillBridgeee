package com.android.sample.ui.listing.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.map.Location
import com.android.sample.model.user.Profile
import com.android.sample.ui.listing.ListingScreenTestTags
import com.android.sample.ui.listing.ListingUiState
import java.util.Date
import org.junit.Rule
import org.junit.Test

class BookingsSectionTest {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  private val sampleBooking =
      Booking(
          bookingId = "booking-123",
          associatedListingId = "listing-123",
          listingCreatorId = "creator-456",
          bookerId = "booker-789",
          sessionStart = Date(),
          sessionEnd = Date(System.currentTimeMillis() + 3600000),
          status = BookingStatus.PENDING,
          price = 50.0)

  private val sampleBooker =
      Profile(
          userId = "booker-789",
          name = "Jane Smith",
          email = "jane@example.com",
          description = "Music enthusiast",
          location = Location(latitude = 40.7128, longitude = -74.0060, name = "New York"))

  private fun setBookingsContent(
      uiState: ListingUiState,
      onApprove: (String) -> Unit = {},
      onReject: (String) -> Unit = {},
      onPaymentComplete: (String) -> Unit = {},
      onPaymentReceived: (String) -> Unit = {}
  ) {
    compose.setContent {
      LazyColumn {
        bookingsSection(
            uiState = uiState,
            onApproveBooking = onApprove,
            onRejectBooking = onReject,
            onPaymentComplete = onPaymentComplete,
            onPaymentReceived = onPaymentReceived)
      }
    }
  }

  @Test
  fun bookingsSection_displaysTitle() {
    val uiState =
        ListingUiState(
            listing = null,
            creator = null,
            isOwnListing = true,
            listingBookings = emptyList(),
            bookerProfiles = emptyMap(),
            bookingsLoading = false)

    setBookingsContent(uiState)

    compose.onNodeWithText("Bookings").assertIsDisplayed()
  }

  @Test
  fun bookingsSection_loadingState_showsProgressIndicator() {
    val uiState =
        ListingUiState(
            listing = null,
            creator = null,
            isOwnListing = true,
            listingBookings = emptyList(),
            bookerProfiles = emptyMap(),
            bookingsLoading = true)

    setBookingsContent(uiState)

    compose.onNodeWithTag(ListingScreenTestTags.BOOKINGS_LOADING).assertIsDisplayed()
  }

  @Test
  fun bookingsSection_emptyState_showsNoBookingsMessage() {
    val uiState =
        ListingUiState(
            listing = null,
            creator = null,
            isOwnListing = true,
            listingBookings = emptyList(),
            bookerProfiles = emptyMap(),
            bookingsLoading = false)

    setBookingsContent(uiState)

    compose.onNodeWithTag(ListingScreenTestTags.NO_BOOKINGS).assertIsDisplayed()
    compose.onNodeWithText("No bookings yet").assertIsDisplayed()
  }

  @Test
  fun bookingsSection_withBookings_displaysBookingCards() {
    val uiState =
        ListingUiState(
            listing = null,
            creator = null,
            isOwnListing = true,
            listingBookings = listOf(sampleBooking),
            bookerProfiles = mapOf("booker-789" to sampleBooker),
            bookingsLoading = false)

    setBookingsContent(uiState)

    compose.onNodeWithTag(ListingScreenTestTags.NO_BOOKINGS).assertDoesNotExist()
    compose.onNodeWithTag(ListingScreenTestTags.BOOKING_CARD).assertIsDisplayed()
  }

  @Test
  fun bookingsSection_multipleBookings_displaysAllCards() {
    val booking1 = sampleBooking.copy(bookingId = "booking-1")
    val booking2 = sampleBooking.copy(bookingId = "booking-2")
    val booking3 = sampleBooking.copy(bookingId = "booking-3")

    val uiState =
        ListingUiState(
            listing = null,
            creator = null,
            isOwnListing = true,
            listingBookings = listOf(booking1, booking2, booking3),
            bookerProfiles = mapOf("booker-789" to sampleBooker),
            bookingsLoading = false)

    setBookingsContent(uiState)

    compose.onAllNodesWithTag(ListingScreenTestTags.BOOKING_CARD).assertCountEquals(3)
  }

  @Test
  fun bookingsSection_bookingCards_haveApproveButton() {
    val booking = sampleBooking.copy(status = BookingStatus.PENDING)
    val uiState =
        ListingUiState(
            listing = null,
            creator = null,
            isOwnListing = true,
            listingBookings = listOf(booking),
            bookerProfiles = mapOf("booker-789" to sampleBooker),
            bookingsLoading = false)

    setBookingsContent(uiState)

    compose.onNodeWithTag(ListingScreenTestTags.APPROVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun bookingsSection_bookingCards_haveRejectButton() {
    val booking = sampleBooking.copy(status = BookingStatus.PENDING)
    val uiState =
        ListingUiState(
            listing = null,
            creator = null,
            isOwnListing = true,
            listingBookings = listOf(booking),
            bookerProfiles = mapOf("booker-789" to sampleBooker),
            bookingsLoading = false)

    setBookingsContent(uiState)

    compose.onNodeWithTag(ListingScreenTestTags.REJECT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun bookingsSection_approveCallback_triggeredWithBookingId() {
    var approvedBookingId: String? = null
    val booking = sampleBooking.copy(bookingId = "specific-id", status = BookingStatus.PENDING)

    val uiState =
        ListingUiState(
            listing = null,
            creator = null,
            isOwnListing = true,
            listingBookings = listOf(booking),
            bookerProfiles = mapOf("booker-789" to sampleBooker),
            bookingsLoading = false)

    setBookingsContent(uiState, onApprove = { approvedBookingId = it })

    compose.onNodeWithTag(ListingScreenTestTags.APPROVE_BUTTON).performClick()

    assert(approvedBookingId == "specific-id")
  }

  @Test
  fun bookingsSection_rejectCallback_triggeredWithBookingId() {
    var rejectedBookingId: String? = null
    val booking = sampleBooking.copy(bookingId = "specific-id", status = BookingStatus.PENDING)

    val uiState =
        ListingUiState(
            listing = null,
            creator = null,
            isOwnListing = true,
            listingBookings = listOf(booking),
            bookerProfiles = mapOf("booker-789" to sampleBooker),
            bookingsLoading = false)

    setBookingsContent(uiState, onReject = { rejectedBookingId = it })

    compose.onNodeWithTag(ListingScreenTestTags.REJECT_BUTTON).performClick()

    assert(rejectedBookingId == "specific-id")
  }

  @Test
  fun bookingsSection_mixedStatusBookings_displaysAll() {
    val booking1 = sampleBooking.copy(bookingId = "booking-1", status = BookingStatus.PENDING)
    val booking2 = sampleBooking.copy(bookingId = "booking-2", status = BookingStatus.CONFIRMED)
    val booking3 = sampleBooking.copy(bookingId = "booking-3", status = BookingStatus.COMPLETED)

    val uiState =
        ListingUiState(
            listing = null,
            creator = null,
            isOwnListing = true,
            listingBookings = listOf(booking1, booking2, booking3),
            bookerProfiles = mapOf("booker-789" to sampleBooker),
            bookingsLoading = false)

    setBookingsContent(uiState)

    compose.onAllNodesWithTag(ListingScreenTestTags.BOOKING_CARD).assertCountEquals(3)
    compose.onNodeWithText("PENDING").assertExists()
    compose.onNodeWithText("CONFIRMED").assertExists()
    compose.onNodeWithText("COMPLETED").assertExists()
  }

  @Test
  fun bookingsSection_withBookings_doesNotShowEmptyMessage() {
    val uiState =
        ListingUiState(
            listing = null,
            creator = null,
            isOwnListing = true,
            listingBookings = listOf(sampleBooking),
            bookerProfiles = mapOf("booker-789" to sampleBooker),
            bookingsLoading = false)

    setBookingsContent(uiState)

    compose.onNodeWithTag(ListingScreenTestTags.NO_BOOKINGS).assertDoesNotExist()
    compose.onNodeWithText("No bookings yet").assertDoesNotExist()
  }
}
