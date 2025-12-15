package com.android.sample.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.bookings.BookingDetailsTestTag
import com.android.sample.ui.components.BookingCardTestTag
import com.android.sample.ui.components.BottomBarTestTag
import com.android.sample.ui.components.ProposalCardTestTags
import com.android.sample.ui.listing.ListingScreenTestTags
import com.android.sample.ui.newListing.NewListingScreenTestTag
import com.android.sample.ui.profile.MyProfileScreenTestTag
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
 * - Line 64: navigateToNewListing function
 * - Line 191: addProfileRoute onListingClick
 * - Line 251: addSkillsRoute onListingClick
 * - Lines 274-276: addBookingsRoute onBookingClick
 * - Lines 441-443: addOthersProfileRoute onProposalClick, onRequestClick
 * - Lines 467-469: addListingRoute callbacks
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
   * Test for addHomeRoute onNavigateToAddNewListing callback (lines 221-224) This also covers line
   * 64 (navigateToNewListing function) Clicking on FAB in HomeScreen should navigate to NewListing
   * screen when user is logged in
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

  /**
   * Test for addSkillsRoute onListingClick callback (line 251) Navigate to Skills screen, then
   * click on a listing card
   */
  @Test
  fun skillsScreen_clickListingCard_navigatesToListing() {
    composeRule.setContent { CreateAppContent() }
    composeRule.waitForIdle()

    // First navigate to Skills screen by clicking a skill card
    composeRule.onAllNodesWithTag(HomeScreenTestTags.SKILL_CARD)[0].performClick()
    composeRule.waitForIdle()

    // Now click on a listing card in the skills screen to trigger onListingClick
    composeRule.onAllNodesWithTag(SubjectListTestTags.LISTING_CARD)[0].performClick()
    composeRule.waitForIdle()

    // Verify we navigated to listing screen
    composeRule.onNodeWithTag(ListingScreenTestTags.SCREEN).assertIsDisplayed()
  }

  /**
   * Test for addBookingsRoute onBookingClick callback (lines 274-276) Navigate to Bookings screen
   * and click on a booking card
   */
  @Test
  fun bookingsScreen_clickBookingCard_navigatesToBookingDetails() {
    composeRule.setContent { CreateAppContent() }
    composeRule.waitForIdle()

    // Navigate to Bookings screen
    composeRule.onNodeWithTag(BottomBarTestTag.NAV_BOOKINGS).performClick()

    // Wait a bit longer for the screen to settle
    Thread.sleep(2000)
    composeRule.waitForIdle()

    // Wait for booking cards to be displayed (wait for at least 1 card)
    composeRule.waitUntil(timeoutMillis = 10000) {
      composeRule.onAllNodesWithTag(BookingCardTestTag.CARD).fetchSemanticsNodes().isNotEmpty()
    }

    // Click on first booking card to trigger onBookingClick
    composeRule.onAllNodesWithTag(BookingCardTestTag.CARD)[0].performClick()
    composeRule.waitForIdle()

    // Verify we navigated to booking details
    composeRule.onNodeWithTag(BookingDetailsTestTag.HEADER).assertIsDisplayed()
  }

  /**
   * Test for addBookingDetailsRoute onCreatorClick callback (lines 498-500) Navigate to
   * BookingDetails and click on More Info button to view creator profile
   */
  @Test
  fun bookingDetailsScreen_clickMoreInfo_navigatesToOthersProfile() {
    composeRule.setContent { CreateAppContent() }
    composeRule.waitForIdle()

    // Navigate to Bookings screen
    composeRule.onNodeWithTag(BottomBarTestTag.NAV_BOOKINGS).performClick()

    // Wait a bit longer for the screen to settle
    Thread.sleep(2000)
    composeRule.waitForIdle()

    // Wait for booking cards to be displayed (wait for at least 1 card)
    composeRule.waitUntil(timeoutMillis = 10000) {
      composeRule.onAllNodesWithTag(BookingCardTestTag.CARD).fetchSemanticsNodes().isNotEmpty()
    }

    // Click on first booking card
    composeRule.onAllNodesWithTag(BookingCardTestTag.CARD)[0].performClick()
    composeRule.waitForIdle()

    // Wait for More Info button to be displayed
    composeRule.waitUntil(timeoutMillis = 10000) {
      composeRule
          .onAllNodesWithTag(BookingDetailsTestTag.MORE_INFO_BUTTON)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Click on More Info button to trigger onCreatorClick
    composeRule.onNodeWithTag(BookingDetailsTestTag.MORE_INFO_BUTTON).performClick()
    composeRule.waitForIdle()

    // Verify we navigated to profile screen
    composeRule.onNodeWithTag(ProfileScreenTestTags.SCREEN).assertIsDisplayed()
  }

  /**
   * Test for addOthersProfileRoute onProposalClick callback (lines 441-443) Navigate to another
   * user's profile via listing, then click on their proposal
   */
  @Test
  fun othersProfileScreen_verifyProposalsSection_isDisplayed() {
    composeRule.setContent { CreateAppContent() }
    composeRule.waitForIdle()

    // Navigate to a listing first
    composeRule.onAllNodesWithTag(HomeScreenTestTags.PROPOSAL_CARD)[0].performClick()
    composeRule.waitForIdle()

    // Click on creator name to go to their profile
    composeRule.onNodeWithTag(ListingScreenTestTags.CREATOR_NAME).performClick()
    composeRule.waitForIdle()

    // Verify we're on profile screen and proposals section is visible
    composeRule.onNodeWithTag(ProfileScreenTestTags.SCREEN).assertIsDisplayed()
    composeRule.onNodeWithTag(ProfileScreenTestTags.PROPOSALS_SECTION).assertIsDisplayed()
  }

  /**
   * Test for addOthersProfileRoute - click on proposal card to navigate to listing (line 441)
   * Navigate to another user's profile, then click on a proposal card
   */
  @Test
  fun othersProfileScreen_clickProposalCard_navigatesToListing() {
    composeRule.setContent { CreateAppContent() }
    composeRule.waitForIdle()

    // Navigate to a listing first
    composeRule.onAllNodesWithTag(HomeScreenTestTags.PROPOSAL_CARD)[0].performClick()
    composeRule.waitForIdle()

    // Click on creator name to go to their profile
    composeRule.onNodeWithTag(ListingScreenTestTags.CREATOR_NAME).performClick()
    composeRule.waitForIdle()

    // Click on a proposal card in their profile to trigger onProposalClick
    composeRule.onNodeWithTag(ProposalCardTestTags.CARD).performClick()
    composeRule.waitForIdle()

    // Verify we navigated to listing screen
    composeRule.onNodeWithTag(ListingScreenTestTags.SCREEN).assertIsDisplayed()
  }

  /**
   * Test for addProfileRoute onListingClick callback (line 191) Navigate to own profile, go to
   * Listings tab and click on a listing
   */
  @Test
  fun myProfileScreen_clickListingsTab_verifiesListingsSection() {
    composeRule.setContent { CreateAppContent() }
    composeRule.waitForIdle()

    // Navigate to Profile screen
    composeRule.onNodeWithTag(BottomBarTestTag.NAV_PROFILE).performClick()
    composeRule.waitForIdle()

    // Click on Listings tab to see own listings
    composeRule.onNodeWithTag(MyProfileScreenTestTag.LISTINGS_TAB).performClick()
    composeRule.waitForIdle()

    // Verify listings section is displayed
    composeRule.onNodeWithTag(MyProfileScreenTestTag.LISTINGS_SECTION).assertIsDisplayed()
  }

  /**
   * Test for addProfileRoute onListingClick callback (line 191) Navigate to own profile, go to
   * Listings tab and click on own listing
   */
  @Test
  fun myProfileScreen_clickOwnListing_navigatesToListingDetails() {
    composeRule.setContent { CreateAppContent() }
    composeRule.waitForIdle()

    // Navigate to Profile screen
    composeRule.onNodeWithTag(BottomBarTestTag.NAV_PROFILE).performClick()
    composeRule.waitForIdle()

    // Click on Listings tab to see own listings
    composeRule.onNodeWithTag(MyProfileScreenTestTag.LISTINGS_TAB).performClick()
    composeRule.waitForIdle()

    // Click on own listing card to trigger onListingClick
    composeRule.onNodeWithTag(ProposalCardTestTags.CARD).performClick()
    composeRule.waitForIdle()

    // Verify we navigated to listing screen
    composeRule.onNodeWithTag(ListingScreenTestTags.SCREEN).assertIsDisplayed()
  }

  /**
   * Test for addBookingDetailsRoute onBookerClick callback (lines 502-505) Navigate to a PENDING
   * booking where current user is the creator, then click on booker name row to view booker's
   * profile
   */
  @Test
  fun bookingDetailsScreen_clickBookerNameRow_navigatesToBookerProfile() {
    composeRule.setContent { CreateAppContent() }
    composeRule.waitForIdle()

    // Navigate to Bookings screen
    composeRule.onNodeWithTag(BottomBarTestTag.NAV_BOOKINGS).performClick()

    // Wait a bit longer for the screen to settle (especially important for CI)
    Thread.sleep(2000)
    composeRule.waitForIdle()

    // Wait for booking cards to be displayed (creator_1 sees b1, b2, b3 = 3 bookings)
    composeRule.waitUntil(timeoutMillis = 10000) {
      composeRule.onAllNodesWithTag(BookingCardTestTag.CARD).fetchSemanticsNodes().size >= 3
    }

    // Click on the third booking card (b3) which has PENDING status and creator_1 as listing
    // creator
    // This will show the booker info section since creator_1 is the listing creator
    composeRule.onAllNodesWithTag(BookingCardTestTag.CARD)[2].performClick()
    composeRule.waitForIdle()

    // Wait for booking details content to load
    composeRule.waitUntil(timeoutMillis = 10000) {
      composeRule
          .onAllNodesWithTag(BookingDetailsTestTag.CONTENT)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Scroll to make sure the booker section is visible (important for smaller screens in CI)
    composeRule
        .onNodeWithTag(BookingDetailsTestTag.CONTENT)
        .performScrollToNode(hasTestTag(BookingDetailsTestTag.BOOKER_SECTION))
    composeRule.waitForIdle()

    // Wait for booker section to appear after scrolling
    composeRule.waitUntil(timeoutMillis = 10000) {
      composeRule
          .onAllNodesWithTag(BookingDetailsTestTag.BOOKER_NAME_ROW)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Click on booker name row to trigger onBookerClick
    composeRule.onNodeWithTag(BookingDetailsTestTag.BOOKER_NAME_ROW).performClick()
    composeRule.waitForIdle()

    // Verify we navigated to profile screen (booker's profile)
    composeRule.onNodeWithTag(ProfileScreenTestTags.SCREEN).assertIsDisplayed()
  }
}
