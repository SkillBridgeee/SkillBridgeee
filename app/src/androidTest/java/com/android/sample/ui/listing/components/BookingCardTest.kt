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

  // NEW TESTS FOR COVERAGE

  @Test
  fun bookingCard_withState_displaysCorrectly() {
    // Test the BookingCardState overload (lines 50-77)
    val state =
        BookingCardState(
            booking = sampleBooking, bookerProfile = sampleBooker, onApprove = {}, onReject = {})

    compose.setContent { BookingCard(state = state) }

    compose.onNodeWithText("PENDING").assertIsDisplayed()
    compose.onNodeWithText("Jane Smith").assertIsDisplayed()
    compose.onNodeWithTag(ListingScreenTestTags.APPROVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun bookingCard_withState_handlesCallbacks() {
    // Test callbacks work through the state-based API
    var approveCalled = false
    var rejectCalled = false

    val state =
        BookingCardState(
            booking = sampleBooking,
            bookerProfile = sampleBooker,
            onApprove = { approveCalled = true },
            onReject = { rejectCalled = true })

    compose.setContent { BookingCard(state = state) }

    compose.onNodeWithTag(ListingScreenTestTags.APPROVE_BUTTON).performClick()
    assert(approveCalled)

    compose.onNodeWithTag(ListingScreenTestTags.REJECT_BUTTON).performClick()
    assert(rejectCalled)
  }

  @Test
  fun bookingCard_showsPaymentCompleteButton_forLearner() {
    // Test lines 211-217: Payment Complete button for learner (bookerId)
    val booking =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.PENDING_PAYMENT,
            bookerId = "current-user",
            listingCreatorId = "tutor-123")

    var paymentCompleteCalled = false

    compose.setContent {
      BookingCard(
          booking = booking,
          bookerProfile = sampleBooker,
          onApprove = {},
          onReject = {},
          currentUserId = "current-user",
          onPaymentComplete = { paymentCompleteCalled = true })
    }

    // Payment Complete button should be visible for the learner
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).assertIsDisplayed()
    compose.onNodeWithText("Payment Complete").assertIsDisplayed()

    // Click and verify callback
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).performClick()
    assert(paymentCompleteCalled)
  }

  @Test
  fun bookingCard_hidesPaymentCompleteButton_forTutor() {
    // Test that Payment Complete button is NOT shown to the tutor
    val booking =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.PENDING_PAYMENT,
            bookerId = "student-123",
            listingCreatorId = "current-user")

    compose.setContent {
      BookingCard(
          booking = booking,
          bookerProfile = sampleBooker,
          onApprove = {},
          onReject = {},
          currentUserId = "current-user", // Current user is the tutor
          onPaymentComplete = {})
    }

    // Payment Complete button should NOT be visible for the tutor
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).assertDoesNotExist()
  }

  @Test
  fun bookingCard_showsPaymentReceivedButton_forTutor() {
    // Test lines 220-227: Payment Received button for tutor (listingCreatorId)
    val booking =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.PAID,
            bookerId = "student-123",
            listingCreatorId = "current-user")

    var paymentReceivedCalled = false

    compose.setContent {
      BookingCard(
          booking = booking,
          bookerProfile = sampleBooker,
          onApprove = {},
          onReject = {},
          currentUserId = "current-user", // Current user is the tutor
          onPaymentReceived = { paymentReceivedCalled = true })
    }

    // Payment Received button should be visible for the tutor
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON).assertIsDisplayed()
    compose.onNodeWithText("Payment Received").assertIsDisplayed()

    // Click and verify callback
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON).performClick()
    assert(paymentReceivedCalled)
  }

  @Test
  fun bookingCard_hidesPaymentReceivedButton_forLearner() {
    // Test that Payment Received button is NOT shown to the learner
    val booking =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.PAID,
            bookerId = "current-user",
            listingCreatorId = "tutor-123")

    compose.setContent {
      BookingCard(
          booking = booking,
          bookerProfile = sampleBooker,
          onApprove = {},
          onReject = {},
          currentUserId = "current-user", // Current user is the learner
          onPaymentReceived = {})
    }

    // Payment Received button should NOT be visible for the learner
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON).assertDoesNotExist()
  }

  @Test
  fun bookingCard_hidesPaymentButtons_whenCurrentUserIdIsNull() {
    // Test that payment buttons are hidden when currentUserId is null
    val bookingPending =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.PENDING_PAYMENT)

    compose.setContent {
      BookingCard(
          booking = bookingPending,
          bookerProfile = sampleBooker,
          onApprove = {},
          onReject = {},
          currentUserId = null) // No current user
    }

    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).assertDoesNotExist()
  }

  @Test
  fun bookingCard_hidesPaymentButtons_whenStatusIsNotPendingOrPaid() {
    // Test that payment buttons are hidden for other payment statuses
    val booking =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.CONFIRMED,
            bookerId = "current-user")

    compose.setContent {
      BookingCard(
          booking = booking,
          bookerProfile = sampleBooker,
          onApprove = {},
          onReject = {},
          currentUserId = "current-user",
          onPaymentComplete = {},
          onPaymentReceived = {})
    }

    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).assertDoesNotExist()
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON).assertDoesNotExist()
  }

  @Test
  fun bookingCard_withState_supportsPaymentCallbacks() {
    // Test payment callbacks through the state-based API
    var paymentCompleteCalled = false
    var paymentReceivedCalled = false

    val booking =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.PENDING_PAYMENT,
            bookerId = "current-user")

    val state =
        BookingCardState(
            booking = booking,
            bookerProfile = sampleBooker,
            currentUserId = "current-user",
            onApprove = {},
            onReject = {},
            onPaymentComplete = { paymentCompleteCalled = true },
            onPaymentReceived = { paymentReceivedCalled = true })

    compose.setContent { BookingCard(state = state) }

    // Should show Payment Complete button for learner
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).assertIsDisplayed()
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).performClick()
    assert(paymentCompleteCalled)
  }

  // ============================================================
  // TESTS FOR LISTING TYPE SPECIFIC PAYMENT BUTTON BEHAVIOR
  // ============================================================

  @Test
  fun bookingCard_proposal_showsPaymentCompleteButton_forBooker() {
    // PROPOSAL: Booker is the student who pays
    val booking =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.PENDING_PAYMENT,
            bookerId = "student-user",
            listingCreatorId = "tutor-user")

    var paymentCompleteCalled = false

    compose.setContent {
      BookingCard(
          booking = booking,
          bookerProfile = sampleBooker,
          onApprove = {},
          onReject = {},
          currentUserId = "student-user", // Current user is booker (student)
          listingType = com.android.sample.model.listing.ListingType.PROPOSAL,
          onPaymentComplete = { paymentCompleteCalled = true })
    }

    // Payment Complete button should be visible for the student (booker) in PROPOSAL
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).assertIsDisplayed()
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).performClick()
    assert(paymentCompleteCalled)
  }

  @Test
  fun bookingCard_proposal_hidesPaymentCompleteButton_forCreator() {
    // PROPOSAL: Creator is the tutor who should NOT see payment button
    val booking =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.PENDING_PAYMENT,
            bookerId = "student-user",
            listingCreatorId = "tutor-user")

    compose.setContent {
      BookingCard(
          booking = booking,
          bookerProfile = sampleBooker,
          onApprove = {},
          onReject = {},
          currentUserId = "tutor-user", // Current user is creator (tutor)
          listingType = com.android.sample.model.listing.ListingType.PROPOSAL,
          onPaymentComplete = {})
    }

    // Payment Complete button should NOT be visible for the tutor (creator) in PROPOSAL
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).assertDoesNotExist()
  }

  @Test
  fun bookingCard_proposal_showsPaymentReceivedButton_forCreator() {
    // PROPOSAL: Creator (tutor) receives payment
    val booking =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.PAID,
            bookerId = "student-user",
            listingCreatorId = "tutor-user")

    var paymentReceivedCalled = false

    compose.setContent {
      BookingCard(
          booking = booking,
          bookerProfile = sampleBooker,
          onApprove = {},
          onReject = {},
          currentUserId = "tutor-user", // Current user is creator (tutor)
          listingType = com.android.sample.model.listing.ListingType.PROPOSAL,
          onPaymentReceived = { paymentReceivedCalled = true })
    }

    // Payment Received button should be visible for the tutor (creator) in PROPOSAL
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON).assertIsDisplayed()
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON).performClick()
    assert(paymentReceivedCalled)
  }

  @Test
  fun bookingCard_proposal_hidesPaymentReceivedButton_forBooker() {
    // PROPOSAL: Booker (student) should NOT see payment received button
    val booking =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.PAID,
            bookerId = "student-user",
            listingCreatorId = "tutor-user")

    compose.setContent {
      BookingCard(
          booking = booking,
          bookerProfile = sampleBooker,
          onApprove = {},
          onReject = {},
          currentUserId = "student-user", // Current user is booker (student)
          listingType = com.android.sample.model.listing.ListingType.PROPOSAL,
          onPaymentReceived = {})
    }

    // Payment Received button should NOT be visible for the student (booker) in PROPOSAL
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON).assertDoesNotExist()
  }

  @Test
  fun bookingCard_request_showsPaymentCompleteButton_forCreator() {
    // REQUEST: Creator is the student who pays (reversed from PROPOSAL)
    val booking =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.PENDING_PAYMENT,
            bookerId = "tutor-user",
            listingCreatorId = "student-user")

    var paymentCompleteCalled = false

    compose.setContent {
      BookingCard(
          booking = booking,
          bookerProfile = sampleBooker,
          onApprove = {},
          onReject = {},
          currentUserId = "student-user", // Current user is creator (student in REQUEST)
          listingType = com.android.sample.model.listing.ListingType.REQUEST,
          onPaymentComplete = { paymentCompleteCalled = true })
    }

    // Payment Complete button should be visible for the student (creator) in REQUEST
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).assertIsDisplayed()
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).performClick()
    assert(paymentCompleteCalled)
  }

  @Test
  fun bookingCard_request_hidesPaymentCompleteButton_forBooker() {
    // REQUEST: Booker is the tutor who should NOT see payment button
    val booking =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.PENDING_PAYMENT,
            bookerId = "tutor-user",
            listingCreatorId = "student-user")

    compose.setContent {
      BookingCard(
          booking = booking,
          bookerProfile = sampleBooker,
          onApprove = {},
          onReject = {},
          currentUserId = "tutor-user", // Current user is booker (tutor in REQUEST)
          listingType = com.android.sample.model.listing.ListingType.REQUEST,
          onPaymentComplete = {})
    }

    // Payment Complete button should NOT be visible for the tutor (booker) in REQUEST
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).assertDoesNotExist()
  }

  @Test
  fun bookingCard_request_showsPaymentReceivedButton_forBooker() {
    // REQUEST: Booker (tutor) receives payment (reversed from PROPOSAL)
    val booking =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.PAID,
            bookerId = "tutor-user",
            listingCreatorId = "student-user")

    var paymentReceivedCalled = false

    compose.setContent {
      BookingCard(
          booking = booking,
          bookerProfile = sampleBooker,
          onApprove = {},
          onReject = {},
          currentUserId = "tutor-user", // Current user is booker (tutor in REQUEST)
          listingType = com.android.sample.model.listing.ListingType.REQUEST,
          onPaymentReceived = { paymentReceivedCalled = true })
    }

    // Payment Received button should be visible for the tutor (booker) in REQUEST
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON).assertIsDisplayed()
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON).performClick()
    assert(paymentReceivedCalled)
  }

  @Test
  fun bookingCard_request_hidesPaymentReceivedButton_forCreator() {
    // REQUEST: Creator (student) should NOT see payment received button
    val booking =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.PAID,
            bookerId = "tutor-user",
            listingCreatorId = "student-user")

    compose.setContent {
      BookingCard(
          booking = booking,
          bookerProfile = sampleBooker,
          onApprove = {},
          onReject = {},
          currentUserId = "student-user", // Current user is creator (student in REQUEST)
          listingType = com.android.sample.model.listing.ListingType.REQUEST,
          onPaymentReceived = {})
    }

    // Payment Received button should NOT be visible for the student (creator) in REQUEST
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON).assertDoesNotExist()
  }

  @Test
  fun bookingCard_withState_proposal_correctPaymentBehavior() {
    // Test the state-based API with PROPOSAL listing type
    var paymentCompleteCalled = false

    val booking =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.PENDING_PAYMENT,
            bookerId = "student-user",
            listingCreatorId = "tutor-user")

    val state =
        BookingCardState(
            booking = booking,
            bookerProfile = sampleBooker,
            currentUserId = "student-user", // Booker (student) in PROPOSAL
            listingType = com.android.sample.model.listing.ListingType.PROPOSAL,
            onApprove = {},
            onReject = {},
            onPaymentComplete = { paymentCompleteCalled = true },
            onPaymentReceived = {})

    compose.setContent { BookingCard(state = state) }

    // Student (booker) should see Payment Complete button in PROPOSAL
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).assertIsDisplayed()
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).performClick()
    assert(paymentCompleteCalled)
  }

  @Test
  fun bookingCard_withState_request_correctPaymentBehavior() {
    // Test the state-based API with REQUEST listing type
    var paymentCompleteCalled = false

    val booking =
        sampleBooking.copy(
            paymentStatus = com.android.sample.model.booking.PaymentStatus.PENDING_PAYMENT,
            bookerId = "tutor-user",
            listingCreatorId = "student-user")

    val state =
        BookingCardState(
            booking = booking,
            bookerProfile = sampleBooker,
            currentUserId = "student-user", // Creator (student) in REQUEST
            listingType = com.android.sample.model.listing.ListingType.REQUEST,
            onApprove = {},
            onReject = {},
            onPaymentComplete = { paymentCompleteCalled = true },
            onPaymentReceived = {})

    compose.setContent { BookingCard(state = state) }

    // Student (creator) should see Payment Complete button in REQUEST
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).assertIsDisplayed()
    compose.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).performClick()
    assert(paymentCompleteCalled)
  }
}
