package com.android.sample.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.sample.model.listing.Request
import java.text.SimpleDateFormat
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
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = modifier.clickable { onClick(request.listingId) }.testTag(testTag)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Column(modifier = Modifier.weight(1f)) {
                // Status badge
                Surface(
                    color =
                        if (request.isActive) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(bottom = 8.dp)) {
                      Text(
                          text = if (request.isActive) "Active" else "Inactive",
                          style = MaterialTheme.typography.labelSmall,
                          color =
                              if (request.isActive) MaterialTheme.colorScheme.onSecondaryContainer
                              else MaterialTheme.colorScheme.onErrorContainer,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }

                // Title (skill or description)
                Text(
                    text = request.displayTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(RequestCardTestTags.TITLE))

                Spacer(modifier = Modifier.height(4.dp))

                // Description
                if (request.description.isNotBlank()) {
                  Text(
                      text = request.description,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.testTag(RequestCardTestTags.DESCRIPTION))

                  Spacer(modifier = Modifier.height(8.dp))
                }

                // Location and date
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                      // Location
                      Text(
                          text = "📍 ${request.location.name.ifBlank { "No location" }}",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          modifier = Modifier.testTag(RequestCardTestTags.LOCATION))

                      // Created date
                      val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                      Text(
                          text = "📅 ${dateFormat.format(request.createdAt)}",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          modifier = Modifier.testTag(RequestCardTestTags.CREATED_DATE))
                    }
              }

              Spacer(modifier = Modifier.width(16.dp))

              // Price and arrow
              Column(
                  horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                    Text(
                        text = String.format(Locale.getDefault(), "$%.2f/hr", request.hourlyRate),
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
      }
}
