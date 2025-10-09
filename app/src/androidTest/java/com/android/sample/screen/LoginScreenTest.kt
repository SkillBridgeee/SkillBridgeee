package com.android.sample.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.sample.LoginScreen
import com.android.sample.SignInScreeTestTags
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {
  @get:Rule val composeRule = createComposeRule()

  @Test
  fun allMainSectionsAreDisplayed() {
    composeRule.setContent { LoginScreen() }

    composeRule.onNodeWithTag(SignInScreeTestTags.TITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreeTestTags.SUBTITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreeTestTags.EMAIL_INPUT).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreeTestTags.PASSWORD_INPUT).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreeTestTags.SIGN_IN_BUTTON).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreeTestTags.AUTH_SECTION).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreeTestTags.AUTH_GOOGLE).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreeTestTags.AUTH_GITHUB).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreeTestTags.SIGNUP_LINK).assertIsDisplayed()
  }

  @Test
  fun roleSelectionWorks() {
    composeRule.setContent { LoginScreen() }

    val learnerNode = composeRule.onNodeWithTag(SignInScreeTestTags.ROLE_LEARNER)
    val tutorNode = composeRule.onNodeWithTag(SignInScreeTestTags.ROLE_TUTOR)

    learnerNode.assertIsDisplayed()
    tutorNode.assertIsDisplayed()

    tutorNode.performClick()
    tutorNode.assertIsDisplayed()

    learnerNode.performClick()
    learnerNode.assertIsDisplayed()
  }

  @Test
  fun forgotPasswordLinkWorks() {
    composeRule.setContent { LoginScreen() }

    val forgotPasswordNode = composeRule.onNodeWithTag(SignInScreeTestTags.FORGOT_PASSWORD)

    forgotPasswordNode.assertIsDisplayed()

    forgotPasswordNode.performClick()
    forgotPasswordNode.assertIsDisplayed()
  }

  @Test
  fun emailAndPasswordInputsWorkCorrectly() {
    composeRule.setContent { LoginScreen() }

    val emailField = composeRule.onNodeWithTag(SignInScreeTestTags.EMAIL_INPUT)
    val passwordField = composeRule.onNodeWithTag(SignInScreeTestTags.PASSWORD_INPUT)

    emailField.performTextInput("guillaume.lepin@epfl.ch")
    passwordField.performTextInput("truc1234567890")
  }

  @Test
  fun signInButtonIsClickable() {
    composeRule.setContent { LoginScreen() }

    composeRule.onNodeWithTag(SignInScreeTestTags.SIGN_IN_BUTTON).assertIsDisplayed().performClick()
    composeRule.onNodeWithTag(SignInScreeTestTags.SIGN_IN_BUTTON).assertTextEquals("Sign In")
  }

  @Test
  fun titleIsCorrect() {
    composeRule.setContent { LoginScreen() }
    composeRule.onNodeWithTag(SignInScreeTestTags.TITLE).assertTextEquals("SkillBridgeee")
  }

  @Test
  fun subtitleIsCorrect() {
    composeRule.setContent { LoginScreen() }
    composeRule
        .onNodeWithTag(SignInScreeTestTags.SUBTITLE)
        .assertTextEquals("Welcome back! Please sign in.")
  }

  @Test
  fun learnerButtonTextIsCorrectAndIsClickable() {
    composeRule.setContent { LoginScreen() }
    composeRule.onNodeWithTag(SignInScreeTestTags.ROLE_LEARNER).assertIsDisplayed().performClick()
    composeRule.onNodeWithTag(SignInScreeTestTags.ROLE_LEARNER).assertTextEquals("I'm a Learner")
  }

  @Test
  fun tutorButtonTextIsCorrectAndIsClickable() {
    composeRule.setContent { LoginScreen() }
    composeRule.onNodeWithTag(SignInScreeTestTags.ROLE_TUTOR).assertIsDisplayed().performClick()
    composeRule.onNodeWithTag(SignInScreeTestTags.ROLE_TUTOR).assertTextEquals("I'm a Tutor")
  }

  @Test
  fun forgotPasswordTextIsCorrectAndIsClickable() {
    composeRule.setContent { LoginScreen() }
    composeRule
        .onNodeWithTag(SignInScreeTestTags.FORGOT_PASSWORD)
        .assertIsDisplayed()
        .performClick()
    composeRule
        .onNodeWithTag(SignInScreeTestTags.FORGOT_PASSWORD)
        .assertTextEquals("Forgot password?")
  }

  @Test
  fun signUpLinkTextIsCorrectAndIsClickable() {
    composeRule.setContent { LoginScreen() }
    composeRule.onNodeWithTag(SignInScreeTestTags.SIGNUP_LINK).assertIsDisplayed().performClick()
    composeRule.onNodeWithTag(SignInScreeTestTags.SIGNUP_LINK).assertTextEquals("Sign Up")
  }

  @Test
  fun authSectionTextIsCorrect() {
    composeRule.setContent { LoginScreen() }
    composeRule.onNodeWithTag(SignInScreeTestTags.AUTH_SECTION).assertTextEquals("or continue with")
  }

  @Test
  fun authGoogleButtonIsDisplayed() {
    composeRule.setContent { LoginScreen() }
    composeRule.onNodeWithTag(SignInScreeTestTags.AUTH_GOOGLE).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreeTestTags.AUTH_GOOGLE).assertTextEquals("Google")
    composeRule.onNodeWithTag(SignInScreeTestTags.AUTH_GOOGLE).performClick()
  }

  @Test
  fun authGitHubButtonIsDisplayed() {
    composeRule.setContent { LoginScreen() }
    composeRule.onNodeWithTag(SignInScreeTestTags.AUTH_GITHUB).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreeTestTags.AUTH_GITHUB).assertTextEquals("GitHub")
    composeRule.onNodeWithTag(SignInScreeTestTags.AUTH_GITHUB).performClick()
  }
}
