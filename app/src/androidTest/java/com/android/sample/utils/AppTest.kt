package com.android.sample.utils

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.android.sample.HttpClientProvider
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.components.BottomBarTestTag
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before

abstract class AppTest() {

  abstract fun createInitializedProfileRepo(): ProfileRepository

  open fun initializeHTTPClient(): OkHttpClient = FakeHttpClient.getClient()

  val profileRepository: ProfileRepository
    get() = ProfileRepositoryProvider.repository

  @Before
  open fun setUp() {
    ProfileRepositoryProvider.setForTests(createInitializedProfileRepo())
    HttpClientProvider.client = initializeHTTPClient()
  }

  @After
  open fun tearDown() {
    if (FirebaseEmulator.isRunning) {
      FirebaseEmulator.auth.signOut()
      FirebaseEmulator.clearAuthEmulator()
    }
  }

  fun ComposeTestRule.enterText(testTag: String, text: String) {
    onNodeWithTag(testTag).performTextClearance()
    onNodeWithTag(testTag).performTextInput(text)
  }

  //////// HelperFunction to navigate from Home Screen

  fun ComposeTestRule.navigateToNewListing() {
    onNodeWithTag(HomeScreenTestTags.FAB_ADD).performClick()
  }

  fun ComposeTestRule.navigateToMyProfile() {
    onNodeWithTag(BottomBarTestTag.NAV_PROFILE).performClick()
  }

  ///////

}
