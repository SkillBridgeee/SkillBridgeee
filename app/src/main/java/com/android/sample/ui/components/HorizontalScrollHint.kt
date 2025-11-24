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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

const val HORIZONTAL_SCROLL_HINT_BOX_TAG = "horizontalScrollHintBox"
const val HORIZONTAL_SCROLL_HINT_ICON_TAG = "horizontalScrollHintIcon"

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
        Modifier.width(32.dp)
            .testTag(HORIZONTAL_SCROLL_HINT_BOX_TAG)
            .width(32.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
        contentAlignment = Alignment.Center) {
          Icon(
              modifier = Modifier.testTag(HORIZONTAL_SCROLL_HINT_ICON_TAG),
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Scroll for more subjects",
              tint = MaterialTheme.colorScheme.primary)
        }
  }
}
