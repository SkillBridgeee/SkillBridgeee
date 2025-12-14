package com.android.sample.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.listing.ListingScreenTestTags
import com.android.sample.ui.newListing.NewListingScreenTestTag
import com.android.sample.ui.profile.ProfileScreenTestTags
import com.android.sample.ui.subject.SubjectListTestTags
import com.android.sample.utils.AppTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for NavGraph callback lambdas. These tests trigger actual UI interactions to
 * execute the callback code defined in NavGraph.kt.
 *
 * Coverage targets:
 * - Line 191: addProfileRoute onListingClick
 * - Lines 219-222: addHomeRoute onNavigateToSubjectList, onNavigateToAddNewListing
 * - Lines 274-277: addBookingsRoute onBookingClick
 * - Lines 467-472: addListingRoute callbacks
 * - Lines 498-500: addBookingDetailsRoute onCreatorClick, onBookerClick
 */
@RunWith(AndroidJUnit4::class)
class NavGraphCallbackIntegrationTest : AppTest() {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  /**
   * Test for addHomeRoute onNavigateToSubjectList callback (lines 219-220) Clicking on a skill card
   * in HomeScreen should navigate to Skills screen
   */
  @Test
  fun homeScreen_clickSkillCard_navigatesToSkills() {
    composeRule.setContent { CreateAppContent() }
    composeRule.waitForIdle()

    // Click on a skill card to trigger onNavigateToSubjectList
    composeRule.onAllNodesWithTag(HomeScreenTestTags.SKILL_CARD)[0].performClick()
    composeRule.waitForIdle()

    // Verify we navigated to Skills screen (SubjectListScreen)
    composeRule.onNodeWithTag(SubjectListTestTags.LISTING_LIST).assertIsDisplayed()
  }

  /**
   * Test for addHomeRoute onNavigateToAddNewListing callback (lines 221-224) Clicking on FAB in
   * HomeScreen should navigate to NewListing screen when user is logged in
   */
  @Test
  fun homeScreen_clickFab_navigatesToNewListing() {
    composeRule.setContent { CreateAppContent() }
    composeRule.waitForIdle()

    // Click on FAB to trigger onNavigateToAddNewListing
    composeRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).performClick()
    composeRule.waitForIdle()

    // Verify we navigated to NewListingScreen
    composeRule.onNodeWithTag(NewListingScreenTestTag.INPUT_COURSE_TITLE).assertIsDisplayed()
  }

  /**
   * Test for addHomeRoute onNavigateToListingDetails callback (line 228) Clicking on a proposal
   * card should navigate to Listing details
   */
  @Test
  fun homeScreen_clickProposalCard_navigatesToListing() {
    composeRule.setContent { CreateAppContent() }
    composeRule.waitForIdle()

    // Click on a proposal card to trigger onNavigateToListingDetails
    composeRule.onAllNodesWithTag(HomeScreenTestTags.PROPOSAL_CARD)[0].performClick()
    composeRule.waitForIdle()

    // Verify we navigated to listing screen
    composeRule.onNodeWithTag(ListingScreenTestTags.SCREEN).assertIsDisplayed()
  }

  /**
   * Test for addListingRoute onNavigateToProfile callback (lines 469-472) Clicking on creator name
   * in ListingScreen should navigate to OthersProfile
   */
  @Test
  fun listingScreen_clickCreatorName_navigatesToOthersProfile() {
    composeRule.setContent { CreateAppContent() }
    composeRule.waitForIdle()

    // First navigate to a listing by clicking a proposal card
    composeRule.onAllNodesWithTag(HomeScreenTestTags.PROPOSAL_CARD)[0].performClick()
    composeRule.waitForIdle()

    // Now click on the creator name to trigger onNavigateToProfile
    composeRule.onNodeWithTag(ListingScreenTestTags.CREATOR_NAME).performClick()
    composeRule.waitForIdle()

    // Verify we navigated to profile screen (OthersProfile uses ProfileScreen)
    composeRule.onNodeWithTag(ProfileScreenTestTags.SCREEN).assertIsDisplayed()
  }
}
