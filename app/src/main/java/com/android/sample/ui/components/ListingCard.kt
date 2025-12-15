package com.android.sample.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.sample.model.listing.Listing
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.user.Profile
import java.util.Locale
import kotlin.compareTo

object ListingCardTestTags {
  const val CARD = "ListingCardTestTags.CARD"
  const val BOOK_BUTTON = "ListingCardTestTags.BOOK_BUTTON"
}

/**
 * ListingCard shows a bookable lesson/offer.
 *
 * It includes:
 * - Listing title
 * - Creator name
 * - Hourly rate
 * - Book button
 * - Tutor rating stars + count
 * - Listing rating stars + count
 * - Location
 */
@Composable
fun ListingCard(
    listing: Listing,
    creator: Profile? = null,
    creatorRating: RatingInfo = RatingInfo(),
    listingRating: RatingInfo = RatingInfo(),
    modifier: Modifier = Modifier,
    onOpenListing: (String) -> Unit = {},
    onBook: (String) -> Unit = {},
    testTags: Pair<String?, String?>? = null
) {
  Card(
      shape = MaterialTheme.shapes.large,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier =
          modifier
              .clickable { onOpenListing(listing.listingId) }
              .testTag(testTags?.first ?: ListingCardTestTags.CARD)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
              // Avatar circle with creator initial
              Box(
                  modifier =
                      Modifier.size(48.dp)
                          .clip(MaterialTheme.shapes.extraLarge)
                          .background(MaterialTheme.colorScheme.surfaceVariant),
                  contentAlignment = Alignment.Center) {
                    Text(
                        text = creator?.name?.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                  }

              Spacer(Modifier.width(12.dp))

              // Main content
              Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = listing.displayTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1)

                // Creator name
                Text(
                    text = "by ${creator?.name ?: listing.creatorUserId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(Modifier.height(8.dp))

                // Ratings (two lines)
                RatingsLine(
                    label = "Tutor:",
                    rating = creatorRating,
                )

                Spacer(Modifier.height(4.dp))

                RatingsLine(
                    label = "Listing:",
                    rating = listingRating,
                )

                Spacer(Modifier.height(8.dp))

                // Location (shown once)
                Text(
                    text = listing.location.name.ifBlank { "Unknown" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              Spacer(Modifier.width(12.dp))

              // Price + Book
              Column(
                  horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                    val priceLabel =
                        String.format(Locale.getDefault(), "$%.2f / hr", listing.hourlyRate)

                    Text(
                        text = priceLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold)

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { onBook(listing.listingId) },
                        shape = RoundedCornerShape(8.dp),
                        modifier =
                            Modifier.testTag(testTags?.second ?: ListingCardTestTags.BOOK_BUTTON)) {
                          Text("Book")
                        }
                  }
            }
      }
}

@Composable
private fun RatingsLine(label: String, rating: RatingInfo, modifier: Modifier = Modifier) {
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(Modifier.width(6.dp))

    if (rating.totalRatings > 0) {
      val avg = rating.averageRating.coerceIn(0.0, 5.0)
      RatingStars(ratingOutOfFive = avg)

      Spacer(Modifier.width(6.dp))

      Text(
          text = "(${rating.totalRatings})",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
      Text(
          text = "No ratings yet",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontStyle = FontStyle.Italic)
    }
  }
}
