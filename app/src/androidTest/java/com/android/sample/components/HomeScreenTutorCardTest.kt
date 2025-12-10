package com.android.sample.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.HomePage.HomeScreen
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.HomePage.MainPageViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTutorCardTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private val sampleProfile =
      Profile(
          userId = "user-1",
          name = "Ava Tutor",
          description = "Experienced tutor",
          location = Location(name = "Helsinki"),
          tutorRating = RatingInfo(averageRating = 4.0, totalRatings = 12))

  // Build a concrete Proposal (Listing is sealed; instantiate a subclass)
  private val listingForSample: Proposal =
      Proposal(
          listingId = "listing-1",
          creatorUserId = "user-1",
          skill = Skill(mainSubject = MainSubject.ACADEMICS, skill = "Academics"),
          hourlyRate = 20.0)

  @Before
  fun setupFakeRepos() {
    val fakeProfileRepo =
        object : ProfileRepository {
          override fun getNewUid(): String = "new-user-uid"

          override fun getCurrentUserId(): String = "test-user-id"

          override suspend fun getProfile(userId: String): Profile? =
              if (userId == sampleProfile.userId) sampleProfile else null

          override suspend fun addProfile(profile: Profile) {}

          override suspend fun updateProfile(userId: String, profile: Profile) {}

          override suspend fun deleteProfile(userId: String) {}

          override suspend fun getAllProfiles(): List<Profile> = listOf(sampleProfile)

          override suspend fun searchProfilesByLocation(
              location: Location,
              radiusKm: Double
          ): List<Profile> = listOf(sampleProfile)

          override suspend fun getProfileById(userId: String): Profile? =
              if (userId == sampleProfile.userId) sampleProfile else null

          override suspend fun getSkillsForUser(userId: String): List<Skill> = emptyList()

          override suspend fun updateTutorRatingFields(
              userId: String,
              averageRating: Double,
              totalRatings: Int
          ) {}

          override suspend fun updateStudentRatingFields(
              userId: String,
              averageRating: Double,
              totalRatings: Int
          ) {}

          override suspend fun deleteAccount(userId: String) {
            TODO("Not yet implemented")
          }
        }

    val fakeListingRepo =
        object : ListingRepository {
          override fun getNewUid(): String = "new-listing-uid"

          override suspend fun getAllListings(): List<Listing> = listOf(listingForSample)

          override suspend fun getProposals(): List<Proposal> = listOf(listingForSample)

          override suspend fun getRequests(): List<Request> = emptyList()

          override suspend fun getListing(listingId: String): Listing? =
              if (listingId == listingForSample.listingId) listingForSample else null

          override suspend fun getListingsByUser(userId: String): List<Listing> =
              if (userId == sampleProfile.userId) listOf(listingForSample) else emptyList()

          override suspend fun addProposal(proposal: Proposal) {}

          override suspend fun addRequest(request: Request) {}

          override suspend fun updateListing(listingId: String, listing: Listing) {}

          override suspend fun deleteListing(listingId: String) {}

          override suspend fun deleteAllListingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun deactivateListing(listingId: String) {}

          override suspend fun searchBySkill(skill: Skill): List<Listing> = listOf(listingForSample)

          override suspend fun searchByLocation(
              location: Location,
              radiusKm: Double
          ): List<Listing> = listOf(listingForSample)
        }

    run {
      val profileRepoField =
          ProfileRepositoryProvider::class.java.getSuperclass().getDeclaredField("_repository")
      profileRepoField.isAccessible = true
      profileRepoField.set(ProfileRepositoryProvider, fakeProfileRepo)
    }

    run {
      val listingRepoField =
          ListingRepositoryProvider::class.java.getSuperclass().getDeclaredField("_repository")
      listingRepoField.isAccessible = true
      listingRepoField.set(ListingRepositoryProvider, fakeListingRepo)
    }
  }

  @Test
  fun displaysNewTutorCard_and_clickingCard_triggersListingNavigation() {
    var navigatedToListingId: String? = null

    val vm = MainPageViewModel()

    composeRule.setContent {
      HomeScreen(
          mainPageViewModel = vm,
          onNavigateToSubjectList = { /* no-op */},
          onNavigateToAddNewListing = {},
          onNavigateToListingDetails = { listingId -> navigatedToListingId = listingId })
    }

    composeRule.waitForIdle()

    val cards = composeRule.onAllNodesWithTag(HomeScreenTestTags.PROPOSAL_CARD)
    cards.assertCountEquals(1)

    cards[0].performClick()
    composeRule.waitForIdle()

    assert(navigatedToListingId == listingForSample.listingId) {
      "Expected navigation to listing ${listingForSample.listingId}, got $navigatedToListingId"
    }
  }
}
