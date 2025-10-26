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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun MapScreen(
    navController: NavHostController,
    viewModel: MapViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
  Scaffold { innerPadding ->
    Box(
        modifier = modifier.fillMaxSize().padding(innerPadding),
        contentAlignment = Alignment.Center) {
          Text(
              text = "Map",
              modifier = Modifier.testTag("map_screen_text"),
              style = MaterialTheme.typography.titleMedium)
        }
  }
}
