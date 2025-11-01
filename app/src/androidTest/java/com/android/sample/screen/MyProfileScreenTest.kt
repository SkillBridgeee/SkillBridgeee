package com.android.sample.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performTextInput
import com.android.sample.model.map.Location
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.components.LocationInputFieldTestTags
import com.android.sample.ui.profile.MyProfileScreen
import com.android.sample.ui.profile.MyProfileScreenTestTag
import com.android.sample.ui.profile.MyProfileViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MyProfileScreenTest {

  @get:Rule val compose = createComposeRule()

  private val sampleProfile =
      Profile(
          userId = "demo",
          name = "Kendrick Lamar",
          email = "kendrick@gmail.com",
          description = "Performer and mentor",
          location = Location(name = "EPFL", longitude = 0.0, latitude = 0.0))

  private val sampleSkills =
      listOf(
          Skill(MainSubject.MUSIC, "SINGING", 10.0, ExpertiseLevel.EXPERT),
          Skill(MainSubject.MUSIC, "DANCING", 5.0, ExpertiseLevel.INTERMEDIATE),
          Skill(MainSubject.MUSIC, "GUITAR", 7.0, ExpertiseLevel.BEGINNER),
      )

  /** Fake repository for testing ViewModel logic */
  private class FakeRepo() : ProfileRepository {

    private val profiles = mutableMapOf<String, Profile>()
    private val skillsByUser = mutableMapOf<String, List<Skill>>()

    fun seed(profile: Profile, skills: List<Skill>) {
      profiles[profile.userId] = profile
      skillsByUser[profile.userId] = skills
    }

    override fun getNewUid() = "fake"

    override suspend fun getProfile(userId: String): Profile =
        profiles[userId] ?: error("No profile $userId")

    override suspend fun getProfileById(userId: String) = getProfile(userId)

    override suspend fun addProfile(profile: Profile) {
      profiles[profile.userId] = profile
    }

    override suspend fun updateProfile(userId: String, profile: Profile) {
      profiles[userId] = profile
    }

    override suspend fun deleteProfile(userId: String) {
      profiles.remove(userId)
      skillsByUser.remove(userId)
    }

    override suspend fun getAllProfiles(): List<Profile> = profiles.values.toList()

    override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
        emptyList<Profile>()

    override suspend fun getSkillsForUser(userId: String): List<Skill> =
        skillsByUser[userId] ?: emptyList()
  }

  private lateinit var viewModel: MyProfileViewModel

  @Before
  fun setup() {
    val repo = FakeRepo().apply { seed(sampleProfile, sampleSkills) }
    viewModel = MyProfileViewModel(repo, userId = "demo")

    compose.setContent { MyProfileScreen(profileViewModel = viewModel, profileId = "demo") }

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(MyProfileScreenTestTag.NAME_DISPLAY, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  // --- TESTS ---

  @Test
  fun profileInfo_isDisplayedCorrectly() {
    compose.onNodeWithTag(MyProfileScreenTestTag.PROFILE_ICON).assertIsDisplayed()
    compose
        .onNodeWithTag(MyProfileScreenTestTag.NAME_DISPLAY)
        .assertIsDisplayed()
        .assertTextContains("Kendrick Lamar")
    compose.onNodeWithTag(MyProfileScreenTestTag.ROLE_BADGE).assertTextEquals("Student")
  }

  // ----------------------------------------------------------
  // NAME FIELD TESTS
  // ----------------------------------------------------------
  @Test
  fun nameField_displaysCorrectInitialValue() {
    compose
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME)
        .assertTextContains("Kendrick Lamar")
  }

  @Test
  fun nameField_canBeEdited() {
    val newName = "K Dot"
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).performTextClearance()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).performTextInput(newName)
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).assertTextContains(newName)
  }

  @Test
  fun nameField_showsError_whenEmpty() {
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).performTextClearance()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).performTextInput("")
    compose
        .onNodeWithTag(MyProfileScreenTestTag.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }
  // ----------------------------------------------------------
  // EMAIL FIELD TESTS
  // ----------------------------------------------------------
  @Test
  fun emailField_displaysCorrectInitialValue() {
    compose
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL)
        .assertTextContains("kendrick@gmail.com")
  }

  @Test
  fun emailField_canBeEdited() {
    val newEmail = "kdot@gmail.com"
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL).performTextClearance()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL).performTextInput(newEmail)
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL).assertTextContains(newEmail)
  }

  @Test
  fun emailField_showsError_whenInvalid() {
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL).performTextClearance()
    compose
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL)
        .performTextInput("invalidEmail")
    compose
        .onNodeWithTag(MyProfileScreenTestTag.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  // ----------------------------------------------------------
  // LOCATION FIELD TESTS
  // ----------------------------------------------------------
  @Test
  fun locationField_displaysCorrectInitialValue() {
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).assertTextContains("EPFL")
  }

  @Test
  fun locationField_canBeEdited() {
    val newLocation = "Harvard University"
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextClearance()
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextInput(newLocation)
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).assertTextContains(newLocation)
  }

  @Test
  fun locationField_showsError_whenEmpty() {
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextClearance()
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextInput(" ")
    compose
        .onNodeWithTag(LocationInputFieldTestTags.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  // ----------------------------------------------------------
  // DESCRIPTION FIELD TESTS
  // ----------------------------------------------------------
  @Test
  fun descriptionField_displaysCorrectInitialValue() {
    compose
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC)
        .assertTextContains("Performer and mentor")
  }

  @Test
  fun descriptionField_canBeEdited() {
    val newDesc = "Artist and teacher"
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).performTextClearance()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).performTextInput(newDesc)
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).assertTextContains(newDesc)
  }

  @Test
  fun descriptionField_showsError_whenEmpty() {
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).performTextClearance()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).performTextInput("")
    compose
        .onNodeWithTag(MyProfileScreenTestTag.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  // ----------------------------------------------------------
  // LOGOUT BUTTON TESTS
  // ----------------------------------------------------------
  @Test
  fun logoutButton_isDisplayed() {
    compose.onNodeWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun logoutButton_isClickable() {
    compose.onNodeWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON).assertHasClickAction()
  }

  @Test
  fun logoutButton_hasCorrectText() {
    compose.onNodeWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON).assertTextContains("Logout")
  }
}
