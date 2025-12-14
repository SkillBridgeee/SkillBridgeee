package com.android.sample.ui.listing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.booking.PaymentStatus
import com.android.sample.model.listing.ListingType
import com.android.sample.model.user.Profile
import com.android.sample.ui.listing.ListingScreenTestTags
import java.text.SimpleDateFormat
import java.util.Locale

// String constants for button labels
private const val APPROVE_BUTTON_TEXT = "Approve"
private const val REJECT_BUTTON_TEXT = "Reject"
private const val PROFILE_ICON_CONTENT_DESC = "Profile Icon"

/**
 * Data class to hold the state and callbacks for BookingCard
 *
 * @param booking The booking to display
 * @param bookerProfile Profile of the person who made the booking
 * @param currentUserId The ID of the current user (to determine which buttons to show)
 * @param listingType The type of listing (PROPOSAL or REQUEST) to determine payment roles
 * @param onApprove Callback when approve button is clicked
 * @param onReject Callback when reject button is clicked
 * @param onPaymentComplete Callback when payment complete button is clicked
 * @param onPaymentReceived Callback when payment received button is clicked
 */
data class BookingCardState(
    val booking: Booking,
    val bookerProfile: Profile?,
    val currentUserId: String? = null,
    val listingType: ListingType? = null,
    val onApprove: () -> Unit,
    val onReject: () -> Unit,
    val onPaymentComplete: () -> Unit = {},
    val onPaymentReceived: () -> Unit = {}
)

/**
 * Card displaying a single booking with approve/reject actions
 *
 * @param state The state containing booking data and callbacks
 * @param modifier Modifier for the card
 */
@Composable
fun BookingCard(state: BookingCardState, modifier: Modifier = Modifier) {
  BookingCard(
      booking = state.booking,
      bookerProfile = state.bookerProfile,
      onApprove = state.onApprove,
      onReject = state.onReject,
      onPaymentComplete = state.onPaymentComplete,
      onPaymentReceived = state.onPaymentReceived,
      currentUserId = state.currentUserId,
      listingType = state.listingType,
      modifier = modifier)
}

/**
 * Card displaying a single booking with approve/reject actions
 *
 * @param booking The booking to display
 * @param bookerProfile Profile of the person who made the booking
 * @param onApprove Callback when approve button is clicked
 * @param onReject Callback when reject button is clicked
 * @param onPaymentComplete Callback when payment complete button is clicked
 * @param onPaymentReceived Callback when payment received button is clicked
 * @param currentUserId The ID of the current user (to determine which buttons to show)
 * @param listingType The type of listing (PROPOSAL or REQUEST) to determine payment roles
 * @param modifier Modifier for the card
 */
@Composable
fun BookingCard(
    booking: Booking,
    bookerProfile: Profile?,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
    onPaymentComplete: () -> Unit = {},
    onPaymentReceived: () -> Unit = {},
    currentUserId: String? = null,
    listingType: ListingType? = null
) {
  val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

  Card(
      modifier =
          modifier.fillMaxWidth().testTag(ListingScreenTestTags.BOOKING_CARD).semantics(
              mergeDescendants = true) {},
      colors =
          CardDefaults.cardColors(
              containerColor =
                  when (booking.status) {
                    BookingStatus.PENDING -> MaterialTheme.colorScheme.surface
                    BookingStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer
                    BookingStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                    BookingStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                  })) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              // Status badge
              Text(
                  text = booking.status.name,
                  style = MaterialTheme.typography.labelSmall,
                  color =
                      when (booking.status) {
                        BookingStatus.PENDING -> MaterialTheme.colorScheme.onSurface
                        BookingStatus.CONFIRMED -> MaterialTheme.colorScheme.onPrimaryContainer
                        BookingStatus.CANCELLED -> MaterialTheme.colorScheme.onErrorContainer
                        BookingStatus.COMPLETED -> MaterialTheme.colorScheme.onTertiaryContainer
                      },
                  fontWeight = FontWeight.Bold)

              // Booker info
              if (bookerProfile != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Person, contentDescription = PROFILE_ICON_CONTENT_DESC)
                  Spacer(Modifier.padding(4.dp))
                  Text(
                      text = bookerProfile.name ?: "Unknown",
                      style = MaterialTheme.typography.titleMedium)
                }
              }

              // Session details
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Start:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        dateFormat.format(booking.sessionStart),
                        style = MaterialTheme.typography.bodyMedium)
                  }

              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("End:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        dateFormat.format(booking.sessionEnd),
                        style = MaterialTheme.typography.bodyMedium)
                  }

              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Price:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        String.format(Locale.getDefault(), "$%.2f", booking.price),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold)
                  }

              // Action buttons for pending bookings
              if (booking.status == BookingStatus.PENDING) {
                BookingActionButtons(onApprove = onApprove, onReject = onReject)
              }

              // Payment actions
              PaymentActionButtons(
                  booking = booking,
                  currentUserId = currentUserId,
                  listingType = listingType,
                  onPaymentComplete = onPaymentComplete,
                  onPaymentReceived = onPaymentReceived)
            }
      }
}

@Composable
private fun BookingActionButtons(onApprove: () -> Unit, onReject: () -> Unit) {
  Spacer(Modifier.height(8.dp))
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    Button(
        onClick = onApprove,
        modifier = Modifier.weight(1f).testTag(ListingScreenTestTags.APPROVE_BUTTON)) {
          Text(APPROVE_BUTTON_TEXT)
        }
    Button(
        onClick = onReject,
        modifier = Modifier.weight(1f).testTag(ListingScreenTestTags.REJECT_BUTTON),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
          Text(REJECT_BUTTON_TEXT)
        }
  }
}

@Composable
private fun PaymentActionButtons(
    booking: Booking,
    currentUserId: String?,
    listingType: ListingType?,
    onPaymentComplete: () -> Unit,
    onPaymentReceived: () -> Unit
) {
  // Determine who should pay based on listing type:
  // - PROPOSAL: booker is the student who pays, listing creator is the tutor who receives
  // - REQUEST: listing creator is the student who pays, booker is the tutor who receives
  val isStudentPaying =
      when (listingType) {
        ListingType.PROPOSAL -> currentUserId == booking.bookerId
        ListingType.REQUEST -> currentUserId == booking.listingCreatorId
        null -> currentUserId == booking.bookerId // Default to old behavior if type unknown
      }

  val isTutorReceiving =
      when (listingType) {
        ListingType.PROPOSAL -> currentUserId == booking.listingCreatorId
        ListingType.REQUEST -> currentUserId == booking.bookerId
        null -> currentUserId == booking.listingCreatorId // Default to old behavior if type unknown
      }

  // Only show "Payment Complete" button to the student (payer)
  if (booking.paymentStatus == PaymentStatus.PENDING_PAYMENT && isStudentPaying) {
    Spacer(Modifier.height(8.dp))
    Button(
        onClick = onPaymentComplete,
        modifier = Modifier.fillMaxWidth().testTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON)) {
          Text("Payment Complete")
        }
  }
  // Only show "Payment Received" button to the tutor (receiver)
  else if (booking.paymentStatus == PaymentStatus.PAID && isTutorReceiving) {
    Spacer(Modifier.height(8.dp))
    Button(
        onClick = onPaymentReceived,
        modifier = Modifier.fillMaxWidth().testTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON)) {
          Text("Payment Received")
        }
  }
}
