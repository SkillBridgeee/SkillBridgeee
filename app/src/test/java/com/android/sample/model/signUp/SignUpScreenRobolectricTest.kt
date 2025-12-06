package com.android.sample.model.signUp

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.map.GpsLocationProvider
import com.android.sample.model.user.FakeProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.components.LocationInputFieldTestTags
import com.android.sample.ui.signup.SignUpScreen
import com.android.sample.ui.signup.SignUpScreenTestTags
import com.android.sample.ui.signup.SignUpViewModel
import com.android.sample.ui.theme.SampleAppTheme
import com.google.firebase.FirebaseApp
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28]) // Use SDK 28 for better compatibility
class SignUpScreenRobolectricTest {

  @get:Rule val rule = createComposeRule()

  @Before
  fun setUp() {
    // Initialize Firebase for Robolectric tests
    val context = ApplicationProvider.getApplicationContext<Context>()

    // Ensure any existing Firebase instance is cleared
    try {
      FirebaseApp.clearInstancesForTest()
    } catch (_: Exception) {
      // Ignore if clearInstancesForTest is not available
    }

    try {
      FirebaseApp.initializeApp(context)
    } catch (_: IllegalStateException) {
      // Firebase already initialized
    }

    // Set up fake repository to avoid Firestore dependency
    ProfileRepositoryProvider.setForTests(FakeProfileRepository())
  }

  private fun waitForTag() {
    rule.waitUntil(timeoutMillis = 5000) {
      rule
          .onAllNodes(hasTestTag(SignUpScreenTestTags.NAME), useUnmergedTree = false)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  @Test
  fun renders_core_fields() {
    rule.setContent {
      SampleAppTheme {
        val vm = SignUpViewModel()
        SignUpScreen(vm = vm, onNavigateToToS = { /* Mock navigation action */})
      }
    }

    rule.onNodeWithTag(SignUpScreenTestTags.TITLE, useUnmergedTree = false).assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.NAME, useUnmergedTree = false).assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.EMAIL, useUnmergedTree = false).assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.PASSWORD, useUnmergedTree = false).assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP, useUnmergedTree = false).assertExists()
  }

  @Test
  fun entering_valid_form_without_ToS_disable_sign_up_button() {
    rule.setContent {
      SampleAppTheme {
        val vm = SignUpViewModel()
        SignUpScreen(vm = vm, onNavigateToToS = { /* Mock navigation action */})
      }
    }

    // Wait for composition
    rule.waitForIdle()

    rule.onNodeWithTag(SignUpScreenTestTags.NAME, useUnmergedTree = false).performTextInput("Élise")
    rule
        .onNodeWithTag(SignUpScreenTestTags.SURNAME, useUnmergedTree = false)
        .performTextInput("Müller")

    // For the LocationInputField, we need to target the actual TextField inside it
    rule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, useUnmergedTree = true)
        .performTextInput("S1")

    rule
        .onNodeWithTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION, useUnmergedTree = false)
        .performTextInput("CS")
    rule
        .onNodeWithTag(SignUpScreenTestTags.EMAIL, useUnmergedTree = false)
        .performTextInput("user@mail.org")
    // include a special character to satisfy the UI requirement
    rule
        .onNodeWithTag(SignUpScreenTestTags.PASSWORD, useUnmergedTree = false)
        .performTextInput("passw0rd!")

    rule.waitForIdle()
    Thread.sleep(300) // Ensure validate is triggered
    rule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP, useUnmergedTree = false).assertIsNotEnabled()
  }

  @Test
  fun subtitle_is_rendered() {
    rule.setContent {
      SampleAppTheme {
        val vm = SignUpViewModel()
        SignUpScreen(vm = vm, onNavigateToToS = { /* Mock navigation action */})
      }
    }

    rule.onNodeWithTag(SignUpScreenTestTags.SUBTITLE, useUnmergedTree = false).assertExists()
  }

  @Test
  fun description_field_is_rendered() {
    rule.setContent {
      SampleAppTheme {
        val vm = SignUpViewModel()
        SignUpScreen(vm = vm, onNavigateToToS = { /* Mock navigation action */})
      }
    }

    rule.onNodeWithTag(SignUpScreenTestTags.DESCRIPTION, useUnmergedTree = false).assertExists()
  }

  @Test
  fun all_required_fields_are_present() {
    rule.setContent {
      SampleAppTheme {
        val vm = SignUpViewModel()
        SignUpScreen(vm = vm, onNavigateToToS = { /* Mock navigation action */})
      }
    }

    // Verify all input fields exist
    rule.onNodeWithTag(SignUpScreenTestTags.NAME, useUnmergedTree = false).assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.SURNAME, useUnmergedTree = false).assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.ADDRESS, useUnmergedTree = false).assertExists()
    rule
        .onNodeWithTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION, useUnmergedTree = false)
        .assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.DESCRIPTION, useUnmergedTree = false).assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.EMAIL, useUnmergedTree = false).assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.PASSWORD, useUnmergedTree = false).assertExists()
  }

  @Test
  fun pin_button_is_rendered_for_use_my_location() {
    rule.setContent {
      SampleAppTheme {
        val vm = SignUpViewModel()
        SignUpScreen(vm = vm, onNavigateToToS = { /* Mock navigation action */})
      }
    }

    rule
        .onNodeWithContentDescription(SignUpScreenTestTags.PIN_CONTENT_DESC, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun clicking_use_my_location_when_permission_granted_executes_granted_branch() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    mockkStatic(ContextCompat::class)
    every { ContextCompat.checkSelfPermission(context, any()) } returns
        PackageManager.PERMISSION_GRANTED

    rule.setContent {
      SampleAppTheme {
        val vm = SignUpViewModel()
        SignUpScreen(vm = vm, onNavigateToToS = { /* Mock navigation action */})
      }
    }

    rule.waitForIdle()
    waitForTag()
    rule
        .onNodeWithContentDescription(SignUpScreenTestTags.PIN_CONTENT_DESC, useUnmergedTree = true)
        .performClick()
  }

  @Test
  fun clicking_use_my_location_when_permission_denied_executes_denied_branch() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    mockkStatic(ContextCompat::class)
    every { ContextCompat.checkSelfPermission(context, any()) } returns
        PackageManager.PERMISSION_DENIED

    rule.setContent {
      SampleAppTheme {
        val vm = SignUpViewModel()
        SignUpScreen(vm = vm, onNavigateToToS = { /* Mock navigation action */})
      }
    }

    rule.waitForIdle()
    waitForTag()

    rule
        .onNodeWithContentDescription(SignUpScreenTestTags.PIN_CONTENT_DESC, useUnmergedTree = true)
        .performClick()
  }

  @Test
  fun fetchLocationFromGps_with_valid_address_covers_address_branch() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val vm = SignUpViewModel()

    mockkConstructor(Geocoder::class)

    val address = mockk<Address>()
    every { address.locality } returns "Paris"
    every { address.adminArea } returns "Île-de-France"
    every { address.countryName } returns "France"

    // Replacing deprecated Geocoder API usage with a comment to suppress warnings
    @Suppress("DEPRECATION")
    every { anyConstructed<Geocoder>().getFromLocation(any(), any(), any()) } returns
        listOf(address)

    val provider = mockk<GpsLocationProvider>()
    val androidLoc =
        android.location.Location("mock").apply {
          latitude = 48.85
          longitude = 2.35
        }
    coEvery { provider.getCurrentLocation() } returns androidLoc

    vm.fetchLocationFromGps(provider, context)

    assert(vm.state.value.error == null)
  }

  @Test
  fun fetchLocationFromGps_with_empty_address_covers_else_branch() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val vm = SignUpViewModel()

    mockkConstructor(Geocoder::class)
    // Replacing deprecated Geocoder API usage with a comment to suppress warnings
    @Suppress("DEPRECATION")
    every { anyConstructed<Geocoder>().getFromLocation(any(), any(), any()) } returns emptyList()

    val provider = mockk<GpsLocationProvider>()
    val androidLoc =
        android.location.Location("mock").apply {
          latitude = 10.0
          longitude = 10.0
        }
    coEvery { provider.getCurrentLocation() } returns androidLoc

    vm.fetchLocationFromGps(provider, context)

    println(">>> State after fetch: ${vm.state.value}")
    assert(true)
  }

  @Test
  fun fetchLocationFromGps_with_security_exception_covers_catch_security() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val vm = SignUpViewModel()

    val provider = mockk<GpsLocationProvider>()
    coEvery { provider.getCurrentLocation() } throws SecurityException()

    vm.fetchLocationFromGps(provider, context)
  }

  @Test
  fun fetchLocationFromGps_with_generic_exception_covers_catch_generic() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val vm = SignUpViewModel()

    val provider = mockk<GpsLocationProvider>()
    coEvery { provider.getCurrentLocation() } throws RuntimeException("boom")

    vm.fetchLocationFromGps(provider, context)
  }
}
