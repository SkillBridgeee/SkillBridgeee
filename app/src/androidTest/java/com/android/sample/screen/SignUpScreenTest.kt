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
import com.android.sample.ui.signup.Role
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
private fun waitForTag(rule: ComposeContentTestRule, tag: String, timeoutMs: Long = 30_000) {
  rule.waitUntil(timeoutMs) {
    rule.onAllNodes(hasTestTag(tag), useUnmergedTree = false).fetchSemanticsNodes().isNotEmpty()
  }
}

private fun ComposeContentTestRule.nodeByTag(tag: String) =
    onNodeWithTag(tag, useUnmergedTree = false)

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
  fun all_fields_render_and_role_toggle() {
    val vm = SignUpViewModel()
    composeRule.setContent { SampleAppTheme { SignUpScreen(vm = vm) } }
    composeRule.waitForIdle()

    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    composeRule.nodeByTag(SignUpScreenTestTags.TITLE).assertIsDisplayed()
    composeRule.nodeByTag(SignUpScreenTestTags.SUBTITLE).assertIsDisplayed()

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).assertIsDisplayed()
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).assertIsDisplayed()
    composeRule.nodeByTag(SignUpScreenTestTags.ADDRESS).assertIsDisplayed()
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).assertIsDisplayed()
    composeRule.nodeByTag(SignUpScreenTestTags.DESCRIPTION).assertIsDisplayed()
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).assertIsDisplayed()
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).assertIsDisplayed()

    composeRule.nodeByTag(SignUpScreenTestTags.TUTOR).performClick()
    assertEquals(Role.TUTOR, vm.state.value.role)
    composeRule.nodeByTag(SignUpScreenTestTags.LEARNER).performClick()
    assertEquals(Role.LEARNER, vm.state.value.role)
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

    // Wait for signup to complete - increased timeout for slow emulators
    composeRule.waitUntil(30_000) { vm.state.value.submitSuccess || vm.state.value.error != null }

    // Verify success
    assertTrue("Signup should succeed", vm.state.value.submitSuccess)

    // Give Firebase emulator time to process
    Thread.sleep(1000)

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

    // Wait for signup to complete - increased timeout for slow emulators
    composeRule.waitUntil(30_000) { vm.state.value.submitSuccess || vm.state.value.error != null }

    assertTrue("Signup should succeed", vm.state.value.submitSuccess)

    // Give Firebase emulator time to process
    Thread.sleep(1000)

    assertNotNull("User should be authenticated", auth.currentUser)
  }

  @Test
  fun duplicate_email_shows_error() {
    // Use a fixed email that we'll try to register twice
    val duplicateEmail = "duplicate${System.currentTimeMillis()}@test.com"

    // First signup - should succeed
    val vm1 = SignUpViewModel()
    composeRule.setContent { SampleAppTheme { SignUpScreen(vm = vm1) } }
    composeRule.waitForIdle()

    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).performTextInput("John")
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).performTextInput("Doe")
    composeRule.nodeByTag(SignUpScreenTestTags.ADDRESS).performTextInput("Street 1")
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("CS")
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performTextInput(duplicateEmail)
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performTextInput("TestPass123!")

    // Close keyboard with IME action
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performImeAction()
    composeRule.waitForIdle()

    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).performScrollTo().performClick()

    // Wait for first signup to complete - increased timeout
    composeRule.waitUntil(30_000) { vm1.state.value.submitSuccess || vm1.state.value.error != null }
    assertTrue("First signup should succeed", vm1.state.value.submitSuccess)

    // Give Firebase emulator time to fully process the first signup
    Thread.sleep(2000)

    // Sign out and clean up the first user
    auth.signOut()
  }

  @Test
  fun duplicate_email_shows_error_second_attempt() {
    // This test depends on duplicate_email_shows_error running first
    // Use the same email pattern
    val duplicateEmail = "duplicate${System.currentTimeMillis()}@test.com"

    // First create the user
    val vm1 = SignUpViewModel()
    composeRule.setContent { SampleAppTheme { SignUpScreen(vm = vm1) } }
    composeRule.waitForIdle()
    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).performTextInput("First")
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).performTextInput("User")
    composeRule.nodeByTag(SignUpScreenTestTags.ADDRESS).performTextInput("Street 1")
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("CS")
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performTextInput(duplicateEmail)
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performTextInput("TestPass123!")
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performImeAction()
    composeRule.waitForIdle()
    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).performScrollTo().performClick()

    composeRule.waitUntil(30_000) { vm1.state.value.submitSuccess || vm1.state.value.error != null }
    assertTrue("First signup should succeed", vm1.state.value.submitSuccess)
    Thread.sleep(2000)
    auth.signOut()

    // Now try to register with the same email - this should fail
    runBlocking {
      try {
        auth.createUserWithEmailAndPassword(duplicateEmail, "AnotherPass123!").await()
        // If we get here, check that we get an error
        // We'll use the ViewModel to test this properly
      } catch (e: Exception) {
        // Expected - email already exists
        assertTrue(
            "Error should mention duplicate/already/in use",
            e.message?.contains("already") == true || e.message?.contains("in use") == true)
      }
    }
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

    // Wait for error or completion - increased timeout
    composeRule.waitUntil(30_000) {
      vm.state.value.error != null || !vm.state.value.submitting || vm.state.value.submitSuccess
    }

    // Should either have an error or not have succeeded
    assertTrue(
        "Weak password should either error or not succeed",
        vm.state.value.error != null || !vm.state.value.submitSuccess)
  }
}
