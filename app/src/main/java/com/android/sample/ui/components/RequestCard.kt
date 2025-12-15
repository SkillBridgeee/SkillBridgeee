package com.android.sample.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.sample.model.listing.Request
import com.android.sample.model.rating.RatingInfo

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
