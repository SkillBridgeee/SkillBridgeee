package com.android.sample.testutils

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import com.android.sample.ui.components.LocationInputFieldTestTags
import com.android.sample.ui.login.SignInScreenTestTags
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager
import com.android.sample.ui.signup.SignUpScreenTestTags

object TestUiHelpers {

  /**
   * Performs a full sign up through the app UI in tests. Handles both email/password and Google
   * signups.
   */
  fun signUpThroughUi(
      composeTestRule: AndroidComposeTestRule<*, *>,
      password: String,
      name: String = "Test",
      surname: String = "User",
      levelOfEducation: String = "CS",
      description: String = "Test description",
      addressQuery: String = "Test Location",
      timeoutMs: Long = 10_000L
  ) {
    // Step 1: Navigate to SignUpScreen if necessary
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

    // Step 2: Helper to find nodes by test tag or fallback text
    fun findNode(tag: String, textFallback: String) =
        when {
          composeTestRule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() ->
              composeTestRule.onNode(hasTestTag(tag))
          composeTestRule.onAllNodes(hasText(textFallback)).fetchSemanticsNodes().isNotEmpty() ->
              composeTestRule.onNodeWithText(textFallback)
          else -> composeTestRule.onRoot() // will fail later if missing
        }

    // Step 3: Fill required fields if visible
    val nameNode = findNode(SignUpScreenTestTags.NAME, "Enter your Name")
    val surnameNode = findNode(SignUpScreenTestTags.SURNAME, "Enter your Surname")
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
        findNode(SignUpScreenTestTags.LEVEL_OF_EDUCATION, "Major, Year (e.g. CS, 3rd year)")
    val descriptionNode =
        findNode(SignUpScreenTestTags.DESCRIPTION, "Short description of yourself")
    val passwordNode = findNode(SignUpScreenTestTags.PASSWORD, "Password")

    if (nameNode.exists()) nameNode.performTextInput(name)
    if (surnameNode.exists()) surnameNode.performTextInput(surname)
    if (addressNode.exists()) {
      addressNode.performTextReplacement(addressQuery)
      try {
        addressNode.performImeAction()
      } catch (_: Throwable) {}
    }
    if (levelNode.exists()) levelNode.performTextInput(levelOfEducation)
    if (descriptionNode.exists()) descriptionNode.performTextInput(description)
    if (passwordNode.exists()) passwordNode.performTextInput(password)

    composeTestRule.waitForIdle()
    Thread.sleep(200) // Replace delay() with Thread.sleep()

    // Step 4: Click the Sign Up button
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
    } else if (passwordNode.exists()) {
      passwordNode.performImeAction()
    }

    composeTestRule.waitForIdle()
    Thread.sleep(200) // Replace delay() with Thread.sleep()

    // Step 5: Wait until navigation leaves the signup route
    composeTestRule.waitUntil(timeoutMs) {
      val current = RouteStackManager.getCurrentRoute()
      current == null || !current.startsWith(NavRoutes.SIGNUP_BASE)
    }

    composeTestRule.waitForIdle() // final idle
  }

  // Helper to safely check existence
  private fun SemanticsNodeInteraction.exists(): Boolean {
    return try {
      this.fetchSemanticsNode()
      true
    } catch (_: AssertionError) {
      false
    }
  }
}
