package com.android.sample.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ScrollDownHint(visible: Boolean, modifier: Modifier = Modifier) {
  AnimatedVisibility(visible = visible, modifier = modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Box(
          modifier =
              Modifier.fillMaxWidth()
                  .height(32.dp)
                  .background(
                      Brush.verticalGradient(
                          colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.08f)))))
      Spacer(modifier = Modifier.height(10.dp))

      Icon(
          imageVector = Icons.Default.ArrowDownward,
          contentDescription = "Scroll down",
          tint = MaterialTheme.colorScheme.primary)
    }
  }
}
