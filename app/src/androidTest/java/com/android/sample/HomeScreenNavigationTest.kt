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
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.Skill
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.HomePage.ProposalsSection
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.profile.ProfileScreenTestTags
import org.junit.Rule
import org.junit.Test

class HomeScreenProfileNavigationTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @SuppressLint("UnrememberedMutableState")
  @Test
  fun proposalCard_click_navigatesToProfileScreen() {
    val proposal =
        Proposal(
            listingId = "proposal-1",
            creatorUserId = "alice-id",
            description = "Math tutor",
            location = Location(name = "CityA"),
            hourlyRate = 25.0,
            skill = Skill())

    composeRule.setContent {
      MaterialTheme {
        val navController = rememberNavController()
        val clickedCreatorId = mutableStateOf("")
        NavHost(navController = navController, startDestination = "home") {
          composable("home") {
            ProposalsSection(
                proposals = listOf(proposal),
                ratings = emptyMap<String, RatingInfo>(),
                onProposalClick = { listingId ->
                  clickedCreatorId.value = listingId
                  navController.navigate(NavRoutes.OTHERS_PROFILE)
                })
          }

          composable(route = NavRoutes.OTHERS_PROFILE) {
            Box(modifier = Modifier.fillMaxSize().testTag(ProfileScreenTestTags.SCREEN)) {
              Text(text = "Profile")
            }
          }
        }
      }
    }

    composeRule.onAllNodesWithTag(HomeScreenTestTags.PROPOSAL_CARD).assertCountEquals(1)
    composeRule.onAllNodesWithTag(HomeScreenTestTags.PROPOSAL_CARD)[0].performClick()

    composeRule.onNodeWithTag(ProfileScreenTestTags.SCREEN).assertIsDisplayed()
  }
}
