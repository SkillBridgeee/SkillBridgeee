package com.android.sample.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * A composable that shows a horizontal scroll hint with a forward arrow.
 *
 * @param visible Controls the visibility of the scroll hint.
 * @param modifier Optional [Modifier] for styling.
 */
@Composable
fun HorizontalScrollHint(visible: Boolean, modifier: Modifier = Modifier) {
  AnimatedVisibility(visible = visible, modifier = modifier) {
    Box(
        modifier =
            Modifier.width(32.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
        contentAlignment = Alignment.Center) {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Scroll for more subjects",
              tint = MaterialTheme.colorScheme.primary)
        }
  }
}
