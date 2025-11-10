package com.android.sample.screen

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.bookings.BookingDetailsViewModel
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookingsDetailsViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  /** --- Fakes de base --- * */
  private fun fakeBooking(id: String = "b1") =
      Booking(
          bookingId = id,
          associatedListingId = "L1",
          listingCreatorId = "t1",
          bookerId = "s1",
          sessionStart = Date(),
          sessionEnd = Date(System.currentTimeMillis() + 60 * 60 * 1000),
          status = BookingStatus.CONFIRMED,
          price = 50.0)

  private val fakeProfile =
      Profile(userId = "t1", name = "Alice Dupont", email = "alice@test.com", description = "Tutor")
  private val fakeListing =
      Proposal(
          listingId = "L1",
          creatorUserId = "t1",
          description = "Math Tutoring",
          hourlyRate = 50.0,
          location = Location(),
          skill = Skill(skill = "Math"))

  /** --- Scénario 1 : Chargement réussi --- * */
  @Test
  fun loadBooking_success_updatesUiStateCorrectly() = runTest {
    val fakeBookingRepo =
        object : BookingRepository {
          override fun getNewUid() = "demo"

          override suspend fun getBooking(bookingId: String) = fakeBooking(bookingId)

          override suspend fun getAllBookings() = emptyList<Booking>()

          override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

          override suspend fun getBookingsByUserId(userId: String) = emptyList<Booking>()

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

    val fakeListingRepo =
        object : ListingRepository {
          override fun getNewUid() = "Ldemo"

          override suspend fun getListing(listingId: String): Listing = fakeListing

          override suspend fun getAllListings() = emptyList<Listing>()

          override suspend fun getProposals() =
              emptyList<com.android.sample.model.listing.Proposal>()

          override suspend fun getRequests() = emptyList<com.android.sample.model.listing.Request>()

          override suspend fun getListingsByUser(userId: String) = emptyList<Listing>()

          override suspend fun addProposal(proposal: com.android.sample.model.listing.Proposal) {}

          override suspend fun addRequest(request: com.android.sample.model.listing.Request) {}

          override suspend fun updateListing(listingId: String, listing: Listing) {}

          override suspend fun deleteListing(listingId: String) {}

          override suspend fun deactivateListing(listingId: String) {}

          override suspend fun searchBySkill(skill: Skill) = emptyList<Listing>()

          override suspend fun searchByLocation(location: Location, radiusKm: Double) =
              emptyList<Listing>()
        }

    val fakeProfileRepo =
        object : ProfileRepository {
          override fun getNewUid() = "Pdemo"

          override suspend fun getProfile(userId: String): Profile = fakeProfile

          override suspend fun addProfile(profile: Profile) {}

          override suspend fun updateProfile(userId: String, profile: Profile) {}

          override suspend fun deleteProfile(userId: String) {}

          override suspend fun getAllProfiles() = emptyList<Profile>()

          override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
              emptyList<Profile>()

          override suspend fun getProfileById(userId: String) = fakeProfile

          override suspend fun getSkillsForUser(userId: String) = emptyList<Skill>()
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = fakeBookingRepo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.bookingUiState.value
    assertFalse(state.loadError)
    assertEquals("b1", state.booking.bookingId)
    assertEquals("t1", state.creatorProfile.userId)
    assertEquals("Math Tutoring", state.listing.description)
  }

  /** --- Scénario 2 : Erreur pendant le chargement --- * */
  @Test
  fun loadBooking_error_setsLoadErrorTrue() = runTest {
    val errorBookingRepo =
        object : BookingRepository {
          override fun getNewUid() = "demo"

          override suspend fun getBooking(bookingId: String): Booking {
            throw RuntimeException("Simulated error")
          }

          override suspend fun getAllBookings() = emptyList<Booking>()

          override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

          override suspend fun getBookingsByUserId(userId: String) = emptyList<Booking>()

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

    val fakeListingRepo =
        object : ListingRepository {
          override fun getNewUid() = "Ldemo"

          override suspend fun getListing(listingId: String): Listing = fakeListing

          override suspend fun getAllListings() = emptyList<Listing>()

          override suspend fun getProposals() =
              emptyList<com.android.sample.model.listing.Proposal>()

          override suspend fun getRequests() = emptyList<com.android.sample.model.listing.Request>()

          override suspend fun getListingsByUser(userId: String) = emptyList<Listing>()

          override suspend fun addProposal(proposal: com.android.sample.model.listing.Proposal) {}

          override suspend fun addRequest(request: com.android.sample.model.listing.Request) {}

          override suspend fun updateListing(listingId: String, listing: Listing) {}

          override suspend fun deleteListing(listingId: String) {}

          override suspend fun deactivateListing(listingId: String) {}

          override suspend fun searchBySkill(skill: Skill) = emptyList<Listing>()

          override suspend fun searchByLocation(location: Location, radiusKm: Double) =
              emptyList<Listing>()
        }

    val fakeProfileRepo =
        object : ProfileRepository {
          override fun getNewUid() = "Pdemo"

          override suspend fun getProfile(userId: String): Profile = fakeProfile

          override suspend fun addProfile(profile: Profile) {}

          override suspend fun updateProfile(userId: String, profile: Profile) {}

          override suspend fun deleteProfile(userId: String) {}

          override suspend fun getAllProfiles() = emptyList<Profile>()

          override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
              emptyList<Profile>()

          override suspend fun getProfileById(userId: String) = fakeProfile

          override suspend fun getSkillsForUser(userId: String) = emptyList<Skill>()
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = errorBookingRepo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    vm.load("b_error")
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.bookingUiState.value
    assertTrue(state.loadError)
  }
}
