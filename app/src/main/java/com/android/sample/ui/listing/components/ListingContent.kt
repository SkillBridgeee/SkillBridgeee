package com.android.sample.ui.listing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
 * @param onDeleteListing Callback when a listing is deleted
 * @param modifier Modifier for the content
 */
@Composable
fun ListingContent(
    uiState: ListingUiState,
    onBook: (Date, Date) -> Unit,
    onApproveBooking: (String) -> Unit,
    onRejectBooking: (String) -> Unit,
    onDeleteListing: () -> Unit,
    onEditListing: () -> Unit,
    modifier: Modifier = Modifier,
    autoFillDatesForTesting: Boolean = false
) {
  val listing = uiState.listing ?: return
  val creator = uiState.creator
  var showBookingDialog by remember { mutableStateOf(false) }

  LazyColumn(
      modifier = modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { TypeBadge(listingType = listing.type) }

        item {
          // Title/Description
          Text(
              text = listing.displayTitle(),
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag(ListingScreenTestTags.TITLE))
        }

        item {
          // Description card (if present)
          DescriptionCard(listing.description)
        }

        item {
          // Creator info (if available)
          creator?.let { CreatorCard(it) }
        }

        item { // Skill details
          SkillDetailsCard(skill = listing.skill)
        }

        item { // Location
          LocationCard(locationName = listing.location.name)
        }

        item { // Hourly rate
          HourlyRateCard(hourlyRate = listing.hourlyRate)
        }

        item { // Created date
          PostedDate(listing.createdAt)
        }

        item { Spacer(Modifier.height(8.dp)) }

        // Action section (book button or bookings management)
        actionSection(
            uiState = uiState,
            onShowBookingDialog = { showBookingDialog = true },
            onApproveBooking = onApproveBooking,
            onRejectBooking = onRejectBooking,
            onDeleteListing = onDeleteListing,
            onEditListing = onEditListing)
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

/** Type badge showing whether the listing is offering to teach or looking for a tutor */
@Composable
private fun TypeBadge(listingType: ListingType, modifier: Modifier = Modifier) {
  val (text, color) =
      if (listingType == ListingType.PROPOSAL) {
        "Offering to Teach" to MaterialTheme.colorScheme.primary
      } else {
        "Looking for Tutor" to MaterialTheme.colorScheme.secondary
      }

  Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      color = color,
      modifier = modifier.testTag(ListingScreenTestTags.TYPE_BADGE))
}

@Composable
private fun DescriptionCard(description: String) {
  Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(
            text = description.ifBlank { "This Listing has no Description." },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp).testTag(ListingScreenTestTags.DESCRIPTION))
      }
}

/** Creator information card */
@Composable
private fun CreatorCard(creator: com.android.sample.model.user.Profile) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

/** Skill details card */
@Composable
private fun SkillDetailsCard(skill: com.android.sample.model.skill.Skill) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
          "Skill Details",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold)

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Subject:", style = MaterialTheme.typography.bodyMedium)
        Text(
            skill.mainSubject.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium)
      }

      if (skill.skill.isNotBlank()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Skill:", style = MaterialTheme.typography.bodyMedium)
          Text(
              skill.skill,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.testTag(ListingScreenTestTags.SKILL))
        }
      }

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Expertise:", style = MaterialTheme.typography.bodyMedium)
        Text(
            skill.expertise.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.testTag(ListingScreenTestTags.EXPERTISE))
      }
    }
  }
}

/** Location card */
@Composable
private fun LocationCard(locationName: String) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.LocationOn, contentDescription = null)
          Spacer(Modifier.padding(4.dp))
          Text(
              text = locationName,
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.testTag(ListingScreenTestTags.LOCATION))
        }
  }
}

/** Hourly rate card */
@Composable
private fun HourlyRateCard(hourlyRate: Double) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          Text("Hourly Rate:", style = MaterialTheme.typography.titleMedium)
          Text(
              text = String.format(Locale.getDefault(), "$%.2f/hr", hourlyRate),
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag(ListingScreenTestTags.HOURLY_RATE))
        }
  }
}

@Composable
private fun PostedDate(date: Date) {
  val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
  Text(
      text = "Posted on ${dateFormat.format(date)}",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.testTag(ListingScreenTestTags.CREATED_DATE))
}

/** Action button section (book now or bookings management) */
private fun LazyListScope.actionSection(
    uiState: ListingUiState,
    onShowBookingDialog: () -> Unit,
    onApproveBooking: (String) -> Unit,
    onRejectBooking: (String) -> Unit,
    onDeleteListing: () -> Unit,
    onEditListing: () -> Unit
) {
  if (uiState.isOwnListing) {
    bookingsSection(
        uiState = uiState, onApproveBooking = onApproveBooking, onRejectBooking = onRejectBooking)

    item { Spacer(Modifier.height(8.dp)) }

    item {
      Button(onClick = onEditListing, modifier = Modifier.fillMaxWidth()) { Text("Edit Listing") }
    }

    item { Spacer(Modifier.height(8.dp)) }

    item {
      var showDeleteDialog by remember { mutableStateOf(false) }

      Button(
          onClick = { showDeleteDialog = true },
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
            Text("Delete Listing")
          }

      if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Listing") },
            text = {
              Text("Are you sure you want to delete this listing? This action cannot be undone.")
            },
            confirmButton = {
              Button(
                  onClick = {
                    showDeleteDialog = false
                    onDeleteListing()
                  },
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                  }
            },
            dismissButton = { Button(onClick = { showDeleteDialog = false }) { Text("Cancel") } })
      }
    }
  } else {
    item {
      Button(
          onClick = onShowBookingDialog,
          modifier = Modifier.fillMaxWidth().testTag(ListingScreenTestTags.BOOK_BUTTON),
          enabled = !uiState.bookingInProgress) {
            if (uiState.bookingInProgress) {
              CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            }
            Text(if (uiState.bookingInProgress) "Creating Booking..." else "Book Now")
          }
    }
  }
}
