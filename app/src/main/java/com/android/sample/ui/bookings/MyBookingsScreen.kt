// Kotlin
package com.android.sample.ui.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.ui.components.BookingCard

object MyBookingsPageTestTag {
  const val BOOKING_CARD = "bookingCard"
  const val BOOKING_DETAILS_BUTTON = "bookingDetailsButton"
  const val NAV_HOME = "navHome"
  const val NAV_BOOKINGS = "navBookings"
  const val NAV_PROFILE = "navProfile"
  const val EMPTY_BOOKINGS = "emptyBookings"
  const val NAV_MAP = "nav_map"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    modifier: Modifier = Modifier,
    viewModel: MyBookingsViewModel = viewModel(),
    onBookingClick: (String) -> Unit
) {
  Scaffold { inner ->
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    when {
      uiState.isLoading -> CircularProgressIndicator()
      uiState.hasError -> Text("Failed to load your bookings")
      uiState.bookings.isEmpty() -> Text("No bookings available")
      else ->
          BookingsList(
              bookings = uiState.bookings,
              onBookingClick = onBookingClick,
              modifier = modifier.padding(inner))
    }
  }
}

@Composable
fun BookingsList(
    bookings: List<BookingCardUI>,
    onBookingClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  LazyColumn(
      modifier = modifier.fillMaxSize().padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(bookings, key = { it.booking.bookingId }) { bookingUI ->
          BookingCard(
              booking = bookingUI.booking,
              listing = bookingUI.listing,
              creator = bookingUI.creatorProfile,
              onClickBookingCard = { it -> onBookingClick(it) })
        }
      }
}
