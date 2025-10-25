package com.android.sample.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.sample.model.listing.Listing
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.user.Profile
import java.util.Locale

object ListingCardTestTags {
  const val CARD = "ListingCardTestTags.CARD"
  const val BOOK_BUTTON = "ListingCardTestTags.BOOK_BUTTON"
}

/**
 * ListingCard shows a bookable lesson/offer.
 *
 * It includes:
 * - Listing title (usually listing.description)
 * - Tutor name ("by Alice Johnson")
 * - Hourly rate
 * - A "Book" button
 * - Rating stars + rating count
 * - Location
 *
 * Behavior:
 * - Tapping anywhere on the card calls [onOpenListing] with the listing ID (navigate to future
 *   Listing Details screen).
 * - Tapping "Book" calls [onBook] with the listing ID (start booking flow).
 */
@Composable
fun ListingCard(
    listing: Listing,
    creator: Profile? = null,
    creatorRating: RatingInfo = RatingInfo(),
    modifier: Modifier = Modifier,
    onOpenListing: (String) -> Unit = {},
    onBook: (String) -> Unit = {},
    cardTestTag: String? = null,
    bookButtonTestTag: String? = null
) {
  Card(
      shape = MaterialTheme.shapes.large,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier =
          modifier
              .clickable { onOpenListing(listing.listingId) }
              .testTag(cardTestTag ?: ListingCardTestTags.CARD)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
          // Avatar circle with tutor initial
          Box(
              modifier =
                  Modifier.size(48.dp)
                      .clip(MaterialTheme.shapes.extraLarge)
                      .background(MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center) {
                Text(
                    text = (creator?.name?.firstOrNull()?.uppercase() ?: "?"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
              }

          Spacer(Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            // Title: description if present, else fallback to skill / subject
            val title =
                listing.description.ifBlank {
                  listing.skill.skill.ifBlank { listing.skill.mainSubject.name }
                }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1)

            // Tutor name
            Text(
                text = "by ${creator?.name ?: listing.creatorUserId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(8.dp))

            // Rating stars + (count) + Location
            Row(verticalAlignment = Alignment.CenterVertically) {
              RatingStars(ratingOutOfFive = creatorRating.averageRating)
              Spacer(Modifier.width(6.dp))
              Text(
                  text = "(${creatorRating.totalRatings})",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(Modifier.width(8.dp))
              Column {
                Text(
                    text = listing.location.name.ifBlank { "Unknown" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }

          Spacer(Modifier.width(12.dp))

          Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
            val priceLabel = String.format(Locale.getDefault(), "$%.2f / hr", listing.hourlyRate)

            Text(
                text = priceLabel,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold)

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { onBook(listing.listingId) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag(bookButtonTestTag ?: ListingCardTestTags.BOOK_BUTTON)) {
                  Text("Book")
                }
          }
        }
      }
}
