// Kotlin
package com.android.sample.ui.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
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
    viewModel: MyBookingsViewModel = MyBookingsViewModel(),
    onBookingClick: (String) -> Unit
) {
  Scaffold { inner ->
    val bookings by viewModel.uiState.collectAsState(initial = emptyList())
    BookingsList(
        bookings = bookings, onBookingClick = onBookingClick, modifier = modifier.padding(inner))
  }
}

@Composable
fun BookingsList(
    bookings: List<BookingCardUIV2>,
    onBookingClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  if (bookings.isEmpty()) {
    Box(
        modifier =
            modifier.fillMaxSize().padding(16.dp).testTag(MyBookingsPageTestTag.EMPTY_BOOKINGS),
        contentAlignment = Alignment.Center) {
          Text(text = "No bookings available")
        }
    return
  }

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
