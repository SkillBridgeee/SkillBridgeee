package com.android.sample.ui.signup

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasText
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

class SignUpScreenTest {

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun all_fields_render_and_role_toggle() {
    val vm = SignUpViewModel(UiRepo())
    composeRule.setContent { SignUpScreen(vm = vm) }

    // headers
    composeRule.onNodeWithTag(SignUpScreenTestTags.TITLE).assertIsDisplayed()
    composeRule.onNodeWithTag(SignUpScreenTestTags.SUBTITLE).assertIsDisplayed()

    // inputs exist
    composeRule.onNodeWithTag(SignUpScreenTestTags.NAME).assertIsDisplayed()
    composeRule.onNodeWithTag(SignUpScreenTestTags.SURNAME).assertIsDisplayed()
    composeRule.onNodeWithTag(SignUpScreenTestTags.ADDRESS).assertIsDisplayed()
    composeRule.onNodeWithTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).assertIsDisplayed()
    composeRule.onNodeWithTag(SignUpScreenTestTags.DESCRIPTION).assertIsDisplayed()
    composeRule.onNodeWithTag(SignUpScreenTestTags.EMAIL).assertIsDisplayed()
    composeRule.onNodeWithTag(SignUpScreenTestTags.PASSWORD).assertIsDisplayed()

    // role toggles
    composeRule.onNodeWithTag(SignUpScreenTestTags.TUTOR).performClick()
    assertEquals(Role.TUTOR, vm.state.value.role)
    composeRule.onNodeWithTag(SignUpScreenTestTags.LEARNER).performClick()
    assertEquals(Role.LEARNER, vm.state.value.role)
  }

  @Test
  fun button_shows_submitting_text_during_long_operation() {
    val vm = SignUpViewModel(SlowRepoUi())
    composeRule.setContent { SignUpScreen(vm = vm) }

    // fill valid
    composeRule.onNodeWithTag(SignUpScreenTestTags.NAME).performTextInput("Alan")
    composeRule.onNodeWithTag(SignUpScreenTestTags.SURNAME).performTextInput("Turing")
    composeRule.onNodeWithTag(SignUpScreenTestTags.ADDRESS).performTextInput("S2")
    composeRule.onNodeWithTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("Math")
    composeRule.onNodeWithTag(SignUpScreenTestTags.EMAIL).performTextInput("alan@code.org")
    composeRule.onNodeWithTag(SignUpScreenTestTags.PASSWORD).performTextInput("abcdef12")

    // click and verify "Submitting…" appears
    composeRule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).performClick()
    composeRule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).assert(hasText("Submitting…"))

    // wait until done; then label returns to "Sign Up"
    composeRule.waitUntil(3_000) { vm.state.value.submitSuccess }
    composeRule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).assert(hasText("Sign Up"))
  }

  @Test
  fun failing_submit_reenables_button_and_sets_error() {
    val vm = SignUpViewModel(SlowFailRepo())
    composeRule.setContent { SignUpScreen(vm = vm) }

    composeRule.onNodeWithTag(SignUpScreenTestTags.NAME).performTextInput("Alan")
    composeRule.onNodeWithTag(SignUpScreenTestTags.SURNAME).performTextInput("Turing")
    composeRule.onNodeWithTag(SignUpScreenTestTags.ADDRESS).performTextInput("Street 2")
    composeRule.onNodeWithTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("Math")
    composeRule.onNodeWithTag(SignUpScreenTestTags.EMAIL).performTextInput("alan@code.org")
    composeRule.onNodeWithTag(SignUpScreenTestTags.PASSWORD).performTextInput("abcdef12")

    composeRule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).assertIsEnabled()
    composeRule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).performClick()

    composeRule.waitUntil(3_000) { !vm.state.value.submitting }
    assertNotNull(vm.state.value.error)
    composeRule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).assertIsEnabled()
  }

  @Test
  fun uppercase_email_is_accepted_and_trimmed() {
    val repo = UiRepo()
    val vm = SignUpViewModel(repo)
    composeRule.setContent { SignUpScreen(vm = vm) }

    composeRule.onNodeWithTag(SignUpScreenTestTags.NAME).performTextInput("Élise")
    composeRule.onNodeWithTag(SignUpScreenTestTags.SURNAME).performTextInput("Müller")
    composeRule.onNodeWithTag(SignUpScreenTestTags.ADDRESS).performTextInput("S1")
    composeRule.onNodeWithTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performTextInput("CS")
    composeRule
        .onNodeWithTag(SignUpScreenTestTags.EMAIL)
        .performTextInput("  USER@MAIL.Example.ORG ")
    composeRule.onNodeWithTag(SignUpScreenTestTags.PASSWORD).performTextInput("passw0rd")

    composeRule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).assertIsEnabled()
    composeRule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).performClick()
    composeRule.waitUntil(3_000) { vm.state.value.submitSuccess }
    assertEquals(1, repo.added.size)
    assertEquals("Élise Müller", repo.added[0].name)
  }
}
