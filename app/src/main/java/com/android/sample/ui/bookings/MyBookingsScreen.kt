// Kotlin
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
  const val LOADING = "myBookingsLoading"
  const val ERROR = "myBookingsError"
  const val EMPTY = "myBookingsEmpty"
  const val BOOKING_CARD = "bookingCard"
  const val NAV_HOME = "navHome"
  const val NAV_BOOKINGS = "navBookings"
  const val NAV_PROFILE = "navProfile"
  const val NAV_MAP = "navMap"
}

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

@Composable
fun BookingsList(
    bookings: List<BookingCardUI>,
    onBookingClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  LazyColumn(
      modifier = modifier.fillMaxSize().padding(12.dp),
      contentPadding = PaddingValues(6.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(bookings, key = { it.booking.bookingId }) { bookingUI ->
          BookingCard(
              booking = bookingUI.booking,
              listing = bookingUI.listing,
              creator = bookingUI.creatorProfile,
              onClickBookingCard = { bookingId -> onBookingClick(bookingId) })
        }
      }
}

@Composable
private fun CenteredText(text: String, tag: String) {
  Box(modifier = Modifier.fillMaxSize().testTag(tag), contentAlignment = Alignment.Center) {
    Text(text = text)
  }
}
