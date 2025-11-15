package com.android.sample.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.model.booking.*
import com.android.sample.model.listing.*
import com.android.sample.model.map.Location
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.bookings.*
import java.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookingDetailsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // ----- FAKES -----
  private val fakeBookingRepo =
      object : BookingRepository {
        override fun getNewUid() = "b1"

        override suspend fun getBooking(bookingId: String) =
            Booking(
                bookingId = bookingId,
                associatedListingId = "l1",
                listingCreatorId = "u1",
                price = 50.0,
                sessionStart = Date(1736546400000),
                sessionEnd = Date(1736550000000),
                status = BookingStatus.PENDING,
                bookerId = "asdf")

        override suspend fun getBookingsByUserId(userId: String) = emptyList<Booking>()

        override suspend fun getAllBookings() = emptyList<Booking>()

        override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

        override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

        override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

        override suspend fun addBooking(booking: Booking) {}

        override suspend fun updateBooking(bookingId: String, booking: Booking) {}

        override suspend fun deleteBooking(bookingId: String) {}

        override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

        override suspend fun confirmBooking(bookingId: String) {}

        override suspend fun completeBooking(bookingId: String) {}

        override suspend fun cancelBooking(bookingId: String) {}
      }

  private val fakeListingRepo =
      object : ListingRepository {
        override fun getNewUid() = "l1"

        override suspend fun getListing(listingId: String) =
            Proposal(
                listingId = listingId,
                description = "Cours de maths",
                skill = Skill(skill = "Algebra", mainSubject = MainSubject.ACADEMICS),
                location = Location(name = "Geneva"))

        override suspend fun getAllListings() = emptyList<Listing>()

        override suspend fun getProposals() = emptyList<Proposal>()

        override suspend fun getRequests() = emptyList<com.android.sample.model.listing.Request>()

        override suspend fun getListingsByUser(userId: String) = emptyList<Listing>()

        override suspend fun addProposal(proposal: Proposal) {}

        override suspend fun addRequest(request: com.android.sample.model.listing.Request) {}

        override suspend fun updateListing(listingId: String, listing: Listing) {}

        override suspend fun deleteListing(listingId: String) {}

        override suspend fun deactivateListing(listingId: String) {}

        override suspend fun searchBySkill(skill: com.android.sample.model.skill.Skill) =
            emptyList<Listing>()

        override suspend fun searchByLocation(location: Location, radiusKm: Double) =
            emptyList<Listing>()
      }

  private val fakeProfileRepo =
      object : ProfileRepository {
        override fun getNewUid() = "u1"

        override suspend fun getProfile(userId: String) =
            Profile(userId = userId, name = "John Doe", email = "john.doe@example.com")

        override suspend fun getProfileById(userId: String) = getProfile(userId)

        override suspend fun addProfile(profile: Profile) {}

        override suspend fun updateProfile(userId: String, profile: Profile) {}

        override suspend fun deleteProfile(userId: String) {}

        override suspend fun getAllProfiles() = emptyList<Profile>()

        override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
            emptyList<Profile>()

        override suspend fun getSkillsForUser(userId: String) =
            emptyList<com.android.sample.model.skill.Skill>()
      }

  private fun fakeViewModel() =
      BookingDetailsViewModel(
          bookingRepository = fakeBookingRepo,
          listingRepository = fakeListingRepo,
          profileRepository = fakeProfileRepo)

  // ----- TESTS -----

  @Test
  fun bookingDetailsScreen_displaysAllSections() {
    val vm = fakeViewModel()
    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = {})
    }

    // Vérifie les sections visibles
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.HEADER).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.CREATOR_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.LISTING_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.SCHEDULE_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.DESCRIPTION_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.STATUS).assertExists()

    // Vérifie le nom et email du créateur
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.CREATOR_NAME)
        .assert(hasAnyChild(hasText("John Doe")))
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.CREATOR_EMAIL)
        .assert(hasAnyChild(hasText("john.doe@example.com")))
  }

  @Test
  fun bookingDetailsScreen_clickMoreInfo_callsCallback() {
    var clickedId: String? = null
    val vm = fakeViewModel()
    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = { clickedId = it })
    }

    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.MORE_INFO_BUTTON)
        .assertIsDisplayed()
        .performClick()

    assert(clickedId == "u1")
  }

  private val fakeProfileRepoError =
      object : ProfileRepository {
        override fun getNewUid() = "u1"

        override suspend fun getProfile(userId: String) = throw error("test")

        override suspend fun getProfileById(userId: String) = getProfile(userId)

        override suspend fun addProfile(profile: Profile) {}

        override suspend fun updateProfile(userId: String, profile: Profile) {}

        override suspend fun deleteProfile(userId: String) {}

        override suspend fun getAllProfiles() = emptyList<Profile>()

        override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
            emptyList<Profile>()

        override suspend fun getSkillsForUser(userId: String) =
            emptyList<com.android.sample.model.skill.Skill>()
      }

  private fun fakeViewModelError() =
      BookingDetailsViewModel(
          bookingRepository = fakeBookingRepo,
          listingRepository = fakeListingRepo,
          profileRepository = fakeProfileRepoError)

  @Test
  fun bookingDetailsScreen_errorScreen() {
    var clickedId: String? = null
    val vm = fakeViewModelError()
    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = { clickedId = it })
    }

    composeTestRule.onNodeWithTag(BookingDetailsTestTag.ERROR).assertIsDisplayed()
  }

  private val fakeBookingRepo2 =
      object : BookingRepository {
        override fun getNewUid() = "b1"

        override suspend fun getBooking(bookingId: String) =
            Booking(
                bookingId = bookingId,
                associatedListingId = "l1",
                listingCreatorId = "u1",
                price = 50.0,
                sessionStart = Date(1736546400000),
                sessionEnd = Date(1736550000000),
                status = BookingStatus.PENDING,
                bookerId = "asdf")

        override suspend fun getBookingsByUserId(userId: String) = emptyList<Booking>()

        override suspend fun getAllBookings() = emptyList<Booking>()

        override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

        override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

        override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

        override suspend fun addBooking(booking: Booking) {}

        override suspend fun updateBooking(bookingId: String, booking: Booking) {}

        override suspend fun deleteBooking(bookingId: String) {}

        override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

        override suspend fun confirmBooking(bookingId: String) {}

        override suspend fun completeBooking(bookingId: String) {}

        override suspend fun cancelBooking(bookingId: String) {}
      }

  private val fakeListingRepo2 =
      object : ListingRepository {
        override fun getNewUid() = "l1"

        override suspend fun getListing(listingId: String) =
            Request(
                listingId = listingId,
                description = "Cours de maths",
                skill = Skill(skill = "Algebra", mainSubject = MainSubject.ACADEMICS),
                location = Location(name = "Geneva"))

        override suspend fun getAllListings() = emptyList<Listing>()

        override suspend fun getProposals() = emptyList<Proposal>()

        override suspend fun getRequests() = emptyList<com.android.sample.model.listing.Request>()

        override suspend fun getListingsByUser(userId: String) = emptyList<Listing>()

        override suspend fun addProposal(proposal: Proposal) {}

        override suspend fun addRequest(request: com.android.sample.model.listing.Request) {}

        override suspend fun updateListing(listingId: String, listing: Listing) {}

        override suspend fun deleteListing(listingId: String) {}

        override suspend fun deactivateListing(listingId: String) {}

        override suspend fun searchBySkill(skill: com.android.sample.model.skill.Skill) =
            emptyList<Listing>()

        override suspend fun searchByLocation(location: Location, radiusKm: Double) =
            emptyList<Listing>()
      }

  private fun fakeViewModel2() =
      BookingDetailsViewModel(
          bookingRepository = fakeBookingRepo2,
          listingRepository = fakeListingRepo2,
          profileRepository = fakeProfileRepo)

  @Test
  fun bookingDetailsScreen_displaysAllSections2() {
    val vm = fakeViewModel2()
    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = {})
    }

    // Vérifie les sections visibles
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.HEADER).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.CREATOR_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.LISTING_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.SCHEDULE_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.DESCRIPTION_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.STATUS).assertExists()
  }
}
