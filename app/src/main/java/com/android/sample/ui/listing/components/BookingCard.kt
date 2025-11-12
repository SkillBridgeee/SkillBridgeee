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
import com.android.sample.model.user.Profile
import com.android.sample.ui.listing.ListingScreenTestTags
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Card displaying a single booking with approve/reject actions
 *
 * @param booking The booking to display
 * @param bookerProfile Profile of the person who made the booking
 * @param onApprove Callback when approve button is clicked
 * @param onReject Callback when reject button is clicked
 * @param modifier Modifier for the card
 */
@Composable
fun BookingCard(
    booking: Booking,
    bookerProfile: Profile?,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
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
                  Icon(Icons.Default.Person, contentDescription = null)
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
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                      Button(
                          onClick = onApprove,
                          modifier =
                              Modifier.weight(1f).testTag(ListingScreenTestTags.APPROVE_BUTTON)) {
                            Text("Approve")
                          }
                      Button(
                          onClick = onReject,
                          modifier =
                              Modifier.weight(1f).testTag(ListingScreenTestTags.REJECT_BUTTON),
                          colors =
                              ButtonDefaults.buttonColors(
                                  containerColor = MaterialTheme.colorScheme.error)) {
                            Text("Reject")
                          }
                    }
              }
            }
      }
}
