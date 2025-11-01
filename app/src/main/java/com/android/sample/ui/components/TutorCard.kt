// kotlin
package com.android.sample.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.sample.model.user.Profile
import com.android.sample.ui.theme.White

object NewTutorCardTestTags {
  const val CARD = "TutorCardTestTags.CARD"
}

@Composable
fun NewTutorCard(
    profile: Profile,
    modifier: Modifier = Modifier,
    secondaryText: String? = null, // optional subtitle override
    onOpenProfile: (String) -> Unit = {}, // navigate to tutor profile
    cardTestTag: String? = null,
) {
  // Centralized, non-hardcoded fallbacks
  val unknownLabel = "Unknown"
  val tutorLabel = "Tutor"
  val lessonsLabel = "Lessons"

  val displayName = profile.name?.takeIf { it.isNotBlank() } ?: tutorLabel
  val avatarText = displayName.firstOrNull()?.uppercase() ?: unknownLabel.first().toString()
  val subtitle = secondaryText ?: profile.description.ifBlank { lessonsLabel }
  val locationText = profile.location.name.ifBlank { unknownLabel }

  ElevatedCard(
      shape = MaterialTheme.shapes.large,
      colors = CardDefaults.elevatedCardColors(containerColor = White),
      modifier =
          modifier
              .clickable { onOpenProfile(profile.userId) }
              .testTag(cardTestTag ?: NewTutorCardTestTags.CARD)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(16.dp)) {
              // Avatar circle with initial
              Box(
                  modifier =
                      Modifier.size(44.dp)
                          .clip(MaterialTheme.shapes.extraLarge)
                          .background(MaterialTheme.colorScheme.surfaceVariant),
                  contentAlignment = Alignment.Center) {
                    Text(
                        text = avatarText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                  }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                // Tutor name
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold)

                // Short bio / description / override text
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(Modifier.height(8.dp))

                // Rating row (stars + total ratings)
                Row(verticalAlignment = Alignment.CenterVertically) {
                  RatingStars(ratingOutOfFive = profile.tutorRating.averageRating)
                  Spacer(Modifier.width(6.dp))
                  Text(
                      text = "(${profile.tutorRating.totalRatings})",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(Modifier.height(4.dp))

                // Location
                Text(
                    text = locationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
      }
}
