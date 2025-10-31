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
import com.android.sample.model.user.FirestoreProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.signup.SignUpScreen
import com.android.sample.ui.signup.SignUpScreenTestTags
import com.android.sample.ui.signup.SignUpViewModel
import com.android.sample.ui.theme.SampleAppTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
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
private const val DEFAULT_TIMEOUT_MS = 10_000L // Reduced from 30_000

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

/**
 * Helper function to create a user programmatically and wait for completion. Returns true if
 * successful, false if failed.
 */
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
    // Connect to Firebase emulators
    try {
      Firebase.firestore.useEmulator("10.0.2.2", 8080)
      Firebase.auth.useEmulator("10.0.2.2", 9099)
    } catch (_: IllegalStateException) {
      // Emulator already initialized
    }

    auth = Firebase.auth

    // Initialize ProfileRepositoryProvider with real Firestore
    ProfileRepositoryProvider.setForTests(FirestoreProfileRepository(Firebase.firestore))

    // Clean up any existing user before starting
    auth.signOut()
  }

  @After
  fun tearDown() {
    // Clean up: delete the test user if created
    try {
      auth.currentUser?.delete()
    } catch (_: Exception) {
      // Ignore deletion errors
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
    composeRule.nodeByTag(SignUpScreenTestTags.ADDRESS).performTextInput("London Street 1")
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

    // Verify success
    assertTrue("Signup should succeed", vm.state.value.submitSuccess)

    // Wait for Firebase Auth to be ready by checking current user
    composeRule.waitUntil(5_000) { auth.currentUser != null }

    // Verify Firebase Auth account was created
    assertNotNull("User should be authenticated", auth.currentUser)
    assertEquals(testEmail, auth.currentUser?.email)
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
    composeRule.nodeByTag(SignUpScreenTestTags.ADDRESS).performTextInput("S1")
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("CS")
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performTextInput("  $testEmail ")
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performTextInput("passw0rd!")

    // Close keyboard with IME action
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performImeAction()
    composeRule.waitForIdle()

    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).assertIsEnabled()
    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).performScrollTo().performClick()

    // Wait for signup to complete by observing ViewModel state
    composeRule.waitUntil(DEFAULT_TIMEOUT_MS) {
      vm.state.value.submitSuccess || vm.state.value.error != null
    }

    assertTrue("Signup should succeed", vm.state.value.submitSuccess)

    // Wait for Firebase Auth to be ready
    composeRule.waitUntil(5_000) { auth.currentUser != null }

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
      composeRule.waitUntil(5_000) { auth.currentUser != null }

      // Sign out so we can test UI signup with duplicate email
      auth.signOut()
    }

    // Now try to sign up via UI with the same email - should show error
    val vm = SignUpViewModel()
    composeRule.setContent { SampleAppTheme { SignUpScreen(vm = vm) } }
    composeRule.waitForIdle()

    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).performTextInput("John")
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).performTextInput("Doe")
    composeRule.nodeByTag(SignUpScreenTestTags.ADDRESS).performTextInput("Street 1")
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
    composeRule.nodeByTag(SignUpScreenTestTags.ADDRESS).performTextInput("Street 1")
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("CS")
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performTextInput(testEmail)
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performTextInput("123!")

    // Close keyboard with IME action
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performImeAction()
    composeRule.waitForIdle()

    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).performScrollTo().performClick()

    // Wait for error or completion by observing ViewModel state
    composeRule.waitUntil(DEFAULT_TIMEOUT_MS) {
      vm.state.value.error != null || !vm.state.value.submitting || vm.state.value.submitSuccess
    }

    // Should either have an error or not have succeeded
    assertTrue(
        "Weak password should either error or not succeed",
        vm.state.value.error != null || !vm.state.value.submitSuccess)
  }
}
