package com.android.sample.ui.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.android.sample.ui.components.BottomNavBar
import com.android.sample.ui.components.TopAppBar
import com.android.sample.ui.theme.BrandBlue
import com.android.sample.ui.theme.CardBg
import com.android.sample.ui.theme.ChipBorder
import com.android.sample.ui.theme.SampleAppTheme

/**
 * MyBookingsScreen - Displays the user's bookings in a scrollable list.
 *
 * This composable renders the "My Bookings" page, including:
 * - A top app bar with navigation and title.
 * - A bottom navigation bar for main app sections.
 * - A vertical list of booking cards, each showing tutor, subject, price, duration, date, and
 *   rating.
 * - A "details" button for each booking, invoking [onOpenDetails] when clicked.
 *
 * UI Structure:
 * - Uses [Scaffold] to provide top and bottom bars.
 * - Booking data is provided by [MyBookingsViewModel] via StateFlow.
 * - Each booking is rendered using a private [BookingCard] composable.
 *
 * Behavior:
 * - The list updates automatically when the view model's data changes.
 * - Handles empty state by showing no cards if there are no bookings.
 * - [onOpenDetails] is called with the selected [BookingCardUi] when the details button is pressed.
 *
 * @param vm The [MyBookingsViewModel] providing the list of bookings.
 * @param navController The [NavHostController] for navigation actions.
 * @param onOpenDetails Callback invoked when the details button is clicked for a booking.
 * @param modifier Optional [Modifier] for the root composable.
 *
 * Usage:
 */
object MyBookingsPageTestTag {
  const val GO_BACK = "MyBookingsPageTestTag.GO_BACK"
  const val TOP_BAR_TITLE = "MyBookingsPageTestTag.TOP_BAR_TITLE"
  const val BOOKING_CARD = "MyBookingsPageTestTag.BOOKING_CARD"
  const val BOOKING_DETAILS_BUTTON = "MyBookingsPageTestTag.BOOKING_DETAILS_BUTTON"
  const val BOTTOM_NAV = "MyBookingsPageTestTag.BOTTOM_NAV"
  const val NAV_HOME = "MyBookingsPageTestTag.NAV_HOME"
  const val NAV_BOOKINGS = "MyBookingsPageTestTag.NAV_BOOKINGS"
  const val NAV_MESSAGES = "MyBookingsPageTestTag.NAV_MESSAGES"
  const val NAV_PROFILE = "MyBookingsPageTestTag.NAV_PROFILE"
}

@Composable
fun MyBookingsScreen(
    vm: MyBookingsViewModel,
    navController: NavHostController,
    onOpenDetails: (BookingCardUi) -> Unit = {},
    modifier: Modifier = Modifier
) {
  Scaffold(
      topBar = {
        Box(Modifier.testTag(MyBookingsPageTestTag.TOP_BAR_TITLE)) { TopAppBar(navController) }
      },
      bottomBar = {
        Box(Modifier.testTag(MyBookingsPageTestTag.BOTTOM_NAV)) { BottomNavBar(navController) }
      }) { innerPadding ->
        val items by vm.items.collectAsState()
        // Pass innerPadding to your content to avoid overlap
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(innerPadding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
              items(items, key = { it.id }) { ui -> BookingCard(ui, onOpenDetails) }
            }
      }
}

@Composable
private fun BookingCard(ui: BookingCardUi, onOpenDetails: (BookingCardUi) -> Unit) {
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
                Text(ui.tutorName.first().uppercase(), fontWeight = FontWeight.Bold)
              }

          Spacer(Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
                ui.tutorName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { /* No-op for now */})
            Spacer(Modifier.height(2.dp))
            Text(ui.subject, color = BrandBlue)
            Spacer(Modifier.height(4.dp))
            RatingRow(stars = ui.ratingStars, count = ui.ratingCount)
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
                "${ui.pricePerHourLabel}-${ui.durationLabel}",
                color = BrandBlue,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(ui.dateLabel, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onOpenDetails(ui) },
                modifier = Modifier.testTag(MyBookingsPageTestTag.BOOKING_DETAILS_BUTTON),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
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
    Text("(${count})")
  }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun MyBookingsScreenPreview() {
  SampleAppTheme {
    MyBookingsScreen(vm = MyBookingsViewModel(), navController = rememberNavController())
  }
}
