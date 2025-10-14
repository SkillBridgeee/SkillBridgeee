package com.android.sample.screen

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.rating.RatingType
import com.android.sample.model.rating.StarRating
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.bookings.BookingCardUi
import com.android.sample.ui.bookings.MyBookingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class MyBookingsViewModelLogicTest {

  private val testDispatcher = StandardTestDispatcher()

  @Before fun setup() { Dispatchers.setMain(testDispatcher) }
  @After fun tearDown() { Dispatchers.resetMain() }

  private fun booking(
    id: String = "b1",
    creatorId: String = "t1",
    bookerId: String = "s1",
    listingId: String = "L1",
    start: Date = Date(),
    end: Date = Date(start.time + 90 * 60 * 1000), // 1h30
    price: Double = 30.0
  ) = Booking(
    bookingId = id,
    associatedListingId = listingId,
    listingCreatorId = creatorId,
    bookerId = bookerId,
    sessionStart = start,
    sessionEnd = end,
    status = BookingStatus.CONFIRMED,
    price = price
  )

  /** Simple in-memory fakes */
  private class FakeBookingRepo(private val list: List<Booking>) : BookingRepository {
    override fun getNewUid() = "X"
    override suspend fun getAllBookings() = list
    override suspend fun getBooking(bookingId: String) = list.first { it.bookingId == bookingId }
    override suspend fun getBookingsByTutor(tutorId: String) = list.filter { it.listingCreatorId == tutorId }
    override suspend fun getBookingsByUserId(userId: String) = list.filter { it.bookerId == userId }
    override suspend fun getBookingsByStudent(studentId: String) = list.filter { it.bookerId == studentId }
    override suspend fun getBookingsByListing(listingId: String) = list.filter { it.associatedListingId == listingId }
    override suspend fun addBooking(booking: Booking) {}
    override suspend fun updateBooking(bookingId: String, booking: Booking) {}
    override suspend fun deleteBooking(bookingId: String) {}
    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}
    override suspend fun confirmBooking(bookingId: String) {}
    override suspend fun completeBooking(bookingId: String) {}
    override suspend fun cancelBooking(bookingId: String) {}
  }

  private class FakeListingRepo(
    private val map: Map<String, Listing>
  ) : ListingRepository {
    override fun getNewUid() = "L"
    override suspend fun getAllListings() = map.values.toList()
    override suspend fun getProposals() = map.values.filterIsInstance<Proposal>()
    override suspend fun getRequests() = map.values.filterIsInstance<Request>()
    override suspend fun getListing(listingId: String) = map.getValue(listingId)
    override suspend fun getListingsByUser(userId: String) = map.values.filter { it.creatorUserId == userId }
    override suspend fun addProposal(proposal: Proposal) {}
    override suspend fun addRequest(request: Request) {}
    override suspend fun updateListing(listingId: String, listing: Listing) {}
    override suspend fun deleteListing(listingId: String) {}
    override suspend fun deactivateListing(listingId: String) {}
    override suspend fun searchBySkill(skill: Skill) = map.values.filter { it.skill == skill }
    override suspend fun searchByLocation(location: com.android.sample.model.map.Location, radiusKm: Double) = emptyList<Listing>()
  }

  private class FakeProfileRepo(
    private val map: Map<String, Profile>
  ) : ProfileRepository {
    override fun getNewUid() = "P"
    override suspend fun getProfile(userId: String) = map.getValue(userId)
    override suspend fun addProfile(profile: Profile) {}
    override suspend fun updateProfile(userId: String, profile: Profile) {}
    override suspend fun deleteProfile(userId: String) {}
    override suspend fun getAllProfiles() = map.values.toList()
    override suspend fun searchProfilesByLocation(location: com.android.sample.model.map.Location, radiusKm: Double) = emptyList<Profile>()
  }

  private class FakeRatingRepo(
    private val map: Map<String, Rating?> // key: listingId
  ) : RatingRepository {
    override fun getNewUid() = "R"
    override suspend fun getAllRatings() = map.values.filterNotNull()
    override suspend fun getRating(ratingId: String) = error("not used")
    override suspend fun getRatingsByFromUser(fromUserId: String) = emptyList<Rating>()
    override suspend fun getRatingsByToUser(toUserId: String) = emptyList<Rating>()
    override suspend fun getRatingsOfListing(listingId: String) = map[listingId]
    override suspend fun addRating(rating: Rating) {}
    override suspend fun updateRating(ratingId: String, rating: Rating) {}
    override suspend fun deleteRating(ratingId: String) {}
    override suspend fun getTutorRatingsOfUser(userId: String) = emptyList<Rating>()
    override suspend fun getStudentRatingsOfUser(userId: String) = emptyList<Rating>()
  }

  @Test
  fun load_success_populates_cards() = runTest {
    // Use defaults for Skill to avoid constructor mismatch
    val listing = Proposal(
      listingId = "L1",
      creatorUserId = "t1",
      description = "desc",
      location = Location(),
      hourlyRate = 30.0
    )

    val prof = Profile(userId = "t1", name = "Alice Martin", email = "a@a.com")

    val rating = Rating(
      ratingId = "r1",
      fromUserId = "s1",
      toUserId = "t1",
      starRating = StarRating.FOUR,
      comment = "",
      ratingType = RatingType.Listing("L1")
    )

    val vm = MyBookingsViewModel(
      bookingRepo = FakeBookingRepo(listOf(booking() /* helper that makes 1h30 */)),
      userId = "s1",
      listingRepo = FakeListingRepo(mapOf("L1" to listing)),
      profileRepo = FakeProfileRepo(mapOf("t1" to prof)),
      ratingRepo = FakeRatingRepo(mapOf("L1" to rating)),
      locale = Locale.US,
      demo = false
    )

    // Let init -> load finish
    testDispatcher.scheduler.advanceUntilIdle()

    val cards = vm.uiState.value
    assertEquals(1, cards.size)

    val c = cards.first()
    assertEquals("b1", c.id)
    assertEquals("t1", c.tutorId)
    assertEquals("Alice Martin", c.tutorName)
    // Subject comes from Skill.mainSubject.toString(); just ensure it's not blank
    assertTrue(c.subject.isNotBlank())
    assertEquals("$30.0/hr", c.pricePerHourLabel)
    assertEquals(4, c.ratingStars)
    assertEquals(1, c.ratingCount)
    // duration of helper booking is 1h30
    assertEquals("1h 30m", c.durationLabel)
    assertTrue(c.dateLabel.matches(Regex("""\d{2}/\d{2}/\d{4}""")))
  }


  @Test
  fun load_empty_results_in_empty_list() = runTest {
    val vm = MyBookingsViewModel(
      bookingRepo = FakeBookingRepo(emptyList()),
      userId = "s1",
      listingRepo = FakeListingRepo(emptyMap()),
      profileRepo = FakeProfileRepo(emptyMap()),
      ratingRepo = FakeRatingRepo(emptyMap()),
      demo = false
    )
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(vm.uiState.value.isEmpty())
  }

  @Test
  fun load_handles_repository_errors_gracefully() = runTest {
    val failingBookingRepo = object : BookingRepository {
      override fun getNewUid() = "X"
      override suspend fun getAllBookings() = emptyList<Booking>()
      override suspend fun getBooking(bookingId: String) = error("boom")
      override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()
      override suspend fun getBookingsByUserId(userId: String) = throw RuntimeException("boom")
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
    val vm = MyBookingsViewModel(
      bookingRepo = failingBookingRepo,
      userId = "s1",
      listingRepo = FakeListingRepo(emptyMap()),
      profileRepo = FakeProfileRepo(emptyMap()),
      ratingRepo = FakeRatingRepo(emptyMap()),
      demo = false
    )
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(vm.uiState.value.isEmpty())
  }

  @Test
  fun load_demo_populates_demo_cards() = runTest {
    val vm = MyBookingsViewModel(
      bookingRepo = FakeBookingRepo(emptyList()),
      userId = "s1",
      listingRepo = FakeListingRepo(emptyMap()),
      profileRepo = FakeProfileRepo(emptyMap()),
      ratingRepo = FakeRatingRepo(emptyMap()),
      demo = true
    )
    testDispatcher.scheduler.advanceUntilIdle()
    val cards = vm.uiState.value
    assertEquals(2, cards.size)
    assertEquals("Alice Martin", cards[0].tutorName)
    assertEquals("Lucas Dupont", cards[1].tutorName)
  }
}
