package com.android.sample.ui.listing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.sample.ui.listing.ListingScreenTestTags
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Dialog for booking a session with date and time selection
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when booking is confirmed with start and end dates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDialog(
    onDismiss: () -> Unit,
    onConfirm: (Date, Date) -> Unit,
    autoFillDatesForTesting: Boolean = false
) {
  // Auto-fill dates for testing if flag is enabled
  val initialStart = if (autoFillDatesForTesting) Date() else null
  val initialEnd = if (autoFillDatesForTesting) Date(System.currentTimeMillis() + 3600000) else null

  var sessionStart by remember { mutableStateOf(initialStart) }
  var sessionEnd by remember { mutableStateOf(initialEnd) }
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
