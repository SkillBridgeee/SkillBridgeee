package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
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
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
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

// AI generated test for SubjectListScreen
@RunWith(AndroidJUnit4::class)
class SubjectListScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  /** ---- Fake data ------------------------------------------------ */
  private val profile1 =
      Profile(
          userId = "debugUser1",
          name = "Liam P.",
          description = "Guitar Lessons",
          tutorRating = RatingInfo(4.9, 23))
  private val profile2 =
      Profile(
          userId = "debugUser2",
          name = "Nora Q.",
          description = "Piano Lessons",
          tutorRating = RatingInfo(4.8, 15))

  private val debugListings =
      listOf(
          Proposal(
              listingId = "sample1",
              creatorUserId = "debugUser1",
              skill = Skill(MainSubject.MUSIC, "guitar"),
              description = "Debug Guitar Lessons",
              location = Location(48.8566, 2.3522, "Paris"),
              hourlyRate = 30.0),
          Proposal(
              listingId = "sample2",
              creatorUserId = "debugUser2",
              skill = Skill(MainSubject.MUSIC, "piano"),
              description = "Debug Piano Coaching",
              location = Location(45.7640, 4.8357, "Lyon"),
              hourlyRate = 35.0))

  /** ---- Fake repositories ---------------------------------------- */
  private fun makeViewModel(
      fail: Boolean = false,
      longDelay: Boolean = false
  ): SubjectListViewModel {
    val listingRepo =
        object : ListingRepository {
          override fun getNewUid(): String {
            TODO("Not yet implemented")
          }

          override suspend fun getAllListings(): List<Listing> {
            if (fail) error("Boom failure")
            if (longDelay) delay(200)
            delay(10)
            return debugListings
          }

          override suspend fun getProposals(): List<Proposal> {
            TODO("Not yet implemented")
          }

          override suspend fun getRequests(): List<Request> {
            TODO("Not yet implemented")
          }

          override suspend fun getListing(listingId: String): Listing? {
            TODO("Not yet implemented")
          }

          override suspend fun getListingsByUser(userId: String): List<Listing> {
            TODO("Not yet implemented")
          }

          override suspend fun addProposal(proposal: Proposal) {}

          override suspend fun addRequest(request: Request) {}

          override suspend fun updateListing(listingId: String, listing: Listing) {}

          override suspend fun deleteListing(listingId: String) {}

          override suspend fun deactivateListing(listingId: String) {}

          override suspend fun searchBySkill(skill: Skill): List<Listing> {
            TODO("Not yet implemented")
          }

          override suspend fun searchByLocation(
              location: Location,
              radiusKm: Double
          ): List<Listing> {
            TODO("Not yet implemented")
          }
        }

    val profileRepo =
        object : ProfileRepository {
          override fun getNewUid(): String = "unused"

          override suspend fun getProfile(userId: String): Profile =
              if (userId == "debugUser1") profile1 else profile2

          override suspend fun addProfile(profile: Profile) {}

          override suspend fun updateProfile(userId: String, profile: Profile) {}

          override suspend fun deleteProfile(userId: String) {}

          override suspend fun getAllProfiles(): List<Profile> = listOf(profile1, profile2)

          override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
              emptyList<Profile>()

          override suspend fun getProfileById(userId: String): Profile = profile1

          override suspend fun getSkillsForUser(userId: String): List<Skill> = emptyList()
        }

    return SubjectListViewModel(listingRepo = listingRepo, profileRepo = profileRepo)
  }

  /** ---- Tests ---------------------------------------------------- */
  @Test
  fun showsSearchbarAndCategorySelector() {
    val vm = makeViewModel()
    composeRule.setContent { MaterialTheme { SubjectListScreen(vm, subject = MainSubject.MUSIC) } }

    composeRule.onNodeWithTag(SubjectListTestTags.SEARCHBAR).assertIsDisplayed()
    composeRule.onNodeWithTag(SubjectListTestTags.CATEGORY_SELECTOR).assertIsDisplayed()
  }

  @Test
  fun displaysListings_afterLoading() {
    val vm = makeViewModel()
    composeRule.setContent { MaterialTheme { SubjectListScreen(vm, subject = MainSubject.MUSIC) } }

    composeRule.waitUntil(5_000) {
      composeRule
          .onAllNodes(
              hasTestTag(SubjectListTestTags.LISTING_CARD) and
                  hasAnyAncestor(hasTestTag(SubjectListTestTags.LISTING_LIST)))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeRule.onNodeWithText("Debug Guitar Lessons").assertIsDisplayed()
    composeRule.onNodeWithText("Debug Piano Coaching").assertIsDisplayed()
  }

  @Test
  fun clickingBook_callsCallback() {
    val clicked = AtomicBoolean(false)
    val vm = makeViewModel()
    composeRule.setContent {
      MaterialTheme {
        SubjectListScreen(vm, onBookTutor = { clicked.set(true) }, subject = MainSubject.MUSIC)
      }
    }

    composeRule.waitUntil(3_000) {
      composeRule
          .onAllNodesWithTag(SubjectListTestTags.LISTING_BOOK_BUTTON)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeRule.onAllNodesWithTag(SubjectListTestTags.LISTING_BOOK_BUTTON).onFirst().performClick()
    assert(clicked.get())
  }

  @Test
  fun showsErrorMessage_whenRepositoryFails() {
    val vm = makeViewModel(fail = true)
    composeRule.setContent { MaterialTheme { SubjectListScreen(vm, subject = MainSubject.MUSIC) } }

    composeRule.waitUntil(3_000) {
      composeRule.onAllNodes(hasText("Boom failure")).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithText("Boom failure").assertIsDisplayed()
  }

  @Test
  fun showsCorrectLessonTypeMessageMusic() {
    val vm = makeViewModel()
    composeRule.setContent { MaterialTheme { SubjectListScreen(vm, subject = MainSubject.MUSIC) } }

    composeRule.onNodeWithText("All Music lessons").assertExists()
  }
}
