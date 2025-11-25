package com.android.sample.endToEnd

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
import com.android.sample.utils.AppTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest : AppTest() {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)

  companion object {
    private const val TEST_PASSWORD = "testPassword123!"
    private const val TEST_DESC = "Happy"
    private const val TEST_DESC_APPEND = " Man"
    private const val TEST_DESC_FULL = "Happy Man"
    private const val TEST_TITLE = "Math Class"
    private const val TEST_EMAIL = "guillaume.lepinuuuuusu@epfl.ch"
    private const val TEST_NAME = "Lepin"
    private const val TEST_SURNAME = "Guillaume"
    private const val TEST_FULL_NAME = "Lepin Guillaume"
    private const val TEST_LOCATION = "London Street 1"
    private const val TEST_EDUCATION = "CS, 3rd year"
    private const val TEST_PROPOSAL = "PROPOSAL"
    private const val TEST_PROPOSAL_DESCRIPTION = "Learn math with me"
    private const val TEST_PROPOSAL_PRICE = "50"
    private const val TEST_PROPOSAL_SUBJECT = "ACADEMICS"
    private const val TEST_BACK_BUTTON = "Back"
  }

  @Test
  fun userCanLoginAndNavigateThroughMainPages() {
    composeTestRule.signUpAndLogin(
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
    composeTestRule.navigateToMyProfile()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.PROFILE_ICON).assertIsDisplayed()

    // Navigate to Map
    composeTestRule.navigateToMap()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_SCREEN).assertIsDisplayed()

    // Navigate to My Bookings
    composeTestRule.navigateToMyBookings()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.EMPTY).assertIsDisplayed()
  }

  @Test
  fun userCanCreateANewListing() {
    composeTestRule.signUpAndLogin(
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
    composeTestRule.navigateToNewListing()
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
    composeTestRule.fillNewListing(newListing)

    // Click the save button
    composeTestRule.clickOn(NewListingScreenTestTag.BUTTON_SAVE_LISTING)
    composeTestRule.waitForIdle()

    Thread.sleep(30000)

    // Verify that we are back on the home screen
    composeTestRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertExists()
  }
}
