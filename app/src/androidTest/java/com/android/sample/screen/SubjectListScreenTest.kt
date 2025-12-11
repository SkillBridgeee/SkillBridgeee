package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
  private val profile3 =
      Profile(
          userId = "debugUser3",
          name = "Sam R.",
          description = "Bass Lessons",
          tutorRating = RatingInfo(4.7, 11))

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
              hourlyRate = 35.0),
          Request(
              listingId = "sample3",
              creatorUserId = "debugUser3",
              skill = Skill(MainSubject.MUSIC, "bass"),
              description = "Looking for Bass lessons",
              location = Location(43.2965, 5.3698, "Marseille"),
              hourlyRate = 25.0))

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

          override suspend fun deleteAllListingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

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

          override fun getCurrentUserId(): String = "testUserId"

          override suspend fun getProfile(userId: String): Profile? =
              when (userId) {
                "debugUser1" -> profile1
                "debugUser2" -> profile2
                "debugUser3" -> profile3
                else -> null
              }

          override suspend fun addProfile(profile: Profile) {}

          override suspend fun updateProfile(userId: String, profile: Profile) {}

          override suspend fun deleteProfile(userId: String) {}

          override suspend fun getAllProfiles(): List<Profile> = listOf(profile1, profile2)

          override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
              emptyList<Profile>()

          override suspend fun getProfileById(userId: String): Profile = profile1

          override suspend fun getSkillsForUser(userId: String): List<Skill> = emptyList()

          override suspend fun updateTutorRatingFields(
              userId: String,
              averageRating: Double,
              totalRatings: Int
          ) {
            TODO("Not yet implemented")
          }

          override suspend fun updateStudentRatingFields(
              userId: String,
              averageRating: Double,
              totalRatings: Int
          ) {
            TODO("Not yet implemented")
          }
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
          .size == 3
    }

    composeRule.onNodeWithText("Debug Guitar Lessons").assertIsDisplayed()
    composeRule.onNodeWithText("Debug Piano Coaching").assertIsDisplayed()
    composeRule.onNodeWithText("Looking for Bass lessons").assertIsDisplayed()
  }

  @Test
  fun filterChips_areDisplayed_andWork() {
    val vm = makeViewModel()
    composeRule.setContent { MaterialTheme { SubjectListScreen(vm, subject = MainSubject.MUSIC) } }

    composeRule.waitUntil(5_000) {
      composeRule
          .onAllNodes(hasTestTag(SubjectListTestTags.LISTING_CARD))
          .fetchSemanticsNodes()
          .size == 3
    }

    // Chips are displayed
    composeRule.onNodeWithText("All").assertIsDisplayed()
    composeRule.onNodeWithText("Proposals").assertIsDisplayed()
    composeRule.onNodeWithText("Requests").assertIsDisplayed()

    // Click "Proposals"
    composeRule.onNodeWithText("Proposals").performClick()
    composeRule.onNodeWithText("Debug Guitar Lessons").assertIsDisplayed()
    composeRule.onNodeWithText("Debug Piano Coaching").assertIsDisplayed()
    composeRule.onNodeWithText("Looking for Bass lessons").assertDoesNotExist()

    // Click "Requests"
    composeRule.onNodeWithText("Requests").performClick()
    composeRule.onNodeWithText("Debug Guitar Lessons").assertDoesNotExist()
    composeRule.onNodeWithText("Debug Piano Coaching").assertDoesNotExist()
    composeRule.onNodeWithText("Looking for Bass lessons").assertIsDisplayed()

    // Click "All"
    composeRule.onNodeWithText("All").performClick()
    composeRule.onNodeWithText("Debug Guitar Lessons").assertIsDisplayed()
    composeRule.onNodeWithText("Debug Piano Coaching").assertIsDisplayed()
    composeRule.onNodeWithText("Looking for Bass lessons").assertIsDisplayed()
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
