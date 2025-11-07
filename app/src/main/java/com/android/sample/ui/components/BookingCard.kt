package com.android.sample.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.booking.color
import com.android.sample.model.booking.dateString
import com.android.sample.model.booking.name
import java.util.Date

@SuppressLint("DefaultLocale")
@Composable
fun BookingCard(
    modifier: Modifier = Modifier,
    booking: Booking,
    listingTitle: String,
    listingHourlyRate: Double,
    tutorName: String,
    onOpenBooking: (String) -> Unit = {},
    testTags: Pair<String?, String?>? = null
) {

  val statusString = booking.status.name()
  val statusColor = booking.status.color()
  val priceString = String.format("$%.2f / hr", listingHourlyRate)
  val bookingDate = booking.dateString()

  Card(
      shape = MaterialTheme.shapes.large,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(0.5.dp, Color.Gray),
      modifier =
          modifier
              .clickable { onOpenBooking(booking.bookingId) }
              .testTag(testTags?.first ?: ListingCardTestTags.CARD)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
          // Avatar circle with tutor initial
          Box(
              modifier =
                  Modifier.size(48.dp)
                      .clip(MaterialTheme.shapes.extraLarge)
                      .background(MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center) {
                Text(
                    text = tutorName.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
              }

          Spacer(Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = listingTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)

            // Tutor name
            Text(
                text = "by $tutorName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)

            Spacer(Modifier.height(8.dp))

            // Status
            Text(
                text = statusString,
                color = statusColor,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                modifier =
                    Modifier.border(
                            width = 1.dp, color = statusColor, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp))
          }

          Spacer(Modifier.width(12.dp))

          Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {

            // date
            Text(
                text = bookingDate,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold)

            Spacer(Modifier.height(8.dp))

            // Price text
            Text(
                text = priceString,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold)
          }
        }
      }
}

@Preview(showBackground = true)
@Composable
fun BookingCardPreview() {

  Column {
    val booking = Booking(status = BookingStatus.PENDING, sessionStart = Date())

    BookingCard(
        listingTitle = "titre du coursaaaaaaaaaaaaammmmmmmmmmmmmmmmmmmmmmmm",
        listingHourlyRate = 12.0,
        tutorName = "jean mich",
        onOpenBooking = { println("Open listing $it") },
        booking = booking)

    val booking1 = Booking(status = BookingStatus.CONFIRMED, sessionStart = Date())

    BookingCard(
        listingTitle = "mm",
        listingHourlyRate = 12.22222,
        tutorName = "asdfasdvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvbbbbbvvbbvbf",
        onOpenBooking = { println("Open listing $it") },
        booking = booking1)

    val booking2 = Booking(status = BookingStatus.COMPLETED, sessionStart = Date())

    BookingCard(
        listingTitle = "asdfasdfasdfs",
        listingHourlyRate = 0.33,
        tutorName = "bg ultime",
        onOpenBooking = { println("Open listing $it") },
        booking = booking2)

    val booking3 = Booking(status = BookingStatus.CANCELLED, sessionStart = Date())

    BookingCard(
        listingTitle = "bookkke",
        listingHourlyRate = 12.0,
        tutorName = "jean mich",
        onOpenBooking = { println("Open listing $it") },
        booking = booking3)
  }
}
