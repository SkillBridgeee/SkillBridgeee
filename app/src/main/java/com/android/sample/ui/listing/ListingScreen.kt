package com.android.sample.ui.listing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.ui.listing.components.ListingContent
import kotlinx.coroutines.launch

/** Test tags for the listing screen */
object ListingScreenTestTags {
  const val SCREEN = "listingScreen"
  const val LOADING = "listingScreenLoading"
  const val ERROR = "listingScreenError"
  const val TYPE_BADGE = "listingScreenTypeBadge"
  const val TITLE = "listingScreenTitle"
  const val DESCRIPTION = "listingScreenDescription"
  const val CREATOR_NAME = "listingScreenCreatorName"
  const val LOCATION = "listingScreenLocation"
  const val HOURLY_RATE = "listingScreenHourlyRate"
  const val SKILL = "listingScreenSkill"
  const val EXPERTISE = "listingScreenExpertise"
  const val CREATED_DATE = "listingScreenCreatedDate"
  const val BOOK_BUTTON = "listingScreenBookButton"
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
  const val PAYMENT_COMPLETE_BUTTON = "listingScreenPaymentCompleteButton"
  const val PAYMENT_RECEIVED_BUTTON = "listingScreenPaymentReceivedButton"
  const val TUTOR_RATING_SECTION = "listing_tutor_rating_section"
  const val TUTOR_RATING_STARS = "listing_tutor_rating_stars"
  const val TUTOR_RATING_SUBMIT = "listing_tutor_rating_submit"
  const val DUPLICATE_BOOKING_DIALOG = "listingScreenDuplicateBookingDialog"
  const val DUPLICATE_BOOKING_CONFIRM = "listingScreenDuplicateBookingConfirm"
  const val DUPLICATE_BOOKING_CANCEL = "listingScreenDuplicateBookingCancel"
}

/**
 * Listing detail screen that displays complete information about a listing and allows booking
 *
 * @param listingId The ID of the listing to display
 * @param onNavigateBack Callback when back button is pressed
 * @param onNavigateToProfile Callback when creator's name is clicked with creator's profile ID
 * @param viewModel The ViewModel for this screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingScreen(
    listingId: String,
    onNavigateBack: () -> Unit,
    onEditListing: () -> Unit,
    onNavigateToProfile: (String) -> Unit = {},
    onNavigateToBookings: () -> Unit = {},
    viewModel: ListingViewModel = viewModel(),
    autoFillDatesForTesting: Boolean = false
) {
  val uiState by viewModel.uiState.collectAsState()
  val scope = rememberCoroutineScope()
  //  val listingRepository = ListingRepositoryProvider.repository

  // Load listing when screen is displayed
  LaunchedEffect(listingId) { viewModel.loadListing(listingId) }

  LaunchedEffect(uiState.listingDeleted) {
    if (uiState.listingDeleted) {
      onNavigateBack()
      viewModel.clearListingDeleted()
    }
  }

  // Show success dialog when booking is created
  if (uiState.bookingSuccess) {
    val successMessage = buildString {
      append(ListingViewModel.MSG_BOOKING_SUCCESS)
      if (uiState.conversationCreationWarning != null) {
        append(
            "\n\nNote: ${uiState.conversationCreationWarning} ${ListingViewModel.MSG_CONVERSATION_ALTERNATIVE}")
      } else {
        append("\n\n${ListingViewModel.MSG_CONVERSATION_SUCCESS}")
      }
    }

    AlertDialog(
        onDismissRequest = {
          viewModel.clearBookingSuccess()
          viewModel.clearConversationWarning()
          onNavigateToBookings()
        },
        title = { Text("Booking Created") },
        text = { Text(successMessage) },
        confirmButton = {
          Button(
              onClick = {
                viewModel.clearBookingSuccess()
                viewModel.clearConversationWarning()
                onNavigateToBookings()
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

  // Show duplicate booking warning dialog
  // Note: We need to track separately whether user confirmed to proceed with duplicate booking
  // This state is managed by passing a callback that will be handled in ListingContent

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
            onDeleteListing = { scope.launch { viewModel.deleteListing() } },
            onEditListing = onEditListing,
            onSubmitTutorRating = { stars -> viewModel.submitTutorRating(stars) },
            modifier = Modifier.padding(padding),
            onNavigateToProfile = onNavigateToProfile,
            autoFillDatesForTesting = autoFillDatesForTesting)
      }
    }
  }
}
