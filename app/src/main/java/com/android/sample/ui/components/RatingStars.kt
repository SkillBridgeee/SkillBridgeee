package com.android.sample.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.math.roundToInt

@Composable
fun RatingStars(ratingOutOfFive: Double, modifier: Modifier = Modifier) {
  val filled = ratingOutOfFive.coerceIn(0.0, 5.0).roundToInt()
  Row(modifier) {
    repeat(5) { i ->
      Icon(
          imageVector = if (i < filled) Icons.Filled.Star else Icons.Outlined.Star,
          contentDescription = null)
    }
  }
}
