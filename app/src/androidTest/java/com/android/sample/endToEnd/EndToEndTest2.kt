package com.android.sample.endToEnd

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.android.sample.MainActivity
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.skill.AcademicSkills
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.map.MapScreenTestTags
import com.android.sample.ui.newListing.NewListingScreenTestTag
import com.android.sample.ui.profile.MyProfileScreenTestTag
import com.android.sample.utils.EndToEndTestHelper
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest2 {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)

  private lateinit var testHelper: EndToEndTestHelper

  @Before
  fun setUp() {
    testHelper = EndToEndTestHelper(composeTestRule)
  }

  companion object {
    private const val TEST_PASSWORD = "testPassword123!"
    private const val TEST_DESC = "Happy"
    private const val TEST_DESC_UPDATED = "Very happy"

    private const val TEST_TITLE = "Math Class"
    private const val TEST_EMAIL = "guillaume.lepinuuuuusu@epfl.ch"
    private const val TEST_NAME = "Lepin"
    private const val TEST_SURNAME = "Guillaume"
    private const val TEST_LOCATION = "London Street 1"
    private const val TEST_EDUCATION = "CS, 3rd year"
    private const val TEST_PROPOSAL_DESCRIPTION = "Learn math with me"
    private const val TEST_PROPOSAL_PRICE = "50"
  }

  @Test
  fun userCanLoginAndNavigateThroughMainPages() {
    testHelper.signUpAndLogin(
        name = TEST_NAME,
        surname = TEST_SURNAME,
        address = TEST_LOCATION,
        levelOfEducation = TEST_EDUCATION,
        description = TEST_DESC,
        email = TEST_EMAIL,
        password = TEST_PASSWORD)

    composeTestRule.waitForIdle()

    // Verify navigation to home screen
    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertExists()

    // Navigate to My Profile
    testHelper.navigateToMyProfile()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.PROFILE_ICON).assertIsDisplayed()

    // Navigate to Map
    testHelper.navigateToMap()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_SCREEN).assertIsDisplayed()

    // Navigate to My Bookings
    testHelper.navigateToMyBookings()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.EMPTY).assertIsDisplayed()
  }

  @Test
  fun userCanCreateANewListing() {
    testHelper.signUpAndLogin(
        name = TEST_NAME,
        surname = TEST_SURNAME,
        address = TEST_LOCATION,
        levelOfEducation = TEST_EDUCATION,
        description = TEST_DESC,
        email = TEST_EMAIL,
        password = TEST_PASSWORD)

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertExists()

    // Navigate to create a new listing
    testHelper.navigateToNewListing()
    composeTestRule.waitForIdle()

    // Create a listing object with test data
    val newListing =
        Proposal(
            title = TEST_TITLE,
            description = TEST_PROPOSAL_DESCRIPTION,
            hourlyRate = TEST_PROPOSAL_PRICE.toDouble(),
            skill =
                Skill(mainSubject = MainSubject.ACADEMICS, skill = AcademicSkills.MATHEMATICS.name),
            location = Location(name = TEST_LOCATION),
        )

    // Fill in the new listing form
    testHelper.fillNewListing(newListing)

    // Click the save button
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.BUTTON_SAVE_LISTING).performClick()
    composeTestRule.waitForIdle()

    // Verify that we are back on the home screen
    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertExists()
  }

  @Test
  fun testUpdateProfileDescription() {
    testHelper.signUpAndLogin(
        name = TEST_NAME,
        surname = TEST_SURNAME,
        address = TEST_LOCATION,
        levelOfEducation = TEST_EDUCATION,
        description = TEST_DESC,
        email = TEST_EMAIL,
        password = TEST_PASSWORD)

    composeTestRule.waitForIdle()

    // Navigate to the profile screen
    testHelper.navigateToMyProfile()
    composeTestRule.waitForIdle()

    // Wait for the profile screen to be visible
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.ROOT_LIST).assertIsDisplayed()

    // Update the description using the new helper function
    testHelper.updateProfileField(MyProfileScreenTestTag.INPUT_PROFILE_DESC, TEST_DESC_UPDATED)

    composeTestRule.waitForIdle()

    // Click the save button
    testHelper.scrollAndClickOn(MyProfileScreenTestTag.SAVE_BUTTON)

    composeTestRule.waitForIdle()

    // Scroll to the success message and assert it is displayed.
    // This implicitly waits for the node to appear.
    composeTestRule
        .onNodeWithTag(MyProfileScreenTestTag.SUCCESS_MSG)
        .performScrollTo()
        .assertIsDisplayed()
  }
}
