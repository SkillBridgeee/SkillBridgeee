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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.sample.model.rating.RatingInfo
import java.util.Locale

@Composable
internal fun ListingCardBase(
    id: String,
    title: String,
    description: String,
    locationName: String,
    createdAt: java.util.Date,
    hourlyRate: Double,
    isActive: Boolean,
    rating: RatingInfo?,
    cardContainerColor: androidx.compose.ui.graphics.Color,
    badgeActiveColor: androidx.compose.ui.graphics.Color,
    badgeActiveTextColor: androidx.compose.ui.graphics.Color,
    priceColor: androidx.compose.ui.graphics.Color,
    testTagCard: String,
    testTagTitle: String,
    testTagDescription: String,
    testTagHourlyRate: String,
    testTagLocation: String,
    testTagCreatedDate: String,
    testTagStatusBadge: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
  Card(
      shape = MaterialTheme.shapes.medium,
      colors = CardDefaults.cardColors(containerColor = cardContainerColor),
      modifier = modifier.clickable { onClick(id) }.testTag(testTagCard)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Column(modifier = Modifier.weight(1f)) {
                StatusBadge(
                    isActive = isActive,
                    activeColor = badgeActiveColor,
                    activeTextColor = badgeActiveTextColor,
                    testTag = testTagStatusBadge)

                CardTitle(title = title, testTag = testTagTitle)
                Spacer(modifier = Modifier.height(4.dp))
                CardDescription(description = description, testTag = testTagDescription)

                LocationAndDateRow(
                    locationName = locationName,
                    createdAt = createdAt,
                    locationTestTag = testTagLocation,
                    dateTestTag = testTagCreatedDate)

                rating?.let {
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    val avg = it.averageRating.coerceIn(0.0, 5.0)
                    RatingStars(ratingOutOfFive = avg)
                    Spacer(Modifier.width(8.dp))

                    val ratingText =
                        if (it.totalRatings == 0) "No ratings yet"
                        else String.format(Locale.getDefault(), "%.1f (%d)", avg, it.totalRatings)

                    Text(
                        text = ratingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle =
                            if (it.totalRatings == 0) FontStyle.Italic else FontStyle.Normal)
                  }
                }
              }

              Spacer(modifier = Modifier.width(16.dp))

              Column(
                  horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                    Text(
                        text = String.format(Locale.getDefault(), "$%.2f/hr", hourlyRate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = priceColor,
                        modifier = Modifier.testTag(testTagHourlyRate))

                    Spacer(modifier = Modifier.height(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
            }
      }
}
