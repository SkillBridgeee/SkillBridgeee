package com.android.sample.ui.signup

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.ui.theme.SampleAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpScreenRobolectricTest {

  @get:Rule val rule = createComposeRule()

  @Test
  fun renders_core_fields() {
    val vm = SignUpViewModel()
    rule.setContent { SampleAppTheme { SignUpScreen(vm = vm) } }

    rule.onNodeWithTag(SignUpScreenTestTags.TITLE, useUnmergedTree = false).assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.NAME, useUnmergedTree = false).assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.EMAIL, useUnmergedTree = false).assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.PASSWORD, useUnmergedTree = false).assertExists()
    rule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP, useUnmergedTree = false).assertExists()
  }

  @Test
  fun entering_valid_form_enables_sign_up_button() {
    val vm = SignUpViewModel()
    rule.setContent { SampleAppTheme { SignUpScreen(vm = vm) } }

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
}
