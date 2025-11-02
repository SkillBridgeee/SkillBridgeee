package com.android.sample.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

object MapScreenTestTags {
  const val MAP_SCREEN_TEXT = "map_screen_text"
}

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
  Scaffold { innerPadding ->
    Box(
        modifier = modifier.fillMaxSize().padding(innerPadding),
        contentAlignment = Alignment.Center) {
          Text(
              text = "Map",
              modifier = Modifier.testTag(MapScreenTestTags.MAP_SCREEN_TEXT),
              style = MaterialTheme.typography.titleMedium)
        }
  }
}
