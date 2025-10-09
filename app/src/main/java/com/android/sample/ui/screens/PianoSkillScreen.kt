package com.android.sample.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager

@Composable
fun PianoSkillScreen(navController: NavController, modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text("Piano Screen")
      Spacer(modifier = Modifier.height(16.dp))
      Button(
          onClick = {
            val route = NavRoutes.PIANO_SKILL_2
            RouteStackManager.addRoute(route)
            navController.navigate(route)
          }) {
            Text("Go to Piano 2")
          }
    }
  }
}
