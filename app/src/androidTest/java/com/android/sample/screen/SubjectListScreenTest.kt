package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.subject.SubjectListScreen
import com.android.sample.ui.subject.SubjectListTestTags
import com.android.sample.ui.subject.SubjectListViewModel
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubjectListScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  /** ---- Fake data + repo ------------------------------------------------ */
  private val p1 = profile("1", "Liam P.", "Guitar Lessons", 4.9, 23)
  private val p2 = profile("2", "David B.", "Sing Lessons", 4.8, 12)
  private val p3 = profile("3", "Stevie W.", "Piano Lessons", 4.7, 15)
  private val p4 = profile("4", "Nora Q.", "Violin Lessons", 4.5, 8)
  private val p5 = profile("5", "Maya R.", "Drum Lessons", 4.2, 5)

  // Simple skills so category filtering can work if we need it later
  private val allSkills =
      mapOf(
          "1" to listOf(skill("GUITARE")),
          "2" to listOf(skill("SING")),
          "3" to listOf(skill("PIANO")),
          "4" to listOf(skill("VIOLIN")),
          "5" to listOf(skill("DRUMS")),
      )

  private fun makeViewModel(): SubjectListViewModel {
    val repo =
        object : ProfileRepository {
          override fun getNewUid(): String {
            TODO("Not yet implemented")
          }

          override suspend fun getProfile(userId: String): Profile {
            TODO("Not yet implemented")
          }

          override suspend fun addProfile(profile: Profile) {
            TODO("Not yet implemented")
          }

          override suspend fun updateProfile(userId: String, profile: Profile) {
            TODO("Not yet implemented")
          }

          override suspend fun deleteProfile(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun getAllProfiles(): List<Profile> {
            // deterministic order; top 3 by rating should be p1,p2,p3
            delay(10) // small async to exercise loading state
            return listOf(p1, p2, p3, p4, p5)
          }

          override suspend fun searchProfilesByLocation(
              location: Location,
              radiusKm: Double
          ): List<Profile> {
            TODO("Not yet implemented")
          }

          override suspend fun getProfileById(userId: String): Profile {
            TODO("Not yet implemented")
          }

          override suspend fun getSkillsForUser(userId: String): List<Skill> {
            return allSkills[userId].orEmpty()
          }
        }
    // pick 3 for "top" section, like production
    return SubjectListViewModel(repository = repo, tutorsPerTopSection = 3)
  }

  /** ---- Helpers --------------------------------------------------------- */
  private fun profile(id: String, name: String, description: String, rating: Double, total: Int) =
      Profile(
          userId = id,
          name = name,
          description = description,
          tutorRating = RatingInfo(averageRating = rating, totalRatings = total))

  private fun skill(s: String) = Skill(userId = "", mainSubject = MainSubject.MUSIC, skill = s)

  private fun setContent(onBook: (Profile) -> Unit = {}) {
    val vm = makeViewModel()
    composeRule.setContent { MaterialTheme { SubjectListScreen(vm, onBook) } }

    // Wait until top section appears
    composeRule.waitUntil(5_000) {
      composeRule
          .onAllNodesWithTag(SubjectListTestTags.TOP_TUTORS_SECTION)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    // THEN wait until the main list has items (non-top tutors)
    composeRule.waitUntil(5_000) {
      composeRule
          .onAllNodes(
              hasTestTag(SubjectListTestTags.TUTOR_CARD) and
                  hasAnyAncestor(hasTestTag(SubjectListTestTags.TUTOR_LIST)))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  /** ---- Tests ----------------------------------------------------------- */
  @Test
  fun showsSearchbarAndCategorySelector() {
    setContent()

    composeRule.onNodeWithTag(SubjectListTestTags.SEARCHBAR).assertIsDisplayed()
    composeRule.onNodeWithTag(SubjectListTestTags.CATEGORY_SELECTOR).assertIsDisplayed()
  }

  @Test
  fun rendersTopTutorsSection_andTutorCards() {
    setContent()

    composeRule.onNodeWithTag(SubjectListTestTags.TOP_TUTORS_SECTION).assertIsDisplayed()

    // Only the cards inside the Top Tutors section
    composeRule
        .onAllNodes(
            hasTestTag(SubjectListTestTags.TUTOR_CARD) and
                hasAnyAncestor(hasTestTag(SubjectListTestTags.TOP_TUTORS_SECTION)))
        .assertCountEquals(3)
  }

  @Test
  fun rendersTutorList_excludingTopTutors() {
    setContent()

    composeRule.onNodeWithTag(SubjectListTestTags.TUTOR_LIST).assertExists()

    composeRule
        .onNodeWithTag(SubjectListTestTags.TUTOR_LIST)
        .performScrollToNode(hasText("Nora Q."))
    composeRule.onNodeWithText("Nora Q.").assertIsDisplayed()

    composeRule
        .onNodeWithTag(SubjectListTestTags.TUTOR_LIST)
        .performScrollToNode(hasText("Maya R."))
    composeRule.onNodeWithText("Maya R.").assertIsDisplayed()
  }

  @Test
  fun clickingBook_callsCallback() {
    val clicked = AtomicBoolean(false)
    setContent(onBook = { clicked.set(true) })

    // First "Book" in the Top section
    composeRule.onAllNodesWithTag(SubjectListTestTags.TUTOR_BOOK_BUTTON).onFirst().performClick()

    assert(clicked.get())
  }

  @Test
  fun searchFiltersList_visually() {
    setContent()

    composeRule.onNodeWithTag(SubjectListTestTags.SEARCHBAR).performTextInput("Nora")

    // Wait until filtered result appears
    composeRule.waitUntil(3_000) {
      composeRule.onAllNodes(hasText("Nora Q.")).fetchSemanticsNodes().isNotEmpty()
    }

    // Only one tutor card remains in the main list
    composeRule
        .onAllNodes(
            hasTestTag(SubjectListTestTags.TUTOR_CARD) and
                hasAnyAncestor(hasTestTag(SubjectListTestTags.TUTOR_LIST)))
        .assertCountEquals(1)

    // “Maya R.” no longer exists in the main list subtree
    composeRule
        .onAllNodes(
            hasText("Maya R.") and hasAnyAncestor(hasTestTag(SubjectListTestTags.TUTOR_LIST)))
        .assertCountEquals(0)
  }

  @Test
  fun showsLoading_thenContent() {
    // During first few ms the LinearProgressIndicator may be visible.
    // We assert that ultimately the content shows and no error.
    setContent()

    composeRule.onNodeWithTag(SubjectListTestTags.TOP_TUTORS_SECTION).assertIsDisplayed()
    composeRule.onNodeWithText("Unknown error").assertDoesNotExist()
  }
}
