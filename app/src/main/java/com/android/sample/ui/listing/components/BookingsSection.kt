package com.android.sample.ui.listing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.sample.ui.listing.ListingScreenTestTags
import com.android.sample.ui.listing.ListingUiState

/**
 * Section displaying bookings for the listing owner
 *
 * @param uiState UI state containing bookings and loading state
 * @param onApproveBooking Callback when a booking is approved
 * @param onRejectBooking Callback when a booking is rejected
 * @param modifier Modifier for the section
 */
@Composable
fun BookingsSection(
    uiState: ListingUiState,
    onApproveBooking: (String) -> Unit,
    onRejectBooking: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  LazyColumn(
      modifier = modifier.fillMaxWidth().testTag(ListingScreenTestTags.BOOKINGS_SECTION),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
          Text(
              text = "Bookings",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold)
        }

        when {
          uiState.bookingsLoading -> {
            item {
              Box(
                  modifier = Modifier.fillMaxWidth().padding(32.dp),
                  contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.testTag(ListingScreenTestTags.BOOKINGS_LOADING))
                  }
            }
          }
          uiState.listingBookings.isEmpty() -> {
            item {
              Card(
                  modifier = Modifier.fillMaxWidth(),
                  colors =
                      CardDefaults.cardColors(
                          containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(
                        text = "No bookings yet",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier =
                            Modifier.padding(16.dp).testTag(ListingScreenTestTags.NO_BOOKINGS))
                  }
            }
          }
          else -> {
            items(uiState.listingBookings) { booking ->
              BookingCard(
                  booking = booking,
                  bookerProfile = uiState.bookerProfiles[booking.bookerId],
                  onApprove = { onApproveBooking(booking.bookingId) },
                  onReject = { onRejectBooking(booking.bookingId) })
            }
          }
        }
      }
}
