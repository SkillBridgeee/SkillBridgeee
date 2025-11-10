package com.android.sample.testutils

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import com.android.sample.ui.components.LocationInputFieldTestTags
import com.android.sample.ui.login.SignInScreenTestTags
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager
import com.android.sample.ui.signup.SignUpScreenTestTags
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

object TestUiHelpers {
  /**
   * Perform a sign up through the app UI for tests that are redirected to the SignUp screen (e.g.
   * Google-sign-in that requires creating an app profile).
   *
   * The function fills the exact fields used by SignUpScreen:
   * - Name (placeholder "Enter your Name")
   * - Surname (placeholder "Enter your Surname")
   * - Address / location query (uses location input test tag if available)
   * - Level of education (placeholder "Major, Year (e.g. CS, 3rd year)")
   * - Description (placeholder "Short description of yourself")
   * - Password only if the password field is present (SignUpScreen hides it for Google signups)
   *
   * Usage: TestUiHelpers.signUpThroughUi(composeTestRule, password = "P@ssw0rd!", name = "Test",
   * surname = "User")
   */
  fun signUpThroughUi(
      composeTestRule: AndroidComposeTestRule<*, *>,
      password: String,
      name: String = "Test",
      surname: String = "User",
      levelOfEducation: String = "CS",
      description: String = "Test description",
      addressQuery: String = "Test Location",
      timeoutMs: Long = 8_000L
  ) {
    // If there's a sign up link on the sign-in screen, open it first.
    val signUpLinkNode =
        when {
          composeTestRule
              .onAllNodes(hasTestTag(SignInScreenTestTags.SIGNUP_LINK))
              .fetchSemanticsNodes()
              .isNotEmpty() -> composeTestRule.onNode(hasTestTag(SignInScreenTestTags.SIGNUP_LINK))
          composeTestRule.onAllNodes(hasText("Sign Up")).fetchSemanticsNodes().isNotEmpty() ->
              composeTestRule.onNodeWithText("Sign Up")
          else -> null
        }
    signUpLinkNode?.apply {
      performClick()
      composeTestRule.waitForIdle()
    }

    // Helper: prefer test tag, fallback to actual placeholder text used in SignUpScreen.
    fun findNodeByTagOrText(tag: String, textFallback: String): SemanticsNodeInteraction {
      return when {
        composeTestRule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() ->
            composeTestRule.onNode(hasTestTag(tag))
        composeTestRule.onAllNodes(hasText(textFallback)).fetchSemanticsNodes().isNotEmpty() ->
            composeTestRule.onNodeWithText(textFallback)
        else -> composeTestRule.onRoot() // will cause an assertion later if missing
      }
    }

    // Locate the fields using the exact placeholders from SignUpScreen where helpful.
    val nameNode = findNodeByTagOrText(SignUpScreenTestTags.NAME, "Enter your Name")
    val surnameNode = findNodeByTagOrText(SignUpScreenTestTags.SURNAME, "Enter your Surname")

    // Location input: try the inner location input test tag first, then the wrapper box tag.
    val addressNode =
        when {
          composeTestRule
              .onAllNodes(hasTestTag(LocationInputFieldTestTags.INPUT_LOCATION))
              .fetchSemanticsNodes()
              .isNotEmpty() ->
              composeTestRule.onNode(hasTestTag(LocationInputFieldTestTags.INPUT_LOCATION))
          composeTestRule
              .onAllNodes(hasTestTag(SignUpScreenTestTags.ADDRESS))
              .fetchSemanticsNodes()
              .isNotEmpty() -> composeTestRule.onNode(hasTestTag(SignUpScreenTestTags.ADDRESS))
          else -> composeTestRule.onRoot()
        }

    val levelNode =
        findNodeByTagOrText(
            SignUpScreenTestTags.LEVEL_OF_EDUCATION, "Major, Year (e.g. CS, 3rd year)")
    val descriptionNode =
        findNodeByTagOrText(SignUpScreenTestTags.DESCRIPTION, "Short description of yourself")
    // Email is intentionally skipped for Google signups (SignUpScreen disables it when
    // isGoogleSignUp).
    val passwordNode = findNodeByTagOrText(SignUpScreenTestTags.PASSWORD, "Password")

    // Fill required fields (guarded by existence checks)
    if (nameNode.exists()) {
      nameNode.performTextInput(name)
    }
    if (surnameNode.exists()) {
      surnameNode.performTextInput(surname)
    }

    // Type into the location input so SignUpViewModel receives the query (it updates
    // address/locationQuery).
    if (addressNode.exists()) {
      addressNode.performTextReplacement(addressQuery)
      // Try to commit the query (some location inputs react to IME action)
      try {
        addressNode.performImeAction()
      } catch (_: Throwable) {}
      composeTestRule.waitForIdle()
    }

    if (levelNode.exists()) {
      levelNode.performTextInput(levelOfEducation)
    }
    if (descriptionNode.exists()) {
      descriptionNode.performTextInput(description)
    }

    // Only fill password if visible (SignUpScreen hides password for Google signups)
    if (passwordNode.exists()) {
      passwordNode.performTextInput(password)
    }

    composeTestRule.waitForIdle()

    // Submit via the Sign Up button (test tag is present on the actual Button)
    val submitNode =
        when {
          composeTestRule
              .onAllNodes(hasTestTag(SignUpScreenTestTags.SIGN_UP))
              .fetchSemanticsNodes()
              .isNotEmpty() -> composeTestRule.onNode(hasTestTag(SignUpScreenTestTags.SIGN_UP))
          composeTestRule.onAllNodes(hasText("Sign Up")).fetchSemanticsNodes().isNotEmpty() ->
              composeTestRule.onNodeWithText("Sign Up")
          else -> composeTestRule.onRoot()
        }

    if (submitNode.exists()) {
      submitNode.performClick()
    } else {
      // fallback: trigger IME action on password field to attempt submit
      if (passwordNode.exists()) {
        passwordNode.performImeAction()
      }
    }

    composeTestRule.waitForIdle()

    // Wait until navigation leaves the signup route or timeout.
    val start = System.currentTimeMillis()
    while (System.currentTimeMillis() - start < timeoutMs) {
      val current = RouteStackManager.getCurrentRoute()
      if (current == null || !current.startsWith(NavRoutes.SIGNUP_BASE)) return
      runBlocking { delay(200) }
    }
    // If timed out, the test will continue and likely fail — logs will help diagnose.
  }

  // Helper extension to detect presence without throwing.
  private fun SemanticsNodeInteraction.exists(): Boolean {
    return try {
      this.fetchSemanticsNode()
      true
    } catch (_: AssertionError) {
      false
    }
  }
}
