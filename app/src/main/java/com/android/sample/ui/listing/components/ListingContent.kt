package com.android.sample.ui.listing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.sample.model.listing.ListingType
import com.android.sample.ui.listing.ListingScreenTestTags
import com.android.sample.ui.listing.ListingUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Content section of the listing screen showing listing details
 *
 * @param uiState UI state containing listing and booking information
 * @param onBook Callback when booking is confirmed with start and end dates
 * @param onApproveBooking Callback when a booking is approved
 * @param onRejectBooking Callback when a booking is rejected
 * @param modifier Modifier for the content
 */
@Composable
fun ListingContent(
    uiState: ListingUiState,
    onBook: (Date, Date) -> Unit,
    onApproveBooking: (String) -> Unit,
    onRejectBooking: (String) -> Unit,
    modifier: Modifier = Modifier,
    autoFillDatesForTesting: Boolean = false
) {
  val listing = uiState.listing ?: return
  val creator = uiState.creator
  var showBookingDialog by remember { mutableStateOf(false) }

  Column(
      modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Type badge
        Text(
            text =
                if (listing.type == ListingType.PROPOSAL) "Offering to Teach"
                else "Looking for Tutor",
            style = MaterialTheme.typography.labelLarge,
            color =
                if (listing.type == ListingType.PROPOSAL) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.testTag(ListingScreenTestTags.TITLE))

        // Title/Description
        Text(
            text = listing.displayTitle(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag(ListingScreenTestTags.TITLE))

        if (listing.description.isNotBlank()) {
          Card(
              modifier = Modifier.fillMaxWidth(),
              colors =
                  CardDefaults.cardColors(
                      containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(
                    text = listing.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp).testTag(ListingScreenTestTags.DESCRIPTION))
              }
        }

        // Creator info
        if (creator != null) {
          Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(Modifier.padding(4.dp))
                    Text(
                        text = creator.name ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.testTag(ListingScreenTestTags.CREATOR_NAME))
                  }
                }
          }
        }

        // Skill details
        Card(modifier = Modifier.fillMaxWidth()) {
          Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Skill Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                      Text("Subject:", style = MaterialTheme.typography.bodyMedium)
                      Text(
                          listing.skill.mainSubject.name,
                          style = MaterialTheme.typography.bodyMedium,
                          fontWeight = FontWeight.Medium)
                    }

                if (listing.skill.skill.isNotBlank()) {
                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Skill:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            listing.skill.skill,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.testTag(ListingScreenTestTags.SKILL))
                      }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                      Text("Expertise:", style = MaterialTheme.typography.bodyMedium)
                      Text(
                          listing.skill.expertise.name,
                          style = MaterialTheme.typography.bodyMedium,
                          fontWeight = FontWeight.Medium,
                          modifier = Modifier.testTag(ListingScreenTestTags.EXPERTISE))
                    }
              }
        }

        // Location
        Card(modifier = Modifier.fillMaxWidth()) {
          Row(
              modifier = Modifier.padding(16.dp).fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text(
                    text = listing.location.name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag(ListingScreenTestTags.LOCATION))
              }
        }

        // Hourly rate
        Card(modifier = Modifier.fillMaxWidth()) {
          Row(
              modifier = Modifier.padding(16.dp).fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text("Hourly Rate:", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = String.format(Locale.getDefault(), "$%.2f/hr", listing.hourlyRate),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag(ListingScreenTestTags.HOURLY_RATE))
              }
        }

        // Created date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        Text(
            text = "Posted on ${dateFormat.format(listing.createdAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag(ListingScreenTestTags.CREATED_DATE))

        Spacer(Modifier.height(8.dp))

        // Book button or bookings management section for owners
        if (uiState.isOwnListing) {
          // Bookings section for listing owner
          BookingsSection(
              uiState = uiState,
              onApproveBooking = onApproveBooking,
              onRejectBooking = onRejectBooking)
        } else {
          Button(
              onClick = { showBookingDialog = true },
              modifier = Modifier.fillMaxWidth().testTag(ListingScreenTestTags.BOOK_BUTTON),
              enabled = !uiState.bookingInProgress) {
                if (uiState.bookingInProgress) {
                  CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                }
                Text(if (uiState.bookingInProgress) "Creating Booking..." else "Book Now")
              }
        }
      }

  // Booking dialog
  if (showBookingDialog) {
    BookingDialog(
        onDismiss = { showBookingDialog = false },
        onConfirm = { start, end ->
          onBook(start, end)
          showBookingDialog = false
        },
        autoFillDatesForTesting = autoFillDatesForTesting)
  }
}
