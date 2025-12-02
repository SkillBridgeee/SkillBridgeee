package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// ---------- helpers ----------
private const val DEFAULT_TIMEOUT_MS = 15_000L

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
    try {
      Firebase.auth.useEmulator("10.0.2.2", 9099)
    } catch (_: IllegalStateException) {}

    auth = Firebase.auth
    ProfileRepositoryProvider.setForTests(FakeProfileRepository())

    auth.signOut()
    composeRule.waitUntil(2_000) { auth.currentUser == null }
  }

  @After
  fun tearDown() {
    try {
      auth.currentUser?.delete()
    } catch (_: Exception) {}
    auth.signOut()
  }

  @Test
  fun all_fields_render() {
    val vm = SignUpViewModel()
    composeRule.setContent { SampleAppTheme { SignUpScreen(vm = vm, onNavigateToToS = {}) } }

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
    composeRule.setContent { SampleAppTheme { SignUpScreen(vm = vm, onNavigateToToS = {}) } }

    composeRule.waitForIdle()
    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    val testEmail = "test${System.currentTimeMillis()}@example.com"

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).performTextInput("Ada")
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).performTextInput("Lovelace")
    composeRule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, true)
        .performTextInput("London Street 1")
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("CS, 3rd year")
    composeRule.nodeByTag(SignUpScreenTestTags.DESCRIPTION).performTextInput("Loves mathematics")
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performTextInput(testEmail)
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performTextInput("TestPass123!")

    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performImeAction()
    composeRule.waitForIdle()

    // *** NEW: accept ToS ***
    composeRule.nodeByTag(SignUpScreenTestTags.TOS_CHECKBOX).performScrollTo().performClick()

    composeRule
        .nodeByTag(SignUpScreenTestTags.SIGN_UP)
        .assertIsEnabled()
        .performScrollTo()
        .performClick()

    composeRule.waitUntil(DEFAULT_TIMEOUT_MS) {
      vm.state.value.verificationEmailSent || vm.state.value.error != null
    }

    assertTrue(vm.state.value.verificationEmailSent)
  }

  @Test
  fun uppercase_email_is_accepted_and_trimmed() {
    val vm = SignUpViewModel()
    composeRule.setContent { SampleAppTheme { SignUpScreen(vm = vm, onNavigateToToS = {}) } }

    composeRule.waitForIdle()
    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    val testEmail = "TEST${System.currentTimeMillis()}@MAIL.Example.ORG"

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).performTextInput("Élise")
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).performTextInput("Müller")
    composeRule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, true)
        .performTextInput("S1")
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("CS")
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performTextInput("  $testEmail ")
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performTextInput("passw0rd!")

    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performImeAction()

    // *** NEW: accept ToS ***
    composeRule.nodeByTag(SignUpScreenTestTags.TOS_CHECKBOX).performScrollTo().performClick()

    composeRule
        .nodeByTag(SignUpScreenTestTags.SIGN_UP)
        .assertIsEnabled()
        .performScrollTo()
        .performClick()

    composeRule.waitUntil(DEFAULT_TIMEOUT_MS) {
      vm.state.value.verificationEmailSent || vm.state.value.error != null
    }

    assertTrue(vm.state.value.verificationEmailSent)
  }

  @Test
  fun duplicate_email_shows_error() {
    val duplicateEmail = "duplicate${System.currentTimeMillis()}@test.com"

    runBlocking {
      val created = createUserProgrammatically(auth, duplicateEmail, "FirstPass123!")
      assertTrue(created)
      composeRule.waitUntil(10_000) { auth.currentUser != null }
      auth.signOut()
    }
    composeRule.waitUntil(3_000) { auth.currentUser == null }

    val vm = SignUpViewModel()
    composeRule.setContent { SampleAppTheme { SignUpScreen(vm = vm, onNavigateToToS = {}) } }
    composeRule.waitForIdle()
    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).performTextInput("John")
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).performTextInput("Doe")
    composeRule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, true)
        .performTextInput("Street 1")
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("CS")
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performTextInput(duplicateEmail)
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performTextInput("SecondPass123!")

    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performImeAction()

    // *** NEW: accept ToS ***
    composeRule.nodeByTag(SignUpScreenTestTags.TOS_CHECKBOX).performScrollTo().performClick()

    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).performScrollTo().performClick()

    composeRule.waitUntil(DEFAULT_TIMEOUT_MS) { vm.state.value.error != null }

    assertTrue(vm.state.value.error != null)
  }

  @Test
  fun weak_password_shows_error() {
    val vm = SignUpViewModel()
    composeRule.setContent { SampleAppTheme { SignUpScreen(vm = vm, onNavigateToToS = {}) } }
    composeRule.waitForIdle()
    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    val testEmail = "weakpass${System.currentTimeMillis()}@test.com"

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).performTextInput("Test")
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).performTextInput("User")
    composeRule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, true)
        .performTextInput("Street 1")
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("CS")
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performTextInput(testEmail)
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performTextInput("123!")

    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performImeAction()

    assertTrue(!vm.state.value.canSubmit)
  }

  @Test
  fun verification_email_sent_message_displays() {
    val vm = SignUpViewModel()
    composeRule.setContent { SampleAppTheme { SignUpScreen(vm = vm, onNavigateToToS = {}) } }
    composeRule.waitForIdle()
    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    val testEmail = "verify${System.currentTimeMillis()}@test.com"

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).performTextInput("Test")
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).performTextInput("User")
    composeRule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, true)
        .performTextInput("Street 1")
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("CS")
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performTextInput(testEmail)
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performTextInput("ValidPass123!")

    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performImeAction()

    // *** NEW: accept ToS ***
    composeRule.nodeByTag(SignUpScreenTestTags.TOS_CHECKBOX).performScrollTo().performClick()

    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).performScrollTo().performClick()

    composeRule.waitUntil(DEFAULT_TIMEOUT_MS) {
      vm.state.value.verificationEmailSent || vm.state.value.error != null
    }

    assertTrue(vm.state.value.verificationEmailSent)

    composeRule.onNodeWithText("✓ Verification Email Sent!", true).assertExists()
    composeRule
        .onNodeWithText(
            "Please check your inbox at $testEmail", substring = true, useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun error_message_displays_on_signup_failure() {
    val duplicateEmail = "error${System.currentTimeMillis()}@test.com"

    runBlocking {
      createUserProgrammatically(auth, duplicateEmail, "FirstPass123!")
      composeRule.waitUntil(10_000) { auth.currentUser != null }
      auth.signOut()
    }
    composeRule.waitUntil(3_000) { auth.currentUser == null }

    val vm = SignUpViewModel()
    composeRule.setContent { SampleAppTheme { SignUpScreen(vm = vm, onNavigateToToS = {}) } }
    composeRule.waitForIdle()
    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).performTextInput("John")
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).performTextInput("Doe")
    composeRule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, true)
        .performTextInput("Street 1")
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("CS")
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performTextInput(duplicateEmail)
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performTextInput("SecondPass123!")

    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performImeAction()

    // *** NEW: accept ToS ***
    composeRule.nodeByTag(SignUpScreenTestTags.TOS_CHECKBOX).performScrollTo().performClick()

    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).performScrollTo().performClick()

    composeRule.waitUntil(DEFAULT_TIMEOUT_MS) { vm.state.value.error != null }

    val errorMsg = vm.state.value.error!!
    composeRule.onNodeWithText(errorMsg, true).assertExists()
  }
}
