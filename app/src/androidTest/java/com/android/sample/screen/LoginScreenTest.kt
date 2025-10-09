package com.android.sample.screen

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertValueEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.sample.LoginScreen
import com.android.sample.SignInScreenTestTags
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {
  @get:Rule val composeRule = createComposeRule()

  @Test
  fun allMainSectionsAreDisplayed() {
    composeRule.setContent { LoginScreen() }

    composeRule.onNodeWithTag(SignInScreenTestTags.TITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.SUBTITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.EMAIL_INPUT).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.PASSWORD_INPUT).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.AUTH_SECTION).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.AUTH_GOOGLE).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.AUTH_GITHUB).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGNUP_LINK).assertIsDisplayed()
  }

  @Test
  fun roleSelectionWorks() {
    composeRule.setContent { LoginScreen() }

    val learnerNode = composeRule.onNodeWithTag(SignInScreenTestTags.ROLE_LEARNER)
    val tutorNode = composeRule.onNodeWithTag(SignInScreenTestTags.ROLE_TUTOR)

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

    val forgotPasswordNode = composeRule.onNodeWithTag(SignInScreenTestTags.FORGOT_PASSWORD)

    forgotPasswordNode.assertIsDisplayed()

    forgotPasswordNode.performClick()
    forgotPasswordNode.assertIsDisplayed()
  }

  @Test
  fun emailAndPasswordInputsWorkCorrectly() {
    composeRule.setContent { LoginScreen() }
    val mail = "guillaume.lepin@epfl.ch"
    val password = "truc1234567890"


    composeRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON)
    composeRule.onNodeWithTag(SignInScreenTestTags.EMAIL_INPUT).performTextInput(mail)
    composeRule.onNodeWithTag(SignInScreenTestTags.PASSWORD_INPUT).performTextInput(password)
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).assertIsEnabled().performClick()


  }

  @Test
  fun signInButtonIsClickable() {
    composeRule.setContent { LoginScreen() }

    composeRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).assertIsDisplayed().assertIsNotEnabled()
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).assertTextEquals("Sign In")
  }

  @Test
  fun titleIsCorrect() {
    composeRule.setContent { LoginScreen() }
    composeRule.onNodeWithTag(SignInScreenTestTags.TITLE).assertTextEquals("SkillBridge")
  }

  @Test
  fun subtitleIsCorrect() {
    composeRule.setContent { LoginScreen() }
    composeRule
        .onNodeWithTag(SignInScreenTestTags.SUBTITLE)
        .assertTextEquals("Welcome back! Please sign in.")
  }

  @Test
  fun learnerButtonTextIsCorrectAndIsClickable() {
    composeRule.setContent { LoginScreen() }
    composeRule.onNodeWithTag(SignInScreenTestTags.ROLE_LEARNER).assertIsDisplayed().performClick()
    composeRule.onNodeWithTag(SignInScreenTestTags.ROLE_LEARNER).assertTextEquals("I'm a Learner")
  }

  @Test
  fun tutorButtonTextIsCorrectAndIsClickable() {
    composeRule.setContent { LoginScreen() }
    composeRule.onNodeWithTag(SignInScreenTestTags.ROLE_TUTOR).assertIsDisplayed().performClick()
    composeRule.onNodeWithTag(SignInScreenTestTags.ROLE_TUTOR).assertTextEquals("I'm a Tutor")
  }

  @Test
  fun forgotPasswordTextIsCorrectAndIsClickable() {
    composeRule.setContent { LoginScreen() }
    composeRule
        .onNodeWithTag(SignInScreenTestTags.FORGOT_PASSWORD)
        .assertIsDisplayed()
        .performClick()
    composeRule
        .onNodeWithTag(SignInScreenTestTags.FORGOT_PASSWORD)
        .assertTextEquals("Forgot password?")
  }

  @Test
  fun signUpLinkTextIsCorrectAndIsClickable() {
    composeRule.setContent { LoginScreen() }
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGNUP_LINK).assertIsDisplayed().performClick()
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGNUP_LINK).assertTextEquals("Sign Up")
  }

  @Test
  fun authSectionTextIsCorrect() {
    composeRule.setContent { LoginScreen() }
    composeRule.onNodeWithTag(SignInScreenTestTags.AUTH_SECTION).assertTextEquals("or continue with")
  }

  @Test
  fun authGoogleButtonIsDisplayed() {
    composeRule.setContent { LoginScreen() }
    composeRule.onNodeWithTag(SignInScreenTestTags.AUTH_GOOGLE).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.AUTH_GOOGLE).assertTextEquals("Google")
    composeRule.onNodeWithTag(SignInScreenTestTags.AUTH_GOOGLE).performClick()
  }

  @Test
  fun authGitHubButtonIsDisplayed() {
    composeRule.setContent { LoginScreen() }
    composeRule.onNodeWithTag(SignInScreenTestTags.AUTH_GITHUB).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.AUTH_GITHUB).assertTextEquals("GitHub")
    composeRule.onNodeWithTag(SignInScreenTestTags.AUTH_GITHUB).performClick()
  }
}
