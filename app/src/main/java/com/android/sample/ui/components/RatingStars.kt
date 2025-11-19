package com.android.sample.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlin.math.roundToInt

/** Test tags for the [RatingStars] composable. */
object RatingStarsTestTags {
  const val FILLED_STAR = "RatingStarsTestTags.FILLED_STAR"
  const val OUTLINED_STAR = "RatingStarsTestTags.OUTLINED_STAR"
}

private const val MAX_STARS = 5

/**
 * A composable that displays a star rating out of 5.
 *
 * Filled stars represent the rating, while outlined stars represent the remaining out of 5.
 *
 * @param ratingOutOfFive The rating value between 0 and 5.
 * @param modifier The modifier to be applied to the composable.
 */
@Composable
fun RatingStars(ratingOutOfFive: Double, modifier: Modifier = Modifier) {
  // Coerce the rating to be within the range of 0 to 5 and round to the nearest integer
  val filled = ratingOutOfFive.coerceIn(0.0, 5.0).roundToInt()
  Row(modifier) {
    repeat(5) { i ->
      val isFilled = i < filled
      Icon(
          imageVector = if (i < filled) Icons.Filled.Star else Icons.Outlined.Star,
          contentDescription = null,
          modifier =
              Modifier.testTag(
                  if (isFilled) RatingStarsTestTags.FILLED_STAR
                  else RatingStarsTestTags.OUTLINED_STAR))
    }
  }
}

/** Test tags for the interactive (clickable) rating input component. */
object RatingStarsInputTestTags {
  const val STAR_PREFIX = "RatingStarsInputTestTags.STAR_" // will append index 1..5
}

/**
 * A composable that displays 5 clickable stars to allow the user to select a rating (1–5).
 *
 * @param selectedStars Current selected rating (1..5). If 0, no star is selected.
 * @param onSelected Callback when a star is clicked, with the new rating value (1..5).
 * @param modifier Modifier applied to the Row.
 */
@Composable
fun RatingStarsInput(
    selectedStars: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
  Row(modifier = modifier) {
    repeat(MAX_STARS) { index ->
      val starNumber = index + 1
      val isFilled = starNumber <= selectedStars

      val imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.Star
      val tint =
          if (isFilled) {
            // bright / active star
            MaterialTheme.colorScheme.primary
          } else {
            // faded / "empty" star
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
          }

      Icon(
          imageVector = imageVector,
          contentDescription = "$starNumber star",
          tint = tint,
          modifier =
              Modifier.clickable { onSelected(starNumber) }
                  .testTag("${RatingStarsInputTestTags.STAR_PREFIX}$starNumber"))
    }
  }
}
