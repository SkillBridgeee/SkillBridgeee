package com.android.sample.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.android.sample.MainApp
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.ui.components.BottomBarTestTag
import com.android.sample.utils.AppTest
import com.android.sample.utils.fakeRepo.BookingFake
import com.android.sample.utils.fakeRepo.ListingFake
import com.android.sample.utils.fakeRepo.ProfileFake
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewListingScreenTestFUN : AppTest() {

  private lateinit var profileRepo: ProfileFake
  private lateinit var bookingRepo: BookingFake
  private lateinit var listingRepo: ListingFake
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()
    UserSessionManager.setCurrentUserId("test-user")

    profileRepo = ProfileFake()
    bookingRepo = BookingFake()
    listingRepo = ListingFake()

    val authViewModel =
        AuthenticationViewModel(
            ApplicationProvider.getApplicationContext(),
            profileRepository = profileRepo,
        )

    composeTestRule.setContent { MainApp(authViewModel = authViewModel, onGoogleSignIn = {}) }
  }

  @Test
  fun test() {
    composeTestRule.onNodeWithTag(BottomBarTestTag.NAV_PROFILE).assertExists()
  }
}
