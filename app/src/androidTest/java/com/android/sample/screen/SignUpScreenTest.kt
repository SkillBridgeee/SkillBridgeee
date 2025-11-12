package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.android.sample.model.user.FakeProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.components.LocationInputFieldTestTags
import com.android.sample.ui.signup.SignUpScreen
import com.android.sample.ui.signup.SignUpScreenTestTags
import com.android.sample.ui.signup.SignUpViewModel
import com.android.sample.ui.theme.SampleAppTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// ---------- helpers ----------
private const val DEFAULT_TIMEOUT_MS = 15_000L // a bit more headroom for CI

private fun waitForTag(
    rule: ComposeContentTestRule,
    tag: String,
    timeoutMs: Long = DEFAULT_TIMEOUT_MS
) {
  rule.waitUntil(timeoutMs) {
    rule.onAllNodes(hasTestTag(tag), useUnmergedTree = false).fetchSemanticsNodes().isNotEmpty()
  }
}

private fun ComposeContentTestRule.nodeByTag(tag: String) =
    onNodeWithTag(tag, useUnmergedTree = false)

/** Create a user via Firebase Auth and await completion. */
private suspend fun createUserProgrammatically(
    auth: FirebaseAuth,
    email: String,
    password: String
): Boolean {
  return try {
    auth.createUserWithEmailAndPassword(email, password).await()
    true
  } catch (_: Exception) {
    false
  }
}

// ---------- tests ----------
class SignUpScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var auth: FirebaseAuth

  @Before
  fun setUp() {
    // Use the Auth emulator; no Firestore dependency in these tests.
    try {
      Firebase.auth.useEmulator("10.0.2.2", 9099)
    } catch (_: IllegalStateException) {
      // already configured
    }

    auth = Firebase.auth

    // Use an in-memory fake repository to avoid Firestore emulator in CI
    ProfileRepositoryProvider.setForTests(FakeProfileRepository())

    // Start from a clean auth state
    auth.signOut()
    composeRule.waitUntil(2_000) { auth.currentUser == null }
  }

  @After
  fun tearDown() {
    try {
      auth.currentUser?.delete()
    } catch (_: Exception) {
      // ignore
    }
    auth.signOut()
  }

  @Test
  fun all_fields_render() {
    val vm = SignUpViewModel()
    composeRule.setContent { SampleAppTheme { SignUpScreen(vm = vm) } }
    composeRule.waitForIdle()

    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    composeRule.nodeByTag(SignUpScreenTestTags.TITLE).assertIsDisplayed()
    composeRule.nodeByTag(SignUpScreenTestTags.SUBTITLE).assertIsDisplayed()

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).performScrollTo().assertIsDisplayed()
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).performScrollTo().assertIsDisplayed()
    composeRule.nodeByTag(SignUpScreenTestTags.ADDRESS).performScrollTo().assertIsDisplayed()
    composeRule
        .nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION)
        .performScrollTo()
        .assertIsDisplayed()
    composeRule.nodeByTag(SignUpScreenTestTags.DESCRIPTION).performScrollTo().assertIsDisplayed()
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performScrollTo().assertIsDisplayed()
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performScrollTo().assertIsDisplayed()
  }

  @Test
  fun successful_signup_creates_firebase_auth_and_profile() {
    val vm = SignUpViewModel()
    composeRule.setContent { SampleAppTheme { SignUpScreen(vm = vm) } }
    composeRule.waitForIdle()

    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    // Use a unique email to avoid conflicts
    val testEmail = "test${System.currentTimeMillis()}@example.com"

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).performTextInput("Ada")
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).performTextInput("Lovelace")
    composeRule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, useUnmergedTree = true)
        .performTextInput("London Street 1")
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("CS, 3rd year")
    composeRule.nodeByTag(SignUpScreenTestTags.DESCRIPTION).performTextInput("Loves mathematics")
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performTextInput(testEmail)
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performTextInput("TestPass123!")

    // Close keyboard with IME action
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performImeAction()
    composeRule.waitForIdle()

    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).assertIsEnabled()
    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).performScrollTo().performClick()

    // Wait for signup to complete by observing ViewModel state
    composeRule.waitUntil(DEFAULT_TIMEOUT_MS) {
      vm.state.value.submitSuccess || vm.state.value.error != null
    }

    // Verify success path in VM
    assertTrue("Signup should succeed", vm.state.value.submitSuccess)

    // Wait for Firebase Auth to reflect the current user
    composeRule.waitUntil(15_000) { auth.currentUser != null }

    // Verify Firebase Auth account was created (normalize for comparison)
    assertNotNull("User should be authenticated", auth.currentUser)
    val actualEmail = auth.currentUser?.email?.trim()?.lowercase()
    val expectedEmail = testEmail.trim().lowercase()
    assertEquals(expectedEmail, actualEmail)
  }

  @Test
  fun uppercase_email_is_accepted_and_trimmed() {
    val vm = SignUpViewModel()
    composeRule.setContent { SampleAppTheme { SignUpScreen(vm = vm) } }
    composeRule.waitForIdle()

    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    // Use a unique email to avoid conflicts
    val testEmail = "TEST${System.currentTimeMillis()}@MAIL.Example.ORG"

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).performTextInput("Élise")
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).performTextInput("Müller")
    composeRule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, useUnmergedTree = true)
        .performTextInput("S1")
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("CS")
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performTextInput("  $testEmail ")
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performTextInput("passw0rd!")

    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performImeAction()
    composeRule.waitForIdle()

    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).assertIsEnabled()
    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).performScrollTo().performClick()

    composeRule.waitUntil(DEFAULT_TIMEOUT_MS) {
      vm.state.value.submitSuccess || vm.state.value.error != null
    }

    assertTrue("Signup should succeed", vm.state.value.submitSuccess)

    composeRule.waitUntil(15_000) { auth.currentUser != null }
    assertNotNull("User should be authenticated", auth.currentUser)
  }

  @Test
  fun duplicate_email_shows_error() {
    // Use a unique email for this test
    val duplicateEmail = "duplicate${System.currentTimeMillis()}@test.com"

    // First, create a user programmatically (not via UI) to ensure independence
    runBlocking {
      val created = createUserProgrammatically(auth, duplicateEmail, "FirstPass123!")
      assertTrue("Programmatic user creation should succeed", created)

      // Wait for auth to be ready
      composeRule.waitUntil(10_000) { auth.currentUser != null }

      // Sign out so we can test UI signup with duplicate email
      auth.signOut()
    }
    // Give CI a moment to settle signed-out state
    composeRule.waitUntil(3_000) { auth.currentUser == null }

    // Now try to sign up via UI with the same email - should show error
    val vm = SignUpViewModel()
    composeRule.setContent { SampleAppTheme { SignUpScreen(vm = vm) } }
    composeRule.waitForIdle()

    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).performTextInput("John")
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).performTextInput("Doe")
    composeRule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, useUnmergedTree = true)
        .performTextInput("Street 1")
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("CS")
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performTextInput(duplicateEmail)
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performTextInput("SecondPass123!")

    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performImeAction()
    composeRule.waitForIdle()

    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).performScrollTo().performClick()

    // Wait for error to appear by observing ViewModel state
    composeRule.waitUntil(DEFAULT_TIMEOUT_MS) {
      vm.state.value.error != null || vm.state.value.submitSuccess
    }

    // Should have an error and not be successful
    assertTrue("Duplicate email should show error", vm.state.value.error != null)
    assertTrue(
        "Error should mention email already registered",
        vm.state.value.error?.contains("already", ignoreCase = true) == true ||
            vm.state.value.error?.contains("registered", ignoreCase = true) == true)
  }

  @Test
  fun weak_password_shows_error() {
    val vm = SignUpViewModel()
    composeRule.setContent { SampleAppTheme { SignUpScreen(vm = vm) } }
    composeRule.waitForIdle()

    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    val testEmail = "weakpass${System.currentTimeMillis()}@test.com"

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).performTextInput("Test")
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).performTextInput("User")
    composeRule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, useUnmergedTree = true)
        .performTextInput("Street 1")
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("CS")
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performTextInput(testEmail)
    // Password "123!" is too short (< 8 chars) and missing a letter
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performTextInput("123!")

    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performImeAction()
    composeRule.waitForIdle()

    // Scroll to the button to ensure it's measured
    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).performScrollTo()
    composeRule.waitForIdle()

    // Verify form validation failed via VM (button enablement is derived from it)
    assertTrue("Weak password should prevent form submission", !vm.state.value.canSubmit)
  }
}
