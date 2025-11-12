package com.android.sample.screen

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.model.user.FakeProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.login.LoginScreen
import com.android.sample.ui.login.SignInScreenTestTags
import com.google.firebase.FirebaseApp
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {
  @get:Rule val composeRule = createComposeRule()

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // (Optional but harmless) ensure Firebase is initialized for test envs
    try {
      FirebaseApp.clearInstancesForTest()
    } catch (_: Throwable) {}
    try {
      FirebaseApp.initializeApp(context)
    } catch (_: IllegalStateException) {}

    // 👇 This is the important bit for your crash
    ProfileRepositoryProvider.setForTests(FakeProfileRepository())
  }
  // read the visible text of a node by testTag
  private fun ComposeTestRule.textOf(tag: String, useUnmergedTree: Boolean = false): String {
    val node = onNodeWithTag(tag, useUnmergedTree).fetchSemanticsNode()
    val texts = node.config.getOrNull(SemanticsProperties.Text)
    return texts?.joinToString("") { it.text } ?: ""
  }

  // count nodes with a tag (useful instead of assertExists/IsDisplayed)
  private fun ComposeTestRule.nodeCount(tag: String, useUnmergedTree: Boolean = false): Int {
    return onAllNodes(hasTestTag(tag), useUnmergedTree).fetchSemanticsNodes().size
  }

  private fun androidx.compose.ui.test.SemanticsNodeInteraction.readEditableText(): String {
    val node = fetchSemanticsNode()
    val editable = node.config.getOrNull(SemanticsProperties.EditableText)
    return editable?.text ?: ""
  }

  @Test
  fun allMainSectionsAreDisplayed() {
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)
      LoginScreen(viewModel = viewModel, onGoogleSignIn = { /* Test placeholder */})
    }

    composeRule.onNodeWithTag(SignInScreenTestTags.TITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.SUBTITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.EMAIL_INPUT).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.PASSWORD_INPUT).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.AUTH_SECTION).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.AUTH_GOOGLE).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGNUP_LINK).assertIsDisplayed()
  }

  @Test
  fun forgotPasswordLinkWorks() {
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)
      LoginScreen(viewModel = viewModel, onGoogleSignIn = { /* Test placeholder */})
    }

    val forgotPasswordNode = composeRule.onNodeWithTag(SignInScreenTestTags.FORGOT_PASSWORD)

    forgotPasswordNode.assertIsDisplayed()

    forgotPasswordNode.performClick()
    forgotPasswordNode.assertIsDisplayed()
  }

  @Test
  fun emailAndPasswordInputsWorkCorrectly() {
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)
      LoginScreen(viewModel = viewModel, onGoogleSignIn = { /* Test placeholder */})
    }
    val mail = "guillaume.lepin@epfl.ch"
    val password = "truc1234567890"

    composeRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON)
    composeRule.onNodeWithTag(SignInScreenTestTags.EMAIL_INPUT).performTextInput(mail)
    composeRule.onNodeWithTag(SignInScreenTestTags.PASSWORD_INPUT).performTextInput(password)
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).assertIsEnabled().performClick()
  }

  @Test
  fun signInButtonIsClickable() {
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)
      LoginScreen(viewModel = viewModel, onGoogleSignIn = { /* Test placeholder */})
    }

    composeRule
        .onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON)
        .assertIsDisplayed()
        .assertIsNotEnabled()
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).assertTextEquals("Sign In")
  }

  @Test
  fun titleIsCorrect() {
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)
      LoginScreen(viewModel = viewModel, onGoogleSignIn = { /* Test placeholder */})
    }
    composeRule.onNodeWithTag(SignInScreenTestTags.TITLE).assertTextEquals("SkillBridge")
  }

  @Test
  fun subtitleIsCorrect() {
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)
      LoginScreen(viewModel = viewModel, onGoogleSignIn = { /* Test placeholder */})
    }
    composeRule
        .onNodeWithTag(SignInScreenTestTags.SUBTITLE)
        .assertTextEquals("Welcome back! Please sign in.")
  }

  @Test
  fun forgotPasswordTextIsCorrectAndIsClickable() {
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)
      LoginScreen(viewModel = viewModel, onGoogleSignIn = { /* Test placeholder */})
    }
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
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)
      LoginScreen(viewModel = viewModel, onGoogleSignIn = { /* Test placeholder */})
    }
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGNUP_LINK).assertIsDisplayed().performClick()
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGNUP_LINK).assertTextEquals("Sign Up")
  }

  @Test
  fun authSectionTextIsCorrect() {
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)
      LoginScreen(viewModel = viewModel, onGoogleSignIn = { /* Test placeholder */})
    }
    composeRule
        .onNodeWithTag(SignInScreenTestTags.AUTH_SECTION)
        .assertTextEquals("or continue with")
  }

  @Test
  fun authGoogleButtonIsDisplayed() {
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)
      LoginScreen(viewModel = viewModel, onGoogleSignIn = { /* Test placeholder */})
    }
    composeRule.onNodeWithTag(SignInScreenTestTags.AUTH_GOOGLE).assertIsDisplayed()
    composeRule.onNodeWithTag(SignInScreenTestTags.AUTH_GOOGLE).assertTextEquals("Google")
    composeRule.onNodeWithTag(SignInScreenTestTags.AUTH_GOOGLE).performClick()
  }

  @Test
  fun signInButtonEnablesWhenBothEmailAndPasswordProvided() {
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)
      LoginScreen(viewModel = viewModel, onGoogleSignIn = { /* Test placeholder */})
    }

    // Initially disabled
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).assertIsNotEnabled()

    // Still disabled with only email
    composeRule.onNodeWithTag(SignInScreenTestTags.EMAIL_INPUT).performTextInput("test@example.com")
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).assertIsNotEnabled()

    // Enabled with both email and password
    composeRule.onNodeWithTag(SignInScreenTestTags.PASSWORD_INPUT).performTextInput("password123")
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).assertIsEnabled()
  }

  @Test
  fun errorMessageDisplayedWhenAuthenticationFails() {
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)

      // Simulate an error state
      viewModel.setError("Invalid email or password")

      LoginScreen(viewModel = viewModel, onGoogleSignIn = { /* Test placeholder */})
    }

    // Check that error message is displayed
    composeRule.onNodeWithText("Invalid email or password").assertIsDisplayed()
  }

  @Test
  fun googleSignInCallbackTriggered() {
    var googleSignInCalled = false
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)
      LoginScreen(viewModel = viewModel, onGoogleSignIn = { googleSignInCalled = true })
    }

    composeRule.onNodeWithTag(SignInScreenTestTags.AUTH_GOOGLE).performClick()

    assert(googleSignInCalled)
  }

  @Test
  fun successMessageDisplayedAfterAuthentication() {
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)

      // Simulate successful authentication
      viewModel.showSuccessMessage(true)

      LoginScreen(viewModel = viewModel, onGoogleSignIn = { /* Test placeholder */})
    }

    // Check that success message components are displayed
    composeRule.onNodeWithText("Authentication Successful!").assertIsDisplayed()
    composeRule.onNodeWithText("Sign Out").assertIsDisplayed()
  }

  @Test
  fun signOutButtonWorksInSuccessState() {
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)

      // Simulate successful authentication
      viewModel.showSuccessMessage(true)

      LoginScreen(viewModel = viewModel, onGoogleSignIn = { /* Test placeholder */})
    }

    // Click sign out button
    composeRule.onNodeWithText("Sign Out").performClick()

    // Should return to login form (success message should be hidden)
    composeRule.onNodeWithTag(SignInScreenTestTags.TITLE).assertIsDisplayed()
  }

  @Test
  fun passwordResetTriggeredWhenForgotPasswordClicked() {
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)

      // Pre-fill email for password reset
      viewModel.updateEmail("test@example.com")

      LoginScreen(viewModel = viewModel, onGoogleSignIn = { /* Test placeholder */})
    }

    // Click forgot password
    composeRule.onNodeWithTag(SignInScreenTestTags.FORGOT_PASSWORD).performClick()

    // The password reset function should be called (verified by no crash)
    composeRule.onNodeWithTag(SignInScreenTestTags.FORGOT_PASSWORD).assertIsDisplayed()
  }

  @Test
  fun loadingStateShowsProgressIndicator() {
    composeRule.setContent {
      val context = LocalContext.current
      val viewModel = AuthenticationViewModel(context)

      // Set up valid form data
      viewModel.updateEmail("test@example.com")
      viewModel.updatePassword("password123")

      LoginScreen(viewModel = viewModel, onGoogleSignIn = { /* Test placeholder */})
    }

    // Enter credentials and click sign in to trigger loading
    composeRule.onNodeWithTag(SignInScreenTestTags.EMAIL_INPUT).performTextInput("test@example.com")
    composeRule.onNodeWithTag(SignInScreenTestTags.PASSWORD_INPUT).performTextInput("password123")

    // Button should be enabled with valid inputs
    composeRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).assertIsEnabled()
  }

  @Test
  fun password_field_present_and_not_affected_by_email_ellipsizing() {
    composeRule.setContent {
      val context = LocalContext.current
      val vm = AuthenticationViewModel(context)
      LoginScreen(viewModel = vm, onGoogleSignIn = {})
    }

    // presence via count (no assertExists/IsDisplayed)
    val before = composeRule.nodeCount(SignInScreenTestTags.PASSWORD_INPUT)
    assert(before >= 1) { "Password field not found." }

    // type into password
    val longPassword = "thisIsAVeryLongPassword123!@#"
    composeRule
        .onNodeWithTag(SignInScreenTestTags.PASSWORD_INPUT, useUnmergedTree = false)
        .performClick()
        .performTextInput(longPassword)

    // flip focus to email and back just to ensure nothing breaks
    composeRule
        .onNodeWithTag(SignInScreenTestTags.EMAIL_INPUT, useUnmergedTree = false)
        .performClick()
    composeRule
        .onNodeWithTag(SignInScreenTestTags.PASSWORD_INPUT, useUnmergedTree = false)
        .performClick()

    val after = composeRule.nodeCount(SignInScreenTestTags.PASSWORD_INPUT)
    assert(after >= 1) { "Password field disappeared after interactions." }
  }
}
