package com.android.sample.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.sample.model.listing.Request
import com.android.sample.model.rating.RatingInfo
import java.util.Locale

object RequestCardTestTags {
  const val CARD = "RequestCardTestTags.CARD"
  const val TITLE = "RequestCardTestTags.TITLE"
  const val DESCRIPTION = "RequestCardTestTags.DESCRIPTION"
  const val HOURLY_RATE = "RequestCardTestTags.HOURLY_RATE"
  const val LOCATION = "RequestCardTestTags.LOCATION"
  const val CREATED_DATE = "RequestCardTestTags.CREATED_DATE"
  const val STATUS_BADGE = "RequestCardTestTags.STATUS_BADGE"
}

/**
 * A card component displaying a request (student looking for a tutor).
 *
 * @param request The request data to display.
 * @param onClick Callback when the card is clicked, receives the request ID.
 * @param modifier Modifier for styling.
 * @param testTag Optional test tag for the card.
 */
@Composable
fun RequestCard(
    request: Request,
    onClick: (String) -> Unit,
    rating: RatingInfo? = null,
    modifier: Modifier = Modifier,
    testTag: String = RequestCardTestTags.CARD
) {
  ListingCardBase(
      id = request.listingId,
      title = request.displayTitle(),
      description = request.description,
      locationName = request.location.name,
      createdAt = request.createdAt,
      hourlyRate = request.hourlyRate,
      isActive = request.isActive,
      rating = rating,
      cardContainerColor = MaterialTheme.colorScheme.surface,
      badgeActiveColor = MaterialTheme.colorScheme.secondaryContainer,
      badgeActiveTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
      priceColor = MaterialTheme.colorScheme.secondary,
      testTagCard = testTag,
      testTagTitle = RequestCardTestTags.TITLE,
      testTagDescription = RequestCardTestTags.DESCRIPTION,
      testTagHourlyRate = RequestCardTestTags.HOURLY_RATE,
      testTagLocation = RequestCardTestTags.LOCATION,
      testTagCreatedDate = RequestCardTestTags.CREATED_DATE,
      testTagStatusBadge = RequestCardTestTags.STATUS_BADGE,
      onClick = onClick,
      modifier = modifier)
}

@Composable
private fun RowScope.RequestCardContent(request: Request, rating: RatingInfo?) {
  Column(modifier = Modifier.weight(1f)) {
    StatusBadge(
        isActive = request.isActive,
        activeColor = MaterialTheme.colorScheme.secondaryContainer,
        activeTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
        testTag = RequestCardTestTags.STATUS_BADGE)

    CardTitle(title = request.displayTitle(), testTag = RequestCardTestTags.TITLE)
    Spacer(modifier = Modifier.height(4.dp))
    CardDescription(description = request.description, testTag = RequestCardTestTags.DESCRIPTION)
    LocationAndDateRow(
        locationName = request.location.name,
        createdAt = request.createdAt,
        locationTestTag = RequestCardTestTags.LOCATION,
        dateTestTag = RequestCardTestTags.CREATED_DATE)

    rating?.let {
      Spacer(modifier = Modifier.height(8.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        val avg = it.averageRating.coerceIn(0.0, 5.0)
        RatingStars(ratingOutOfFive = avg)
        Spacer(Modifier.width(8.dp))
        val ratingText =
            if (it.totalRatings == 0) {
              "No ratings yet"
            } else {
              String.format(Locale.getDefault(), "%.1f (%d)", avg, it.totalRatings)
            }
        Text(
            text = ratingText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontStyle = if (it.totalRatings == 0) FontStyle.Italic else FontStyle.Normal)
      }
    }
  }
}

@Composable
private fun RequestCardPriceSection(hourlyRate: Double) {
  Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
    Text(
        text = String.format(Locale.getDefault(), "$%.2f/hr", hourlyRate),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.testTag(RequestCardTestTags.HOURLY_RATE))

    Spacer(modifier = Modifier.height(8.dp))

    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = "View details",
        tint = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}
