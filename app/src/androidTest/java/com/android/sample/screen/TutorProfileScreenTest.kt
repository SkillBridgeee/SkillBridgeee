package com.android.sample.screen

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.navigation.compose.rememberNavController
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.tutor.TutorPageTestTags
import com.android.sample.ui.tutor.TutorProfileScreen
import com.android.sample.ui.tutor.TutorProfileViewModel
import org.junit.Rule
import org.junit.Test

class TutorProfileScreenTest {

  @get:Rule val compose = createComposeRule()

  private val sampleProfile =
      Profile(
          userId = "demo",
          name = "Kendrick Lamar",
          email = "kendrick@gmail.com",
          description = "Performer and mentor",
          tutorRating = RatingInfo(averageRating = 5.0, totalRatings = 23),
          studentRating = RatingInfo(averageRating = 4.9, totalRatings = 12),
      )

  private val sampleSkills =
      listOf(
          Skill("demo", MainSubject.MUSIC, "SINGING", 10.0, ExpertiseLevel.EXPERT),
          Skill("demo", MainSubject.MUSIC, "DANCING", 5.0, ExpertiseLevel.INTERMEDIATE),
          Skill("demo", MainSubject.MUSIC, "GUITAR", 7.0, ExpertiseLevel.BEGINNER),
      )

  /** Test double that satisfies the full TutorRepository contract. */
  // inside TutorProfileScreenTest
  private class ImmediateRepo(sampleProfile: Profile, sampleSkills: List<Skill>) :
      ProfileRepository {
    private val profiles = mutableMapOf<String, Profile>()

    fun seed(profile: Profile) {
      profiles[profile.userId] = profile
    }

    override fun getNewUid(): String = "fake"

    override suspend fun getProfile(userId: String): Profile =
        profiles[userId] ?: error("No profile $userId")

    override suspend fun getProfileById(userId: String): Profile = getProfile(userId)

    override suspend fun addProfile(profile: Profile) {
      profiles[profile.userId] = profile
    }

    override suspend fun updateProfile(userId: String, profile: Profile) {
      profiles[userId] = profile
    }

    override suspend fun deleteProfile(userId: String) {
      profiles.remove(userId)
    }

    override suspend fun getAllProfiles(): List<Profile> = profiles.values.toList()

    override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
        emptyList<Profile>()

    override suspend fun getSkillsForUser(userId: String) = emptyList<Skill>()
  }

  private fun launch() {
    val repo =
        ImmediateRepo(sampleProfile, sampleSkills).apply {
          seed(sampleProfile) // <-- ensure "demo" is present
        }
    val vm = TutorProfileViewModel(repo)
    compose.setContent {
      val nav = rememberNavController()
      TutorProfileScreen(tutorId = "demo", vm = vm, navController = nav)
    }
    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(TutorPageTestTags.NAME, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  @Test
  fun core_elements_areDisplayed() {
    launch()
    compose.onNodeWithTag(TutorPageTestTags.PFP).assertIsDisplayed()
    compose.onNodeWithTag(TutorPageTestTags.NAME).assertIsDisplayed()
    compose.onNodeWithTag(TutorPageTestTags.RATING).assertIsDisplayed()
    compose.onNodeWithTag(TutorPageTestTags.SKILLS_SECTION).assertIsDisplayed()
    compose.onNodeWithTag(TutorPageTestTags.CONTACT_SECTION).assertIsDisplayed()
  }

  @Test
  fun name_and_ratingCount_areCorrect() {
    launch()
    compose.onNodeWithTag(TutorPageTestTags.NAME).assertTextContains("Kendrick Lamar")
    compose.onNodeWithText("(23)").assertIsDisplayed()
  }

  @Test
  fun skills_render_all_items() {
    launch()
    compose
        .onAllNodesWithTag(TutorPageTestTags.SKILL, useUnmergedTree = true)
        .assertCountEquals(sampleSkills.size)
  }

  @Test
  fun contact_section_shows_email_and_handle() {
    launch()

    // Scroll the LazyColumn so the contact section becomes visible
    compose
        .onNode(hasScrollAction())
        .performScrollToNode(hasTestTag(TutorPageTestTags.CONTACT_SECTION))

    // Now assert visibility and text content
    compose
        .onNodeWithTag(TutorPageTestTags.CONTACT_SECTION, useUnmergedTree = true)
        .assertIsDisplayed()
    compose.onNodeWithText("kendrick@gmail.com").assertIsDisplayed()
    compose.onNodeWithText("@KendrickLamar").assertIsDisplayed()
  }
}
