package com.android.sample.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlin.math.roundToInt

/**
 * Test tags for the [RatingStars] composable.
 */
object RatingStarsTestTags {
    const val FILLED_STAR = "RatingStarsTestTags.FILLED_STAR"
    const val OUTLINED_STAR = "RatingStarsTestTags.OUTLINED_STAR"
}

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
  val filled = ratingOutOfFive.coerceIn(0.0, 5.0).roundToInt()
  Row(modifier) {
    repeat(5) { i ->
        val isFilled = i < filled
      Icon(
          imageVector = if (i < filled) Icons.Filled.Star else Icons.Outlined.Star,
          contentDescription = null,
          modifier = Modifier.testTag(
              if (isFilled)
                  RatingStarsTestTags.FILLED_STAR
              else
                  RatingStarsTestTags.OUTLINED_STAR
          ))
    }
  }
}
