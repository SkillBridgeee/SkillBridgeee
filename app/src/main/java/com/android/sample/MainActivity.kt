package com.android.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.components.BottomNavBar
import com.android.sample.ui.components.TopAppBar
import com.android.sample.ui.navigation.AppNavGraph
import com.android.sample.ui.profile.MyProfileViewModel
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.android.sample.ui.navigation.NavRoutes

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { MainApp() }
  }
}

class MyViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return when (modelClass) {
      MyBookingsViewModel::class.java -> {
        MyBookingsViewModel(userId = userId) as T
      }
      MyProfileViewModel::class.java -> {
        MyProfileViewModel() as T
      }
      MainPageViewModel::class.java -> {
        MainPageViewModel() as T
      }
      else -> throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}

@Composable
fun MainApp() {
  val navController = rememberNavController()

  //To track the current route
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  // Use hardcoded user ID from ProfileRepositoryLocal
  val currentUserId = "test" // This matches profileFake1 in your ProfileRepositoryLocal
  val factory = MyViewModelFactory(currentUserId)

  val bookingsViewModel: MyBookingsViewModel = viewModel(factory = factory)
  val profileViewModel: MyProfileViewModel = viewModel(factory = factory)
  val mainPageViewModel: MainPageViewModel = viewModel(factory = factory)

  // Define main screens that should show bottom nav
  val mainScreenRoutes = listOf(
    NavRoutes.HOME,
    NavRoutes.BOOKINGS,
    NavRoutes.PROFILE,
    NavRoutes.SETTINGS
  )

  // Check if current route should show bottom nav
  val showBottomNav = mainScreenRoutes.contains(currentRoute)

  Scaffold(
    topBar = { TopAppBar(navController) },
    bottomBar = {
      if (showBottomNav) {
        BottomNavBar(navController)
      }
    }
  ) {
      paddingValues ->
    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(paddingValues)) {
      AppNavGraph(
        navController = navController,
        bookingsViewModel = bookingsViewModel,
        profileViewModel = profileViewModel,
        mainPageViewModel = mainPageViewModel
      )
    }
  }
}
