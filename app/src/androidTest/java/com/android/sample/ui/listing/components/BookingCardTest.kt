package com.android.sample.ui.listing.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.map.Location
import com.android.sample.model.user.Profile
import com.android.sample.ui.listing.ListingScreenTestTags
import java.util.Date
import org.junit.Rule
import org.junit.Test

class BookingCardTest {

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

  @Test
  fun bookingCard_displaysPendingStatus() {
    val booking = sampleBooking.copy(status = BookingStatus.PENDING)

    compose.setContent {
      BookingCard(booking = booking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})
    }

    compose.onNodeWithText("PENDING").assertIsDisplayed()
  }

  @Test
  fun bookingCard_displaysConfirmedStatus() {
    val booking = sampleBooking.copy(status = BookingStatus.CONFIRMED)

    compose.setContent {
      BookingCard(booking = booking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})
    }

    compose.onNodeWithText("CONFIRMED").assertIsDisplayed()
  }

  @Test
  fun bookingCard_displaysCancelledStatus() {
    val booking = sampleBooking.copy(status = BookingStatus.CANCELLED)

    compose.setContent {
      BookingCard(booking = booking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})
    }

    compose.onNodeWithText("CANCELLED").assertIsDisplayed()
  }

  @Test
  fun bookingCard_displaysCompletedStatus() {
    val booking = sampleBooking.copy(status = BookingStatus.COMPLETED)

    compose.setContent {
      BookingCard(booking = booking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})
    }

    compose.onNodeWithText("COMPLETED").assertIsDisplayed()
  }

  @Test
  fun bookingCard_displaysBookerName() {
    compose.setContent {
      BookingCard(
          booking = sampleBooking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})
    }

    compose.onNodeWithText("Jane Smith").assertIsDisplayed()
  }

  @Test
  fun bookingCard_withoutBookerProfile_handlesGracefully() {
    compose.setContent {
      BookingCard(booking = sampleBooking, bookerProfile = null, onApprove = {}, onReject = {})
    }

    compose.onNodeWithTag(ListingScreenTestTags.BOOKING_CARD).assertIsDisplayed()
  }

  @Test
  fun bookingCard_displaysStartTime() {
    compose.setContent {
      BookingCard(
          booking = sampleBooking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})
    }

    compose.onNodeWithText("Start:", substring = true).assertIsDisplayed()
  }

  @Test
  fun bookingCard_displaysEndTime() {
    compose.setContent {
      BookingCard(
          booking = sampleBooking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})
    }

    compose.onNodeWithText("End:", substring = true).assertIsDisplayed()
  }

  @Test
  fun bookingCard_displaysPrice() {
    compose.setContent {
      BookingCard(
          booking = sampleBooking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})
    }

    compose.onNodeWithText("Price:", substring = true).assertIsDisplayed()
    compose.onNodeWithText("$50.00", substring = true).assertIsDisplayed()
  }

  @Test
  fun bookingCard_pendingStatus_showsApproveButton() {
    val booking = sampleBooking.copy(status = BookingStatus.PENDING)

    compose.setContent {
      BookingCard(booking = booking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})
    }

    compose.onNodeWithTag(ListingScreenTestTags.APPROVE_BUTTON).assertIsDisplayed()
    compose.onNodeWithText("Approve").assertIsDisplayed()
  }

  @Test
  fun bookingCard_pendingStatus_showsRejectButton() {
    val booking = sampleBooking.copy(status = BookingStatus.PENDING)

    compose.setContent {
      BookingCard(booking = booking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})
    }

    compose.onNodeWithTag(ListingScreenTestTags.REJECT_BUTTON).assertIsDisplayed()
    compose.onNodeWithText("Reject").assertIsDisplayed()
  }

  @Test
  fun bookingCard_confirmedStatus_hidesActionButtons() {
    val booking = sampleBooking.copy(status = BookingStatus.CONFIRMED)

    compose.setContent {
      BookingCard(booking = booking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})
    }

    compose.onNodeWithTag(ListingScreenTestTags.APPROVE_BUTTON).assertDoesNotExist()
    compose.onNodeWithTag(ListingScreenTestTags.REJECT_BUTTON).assertDoesNotExist()
  }

  @Test
  fun bookingCard_cancelledStatus_hidesActionButtons() {
    val booking = sampleBooking.copy(status = BookingStatus.CANCELLED)

    compose.setContent {
      BookingCard(booking = booking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})
    }

    compose.onNodeWithTag(ListingScreenTestTags.APPROVE_BUTTON).assertDoesNotExist()
    compose.onNodeWithTag(ListingScreenTestTags.REJECT_BUTTON).assertDoesNotExist()
  }

  @Test
  fun bookingCard_completedStatus_hidesActionButtons() {
    val booking = sampleBooking.copy(status = BookingStatus.COMPLETED)

    compose.setContent {
      BookingCard(booking = booking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})
    }

    compose.onNodeWithTag(ListingScreenTestTags.APPROVE_BUTTON).assertDoesNotExist()
    compose.onNodeWithTag(ListingScreenTestTags.REJECT_BUTTON).assertDoesNotExist()
  }

  @Test
  fun bookingCard_approveButton_isClickable() {
    var approveCalled = false
    val booking = sampleBooking.copy(status = BookingStatus.PENDING)

    compose.setContent {
      BookingCard(
          booking = booking,
          bookerProfile = sampleBooker,
          onApprove = { approveCalled = true },
          onReject = {})
    }

    compose.onNodeWithTag(ListingScreenTestTags.APPROVE_BUTTON).assertHasClickAction()
    compose.onNodeWithTag(ListingScreenTestTags.APPROVE_BUTTON).performClick()

    assert(approveCalled)
  }

  @Test
  fun bookingCard_rejectButton_isClickable() {
    var rejectCalled = false
    val booking = sampleBooking.copy(status = BookingStatus.PENDING)

    compose.setContent {
      BookingCard(
          booking = booking,
          bookerProfile = sampleBooker,
          onApprove = {},
          onReject = { rejectCalled = true })
    }

    compose.onNodeWithTag(ListingScreenTestTags.REJECT_BUTTON).assertHasClickAction()
    compose.onNodeWithTag(ListingScreenTestTags.REJECT_BUTTON).performClick()

    assert(rejectCalled)
  }

  @Test
  fun bookingCard_displaysPriceWithCorrectFormat() {
    val booking = sampleBooking.copy(price = 123.45)

    compose.setContent {
      BookingCard(booking = booking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})
    }

    compose.onNodeWithText("$123.45", substring = true).assertIsDisplayed()
  }

  @Test
  fun bookingCard_hasCorrectTestTag() {
    compose.setContent {
      BookingCard(
          booking = sampleBooking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})
    }

    compose.onNodeWithTag(ListingScreenTestTags.BOOKING_CARD).assertExists()
  }

  @Test
  fun bookingCard_formatsDateCorrectly() {
    val specificDate = Date(1700000000000L) // Nov 14, 2023
    val booking = sampleBooking.copy(sessionStart = specificDate, sessionEnd = specificDate)

    compose.setContent {
      BookingCard(booking = booking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})
    }

    compose.onNodeWithText("Nov", substring = true).assertExists()
  }
}
