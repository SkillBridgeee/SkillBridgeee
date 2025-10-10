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
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Tutor
import com.android.sample.ui.tutor.TutorPageTestTags
import com.android.sample.ui.tutor.TutorProfileScreen
import com.android.sample.ui.tutor.TutorProfileViewModel
import com.android.sample.ui.tutor.TutorRepository
import org.junit.Rule
import org.junit.Test

class TutorProfileScreenTest {

  @get:Rule val compose = createComposeRule()

  private val sampleTutor =
      Tutor(
          userId = "demo",
          name = "Kendrick Lamar",
          email = "kendrick@gmail.com",
          description = "Performer and mentor",
          skills =
              listOf(
                  Skill("demo", MainSubject.MUSIC, "SINGING", 10.0, ExpertiseLevel.EXPERT),
                  Skill("demo", MainSubject.MUSIC, "DANCING", 5.0, ExpertiseLevel.INTERMEDIATE),
                  Skill("demo", MainSubject.MUSIC, "GUITAR", 7.0, ExpertiseLevel.BEGINNER)),
          starRating = 5.0,
          ratingNumber = 23)

  private class ImmediateRepo(private val t: Tutor) : TutorRepository {
    override suspend fun getTutorById(id: String): Tutor = t
  }

  private fun launch() {
    val vm = TutorProfileViewModel(ImmediateRepo(sampleTutor))
    compose.setContent {
      val navController = rememberNavController()
      TutorProfileScreen(tutorId = "demo", vm = vm, navController = navController)
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
        .assertCountEquals(sampleTutor.skills.size)
  }

  @Test
  fun contact_section_shows_email_and_handle() {
    launch()

    // Wait for Compose to finish any recompositions or loading
    compose.waitForIdle()

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
