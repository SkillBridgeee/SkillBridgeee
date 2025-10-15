package com.android.sample.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.user.Profile
import com.android.sample.ui.theme.TealChip
import com.android.sample.ui.theme.White

object TutorCardTestTags {
  const val CARD = "TutorCardTestTags.CARD"
  const val ACTION_BUTTON = "TutorCardTestTags.ACTION_BUTTON"
}

/**
 * Reusable tutor card.
 *
 * @param profile Tutor data
 * @param pricePerHour e.g. "$25/hr" (null -> show placeholder "—/hr")
 * @param secondaryText Optional subtitle (null -> uses profile.description or "Lessons")
 * @param buttonLabel Primary action button text ("Book" by default)
 * @param onPrimaryAction Callback when the button is pressed
 * @param modifier External modifier
 * @param cardTestTag Optional testTag for the card
 * @param buttonTestTag Optional testTag for the button
 */
@Composable
fun TutorCard(
    modifier: Modifier = Modifier,
    profile: Profile,
    pricePerHour: String? = null,
    secondaryText: String? = null,
    buttonLabel: String = "Book",
    onPrimaryAction: (Profile) -> Unit,
    cardTestTag: String? = null,
    buttonTestTag: String? = null,
) {
  ElevatedCard(
      shape = MaterialTheme.shapes.large,
      colors = CardDefaults.elevatedCardColors(containerColor = White),
      modifier = modifier.testTag(cardTestTag ?: TutorCardTestTags.CARD)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(16.dp)) {
              // Avatar placeholder (replace later with Image if you have URLs)
              Box(
                  modifier =
                      Modifier.size(44.dp)
                          .clip(MaterialTheme.shapes.extraLarge)
                          .background(MaterialTheme.colorScheme.surfaceVariant))

              Spacer(Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name.ifBlank { "Tutor" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)

                val subtitle = secondaryText ?: profile.description.ifBlank { "Lessons" }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)

                Spacer(Modifier.height(4.dp))
                RatingRow(rating = profile.tutorRating)
              }

              Spacer(Modifier.width(8.dp))

              Column(horizontalAlignment = Alignment.End) {
                Text(text = pricePerHour ?: "—/hr", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = { onPrimaryAction(profile) },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = TealChip,
                            contentColor = White,
                            disabledContainerColor = TealChip.copy(alpha = 0.38f),
                            disabledContentColor = White.copy(alpha = 0.38f)),
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.testTag(buttonTestTag ?: TutorCardTestTags.ACTION_BUTTON)) {
                      Text(buttonLabel)
                    }
              }
            }
      }
}

@Composable
private fun RatingRow(rating: RatingInfo) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    RatingStars(ratingOutOfFive = rating.averageRating)
    Spacer(Modifier.width(6.dp))
    Text(
        "(${rating.totalRatings})",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}
