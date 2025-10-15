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

// Ai generated tests for the SubjectListScreen composable
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
          override fun getNewUid(): String = "unused"

          override suspend fun getProfile(userId: String): Profile = error("unused")

          override suspend fun addProfile(profile: Profile) {}

          override suspend fun updateProfile(userId: String, profile: Profile) {}

          override suspend fun deleteProfile(userId: String) {}

          override suspend fun getAllProfiles(): List<Profile> {
            // small async to exercise loading state
            delay(10)
            return listOf(p1, p2, p3, p4, p5)
          }

          override suspend fun searchProfilesByLocation(
              location: Location,
              radiusKm: Double
          ): List<Profile> = emptyList()

          override suspend fun getProfileById(userId: String): Profile = error("unused")

          override suspend fun getSkillsForUser(userId: String): List<Skill> =
              allSkills[userId].orEmpty()
        }
    return SubjectListViewModel(repository = repo)
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

    // Wait until the single list renders at least one TutorCard
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
    fun rendersSingleList_ofTutorCards() {
        setContent()

        val list = composeRule.onNodeWithTag(SubjectListTestTags.TUTOR_LIST)

        // Scroll to each expected name and assert it’s displayed
        list.performScrollToNode(hasText("Liam P."))
        composeRule.onNodeWithText("Liam P.", useUnmergedTree = true).assertIsDisplayed()

        list.performScrollToNode(hasText("David B."))
        composeRule.onNodeWithText("David B.", useUnmergedTree = true).assertIsDisplayed()

        list.performScrollToNode(hasText("Stevie W."))
        composeRule.onNodeWithText("Stevie W.", useUnmergedTree = true).assertIsDisplayed()

        list.performScrollToNode(hasText("Nora Q."))
        composeRule.onNodeWithText("Nora Q.", useUnmergedTree = true).assertIsDisplayed()

        list.performScrollToNode(hasText("Maya R."))
        composeRule.onNodeWithText("Maya R.", useUnmergedTree = true).assertIsDisplayed()
    }

  @Test
  fun clickingBook_callsCallback() {
    val clicked = AtomicBoolean(false)
    setContent(onBook = { clicked.set(true) })

    // Click first Book button in the list
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
    setContent()

    // Assert that ultimately the content shows and no error text
    composeRule.onNodeWithTag(SubjectListTestTags.TUTOR_LIST).assertIsDisplayed()
    composeRule.onNodeWithText("Unknown error").assertDoesNotExist()
  }
}
