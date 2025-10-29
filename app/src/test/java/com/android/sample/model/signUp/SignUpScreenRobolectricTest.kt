package com.android.sample.model.signUp

import android.content.Context
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.user.FakeProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.signup.SignUpScreen
import com.android.sample.ui.signup.SignUpScreenTestTags
import com.android.sample.ui.signup.SignUpViewModel
import com.android.sample.ui.theme.SampleAppTheme
import com.google.firebase.FirebaseApp
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

  @Test
  fun renders_core_fields() {
    rule.setContent {
      SampleAppTheme {
        val vm = SignUpViewModel()
        SignUpScreen(vm = vm)
      }
    }

    rule.onNodeWithTag(SignUpScreenTestTags.TITLE, useUnmergedTree = false).assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.NAME, useUnmergedTree = false).assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.EMAIL, useUnmergedTree = false).assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.PASSWORD, useUnmergedTree = false).assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP, useUnmergedTree = false).assertExists()
  }

  @Test
  fun entering_valid_form_enables_sign_up_button() {
    rule.setContent {
      SampleAppTheme {
        val vm = SignUpViewModel()
        SignUpScreen(vm = vm)
      }
    }

    rule.onNodeWithTag(SignUpScreenTestTags.NAME, useUnmergedTree = false).performTextInput("Élise")
    rule
        .onNodeWithTag(SignUpScreenTestTags.SURNAME, useUnmergedTree = false)
        .performTextInput("Müller")
    rule.onNodeWithTag(SignUpScreenTestTags.ADDRESS, useUnmergedTree = false).performTextInput("S1")
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

    rule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP, useUnmergedTree = false).assertIsEnabled()
  }

  @Test
  fun subtitle_is_rendered() {
    rule.setContent {
      SampleAppTheme {
        val vm = SignUpViewModel()
        SignUpScreen(vm = vm)
      }
    }

    rule.onNodeWithTag(SignUpScreenTestTags.SUBTITLE, useUnmergedTree = false).assertExists()
  }

  @Test
  fun description_field_is_rendered() {
    rule.setContent {
      SampleAppTheme {
        val vm = SignUpViewModel()
        SignUpScreen(vm = vm)
      }
    }

    rule.onNodeWithTag(SignUpScreenTestTags.DESCRIPTION, useUnmergedTree = false).assertExists()
  }

  @Test
  fun all_required_fields_are_present() {
    rule.setContent {
      SampleAppTheme {
        val vm = SignUpViewModel()
        SignUpScreen(vm = vm)
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
}
