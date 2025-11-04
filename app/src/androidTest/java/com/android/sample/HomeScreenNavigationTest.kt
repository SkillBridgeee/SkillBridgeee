package com.android.sample.screen

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.user.Profile
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.HomePage.TutorsSection
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.profile.ProfileScreenTestTags
import org.junit.Rule
import org.junit.Test

class HomeScreenProfileNavigationTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @SuppressLint("UnrememberedMutableState")
  @Test
  fun tutorCard_click_navigatesToProfileScreen() {
    val profile =
        Profile(
            userId = "alice-id",
            name = "Alice",
            description = "Math tutor",
            location = Location(name = "CityA"),
            tutorRating = RatingInfo(averageRating = 5.0, totalRatings = 10))

    composeRule.setContent {
      MaterialTheme {
        val navController = rememberNavController()
        val profileID = mutableStateOf("")
        NavHost(navController = navController, startDestination = "home") {
          composable("home") {
            // Render the section and navigate to the profile route when a card is clicked
            TutorsSection(
                tutors = listOf(profile),
                onTutorClick = { profileId ->
                  profileID.value = profileId
                  navController.navigate(NavRoutes.OTHERS_PROFILE)
                })
          }

          composable(route = NavRoutes.OTHERS_PROFILE) { backStackEntry ->
            // Minimal profile destination for test verification (uses same test tag)
            Box(modifier = Modifier.fillMaxSize().testTag(ProfileScreenTestTags.SCREEN)) {
              Text(text = "Profile")
            }
          }
        }
      }
    }

    // Ensure the tutor card is present and click it
    composeRule.onAllNodesWithTag(HomeScreenTestTags.TUTOR_CARD).assertCountEquals(1)
    composeRule.onAllNodesWithTag(HomeScreenTestTags.TUTOR_CARD)[0].performClick()

    // Verify navigation reached the profile screen (placeholder uses same test tag)
    composeRule.onNodeWithTag(ProfileScreenTestTags.SCREEN).assertIsDisplayed()
  }
}
