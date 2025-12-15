package com.android.sample.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.sample.model.listing.Request
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
    modifier: Modifier = Modifier,
    testTag: String = RequestCardTestTags.CARD
) {
  Card(
      shape = MaterialTheme.shapes.medium,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      modifier = modifier.clickable { onClick(request.listingId) }.testTag(testTag)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              RequestCardContent(request = request)
              Spacer(modifier = Modifier.width(16.dp))
              RequestCardPriceSection(hourlyRate = request.hourlyRate)
            }
      }
}

@Composable
private fun RowScope.RequestCardContent(request: Request) {
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
