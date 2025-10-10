package com.android.sample.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsPlaceholder(navController: NavController, modifier: Modifier = Modifier) {
  Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("💡 Skills") }) }) { innerPadding ->
    Column(
        modifier = modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
          Text("💡 Skills Screen Placeholder", style = MaterialTheme.typography.titleMedium)
          Spacer(modifier = Modifier.height(16.dp))
          Button(
              onClick = {
                val route = NavRoutes.PIANO_SKILL
                RouteStackManager.addRoute(route)
                navController.navigate(route)
              }) {
                Text("Go to Piano")
              }
        }
  }
}
