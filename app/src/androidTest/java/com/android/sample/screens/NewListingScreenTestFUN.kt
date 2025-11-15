package com.android.sample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.components.BottomBarTestTag
import com.android.sample.ui.components.BottomNavBar
import com.android.sample.ui.components.TopAppBar
import com.android.sample.ui.navigation.AppNavGraph
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.utils.InMemoryBootcampTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewListingScreenTestFUN : InMemoryBootcampTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()

    composeTestRule.setContent { CreateApp() }
  }

  @Test
  fun testBottomNavProfileExists() {
    composeTestRule.onNodeWithTag(BottomBarTestTag.NAV_PROFILE).assertExists()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertExists()
  }
}

@Composable
private fun NewListingScreenTestFUN.CreateApp() {
  val navController = rememberNavController()

  val mainScreenRoutes =
      listOf(NavRoutes.HOME, NavRoutes.BOOKINGS, NavRoutes.PROFILE, NavRoutes.MAP)
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val showBottomNav = mainScreenRoutes.contains(currentRoute)

  Scaffold(
      topBar = { TopAppBar(navController) },
      bottomBar = {
        if (showBottomNav) {
          BottomNavBar(navController)
        }
      }) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
          AppNavGraph(
              navController = navController,
              bookingsViewModel = bookingsViewModel,
              profileViewModel = profileViewModel,
              mainPageViewModel = mainPageViewModel,
              authViewModel = authViewModel,
              onGoogleSignIn = {})
        }
        LaunchedEffect(Unit) {
          navController.navigate(NavRoutes.HOME) { popUpTo(0) { inclusive = true } }
        }
      }
}
