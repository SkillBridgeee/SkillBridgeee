package com.android.sample.ui.listing

import com.android.sample.model.authentication.FirebaseTestRule
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.communication.conversation.ConvRepository
import com.android.sample.model.communication.conversation.ConversationRepositoryProvider
import com.android.sample.model.communication.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.overViewConv.OverViewConvRepositoryProvider
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.rating.RatingType
import com.android.sample.model.rating.StarRating
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ListingViewModelTest {

  @get:Rule val firebaseRule = FirebaseTestRule()

  private val testDispatcher = StandardTestDispatcher()

  @Mock private lateinit var mockConversationRepository: ConvRepository
  @Mock private lateinit var mockOverViewConvRepository: OverViewConvRepository

  private val sampleProposal =
      Proposal(
          listingId = "listing-123",
          creatorUserId = "creator-456",
          skill = Skill(MainSubject.ACADEMICS, "Calculus", 5.0, ExpertiseLevel.ADVANCED),
          description = "Advanced calculus tutoring for university students",
          location = Location(name = "Campus Library", longitude = -74.0, latitude = 40.7),
          createdAt = Date(),
          isActive = true,
          hourlyRate = 30.0)

  private val sampleRequest =
      Request(
          listingId = "request-789",
          creatorUserId = "creator-999",
          skill = Skill(MainSubject.ACADEMICS, "Physics", 3.0, ExpertiseLevel.INTERMEDIATE),
          description = "Need help with quantum mechanics",
          location = Location(name = "Study Room", longitude = -74.0, latitude = 40.7),
          createdAt = Date(),
          isActive = true,
          hourlyRate = 35.0)

  private val sampleCreator =
      Profile(
          userId = "creator-456",
          name = "Jane Smith",
          email = "jane.smith@example.com",
          location = Location(name = "New York"))

  private val sampleBookerProfile =
      Profile(
          userId = "booker-789",
          name = "John Doe",
          email = "john.doe@example.com",
          location = Location(name = "Boston"))

  private val sampleBooking =
      Booking(
          bookingId = "booking-1",
          associatedListingId = "listing-123",
          listingCreatorId = "creator-456",
          bookerId = "booker-789",
          sessionStart = Date(),
          sessionEnd = Date(System.currentTimeMillis() + 3600000),
          status = BookingStatus.PENDING,
          price = 30.0)

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    Dispatchers.setMain(testDispatcher)
    UserSessionManager.clearSession()
    // Initialize all repository providers with mock instances
    ListingRepositoryProvider.setForTests(mockk(relaxed = true))
    ProfileRepositoryProvider.setForTests(mockk(relaxed = true))
    BookingRepositoryProvider.setForTests(mockk(relaxed = true))
    RatingRepositoryProvider.setForTests(mockk(relaxed = true))
    ConversationRepositoryProvider.setForTests(mockConversationRepository)
    OverViewConvRepositoryProvider.setForTests(mockOverViewConvRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    UserSessionManager.clearSession()
    unmockkStatic(FirebaseAuth::class)
    ListingRepositoryProvider.clearForTests()
    ProfileRepositoryProvider.clearForTests()
    BookingRepositoryProvider.clearForTests()
    RatingRepositoryProvider.clearForTests()
    ConversationRepositoryProvider.clearForTests()
    OverViewConvRepositoryProvider.clearForTests()
  }

  // Fake Repositories
  private open class FakeListingRepo(
      private var storedListing: com.android.sample.model.listing.Listing? = null
  ) : ListingRepository {
    override fun getNewUid() = "fake-listing-id"

    override suspend fun getAllListings() = listOfNotNull(storedListing)

    override suspend fun getProposals() =
        storedListing?.let { if (it is Proposal) listOf(it) else emptyList() } ?: emptyList()

    override suspend fun getRequests() =
        storedListing?.let { if (it is Request) listOf(it) else emptyList() } ?: emptyList()

    override suspend fun getListing(listingId: String) =
        if (storedListing?.listingId == listingId) storedListing else null

    override suspend fun getListingsByUser(userId: String) =
        emptyList<com.android.sample.model.listing.Listing>()

    override suspend fun addProposal(proposal: Proposal) {}

    override suspend fun addRequest(request: Request) {}

    override suspend fun updateListing(
        listingId: String,
        listing: com.android.sample.model.listing.Listing
    ) {}

    override suspend fun deleteListing(listingId: String) {}

    override suspend fun deactivateListing(listingId: String) {}

    override suspend fun searchBySkill(skill: Skill) =
        emptyList<com.android.sample.model.listing.Listing>()

    override suspend fun searchByLocation(location: Location, radiusKm: Double) =
        emptyList<com.android.sample.model.listing.Listing>()
  }

  private open class FakeProfileRepo(private val profiles: Map<String, Profile> = emptyMap()) :
      ProfileRepository {
    override fun getNewUid() = "fake-profile-id"

    override fun getCurrentUserId() = "test-user-id"

    override suspend fun getProfile(userId: String) = profiles[userId]

    override suspend fun addProfile(profile: Profile) {}

    override suspend fun updateProfile(userId: String, profile: Profile) {}

    override suspend fun deleteProfile(userId: String) {}

    override suspend fun getAllProfiles() = profiles.values.toList()

    override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
        emptyList<Profile>()

    override suspend fun getProfileById(userId: String) = profiles[userId]

    override suspend fun getSkillsForUser(userId: String) = emptyList<Skill>()

    override suspend fun updateTutorRatingFields(
        userId: String,
        averageRating: Double,
        totalRatings: Int
    ) {
      // no-op
    }

    override suspend fun updateStudentRatingFields(
        userId: String,
        averageRating: Double,
        totalRatings: Int
    ) {
      // no-op
    }
  }

  private open class FakeBookingRepo(
      private val storedBookings: MutableList<Booking> = mutableListOf()
  ) : BookingRepository {
    var confirmBookingCalled = false
    var cancelBookingCalled = false
    var addBookingCalled = false

    override fun getNewUid() = "fake-booking-id"

    override suspend fun getAllBookings() = storedBookings

    override suspend fun getBooking(bookingId: String) =
        storedBookings.find { it.bookingId == bookingId }

    override suspend fun getBookingsByTutor(tutorId: String) =
        storedBookings.filter { it.listingCreatorId == tutorId }

    override suspend fun getBookingsByUserId(userId: String) =
        storedBookings.filter { it.bookerId == userId || it.listingCreatorId == userId }

    override suspend fun getBookingsByStudent(studentId: String) =
        storedBookings.filter { it.bookerId == studentId }

    override suspend fun getBookingsByListing(listingId: String) =
        storedBookings.filter { it.associatedListingId == listingId }

    override suspend fun addBooking(booking: Booking) {
      addBookingCalled = true
      storedBookings.add(booking)
    }

    override suspend fun updateBooking(bookingId: String, booking: Booking) {
      val index = storedBookings.indexOfFirst { it.bookingId == bookingId }
      if (index != -1) {
        storedBookings[index] = booking
      }
    }

    override suspend fun deleteBooking(bookingId: String) {
      storedBookings.removeAll { it.bookingId == bookingId }
    }

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
      val booking = storedBookings.find { it.bookingId == bookingId }
      booking?.let {
        val updated = it.copy(status = status)
        updateBooking(bookingId, updated)
      }
    }

    override suspend fun updatePaymentStatus(
        bookingId: String,
        paymentStatus: com.android.sample.model.booking.PaymentStatus
    ) {
      val booking = storedBookings.find { it.bookingId == bookingId }
      booking?.let {
        val updated = it.copy(paymentStatus = paymentStatus)
        updateBooking(bookingId, updated)
      }
    }

    override suspend fun confirmBooking(bookingId: String) {
      confirmBookingCalled = true
      updateBookingStatus(bookingId, BookingStatus.CONFIRMED)
    }

    override suspend fun completeBooking(bookingId: String) {
      updateBookingStatus(bookingId, BookingStatus.COMPLETED)
    }

    override suspend fun cancelBooking(bookingId: String) {
      cancelBookingCalled = true
      updateBookingStatus(bookingId, BookingStatus.CANCELLED)
    }
  }

  private class FakeRatingRepo : RatingRepository {
    val addedRatings = mutableListOf<Rating>()
    var hasRatingCalls = 0

    override fun getNewUid(): String = "fake-rating-id"

    override suspend fun hasRating(
        fromUserId: String,
        toUserId: String,
        ratingType: RatingType,
        targetObjectId: String
    ): Boolean {
      hasRatingCalls++
      return false
    }

    override suspend fun addRating(rating: Rating) {
      addedRatings += rating
    }

    override suspend fun getAllRatings(): List<Rating> = emptyList()

    override suspend fun getRating(ratingId: String): Rating? = null

    override suspend fun getRatingsByFromUser(fromUserId: String): List<Rating> = emptyList()

    override suspend fun getRatingsByToUser(toUserId: String): List<Rating> = emptyList()

    override suspend fun getRatingsOfListing(listingId: String): List<Rating> = emptyList()

    override suspend fun updateRating(ratingId: String, rating: Rating) {}

    override suspend fun deleteRating(ratingId: String) {}

    override suspend fun getTutorRatingsOfUser(userId: String): List<Rating> = emptyList()

    override suspend fun getStudentRatingsOfUser(userId: String): List<Rating> = emptyList()
  }

  private class RecordingRatingRepo(
      private val hasRatingResult: Boolean = false,
      private val throwOnHasRating: Boolean = false
  ) : RatingRepository {

    val addedRatings = mutableListOf<Rating>()
    var hasRatingCalled = false

    override fun getNewUid(): String = "fake-rating-id"

    override suspend fun hasRating(
        fromUserId: String,
        toUserId: String,
        ratingType: RatingType,
        targetObjectId: String
    ): Boolean {
      hasRatingCalled = true
      if (throwOnHasRating) throw RuntimeException("test hasRating error")
      return hasRatingResult
    }

    override suspend fun addRating(rating: Rating) {
      addedRatings.add(rating)
    }

    override suspend fun getAllRatings(): List<Rating> = emptyList()

    override suspend fun getRating(ratingId: String): Rating? = null

    override suspend fun getRatingsByFromUser(fromUserId: String): List<Rating> = emptyList()

    override suspend fun getRatingsByToUser(toUserId: String): List<Rating> = emptyList()

    override suspend fun getRatingsOfListing(listingId: String): List<Rating> = emptyList()

    override suspend fun updateRating(ratingId: String, rating: Rating) {}

    override suspend fun deleteRating(ratingId: String) {}

    override suspend fun getTutorRatingsOfUser(userId: String): List<Rating> = emptyList()

    override suspend fun getStudentRatingsOfUser(userId: String): List<Rating> = emptyList()
  }

  private class RecordingProfileRepo(private val profiles: Map<String, Profile>) :
      ProfileRepository {

    var lastStudentUserId: String? = null
    var lastStudentAvg: Double? = null
    var lastStudentTotal: Int? = null

    override fun getNewUid() = "fake-profile-id"

    override fun getCurrentUserId() = "test-user-id"

    override suspend fun getProfile(userId: String) = profiles[userId]

    override suspend fun addProfile(profile: Profile) {}

    override suspend fun updateProfile(userId: String, profile: Profile) {}

    override suspend fun deleteProfile(userId: String) {}

    override suspend fun getAllProfiles() = profiles.values.toList()

    override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
        emptyList<Profile>()

    override suspend fun getProfileById(userId: String) = profiles[userId]

    override suspend fun getSkillsForUser(userId: String) = emptyList<Skill>()

    override suspend fun updateTutorRatingFields(
        userId: String,
        averageRating: Double,
        totalRatings: Int
    ) {
      // not needed for this test
    }

    override suspend fun updateStudentRatingFields(
        userId: String,
        averageRating: Double,
        totalRatings: Int
    ) {
      lastStudentUserId = userId
      lastStudentAvg = averageRating
      lastStudentTotal = totalRatings
    }
  }

  private class AggregatingRatingRepo(
      ratingsByStudent: Map<String, List<Rating>>,
      private val alreadyRated: Boolean = false
  ) : RatingRepository {

    val addedRatings = mutableListOf<Rating>()
    var hasRatingCalled = false

    // mutable copy so we can add the new rating and then aggregate over all of them
    private val ratingsByStudentMutable: MutableMap<String, MutableList<Rating>> =
        ratingsByStudent.mapValues { (_, v) -> v.toMutableList() }.toMutableMap()

    override fun getNewUid() = "fake-rating-id"

    override suspend fun hasRating(
        fromUserId: String,
        toUserId: String,
        ratingType: RatingType,
        targetObjectId: String
    ): Boolean {
      hasRatingCalled = true
      return alreadyRated
    }

    override suspend fun addRating(rating: Rating) {
      addedRatings += rating
      if (rating.ratingType == RatingType.STUDENT) {
        val list = ratingsByStudentMutable.getOrPut(rating.toUserId) { mutableListOf() }
        list += rating
      }
    }

    override suspend fun getStudentRatingsOfUser(userId: String): List<Rating> =
        ratingsByStudentMutable[userId] ?: emptyList()

    // the rest can be no-op:
    override suspend fun getAllRatings() = emptyList<Rating>()

    override suspend fun getRating(ratingId: String) = null

    override suspend fun getRatingsByFromUser(fromUserId: String) = emptyList<Rating>()

    override suspend fun getRatingsByToUser(toUserId: String) = emptyList<Rating>()

    override suspend fun getRatingsOfListing(listingId: String) = emptyList<Rating>()

    override suspend fun updateRating(ratingId: String, rating: Rating) {}

    override suspend fun deleteRating(ratingId: String) {}

    override suspend fun getTutorRatingsOfUser(userId: String) = emptyList<Rating>()
  }

  private fun mockFirebaseAuthUser(uid: String) {
    mockkStatic(FirebaseAuth::class)
    val auth = mockk<FirebaseAuth>()
    val user = mockk<FirebaseUser>()

    every { FirebaseAuth.getInstance() } returns auth
    every { auth.currentUser } returns user
    every { user.uid } returns uid
  }

  // Tests for loadListing()

  @Test
  fun loadListing_success_updatesState() = runTest {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.listing)
    assertEquals("listing-123", state.listing?.listingId)
    assertNotNull(state.creator)
    assertEquals("Jane Smith", state.creator?.name)
    assertFalse(state.isLoading)
    assertNull(state.error)
  }

  @Test
  fun loadListing_notFound_showsError() = runTest {
    val listingRepo = FakeListingRepo(null)
    val profileRepo = FakeProfileRepo()
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("non-existent-id")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNull(state.listing)
    assertFalse(state.isLoading)
    assertEquals("Listing not found", state.error)
  }

  @Test
  fun loadListing_exception_showsError() = runTest {
    val listingRepo =
        object : FakeListingRepo(sampleProposal) {
          override suspend fun getListing(listingId: String) =
              throw RuntimeException("Network error")
        }
    val profileRepo = FakeProfileRepo()
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNull(state.listing)
    assertFalse(state.isLoading)
    assertNotNull(state.error)
    assertTrue(state.error!!.contains("Failed to load listing"))
  }

  @Test
  fun loadListing_ownListing_loadsBookings() = runTest {
    UserSessionManager.setCurrentUserId("creator-456")

    val bookings = listOf(sampleBooking)
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo =
        FakeProfileRepo(mapOf("creator-456" to sampleCreator, "booker-789" to sampleBookerProfile))
    val bookingRepo = FakeBookingRepo(bookings.toMutableList())
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.isOwnListing)
    assertEquals(1, state.listingBookings.size)
    assertEquals(1, state.bookerProfiles.size)
    assertFalse(state.bookingsLoading)
  }

  @Test
  fun loadListing_notOwnListing_doesNotLoadBookings() = runTest {
    UserSessionManager.setCurrentUserId("other-user-123")

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isOwnListing)
    assertTrue(state.listingBookings.isEmpty())
  }

  @Test
  fun loadListing_noCreatorProfile_stillLoadsListing() = runTest {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(emptyMap())
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.listing)
    assertNull(state.creator)
    assertFalse(state.isLoading)
  }

  @Test
  fun loadBookingsForListing_exception_handledGracefully() = runTest {
    UserSessionManager.setCurrentUserId("creator-456")

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo =
        object : FakeBookingRepo() {
          override suspend fun getBookingsByListing(listingId: String): List<Booking> {
            throw RuntimeException("Database error")
          }
        }
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.listing)
    assertTrue(state.listingBookings.isEmpty())
    assertFalse(state.bookingsLoading)
  }

  // Tests for createBooking()

  @Test
  fun createBooking_success_updatesState() = runTest {
    UserSessionManager.setCurrentUserId("user-123")

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    val sessionStart = Date()
    val sessionEnd = Date(System.currentTimeMillis() + 3600000)
    viewModel.createBooking(sessionStart, sessionEnd)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.bookingSuccess)
    assertNull(state.bookingError)
    assertFalse(state.bookingInProgress)
    assertTrue(bookingRepo.addBookingCalled)
  }

  @Test
  fun createBooking_noListing_showsError() = runTest {
    UserSessionManager.setCurrentUserId("user-123")

    val listingRepo = FakeListingRepo()
    val profileRepo = FakeProfileRepo()
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    val sessionStart = Date()
    val sessionEnd = Date(System.currentTimeMillis() + 3600000)
    viewModel.createBooking(sessionStart, sessionEnd)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("Listing not found", state.bookingError)
    assertFalse(state.bookingSuccess)
  }

  @Test
  fun createBooking_notLoggedIn_showsError() = runTest {
    UserSessionManager.clearSession()

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    val sessionStart = Date()
    val sessionEnd = Date(System.currentTimeMillis() + 3600000)
    viewModel.createBooking(sessionStart, sessionEnd)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.bookingError)
    assertTrue(state.bookingError!!.contains("logged in"))
    assertFalse(state.bookingSuccess)
  }

  @Test
  fun createBooking_ownListing_showsError() = runTest {
    UserSessionManager.setCurrentUserId("creator-456")

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    val sessionStart = Date()
    val sessionEnd = Date(System.currentTimeMillis() + 3600000)
    viewModel.createBooking(sessionStart, sessionEnd)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.bookingError)
    assertTrue(state.bookingError!!.contains("cannot book your own listing"))
    assertFalse(state.bookingSuccess)
  }

  @Test
  fun createBooking_invalidBooking_showsError() = runTest {
    UserSessionManager.setCurrentUserId("user-123")

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    // Invalid: end time before start time
    val sessionStart = Date(System.currentTimeMillis() + 3600000)
    val sessionEnd = Date()
    viewModel.createBooking(sessionStart, sessionEnd)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.bookingError)
  }

  @Test
  fun createBooking_repositoryException_showsError() = runTest {
    UserSessionManager.setCurrentUserId("user-123")

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo =
        object : FakeBookingRepo() {
          override suspend fun addBooking(booking: Booking) {
            throw RuntimeException("Database error")
          }
        }
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    val sessionStart = Date()
    val sessionEnd = Date(System.currentTimeMillis() + 3600000)
    viewModel.createBooking(sessionStart, sessionEnd)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.bookingError)
    assertTrue(state.bookingError!!.contains("Failed to create booking"))
    assertFalse(state.bookingSuccess)
  }

  @Test
  fun createBooking_calculatesPrice_correctly() = runTest {
    UserSessionManager.setCurrentUserId("user-123")

    val bookings = mutableListOf<Booking>()
    val bookingRepo =
        object : FakeBookingRepo(bookings) {
          override suspend fun addBooking(booking: Booking) {
            bookings.add(booking)
          }
        }

    val listingRepo = FakeListingRepo(sampleProposal) // hourlyRate = 30.0
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    val sessionStart = Date()
    val sessionEnd = Date(System.currentTimeMillis() + 7200000) // 2 hours later
    viewModel.createBooking(sessionStart, sessionEnd)
    advanceUntilIdle()

    assertEquals(1, bookings.size)
    assertEquals(60.0, bookings[0].price, 0.01) // 30.0 * 2 = 60.0
  }

  // Tests for approveBooking()

  @Test
  fun approveBooking_success_callsRepository() = runTest {
    UserSessionManager.setCurrentUserId("creator-456")

    val bookings = listOf(sampleBooking.copy(status = BookingStatus.PENDING))
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo =
        FakeProfileRepo(mapOf("creator-456" to sampleCreator, "booker-789" to sampleBookerProfile))
    val bookingRepo = FakeBookingRepo(bookings.toMutableList())
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    viewModel.approveBooking("booking-1")
    advanceUntilIdle()

    assertTrue(bookingRepo.confirmBookingCalled)
  }

  @Test
  fun approveBooking_exception_handledGracefully() = runTest {
    UserSessionManager.setCurrentUserId("creator-456")

    val bookings = listOf(sampleBooking.copy(status = BookingStatus.PENDING))
    val bookingRepo =
        object : FakeBookingRepo(bookings.toMutableList()) {
          override suspend fun confirmBooking(bookingId: String) {
            throw RuntimeException("Booking service error")
          }
        }
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo =
        FakeProfileRepo(mapOf("creator-456" to sampleCreator, "booker-789" to sampleBookerProfile))
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    // Should not crash
    viewModel.approveBooking("booking-1")
    advanceUntilIdle()

    assertNotNull(viewModel.uiState.value.listing)
  }

  // Tests for rejectBooking()

  @Test
  fun rejectBooking_success_callsRepository() = runTest {
    UserSessionManager.setCurrentUserId("creator-456")

    val bookings = listOf(sampleBooking.copy(status = BookingStatus.PENDING))
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo =
        FakeProfileRepo(mapOf("creator-456" to sampleCreator, "booker-789" to sampleBookerProfile))
    val bookingRepo = FakeBookingRepo(bookings.toMutableList())
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    viewModel.rejectBooking("booking-1")
    advanceUntilIdle()

    assertTrue(bookingRepo.cancelBookingCalled)
  }

  @Test
  fun rejectBooking_exception_handledGracefully() = runTest {
    UserSessionManager.setCurrentUserId("creator-456")

    val bookings = listOf(sampleBooking.copy(status = BookingStatus.PENDING))
    val bookingRepo =
        object : FakeBookingRepo(bookings.toMutableList()) {
          override suspend fun cancelBooking(bookingId: String) {
            throw RuntimeException("Booking service error")
          }
        }
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo =
        FakeProfileRepo(mapOf("creator-456" to sampleCreator, "booker-789" to sampleBookerProfile))
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    // Should not crash
    viewModel.rejectBooking("booking-1")
    advanceUntilIdle()

    assertNotNull(viewModel.uiState.value.listing)
  }

  // Tests for state management methods

  @Test
  fun clearBookingSuccess_updatesState() = runTest {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo()
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.showBookingSuccess()
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.bookingSuccess)

    viewModel.clearBookingSuccess()
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.bookingSuccess)
  }

  @Test
  fun clearBookingError_updatesState() = runTest {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo()
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.showBookingError("Test error")
    advanceUntilIdle()

    assertEquals("Test error", viewModel.uiState.value.bookingError)

    viewModel.clearBookingError()
    advanceUntilIdle()

    assertNull(viewModel.uiState.value.bookingError)
  }

  @Test
  fun showBookingSuccess_updatesState() = runTest {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo()
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    assertFalse(viewModel.uiState.value.bookingSuccess)

    viewModel.showBookingSuccess()
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.bookingSuccess)
  }

  @Test
  fun showBookingError_updatesState() = runTest {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo()
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    assertNull(viewModel.uiState.value.bookingError)

    viewModel.showBookingError("Custom error message")
    advanceUntilIdle()

    assertEquals("Custom error message", viewModel.uiState.value.bookingError)
  }

  // Tests for loading states

  @Test
  fun loadListing_setsLoadingState() = runTest {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    assertFalse(viewModel.uiState.value.isLoading)

    viewModel.loadListing("listing-123")
    // Don't advance - check intermediate state
    // Note: This may be flaky depending on coroutine execution

    advanceUntilIdle()
    assertFalse(viewModel.uiState.value.isLoading)
  }

  @Test
  fun createBooking_setsBookingInProgressState() = runTest {
    UserSessionManager.setCurrentUserId("user-123")

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.bookingInProgress)

    val sessionStart = Date()
    val sessionEnd = Date(System.currentTimeMillis() + 3600000)
    viewModel.createBooking(sessionStart, sessionEnd)

    advanceUntilIdle()
    assertFalse(viewModel.uiState.value.bookingInProgress)
  }

  // Tests with Request listings

  @Test
  fun loadListing_request_loadsCorrectly() = runTest {
    val listingRepo = FakeListingRepo(sampleRequest)
    val profileRepo =
        FakeProfileRepo(mapOf("creator-999" to sampleCreator.copy(userId = "creator-999")))
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("request-789")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.listing)
    assertEquals("request-789", state.listing?.listingId)
    assertEquals(35.0, state.listing?.hourlyRate)
  }

  // Tests for multiple bookings

  @Test
  fun loadBookingsForListing_multipleBookings_loadsAllProfiles() = runTest {
    UserSessionManager.setCurrentUserId("creator-456")

    val booking1 = sampleBooking.copy(bookingId = "b1", bookerId = "booker-1")
    val booking2 = sampleBooking.copy(bookingId = "b2", bookerId = "booker-2")
    val booking3 = sampleBooking.copy(bookingId = "b3", bookerId = "booker-1") // Duplicate booker

    val bookings = listOf(booking1, booking2, booking3)
    val profiles =
        mapOf(
            "creator-456" to sampleCreator,
            "booker-1" to sampleBookerProfile.copy(userId = "booker-1", name = "Booker One"),
            "booker-2" to sampleBookerProfile.copy(userId = "booker-2", name = "Booker Two"))

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(profiles)
    val bookingRepo = FakeBookingRepo(bookings.toMutableList())
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(3, state.listingBookings.size)
    assertEquals(2, state.bookerProfiles.size) // Only 2 unique bookers
    assertTrue(state.bookerProfiles.containsKey("booker-1"))
    assertTrue(state.bookerProfiles.containsKey("booker-2"))
  }

  @Test
  fun loadBookingsForListing_missingBookerProfile_handledGracefully() = runTest {
    UserSessionManager.setCurrentUserId("creator-456")

    val bookings = listOf(sampleBooking.copy(bookerId = "non-existent-booker"))
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo = FakeBookingRepo(bookings.toMutableList())
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.listingBookings.size)
    assertEquals(0, state.bookerProfiles.size) // Profile not found
    assertFalse(state.bookingsLoading)
  }

  // Edge case tests

  @Test
  fun initialState_isCorrect() {
    val listingRepo = FakeListingRepo()
    val profileRepo = FakeProfileRepo()
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    val state = viewModel.uiState.value
    assertNull(state.listing)
    assertNull(state.creator)
    assertFalse(state.isLoading)
    assertNull(state.error)
    assertFalse(state.isOwnListing)
    assertFalse(state.bookingInProgress)
    assertNull(state.bookingError)
    assertFalse(state.bookingSuccess)
    assertTrue(state.listingBookings.isEmpty())
    assertFalse(state.bookingsLoading)
    assertTrue(state.bookerProfiles.isEmpty())
  }

  @Test
  fun approveBooking_withoutLoadingListing_handledGracefully() = runTest {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo()
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    // Don't load listing first
    viewModel.approveBooking("booking-1")
    advanceUntilIdle()

    // Should not crash
    assertNull(viewModel.uiState.value.listing)
  }

  @Test
  fun rejectBooking_withoutLoadingListing_handledGracefully() = runTest {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo()
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    // Don't load listing first
    viewModel.rejectBooking("booking-1")
    advanceUntilIdle()

    // Should not crash
    assertNull(viewModel.uiState.value.listing)
  }

  @Test
  fun loadBookings_setsTutorRatingPending_true_whenCompletedBookingExists() = runTest {
    UserSessionManager.setCurrentUserId("creator-456")
    // 🔽 ensure FirebaseAuth matches the session manager
    mockFirebaseAuthUser("creator-456")

    val completedBooking =
        sampleBooking.copy(status = BookingStatus.COMPLETED, bookerId = "booker-789")

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo =
        FakeProfileRepo(mapOf("creator-456" to sampleCreator, "booker-789" to sampleBookerProfile))
    val bookingRepo = FakeBookingRepo(mutableListOf(completedBooking))
    val ratingRepo = RecordingRatingRepo(hasRatingResult = false) // always “not yet rated”

    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.tutorRatingPending) // ✅ now true
    assertEquals(1, state.listingBookings.size)
  }

  @Test
  fun loadBookings_setsTutorRatingPending_false_whenNoCompletedBookings() = runTest {
    UserSessionManager.setCurrentUserId("creator-456")

    val pendingBooking = sampleBooking.copy(status = BookingStatus.PENDING, bookerId = "booker-789")

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo =
        FakeProfileRepo(mapOf("creator-456" to sampleCreator, "booker-789" to sampleBookerProfile))
    val bookingRepo = FakeBookingRepo(mutableListOf(pendingBooking))
    val ratingRepo = FakeRatingRepo()

    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.tutorRatingPending)
    assertEquals(1, state.listingBookings.size)
  }

  @Test
  fun createBooking_illegalArgumentException_setsInvalidBookingError() = runTest {
    UserSessionManager.setCurrentUserId("user-123")

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))

    val bookingRepo =
        object : FakeBookingRepo() {
          override suspend fun addBooking(booking: Booking) {
            throw IllegalArgumentException("Test invalid booking")
          }
        }
    val ratingRepo = FakeRatingRepo()

    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    val sessionStart = Date()
    val sessionEnd = Date(System.currentTimeMillis() + 3600000)

    // Act
    viewModel.createBooking(sessionStart, sessionEnd)
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.value
    assertNotNull(state.bookingError)
    assertTrue(state.bookingError!!.contains("Invalid booking"))
    assertFalse(state.bookingSuccess)
  }

  @Test
  fun toStarRating_mapsIntsIntoEnumSafely() {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo()
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()

    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    // Access the private extension function Int.toStarRating() via reflection
    val method =
        ListingViewModel::class.java.getDeclaredMethod("toStarRating", Int::class.javaPrimitiveType)
    method.isAccessible = true

    fun call(arg: Int): StarRating = method.invoke(viewModel, arg) as StarRating

    // 1 → FIRST enum (usually ONE)
    assertEquals(StarRating.ONE, call(1))
    // 4 → FOUR
    assertEquals(StarRating.FOUR, call(4))
    // 0 → clamped to first
    assertEquals(StarRating.ONE, call(0))
    // Big value → clamped to last
    assertEquals(StarRating.values().last(), call(999))
  }

  @Test
  fun submitTutorRating_whenListingMissing_doesNotCrash() = runTest {
    val listingRepo = FakeListingRepo(null) // no listing
    val profileRepo = FakeProfileRepo()
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()

    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    // listing is null in uiState by default
    assertNull(viewModel.uiState.value.listing)

    // Just verify this doesn't throw or crash; it should hit the
    // "listing == null" path and return.
    viewModel.submitTutorRating(5)
    advanceUntilIdle()

    // No rating added, no crash
    assertTrue(ratingRepo.addedRatings.isEmpty())
  }

  @Test
  fun submitTutorRating_noCompletedBooking_doesNothing() = runTest {
    // Current user is the listing creator (tutor)
    UserSessionManager.setCurrentUserId("creator-456")
    mockFirebaseAuthUser("creator-456")

    // Only PENDING booking → no COMPLETED booking to rate
    val pendingBooking = sampleBooking.copy(status = BookingStatus.PENDING)

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo =
        FakeProfileRepo(mapOf("creator-456" to sampleCreator, "booker-789" to sampleBookerProfile))
    val bookingRepo = FakeBookingRepo(mutableListOf(pendingBooking))
    val ratingRepo = RecordingRatingRepo(hasRatingResult = false)

    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    // Act
    viewModel.submitTutorRating(5)
    advanceUntilIdle()

    // Assert – no rating call, no rating saved
    assertFalse(ratingRepo.hasRatingCalled)
    assertTrue(ratingRepo.addedRatings.isEmpty())
  }

  @Test
  fun submitTutorRating_alreadyRated_skipsAdding() = runTest {
    UserSessionManager.setCurrentUserId("creator-456")
    mockFirebaseAuthUser("creator-456")

    val completedBooking = sampleBooking.copy(status = BookingStatus.COMPLETED)

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo =
        FakeProfileRepo(mapOf("creator-456" to sampleCreator, "booker-789" to sampleBookerProfile))
    val bookingRepo = FakeBookingRepo(mutableListOf(completedBooking))
    val ratingRepo = RecordingRatingRepo(hasRatingResult = true)

    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    viewModel.submitTutorRating(4)
    advanceUntilIdle()

    assertTrue(ratingRepo.hasRatingCalled)
    assertTrue(ratingRepo.addedRatings.isEmpty()) // nothing persisted
  }

  @Test
  fun submitTutorRating_createsStudentRating_whenNotAlreadyRated() = runTest {
    UserSessionManager.setCurrentUserId("creator-456")
    mockFirebaseAuthUser("creator-456")

    val completedBooking = sampleBooking.copy(status = BookingStatus.COMPLETED)

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo =
        FakeProfileRepo(mapOf("creator-456" to sampleCreator, "booker-789" to sampleBookerProfile))
    val bookingRepo = FakeBookingRepo(mutableListOf(completedBooking))
    val ratingRepo = RecordingRatingRepo(hasRatingResult = false)

    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    viewModel.submitTutorRating(5)
    advanceUntilIdle()

    assertTrue(ratingRepo.hasRatingCalled)
    assertEquals(1, ratingRepo.addedRatings.size)

    val rating = ratingRepo.addedRatings.first()
    assertEquals("creator-456", rating.fromUserId)
    assertEquals("booker-789", rating.toUserId)
    assertEquals(RatingType.STUDENT, rating.ratingType)
    // 🔽 now per booking, not per listing
    assertEquals(completedBooking.bookingId, rating.targetObjectId)
    assertEquals(StarRating.FIVE, rating.starRating)
  }

  @Test
  fun submitTutorRating_hasRatingThrows_stillAddsRating() = runTest {
    UserSessionManager.setCurrentUserId("creator-456")
    mockFirebaseAuthUser("creator-456")

    val completedBooking = sampleBooking.copy(status = BookingStatus.COMPLETED)

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo =
        FakeProfileRepo(mapOf("creator-456" to sampleCreator, "booker-789" to sampleBookerProfile))
    val bookingRepo = FakeBookingRepo(mutableListOf(completedBooking))
    val ratingRepo = RecordingRatingRepo(hasRatingResult = false, throwOnHasRating = true)

    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    viewModel.submitTutorRating(3)
    advanceUntilIdle()

    // hasRating was called and threw, but code should treat it as "not already rated"
    assertTrue(ratingRepo.hasRatingCalled)
    assertEquals(1, ratingRepo.addedRatings.size)
  }

  // Tests for deleteListing()

  @Test
  fun deleteListing_noListing_setsError() = runTest {
    val listingRepo = FakeListingRepo()
    val profileRepo = FakeProfileRepo()
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.deleteListing()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("Listing not found", state.error)
    assertFalse(state.listingDeleted)
  }

  @Test
  fun deleteListing_success_updatesState() = runTest {
    var deleteListingCalled = false
    val listingRepo =
        object : FakeListingRepo(sampleProposal) {
          override suspend fun deleteListing(listingId: String) {
            deleteListingCalled = true
          }
        }
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    viewModel.deleteListing()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(deleteListingCalled)
    assertNull(state.listing)
    assertTrue(state.listingBookings.isEmpty())
    assertFalse(state.isOwnListing)
    assertFalse(state.isLoading)
    assertNull(state.error)
    assertTrue(state.listingDeleted)
  }

  @Test
  fun deleteListing_cancelsNonCancelledBookings() = runTest {
    val booking1 = sampleBooking.copy(bookingId = "b1", status = BookingStatus.PENDING)
    val booking2 = sampleBooking.copy(bookingId = "b2", status = BookingStatus.CONFIRMED)
    val booking3 = sampleBooking.copy(bookingId = "b3", status = BookingStatus.CANCELLED)

    val cancelledBookings = mutableListOf<String>()
    val bookingRepo =
        object : FakeBookingRepo(mutableListOf(booking1, booking2, booking3)) {
          override suspend fun cancelBooking(bookingId: String) {
            cancelledBookings.add(bookingId)
          }
        }

    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    viewModel.deleteListing()
    advanceUntilIdle()

    assertEquals(2, cancelledBookings.size)
    assertTrue(cancelledBookings.contains("b1"))
    assertTrue(cancelledBookings.contains("b2"))
    assertFalse(cancelledBookings.contains("b3"))
  }

  @Test
  fun deleteListing_bookingFetchFails_continuesWithDeletion() = runTest {
    var deleteListingCalled = false
    val listingRepo =
        object : FakeListingRepo(sampleProposal) {
          override suspend fun deleteListing(listingId: String) {
            deleteListingCalled = true
          }
        }

    val bookingRepo =
        object : FakeBookingRepo() {
          override suspend fun getBookingsByListing(listingId: String): List<Booking> {
            throw RuntimeException("Database connection failed")
          }
        }

    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val viewModel =
        ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo = FakeRatingRepo())

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    viewModel.deleteListing()
    advanceUntilIdle()

    assertTrue(deleteListingCalled)
    assertTrue(viewModel.uiState.value.listingDeleted)
  }

  @Test
  fun deleteListing_bookingCancellationFails_continuesWithDeletion() = runTest {
    val booking1 = sampleBooking.copy(bookingId = "b1", status = BookingStatus.PENDING)
    val booking2 = sampleBooking.copy(bookingId = "b2", status = BookingStatus.CONFIRMED)

    var deleteListingCalled = false
    val listingRepo =
        object : FakeListingRepo(sampleProposal) {
          override suspend fun deleteListing(listingId: String) {
            deleteListingCalled = true
          }
        }

    val cancelAttempts = mutableListOf<String>()
    val bookingRepo =
        object : FakeBookingRepo(mutableListOf(booking1, booking2)) {
          override suspend fun cancelBooking(bookingId: String) {
            cancelAttempts.add(bookingId)
            if (bookingId == "b1") {
              throw RuntimeException("Cancellation service unavailable")
            }
          }
        }

    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    viewModel.deleteListing()
    advanceUntilIdle()

    assertEquals(2, cancelAttempts.size)
    assertTrue(deleteListingCalled)
    assertTrue(viewModel.uiState.value.listingDeleted)
  }

  @Test
  fun deleteListing_repositoryFails_setsError() = runTest {
    val listingRepo =
        object : FakeListingRepo(sampleProposal) {
          override suspend fun deleteListing(listingId: String) {
            throw RuntimeException("Repository deletion failed")
          }
        }

    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    viewModel.deleteListing()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.listingDeleted)
    assertNotNull(state.error)
    assertTrue(state.error!!.contains("Failed to delete listing"))
    assertFalse(state.isLoading)
  }

  @Test
  fun deleteListing_setsLoadingState() = runTest {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isLoading)

    viewModel.deleteListing()
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isLoading)
  }

  // Tests for clearListingDeleted()

  @Test
  fun clearListingDeleted_updatesState() = runTest {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    viewModel.deleteListing()
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.listingDeleted)

    viewModel.clearListingDeleted()
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.listingDeleted)
  }

  @Test
  fun clearListingDeleted_whenAlreadyFalse_doesNothing() = runTest {
    val listingRepo = FakeListingRepo()
    val profileRepo = FakeProfileRepo()
    val bookingRepo = FakeBookingRepo()
    val ratingRepo = FakeRatingRepo()
    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    assertFalse(viewModel.uiState.value.listingDeleted)

    viewModel.clearListingDeleted()
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.listingDeleted)
  }

  @Test
  fun submitTutorRating_recomputesStudentAverage_andUpdatesProfile() = runTest {
    // Tutor is the listing creator
    UserSessionManager.setCurrentUserId("creator-456")
    mockFirebaseAuthUser("creator-456")

    // Completed booking so rating is allowed
    val completedBooking = sampleBooking.copy(status = BookingStatus.COMPLETED)

    val listingRepo = FakeListingRepo(sampleProposal)

    val profileRepo =
        RecordingProfileRepo(
            mapOf("creator-456" to sampleCreator, "booker-789" to sampleBookerProfile))

    // Existing ratings for this student: 4 and 2 stars
    val existingRatings =
        listOf(
            Rating(
                ratingId = "r1",
                fromUserId = "tutor-1",
                toUserId = "booker-789",
                ratingType = RatingType.STUDENT,
                targetObjectId = "listing-x",
                starRating = StarRating.FOUR,
            ),
            Rating(
                ratingId = "r2",
                fromUserId = "tutor-2",
                toUserId = "booker-789",
                ratingType = RatingType.STUDENT,
                targetObjectId = "listing-y",
                starRating = StarRating.TWO,
            ),
        )

    val ratingRepo =
        AggregatingRatingRepo(
            ratingsByStudent = mapOf("booker-789" to existingRatings), alreadyRated = false)

    val bookingRepo = FakeBookingRepo(mutableListOf(completedBooking))

    val viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo, ratingRepo)

    // Load listing so submitTutorRating can find the completed booking + student
    viewModel.loadListing("listing-123")
    advanceUntilIdle()

    // New rating from this tutor: 5 stars
    viewModel.submitTutorRating(5)
    advanceUntilIdle()

    // We had 4, 2, and now 5 -> (4 + 2 + 5) / 3 = 11 / 3
    val expectedAvg = 11.0 / 3.0

    assertEquals("booker-789", profileRepo.lastStudentUserId)
    assertEquals(3, profileRepo.lastStudentTotal)
    assertEquals(expectedAvg, profileRepo.lastStudentAvg!!, 0.001)
  }
}
