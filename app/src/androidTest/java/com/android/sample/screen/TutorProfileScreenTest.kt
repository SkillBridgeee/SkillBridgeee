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
  private class ImmediateRepo(
      private val profile: Profile,
      private val skills: List<Skill>,
  ) : ProfileRepository {
    override suspend fun getProfileById(userId: String): Profile = profile

    override suspend fun getSkillsForUser(userId: String): List<Skill> = skills

    override fun getNewUid(): String {
      TODO("Not yet implemented")
    }

    override suspend fun getProfile(userId: String): Profile {
      TODO("Not yet implemented")
    }

    // No-ops to satisfy the interface (if your interface includes writes)
    override suspend fun addProfile(profile: Profile) {
      /* no-op */
    }

    override suspend fun updateProfile(userId: String, profile: Profile) {
      TODO("Not yet implemented")
    }

    override suspend fun deleteProfile(userId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun getAllProfiles(): List<Profile> {
      TODO("Not yet implemented")
    }

    override suspend fun searchProfilesByLocation(
        location: Location,
        radiusKm: Double
    ): List<Profile> {
      TODO("Not yet implemented")
    }
  }

  private fun launch() {
    val vm = TutorProfileViewModel(ImmediateRepo(sampleProfile, sampleSkills))
    compose.setContent {
      val navController = rememberNavController()
      TutorProfileScreen(tutorId = "demo", vm = vm, navController = navController)
    }
    // Wait until the VM finishes its initial load and the NAME node appears
    compose.waitUntil(timeoutMillis = 5_000) {
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

  @Test
  fun top_bar_isDisplayed() {
    launch()
    compose.onNodeWithTag(TutorPageTestTags.TOP_BAR, useUnmergedTree = true).assertIsDisplayed()
  }
}
