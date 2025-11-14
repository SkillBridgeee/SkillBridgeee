// package com.android.sample.screens
//
// import androidx.compose.ui.test.junit4.createComposeRule
// import androidx.compose.ui.test.onNodeWithTag
// import androidx.test.core.app.ApplicationProvider
// import com.android.sample.MainApp
// import com.android.sample.model.authentication.AuthenticationViewModel
// import com.android.sample.model.authentication.UserSessionManager
// import com.android.sample.model.user.ProfileRepository
// import com.android.sample.ui.components.BottomBarTestTag
// import com.android.sample.utils.AppTest
// import com.android.sample.utils.FirebaseEmulator
// import com.android.sample.utils.InMemoryBootcampTest
// import com.android.sample.utils.fakeRepo.BookingFake
// import com.android.sample.utils.fakeRepo.ListingFake
// import com.android.sample.utils.fakeRepo.ProfileFake
// import kotlinx.coroutines.runBlocking
// import kotlinx.coroutines.tasks.await
// import org.junit.Before
// import org.junit.Rule
// import org.junit.Test
//
// class NewListingScreenTestFUN : InMemoryBootcampTest() {
//
//  @get:Rule val composeTestRule = createComposeRule()
//
//
//  @Before
//  override fun setUp() {
//    super.setUp()
//    runBlocking { FirebaseEmulator.auth.signInAnonymously().await() }
//    runBlocking { profileRepository.addProfile(profile1) }
//
//
//    composeTestRule.setContent { MainApp(
//      authViewModel = AuthenticationViewModel(
//      )
//    ) }
//
//  }
//
//  @Test
//  fun test() {
//    composeTestRule.onNodeWithTag(BottomBarTestTag.NAV_PROFILE).assertExists()
//  }
// }
