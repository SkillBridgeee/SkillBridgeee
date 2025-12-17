package com.android.sample.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.color
import com.android.sample.model.booking.dateString
import com.android.sample.model.booking.name
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingType
import com.android.sample.model.user.Profile
import java.util.Locale

object BookingCardTestTag {
  const val CARD = "booking_card"
  const val LISTING_TITLE = "booking_card_listing_title"
  const val CREATOR_NAME = "booking_card_creator_name"
  const val STATUS = "booking_card_status"
  const val DATE = "booking_card_date"
  const val PRICE = "booking_card_price"
}

@Composable
fun BookingCard(
    modifier: Modifier = Modifier,
    booking: Booking,
    listing: Listing,
    creator: Profile,
    onClickBookingCard: (String) -> Unit = {}
) {

  val statusString = booking.status.name()
  val statusColor = booking.status.color()
  val bookingDate = booking.dateString()
  val listingType = listing.type
  val listingTitle = listing.displayTitle()
  val creatorName = creator.name ?: "Unknown"
  val priceString =
      remember(listing.hourlyRate) { String.format(Locale.ROOT, "$%.2f / hr", listing.hourlyRate) }

  Card(
      shape = MaterialTheme.shapes.large,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(0.5.dp, Color.Gray),
      modifier =
          modifier
              .clickable { onClickBookingCard(booking.bookingId) }
              .testTag(BookingCardTestTag.CARD)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
          Spacer(Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cardTitle(listing, listingTitle),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag(BookingCardTestTag.LISTING_TITLE))

            // Creator name
            Text(
                text = creatorName(creatorName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag(BookingCardTestTag.CREATOR_NAME))

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
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag(BookingCardTestTag.STATUS))
          }

          Spacer(Modifier.width(12.dp))

          Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {

            // Date
            Text(
                text = bookingDate,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag(BookingCardTestTag.DATE))

            Spacer(Modifier.height(8.dp))

            // Price text
            Text(
                text = priceString,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag(BookingCardTestTag.PRICE))
          }
        }
      }
}

@Composable
private fun cardTitle(listing: Listing, listingTitle: String): AnnotatedString {
  val currentUser = UserSessionManager.getCurrentUserId()
  val tutorStudentPrefix: String =
      when (listing.type) {
        ListingType.REQUEST -> {
          if (listing.creatorUserId == currentUser) "Student for " else "Tutor for "
        }
        ListingType.PROPOSAL -> {
          if (listing.creatorUserId == currentUser) "Tutor for " else "Student for "
        }
      }
  val styledText = buildAnnotatedString {
    withStyle(style = SpanStyle(fontSize = MaterialTheme.typography.bodySmall.fontSize)) {
      append(tutorStudentPrefix)
    }
    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) { append(listingTitle) }
  }
  return styledText
}

@Composable
private fun creatorName(creatorName: String): AnnotatedString {
  val creatorNamePrefix = "by "
  val styledText = buildAnnotatedString {
    withStyle(style = SpanStyle(fontSize = MaterialTheme.typography.bodySmall.fontSize)) {
      append(creatorNamePrefix)
    }
    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) { append(creatorName) }
  }
  return styledText
}
