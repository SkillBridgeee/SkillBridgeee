package com.android.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.android.sample.ui.components.BottomNavBar
import com.android.sample.ui.components.TopAppBar
import com.android.sample.ui.navigation.AppNavGraph

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { MainApp() }
  }
}

@Composable
fun MainApp() {
  val navController = rememberNavController()

  Scaffold(topBar = { TopAppBar(navController) }, bottomBar = { BottomNavBar(navController) }) {
      paddingValues ->
    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(paddingValues)) {
      AppNavGraph(navController = navController)
    }
  }
}
