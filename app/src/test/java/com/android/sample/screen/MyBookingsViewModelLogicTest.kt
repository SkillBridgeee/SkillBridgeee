package com.android.sample.screen

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingType
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.bookings.MyBookingsViewModel
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyBookingsViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var fakeBookingRepo: FakeBookingRepo
  private lateinit var fakeProfileRepo: FakeProfileRepo
  private lateinit var fakeListingRepo: FakeListingRepo

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // region --- Fake repositories ---

  private open class FakeBookingRepo(private val list: List<Booking>) : BookingRepository {
    override fun getNewUid() = "X"

    override suspend fun getAllBookings() = list

    override suspend fun getBooking(bookingId: String) = list.first { it.bookingId == bookingId }

    override suspend fun getBookingsByTutor(tutorId: String) =
        list.filter { it.listingCreatorId == tutorId }

    override suspend fun getBookingsByUserId(userId: String) = list

    override suspend fun getBookingsByStudent(studentId: String) =
        list.filter { it.bookerId == studentId }

    override suspend fun getBookingsByListing(listingId: String) =
        list.filter { it.associatedListingId == listingId }

    override suspend fun addBooking(booking: Booking) {}

    override suspend fun updateBooking(bookingId: String, booking: Booking) {}

    override suspend fun deleteBooking(bookingId: String) {}

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

    override suspend fun confirmBooking(bookingId: String) {}

    override suspend fun completeBooking(bookingId: String) {}

    override suspend fun cancelBooking(bookingId: String) {}
  }

  private class FakeProfileRepo(private val map: Map<String, Profile>) : ProfileRepository {
    override fun getNewUid() = "P"

    override suspend fun getProfile(userId: String) = map[userId]

    override suspend fun addProfile(profile: Profile) {}

    override suspend fun updateProfile(userId: String, profile: Profile) {}

    override suspend fun deleteProfile(userId: String) {}

    override suspend fun getAllProfiles() = map.values.toList()

    override suspend fun searchProfilesByLocation(
        location: Location,
        radiusKm: Double
    ): List<Profile> {
      TODO("Not yet implemented")
    }

    override suspend fun getProfileById(userId: String): Profile? {
      TODO("Not yet implemented")
    }

    override suspend fun getSkillsForUser(userId: String): List<Skill> {
      TODO("Not yet implemented")
    }
  }

  private class FakeListingRepo(private val map: Map<String, Listing>) : ListingRepository {
    override fun getNewUid() = "L"

    override suspend fun getAllListings() = map.values.toList()

    override suspend fun getProposals() = map.values.filterIsInstance<Proposal>()

    override suspend fun getRequests() = map.values.filterIsInstance<Request>()

    override suspend fun getListing(listingId: String) = map[listingId]

    override suspend fun getListingsByUser(userId: String) =
        map.values.filter { it.creatorUserId == userId }

    override suspend fun addProposal(proposal: Proposal) {}

    override suspend fun addRequest(request: Request) {}

    override suspend fun updateListing(listingId: String, listing: Listing) {}

    override suspend fun deleteListing(listingId: String) {}

    override suspend fun deactivateListing(listingId: String) {}

    override suspend fun searchBySkill(skill: Skill) = emptyList<Listing>()

    override suspend fun searchByLocation(location: Location, radiusKm: Double) =
        emptyList<Listing>()
  }

  // endregion

  // region --- Object builders ---

  private fun booking(
      id: String = "b1",
      creatorId: String = "t1",
      bookerId: String = "s1",
      listingId: String = "L1",
      start: Date = Date(),
      end: Date = Date(start.time + 3600000),
      price: Double = 30.0
  ) =
      Booking(
          bookingId = id,
          associatedListingId = listingId,
          listingCreatorId = creatorId,
          bookerId = bookerId,
          sessionStart = start,
          sessionEnd = end,
          status = BookingStatus.CONFIRMED,
          price = price)

  private fun profile(id: String, name: String = "Name$id") =
      Profile(
          userId = id,
          name = name,
          email = "$name@test.com",
          description = "Bio of $name",
          levelOfEducation = "Master",
          hourlyRate = "25")

  private fun listing(
      id: String,
      creatorId: String,
      type: ListingType = ListingType.PROPOSAL
  ): Listing {
    val base = ListingType.PROPOSAL
    return if (type == ListingType.PROPOSAL)
        Proposal(
            listingId = id,
            creatorUserId = creatorId,
            skill = Skill(skill = "Math"),
            description = "Tutor listing")
    else
        Request(
            listingId = id,
            creatorUserId = creatorId,
            skill = Skill(skill = "Physics"),
            description = "Student request")
  }

  // endregion

  // region --- Tests ---

  @Test
  fun `load() sets empty bookings when user has none`() = runTest {
    fakeBookingRepo = FakeBookingRepo(emptyList())
    fakeProfileRepo = FakeProfileRepo(emptyMap())
    fakeListingRepo = FakeListingRepo(emptyMap())

    val viewModel =
        MyBookingsViewModel(
            bookingRepo = fakeBookingRepo,
            listingRepo = fakeListingRepo,
            profileRepo = fakeProfileRepo)

    viewModel.load()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertFalse(state.hasError)
    assertTrue(state.bookings.isEmpty())
  }

  @Test
  fun `load() builds correct BookingCardUI list`() = runTest {
    val booking1 = booking("b1", creatorId = "t1", bookerId = "s1", listingId = "L1")
    val booking2 = booking("b2", creatorId = "t2", bookerId = "s1", listingId = "L2")
    val bookings = listOf(booking1, booking2)

    val profiles = mapOf("t1" to profile("t1", "Tutor1"), "t2" to profile("t2", "Tutor2"))
    val listings =
        mapOf(
            "L1" to listing("L1", "t1", ListingType.PROPOSAL),
            "L2" to listing("L2", "t2", ListingType.PROPOSAL))

    fakeBookingRepo = FakeBookingRepo(bookings)
    fakeProfileRepo = FakeProfileRepo(profiles)
    fakeListingRepo = FakeListingRepo(listings)

    val viewModel =
        MyBookingsViewModel(
            bookingRepo = fakeBookingRepo,
            listingRepo = fakeListingRepo,
            profileRepo = fakeProfileRepo)

    viewModel.load()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertFalse(state.hasError)
    assertEquals(2, state.bookings.size)
    assertEquals("Tutor1", state.bookings[0].creatorProfile.name)
    assertEquals("Tutor2", state.bookings[1].creatorProfile.name)
  }

  @Test
  fun `load() handles missing profile or listing gracefully`() = runTest {
    val booking1 = booking("b1", creatorId = "t1", bookerId = "s1", listingId = "L1")
    fakeBookingRepo = FakeBookingRepo(listOf(booking1))
    fakeProfileRepo = FakeProfileRepo(emptyMap())
    fakeListingRepo = FakeListingRepo(emptyMap())

    val viewModel =
        MyBookingsViewModel(
            bookingRepo = fakeBookingRepo,
            listingRepo = fakeListingRepo,
            profileRepo = fakeProfileRepo)

    viewModel.load()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.bookings.isEmpty())
    assertFalse(state.hasError)
    assertFalse(state.isLoading)
  }

  @Test
  fun `load() sets error when repository throws exception`() = runTest {
    val errorRepo =
        object : FakeBookingRepo(emptyList()) {
          override suspend fun getBookingsByUserId(userId: String): List<Booking> {
            throw RuntimeException("Network error")
          }
        }

    fakeBookingRepo = errorRepo
    fakeProfileRepo = FakeProfileRepo(emptyMap())
    fakeListingRepo = FakeListingRepo(emptyMap())

    val viewModel =
        MyBookingsViewModel(
            bookingRepo = fakeBookingRepo,
            listingRepo = fakeListingRepo,
            profileRepo = fakeProfileRepo)

    viewModel.load()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.hasError)
    assertFalse(state.isLoading)
    assertTrue(state.bookings.isEmpty())
  }

  // endregion
}
