package com.android.sample.ui.listing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.listing.ListingType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Test tags for the listing screen */
object ListingScreenTestTags {
  const val SCREEN = "listingScreen"
  const val TOP_BAR = "listingScreenTopBar"
  const val BACK_BUTTON = "listingScreenBackButton"
  const val LOADING = "listingScreenLoading"
  const val ERROR = "listingScreenError"
  const val TITLE = "listingScreenTitle"
  const val DESCRIPTION = "listingScreenDescription"
  const val CREATOR_NAME = "listingScreenCreatorName"
  const val LOCATION = "listingScreenLocation"
  const val HOURLY_RATE = "listingScreenHourlyRate"
  const val SKILL = "listingScreenSkill"
  const val EXPERTISE = "listingScreenExpertise"
  const val CREATED_DATE = "listingScreenCreatedDate"
  const val BOOK_BUTTON = "listingScreenBookButton"
  const val OWN_LISTING_MESSAGE = "listingScreenOwnListingMessage"
  const val BOOKING_DIALOG = "listingScreenBookingDialog"
  const val SESSION_START_BUTTON = "listingScreenSessionStartButton"
  const val SESSION_END_BUTTON = "listingScreenSessionEndButton"
  const val CONFIRM_BOOKING_BUTTON = "listingScreenConfirmBookingButton"
  const val CANCEL_BOOKING_BUTTON = "listingScreenCancelBookingButton"
  const val SUCCESS_DIALOG = "listingScreenSuccessDialog"
  const val ERROR_DIALOG = "listingScreenErrorDialog"
  const val BOOKINGS_SECTION = "listingScreenBookingsSection"
  const val BOOKINGS_LOADING = "listingScreenBookingsLoading"
  const val BOOKING_CARD = "listingScreenBookingCard"
  const val APPROVE_BUTTON = "listingScreenApproveButton"
  const val REJECT_BUTTON = "listingScreenRejectButton"
  const val NO_BOOKINGS = "listingScreenNoBookings"
  const val START_DATE_PICKER_DIALOG = "listingScreenStartDatePickerDialog"
  const val START_TIME_PICKER_DIALOG = "listingScreenStartTimePickerDialog"
  const val END_DATE_PICKER_DIALOG = "listingScreenEndDatePickerDialog"
  const val END_TIME_PICKER_DIALOG = "listingScreenEndTimePickerDialog"
  const val DATE_PICKER_OK_BUTTON = "listingScreenDatePickerOkButton"
  const val DATE_PICKER_CANCEL_BUTTON = "listingScreenDatePickerCancelButton"
  const val TIME_PICKER_OK_BUTTON = "listingScreenTimePickerOkButton"
  const val TIME_PICKER_CANCEL_BUTTON = "listingScreenTimePickerCancelButton"
}

/**
 * Listing detail screen that displays complete information about a listing and allows booking
 *
 * @param listingId The ID of the listing to display
 * @param onNavigateBack Callback when back button is pressed
 * @param viewModel The ViewModel for this screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingScreen(
    listingId: String,
    onNavigateBack: () -> Unit,
    viewModel: ListingViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  // Load listing when screen is displayed
  LaunchedEffect(listingId) { viewModel.loadListing(listingId) }

  // Show success dialog when booking is created
  if (uiState.bookingSuccess) {
    AlertDialog(
        onDismissRequest = {
          viewModel.clearBookingSuccess()
          onNavigateBack()
        },
        title = { Text("Booking Created") },
        text = { Text("Your booking has been created successfully and is pending confirmation.") },
        confirmButton = {
          Button(
              onClick = {
                viewModel.clearBookingSuccess()
                onNavigateBack()
              }) {
                Text("OK")
              }
        },
        modifier = Modifier.testTag(ListingScreenTestTags.SUCCESS_DIALOG))
  }

  // Show error dialog when booking fails
  uiState.bookingError?.let { error ->
    AlertDialog(
        onDismissRequest = { viewModel.clearBookingError() },
        title = { Text("Booking Error") },
        text = { Text(error) },
        confirmButton = { Button(onClick = { viewModel.clearBookingError() }) { Text("OK") } },
        modifier = Modifier.testTag(ListingScreenTestTags.ERROR_DIALOG))
  }

  Scaffold(
      modifier = Modifier.fillMaxSize().testTag(ListingScreenTestTags.SCREEN),
  ) { padding ->
    when {
      uiState.isLoading -> {
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center) {
              CircularProgressIndicator(modifier = Modifier.testTag(ListingScreenTestTags.LOADING))
            }
      }
      uiState.error != null -> {
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center) {
              Text(
                  text = uiState.error ?: "Unknown error",
                  color = MaterialTheme.colorScheme.error,
                  modifier = Modifier.testTag(ListingScreenTestTags.ERROR))
            }
      }
      uiState.listing != null -> {
        ListingContent(
            uiState = uiState,
            onBook = { start, end -> viewModel.createBooking(start, end) },
            onApproveBooking = { bookingId -> viewModel.approveBooking(bookingId) },
            onRejectBooking = { bookingId -> viewModel.rejectBooking(bookingId) },
            modifier = Modifier.padding(padding))
      }
    }
  }
}

@Composable
private fun ListingContent(
    uiState: ListingUiState,
    onBook: (Date, Date) -> Unit,
    onApproveBooking: (String) -> Unit,
    onRejectBooking: (String) -> Unit,
    modifier: Modifier = Modifier
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
                    text = listing.location.name ?: "",
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
        })
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingDialog(onDismiss: () -> Unit, onConfirm: (Date, Date) -> Unit) {
  var sessionStart by remember { mutableStateOf<Date?>(null) }
  var sessionEnd by remember { mutableStateOf<Date?>(null) }
  var showStartDatePicker by remember { mutableStateOf(false) }
  var showStartTimePicker by remember { mutableStateOf(false) }
  var showEndDatePicker by remember { mutableStateOf(false) }
  var showEndTimePicker by remember { mutableStateOf(false) }

  val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Book Session") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Text("Select session start and end times:")

          // Session start
          Button(
              onClick = { showStartDatePicker = true },
              modifier =
                  Modifier.fillMaxWidth().testTag(ListingScreenTestTags.SESSION_START_BUTTON)) {
                Text(sessionStart?.let { dateFormat.format(it) } ?: "Select Start Time")
              }

          // Session end
          Button(
              onClick = { showEndDatePicker = true },
              modifier = Modifier.fillMaxWidth().testTag(ListingScreenTestTags.SESSION_END_BUTTON),
              enabled = true) {
                Text(sessionEnd?.let { dateFormat.format(it) } ?: "Select End Time")
              }
        }
      },
      confirmButton = {
        Button(
            onClick = {
              if (sessionStart != null && sessionEnd != null) {
                onConfirm(sessionStart!!, sessionEnd!!)
              }
            },
            enabled = sessionStart != null && sessionEnd != null,
            modifier = Modifier.testTag(ListingScreenTestTags.CONFIRM_BOOKING_BUTTON)) {
              Text("Confirm")
            }
      },
      dismissButton = {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag(ListingScreenTestTags.CANCEL_BOOKING_BUTTON)) {
              Text("Cancel")
            }
      },
      modifier = Modifier.testTag(ListingScreenTestTags.BOOKING_DIALOG))

  // Date/Time pickers
  if (showStartDatePicker) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = { showStartDatePicker = false },
        confirmButton = {
          TextButton(
              onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                  val calendar = Calendar.getInstance().apply { timeInMillis = millis }
                  sessionStart = calendar.time
                }
                showStartDatePicker = false
                showStartTimePicker = true
              },
              modifier = Modifier.testTag(ListingScreenTestTags.DATE_PICKER_OK_BUTTON)) {
                Text("OK")
              }
        },
        dismissButton = {
          TextButton(
              onClick = { showStartDatePicker = false },
              modifier = Modifier.testTag(ListingScreenTestTags.DATE_PICKER_CANCEL_BUTTON)) {
                Text("Cancel")
              }
        },
        modifier = Modifier.testTag(ListingScreenTestTags.START_DATE_PICKER_DIALOG)) {
          DatePicker(state = datePickerState)
        }
  }

  if (showStartTimePicker) {
    val timePickerState = rememberTimePickerState()
    AlertDialog(
        onDismissRequest = { showStartTimePicker = false },
        title = { Text("Select Start Time") },
        text = { TimePicker(state = timePickerState) },
        confirmButton = {
          TextButton(
              onClick = {
                sessionStart?.let { date ->
                  val calendar =
                      Calendar.getInstance().apply {
                        time = date
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                      }
                  sessionStart = calendar.time
                }
                showStartTimePicker = false
              },
              modifier = Modifier.testTag(ListingScreenTestTags.TIME_PICKER_OK_BUTTON)) {
                Text("OK")
              }
        },
        dismissButton = {
          TextButton(
              onClick = { showStartTimePicker = false },
              modifier = Modifier.testTag(ListingScreenTestTags.TIME_PICKER_CANCEL_BUTTON)) {
                Text("Cancel")
              }
        },
        modifier = Modifier.testTag(ListingScreenTestTags.START_TIME_PICKER_DIALOG))
  }

  if (showEndDatePicker) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = { showEndDatePicker = false },
        confirmButton = {
          TextButton(
              onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                  val calendar = Calendar.getInstance().apply { timeInMillis = millis }
                  sessionEnd = calendar.time
                }
                showEndDatePicker = false
                showEndTimePicker = true
              },
              modifier = Modifier.testTag(ListingScreenTestTags.DATE_PICKER_OK_BUTTON)) {
                Text("OK")
              }
        },
        dismissButton = {
          TextButton(
              onClick = { showEndDatePicker = false },
              modifier = Modifier.testTag(ListingScreenTestTags.DATE_PICKER_CANCEL_BUTTON)) {
                Text("Cancel")
              }
        },
        modifier = Modifier.testTag(ListingScreenTestTags.END_DATE_PICKER_DIALOG)) {
          DatePicker(state = datePickerState)
        }
  }

  if (showEndTimePicker) {
    val timePickerState = rememberTimePickerState()
    AlertDialog(
        onDismissRequest = { showEndTimePicker = false },
        title = { Text("Select End Time") },
        text = { TimePicker(state = timePickerState) },
        confirmButton = {
          TextButton(
              onClick = {
                sessionEnd?.let { date ->
                  val calendar =
                      Calendar.getInstance().apply {
                        time = date
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                      }
                  sessionEnd = calendar.time
                }
                showEndTimePicker = false
              },
              modifier = Modifier.testTag(ListingScreenTestTags.TIME_PICKER_OK_BUTTON)) {
                Text("OK")
              }
        },
        dismissButton = {
          TextButton(
              onClick = { showEndTimePicker = false },
              modifier = Modifier.testTag(ListingScreenTestTags.TIME_PICKER_CANCEL_BUTTON)) {
                Text("Cancel")
              }
        },
        modifier = Modifier.testTag(ListingScreenTestTags.END_TIME_PICKER_DIALOG))
  }
}

/** Section displaying bookings for the listing owner */
@Composable
private fun BookingsSection(
    uiState: ListingUiState,
    onApproveBooking: (String) -> Unit,
    onRejectBooking: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  Column(
      modifier = modifier.fillMaxWidth().testTag(ListingScreenTestTags.BOOKINGS_SECTION),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Bookings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)

        when {
          uiState.bookingsLoading -> {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center) {
                  CircularProgressIndicator(
                      modifier = Modifier.testTag(ListingScreenTestTags.BOOKINGS_LOADING))
                }
          }
          uiState.listingBookings.isEmpty() -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                  Text(
                      text = "No bookings yet",
                      style = MaterialTheme.typography.bodyMedium,
                      modifier = Modifier.padding(16.dp).testTag(ListingScreenTestTags.NO_BOOKINGS))
                }
          }
          else -> {
            uiState.listingBookings.forEach { booking ->
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

/** Card displaying a single booking with approve/reject actions */
@Composable
private fun BookingCard(
    booking: com.android.sample.model.booking.Booking,
    bookerProfile: com.android.sample.model.user.Profile?,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
  val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

  Card(
      modifier = modifier.fillMaxWidth().testTag(ListingScreenTestTags.BOOKING_CARD),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  when (booking.status) {
                    com.android.sample.model.booking.BookingStatus.PENDING ->
                        MaterialTheme.colorScheme.surface
                    com.android.sample.model.booking.BookingStatus.CONFIRMED ->
                        MaterialTheme.colorScheme.primaryContainer
                    com.android.sample.model.booking.BookingStatus.CANCELLED ->
                        MaterialTheme.colorScheme.errorContainer
                    com.android.sample.model.booking.BookingStatus.COMPLETED ->
                        MaterialTheme.colorScheme.tertiaryContainer
                  })) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              // Status badge
              Text(
                  text = booking.status.name,
                  style = MaterialTheme.typography.labelSmall,
                  color =
                      when (booking.status) {
                        com.android.sample.model.booking.BookingStatus.PENDING ->
                            MaterialTheme.colorScheme.onSurface
                        com.android.sample.model.booking.BookingStatus.CONFIRMED ->
                            MaterialTheme.colorScheme.onPrimaryContainer
                        com.android.sample.model.booking.BookingStatus.CANCELLED ->
                            MaterialTheme.colorScheme.onErrorContainer
                        com.android.sample.model.booking.BookingStatus.COMPLETED ->
                            MaterialTheme.colorScheme.onTertiaryContainer
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
              if (booking.status == com.android.sample.model.booking.BookingStatus.PENDING) {
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
