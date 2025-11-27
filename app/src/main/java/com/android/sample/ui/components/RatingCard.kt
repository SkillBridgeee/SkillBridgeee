package com.android.sample.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.sample.model.rating.Rating
import com.android.sample.model.user.Profile

object RatingTestTags {
  const val CARD = "RatingCardTestTags.CARD"
  const val STARS = "RatingCardTestTags.STARS"
  const val COMMENT = "RatingCardTestTags.COMMENT"
  const val RATER_NAME = "RatingCardTestTags.RATER_NAME"
  const val INFO_PART = "RatingCardTestTags.INFO_PART"
  const val RATER_IMAGE = "RatingCardTestTags.RATER_IMAGE"
}

@Composable
@Preview
fun RatingCard(
    rating: Rating = Rating(),
    rater: Profile? = null,
) {
  Card(
      shape = MaterialTheme.shapes.large,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.testTag(RatingTestTags.CARD)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
          // Avatar circle with rater initial
          Box(
              modifier =
                  Modifier.size(48.dp)
                      .clip(MaterialTheme.shapes.extraLarge)
                      .background(MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center) {
                Text(
                    modifier = Modifier.testTag(RatingTestTags.RATER_IMAGE),
                    text = (rater?.name?.firstOrNull()?.uppercase() ?: "U"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
              }

          Spacer(Modifier.width(6.dp))

          Column {
            Row(
                modifier =
                    Modifier.fillMaxWidth().padding(4.dp).testTag(RatingTestTags.INFO_PART)) {
                  Text(
                      text = "by ${rater?.name ?: "Unknown"}",
                      style = MaterialTheme.typography.titleMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.testTag(RatingTestTags.RATER_NAME))

                  Spacer(modifier = Modifier.weight(1f))

                  val grade = rating.starRating.value // 1..5

                  RatingStars(
                      ratingOutOfFive = grade.toDouble(),
                      modifier = Modifier.testTag(RatingTestTags.STARS))
                }

            Spacer(Modifier.height(8.dp))

            Text(
                modifier = Modifier.testTag(RatingTestTags.COMMENT),
                text = rating.comment.takeUnless { it.isBlank() } ?: "No comment provided",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
}
