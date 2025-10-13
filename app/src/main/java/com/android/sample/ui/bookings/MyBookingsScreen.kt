package com.android.sample.ui.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.android.sample.model.booking.FakeBookingRepository
import com.android.sample.ui.components.BottomNavBar
import com.android.sample.ui.components.TopAppBar
import com.android.sample.ui.theme.BrandBlue
import com.android.sample.ui.theme.CardBg
import com.android.sample.ui.theme.ChipBorder
import com.android.sample.ui.theme.SampleAppTheme

/**
 * Testing tags used in this file:
 * - Top bar wrapper: [MyBookingsPageTestTag.TOP_BAR_TITLE]
 * - Bottom nav wrapper: [MyBookingsPageTestTag.BOTTOM_NAV]
 * - Each booking card: [MyBookingsPageTestTag.BOOKING_CARD]
 * - Each details button: [MyBookingsPageTestTag.BOOKING_DETAILS_BUTTON]
 */
object MyBookingsPageTestTag {
  const val GO_BACK = "MyBookingsPageTestTag.GO_BACK"
  const val TOP_BAR_TITLE = "MyBookingsPageTestTag.TOP_BAR_TITLE" // <— Missing before; added.
  const val BOOKING_CARD = "MyBookingsPageTestTag.BOOKING_CARD"
  const val BOOKING_DETAILS_BUTTON = "MyBookingsPageTestTag.BOOKING_DETAILS_BUTTON"
  const val BOTTOM_NAV = "MyBookingsPageTestTag.BOTTOM_NAV"
  const val NAV_HOME = "MyBookingsPageTestTag.NAV_HOME"
  const val NAV_BOOKINGS = "MyBookingsPageTestTag.NAV_BOOKINGS"
  const val NAV_MESSAGES = "MyBookingsPageTestTag.NAV_MESSAGES"
  const val NAV_PROFILE = "MyBookingsPageTestTag.NAV_PROFILE"
}

/**
 * Renders the **My Bookings** page.
 *
 * ### Responsibilities
 * - Shows a scrollable list of user bookings.
 * - Provides the shared top app bar and bottom navigation.
 * - Emits a callback when the “details” button on a card is pressed.
 *
 * ### Data flow
 * - Collects [MyBookingsViewModel.items] and renders each item via [BookingCard].
 * - The list uses stable keys ([BookingCardUi.id]) to support smooth updates.
 *
 * ### Testing hooks
 * - Top bar wrapper: [MyBookingsPageTestTag.TOP_BAR_TITLE]
 * - Bottom nav wrapper: [MyBookingsPageTestTag.BOTTOM_NAV]
 * - Each booking card: [MyBookingsPageTestTag.BOOKING_CARD]
 * - Each details button: [MyBookingsPageTestTag.BOOKING_DETAILS_BUTTON]
 *
 * ### Empty state
 * - When [MyBookingsViewModel.items] is empty, no cards are rendered (dedicated empty UI can be
 *   added later without changing this contract).
 *
 * @param vm ViewModel that exposes the list of bookings as a `StateFlow<List<BookingCardUi>>`.
 * @param navController Host controller for navigation used by the shared bars.
 * @param onOpenDetails Invoked with the associated [BookingCardUi] when a card’s “details” is
 *   tapped.
 * @param modifier Optional root [Modifier].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    viewModel: MyBookingsViewModel,
    navController: NavHostController,
    onOpenDetails: ((BookingCardUi) -> Unit)? = null,
    onOpenTutor: ((BookingCardUi) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
  Scaffold(
      topBar = {
        Box(Modifier.testTag(MyBookingsPageTestTag.TOP_BAR_TITLE)) { TopAppBar(navController) }
      },
      bottomBar = {
        Box(Modifier.testTag(MyBookingsPageTestTag.BOTTOM_NAV)) { BottomNavBar(navController) }
      }) { innerPadding ->
        MyBookingsContent(
            viewModel = viewModel,
            navController = navController,
            onOpenDetails = onOpenDetails,
            onOpenTutor = onOpenTutor,
            modifier = modifier.padding(innerPadding))
      }
}

/**
 * Content-only composable that renders the scrollable list of bookings. Use this directly in tests
 * that already provide top/bottom bars to avoid duplicate tags.
 */
@Composable
fun MyBookingsContent(
    viewModel: MyBookingsViewModel,
    navController: NavHostController,
    onOpenDetails: ((BookingCardUi) -> Unit)? = null,
    onOpenTutor: ((BookingCardUi) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
  val items by viewModel.items.collectAsState()

  LazyColumn(
      modifier = modifier.fillMaxSize().padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items, key = { it.id }) { ui ->
          BookingCard(
              ui = ui,
              onOpenDetails = {
                onOpenDetails?.invoke(it) ?: navController.navigate("lesson/${it.id}")
              },
              onOpenTutor = {
                onOpenTutor?.invoke(it) ?: navController.navigate("tutor/${it.tutorId}")
              })
        }
      }
}

/**
 * Visual representation of a single booking.
 *
 * ### Shows
 * - Avatar initial (first letter of tutor’s name) inside a circular chip.
 * - Tutor name, subject (link-styled color), star rating (0..5) with count.
 * - Price per hour + duration (e.g., `$50/hr-2hrs`) and the booking date.
 * - Primary “details” button that triggers [onOpenDetails].
 */
@Composable
private fun BookingCard(
    ui: BookingCardUi,
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
                Text(ui.tutorName.first().uppercase(), fontWeight = FontWeight.Bold)
              }

          Spacer(Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
                ui.tutorName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onOpenTutor(ui) })
            Spacer(Modifier.height(2.dp))
            Text(ui.subject, color = BrandBlue)
            Spacer(Modifier.height(6.dp))
            Text(
                "${ui.pricePerHourLabel}-${ui.durationLabel}",
                color = BrandBlue,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(ui.dateLabel)
            Spacer(Modifier.height(6.dp))
            RatingRow(stars = ui.ratingStars, count = ui.ratingCount)
          }

          Column(horizontalAlignment = Alignment.End) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onOpenDetails(ui) },
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

/**
 * Small row that renders a 0..5 star visualization and the rating count.
 *
 * The provided [stars] value is clamped to the valid range for safety.
 */
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

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun MyBookingsScreenPreview() {
  SampleAppTheme {
    val vm = MyBookingsViewModel(FakeBookingRepository(), "s1")
    LaunchedEffect(Unit) { vm.refresh() }
    MyBookingsScreen(viewModel = vm, navController = rememberNavController())
  }
}
