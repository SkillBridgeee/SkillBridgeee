package com.android.sample.ui.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.ui.components.BookingCard

object MyBookingsPageTestTag {
  const val MY_BOOKINGS_SCREEN = "myBookingsScreenScreen"
  const val LOADING = "myBookingsLoading"
  const val ERROR = "myBookingsError"
  const val EMPTY = "myBookingsEmpty"
  const val NAV_HOME = "navHome"
  const val NAV_BOOKINGS = "navBookings"
  const val NAV_PROFILE = "navProfile"
  const val NAV_MAP = "navMap"
}

/**
 * Main composable function that displays the "My Bookings" screen.
 *
 * This screen is responsible for showing all bookings belonging to the current user. It observes
 * the [MyBookingsViewModel] to manage loading, error, and empty states.
 *
 * Depending on the current UI state:
 * - Displays a loading message while data is being fetched.
 * - Displays an error message if the data retrieval fails.
 * - Displays an empty message if there are no bookings.
 * - Displays a list of bookings once successfully loaded.
 *
 * @param modifier Optional [Modifier] for styling or layout adjustments.
 * @param viewModel The [MyBookingsViewModel] that provides the booking data and UI state.
 * @param onBookingClick Callback invoked when a booking card is clicked, passing the booking ID.
 */
@Composable
fun MyBookingsScreen(
    modifier: Modifier = Modifier,
    viewModel: MyBookingsViewModel = viewModel(),
    onBookingClick: (String) -> Unit
) {
  Scaffold(modifier = modifier.testTag(MyBookingsPageTestTag.MY_BOOKINGS_SCREEN)) { inner ->
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    when {
      uiState.isLoading -> CenteredText("Loading...", MyBookingsPageTestTag.LOADING)
      uiState.hasError -> CenteredText("Failed to load your bookings", MyBookingsPageTestTag.ERROR)
      uiState.bookings.isEmpty() ->
          CenteredText("No bookings available", MyBookingsPageTestTag.EMPTY)
      else ->
          BookingsList(
              bookings = uiState.bookings,
              onBookingClick = onBookingClick,
              modifier = modifier.padding(inner))
    }
  }
}

/**
 * Composable function that displays a scrollable list of booking cards.
 *
 * The list is rendered using a [LazyColumn], where each item corresponds to a [BookingCard]. It
 * also handles spacing and padding between items for a clean layout.
 *
 * @param bookings A list of [BookingCardUI] objects representing the user's bookings.
 * @param onBookingClick Callback triggered when a booking card is clicked.
 * @param modifier Optional [Modifier] to apply to the list container.
 */
@Composable
fun BookingsList(
    bookings: List<BookingCardUI>,
    onBookingClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(12.dp),
        contentPadding = PaddingValues(6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(bookings, key = { it.booking.bookingId }) { bookingUI ->
            BookingCard(
                booking = bookingUI.booking,
                listing = bookingUI.listing,
                creator = bookingUI.creatorProfile,
                onClickBookingCard = { bookingId -> onBookingClick(bookingId) },
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

/**
 * Composable helper function that displays centered text within the screen.
 *
 * This is used for displaying loading, error, or empty states in a simple, centered layout. It also
 * includes a test tag to facilitate UI testing.
 *
 * @param text The message text to be displayed.
 * @param tag The test tag to identify the composable in UI tests.
 */
@Composable
private fun CenteredText(text: String, tag: String) {
  Box(modifier = Modifier.fillMaxSize().testTag(tag), contentAlignment = Alignment.Center) {
    Text(text = text)
  }
}
