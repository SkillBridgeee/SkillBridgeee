// Kotlin
package com.android.sample.ui.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.android.sample.ui.theme.BrandBlue
import com.android.sample.ui.theme.CardBg
import com.android.sample.ui.theme.ChipBorder

object MyBookingsPageTestTag {
  const val BOOKING_CARD = "bookingCard"
  const val BOOKING_DETAILS_BUTTON = "bookingDetailsButton"
  const val NAV_HOME = "navHome"
  const val NAV_BOOKINGS = "navBookings"
  const val NAV_MESSAGES = "navMessages"
  const val NAV_PROFILE = "navProfile"
  const val EMPTY_BOOKINGS = "emptyBookings"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    viewModel: MyBookingsViewModel,
    navController: NavHostController,
    onOpenDetails: ((BookingCardUi) -> Unit)? = null,
    onOpenTutor: ((BookingCardUi) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
  Scaffold { inner ->
    val bookings by viewModel.uiState.collectAsState(initial = emptyList())
    BookingsList(
        bookings = bookings,
        navController = navController,
        onOpenDetails = onOpenDetails,
        onOpenTutor = onOpenTutor,
        modifier = modifier.padding(inner))
  }
}

@Composable
fun BookingsList(
    bookings: List<BookingCardUi>,
    navController: NavHostController,
    onOpenDetails: ((BookingCardUi) -> Unit)? = null,
    onOpenTutor: ((BookingCardUi) -> Unit)? = null,
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
        items(bookings, key = { it.id }) { booking ->
          BookingCard(
              booking = booking,
              onOpenDetails = {
                onOpenDetails?.invoke(it) ?: navController.navigate("lesson/${it.id}")
              },
              onOpenTutor = {
                onOpenTutor?.invoke(it) ?: navController.navigate("tutor/${it.tutorId}")
              })
        }
      }
}

@Composable
private fun BookingCard(
    booking: BookingCardUi,
    onOpenDetails: (BookingCardUi) -> Unit,
    onOpenTutor: (BookingCardUi) -> Unit
) {
  Card(
      modifier = Modifier.fillMaxWidth().testTag(MyBookingsPageTestTag.BOOKING_CARD),
      shape = MaterialTheme.shapes.large,
      colors = CardDefaults.cardColors(containerColor = CardBg)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
          Box(
              modifier =
                  Modifier.size(36.dp)
                      .background(Color.White, CircleShape)
                      .border(2.dp, ChipBorder, CircleShape),
              contentAlignment = Alignment.Center) {
                val first = booking.tutorName.firstOrNull()?.uppercaseChar() ?: '—'
                Text(first.toString(), fontWeight = FontWeight.Bold)
              }

          Spacer(Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
                booking.tutorName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onOpenTutor(booking) })
            Spacer(Modifier.height(2.dp))
            Text(booking.subject, color = BrandBlue)
            Spacer(Modifier.height(6.dp))
            Text(
                "${booking.pricePerHourLabel} - ${booking.durationLabel}",
                color = BrandBlue,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(booking.dateLabel)
            Spacer(Modifier.height(6.dp))
            RatingRow(stars = booking.ratingStars, count = booking.ratingCount)
          }

          Column(horizontalAlignment = Alignment.End) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onOpenDetails(booking) },
                modifier = Modifier.testTag(MyBookingsPageTestTag.BOOKING_DETAILS_BUTTON),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = BrandBlue, contentColor = Color.White)) {
                  Text("details")
                }
          }
        }
      }
}

@Composable
private fun RatingRow(stars: Int, count: Int) {
  val full = "★".repeat(stars.coerceIn(0, 5))
  val empty = "☆".repeat((5 - stars).coerceIn(0, 5))
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(full + empty)
    Spacer(Modifier.width(6.dp))
    Text("($count)")
  }
}
