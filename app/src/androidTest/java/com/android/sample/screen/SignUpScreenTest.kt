package com.android.sample.ui.signup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

// ---------- helpers ----------
private fun waitForTag(rule: ComposeContentTestRule, tag: String, timeoutMs: Long = 5_000) {
  rule.waitUntil(timeoutMs) {
    rule.onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
  }
}

private fun ComposeContentTestRule.nodeByTag(tag: String) =
    onNodeWithTag(tag, useUnmergedTree = true)

// ---------- fakes ----------
private class UiRepo : ProfileRepository {
  val added = mutableListOf<Profile>()
  private var uid = 1

  override fun getNewUid(): String = "ui-$uid".also { uid++ }

  override suspend fun getProfile(userId: String): Profile = added.first { it.userId == userId }

  override suspend fun addProfile(profile: Profile) {
    added += profile
  }

  override suspend fun updateProfile(userId: String, profile: Profile) {}

  override suspend fun deleteProfile(userId: String) {}

  override suspend fun getAllProfiles(): List<Profile> = added

  override suspend fun searchProfilesByLocation(
      location: com.android.sample.model.map.Location,
      radiusKm: Double
  ): List<Profile> = emptyList()
}

private class SlowRepoUi : ProfileRepository {
  override fun getNewUid(): String = "slow"

  override suspend fun getProfile(userId: String): Profile = error("unused")

  override suspend fun addProfile(profile: Profile) {
    delay(250)
  }

  override suspend fun updateProfile(userId: String, profile: Profile) {}

  override suspend fun deleteProfile(userId: String) {}

  override suspend fun getAllProfiles(): List<Profile> = emptyList()

  override suspend fun searchProfilesByLocation(
      location: com.android.sample.model.map.Location,
      radiusKm: Double
  ): List<Profile> = emptyList()
}

private class SlowFailRepo : ProfileRepository {
  override fun getNewUid(): String = "bad"

  override suspend fun getProfile(userId: String): Profile = error("unused")

  override suspend fun addProfile(profile: Profile) {
    delay(120)
    error("nope")
  }

  override suspend fun updateProfile(userId: String, profile: Profile) {}

  override suspend fun deleteProfile(userId: String) {}

  override suspend fun getAllProfiles(): List<Profile> = emptyList()

  override suspend fun searchProfilesByLocation(
      location: com.android.sample.model.map.Location,
      radiusKm: Double
  ): List<Profile> = emptyList()
}

// ---------- tests ----------
class SignUpScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun all_fields_render_and_role_toggle() {
    val vm = SignUpViewModel(UiRepo())
    composeRule.setContent { SignUpScreen(vm = vm) }

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
  fun failing_submit_reenables_button_and_sets_error() {
    val vm = SignUpViewModel(SlowFailRepo())
    composeRule.setContent { SignUpScreen(vm = vm) }

    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).performTextInput("Alan")
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).performTextInput("Turing")
    composeRule.nodeByTag(SignUpScreenTestTags.ADDRESS).performTextInput("Street 2")
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("Math")
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performTextInput("alan@code.org")
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performTextInput("abcdef12")

    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).assertIsEnabled()
    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).performClick()

    composeRule.waitUntil(7_000) { !vm.state.value.submitting && vm.state.value.error != null }
    assertNotNull(vm.state.value.error)
    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).assertIsEnabled()
  }

  @Test
  fun uppercase_email_is_accepted_and_trimmed() {
    val repo = UiRepo()
    val vm = SignUpViewModel(repo)
    composeRule.setContent { SignUpScreen(vm = vm) }

    waitForTag(composeRule, SignUpScreenTestTags.NAME)

    composeRule.nodeByTag(SignUpScreenTestTags.NAME).performTextInput("Élise")
    composeRule.nodeByTag(SignUpScreenTestTags.SURNAME).performTextInput("Müller")
    composeRule.nodeByTag(SignUpScreenTestTags.ADDRESS).performTextInput("S1")
    composeRule.nodeByTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("CS")
    composeRule.nodeByTag(SignUpScreenTestTags.EMAIL).performTextInput("  USER@MAIL.Example.ORG ")
    composeRule.nodeByTag(SignUpScreenTestTags.PASSWORD).performTextInput("passw0rd")

    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).assertIsEnabled()
    composeRule.nodeByTag(SignUpScreenTestTags.SIGN_UP).performClick()

    composeRule.waitUntil(7_000) { vm.state.value.submitSuccess }
    assertEquals(1, repo.added.size)
    assertEquals("Élise Müller", repo.added[0].name)
  }
}
