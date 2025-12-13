package com.android.sample.screen

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.android.sample.model.authentication.AuthenticationRepository
import com.android.sample.model.authentication.FirebaseTestRule
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.communication.ConversationManagerInter
import com.android.sample.model.communication.conversation.ConvRepository
import com.android.sample.model.communication.conversation.Conversation
import com.android.sample.model.communication.conversation.ConversationRepositoryProvider
import com.android.sample.model.communication.conversation.Message
import com.android.sample.model.communication.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.overViewConv.OverViewConvRepositoryProvider
import com.android.sample.model.communication.overViewConv.OverViewConversation
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.GpsLocationProvider
import com.android.sample.model.map.Location
import com.android.sample.model.map.LocationRepository
import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.rating.RatingType
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.profile.DESC_EMPTY_MSG
import com.android.sample.ui.profile.EMAIL_EMPTY_MSG
import com.android.sample.ui.profile.EMAIL_INVALID_MSG
import com.android.sample.ui.profile.GPS_FAILED_MSG
import com.android.sample.ui.profile.LOCATION_EMPTY_MSG
import com.android.sample.ui.profile.LOCATION_PERMISSION_DENIED_MSG
import com.android.sample.ui.profile.MyProfileViewModel
import com.android.sample.ui.profile.NAME_EMPTY_MSG
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class MyProfileViewModelTest {

  @get:Rule val firebaseRule = FirebaseTestRule()

  private val dispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
    BookingRepositoryProvider.setForTests(FakeBookingRepo())
    ConversationRepositoryProvider.setForTests(FakeConversationRepo())
    OverViewConvRepositoryProvider.setForTests(FakeOverViewConvRepo())
    UserSessionManager.setCurrentUserId("testUid")
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    UserSessionManager.clearSession()
  }

  // -------- Fake repositories ------------------------------------------------------

  private open class FakeProfileRepo(private var storedProfile: Profile? = null) :
      ProfileRepository {
    var updatedProfile: Profile? = null
    var updateCalled = false
    var getProfileCalled = false

    override fun getNewUid(): String = "fake"

    override fun getCurrentUserId(): String = "test-user-id"

    override suspend fun getProfile(userId: String): Profile {
      getProfileCalled = true
      return storedProfile ?: error("Profile not found")
    }

    override suspend fun addProfile(profile: Profile) {}

    override suspend fun updateProfile(userId: String, profile: Profile) {
      updateCalled = true
      updatedProfile = profile
    }

    override suspend fun deleteProfile(userId: String) {}

    override suspend fun getAllProfiles(): List<Profile> = emptyList()

    override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
        emptyList<Profile>()

    override suspend fun getProfileById(userId: String) =
        storedProfile ?: error("Profile not found")

    override suspend fun getSkillsForUser(userId: String) = emptyList<Skill>()

    override suspend fun updateTutorRatingFields(
        userId: String,
        averageRating: Double,
        totalRatings: Int
    ) {
      // no-op in this test fake
    }

    override suspend fun updateStudentRatingFields(
        userId: String,
        averageRating: Double,
        totalRatings: Int
    ) {
      // no-op in this test fake
    }
  }

  private class FakeConversationRepo : ConvRepository {
    override fun getNewUid(): String {
      TODO("Not yet implemented")
    }

    override suspend fun getConv(convId: String): Conversation? {
      TODO("Not yet implemented")
    }

    override suspend fun createConv(conversation: Conversation) {
      TODO("Not yet implemented")
    }

    override suspend fun deleteConv(convId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun sendMessage(convId: String, message: Message) {
      TODO("Not yet implemented")
    }

    override fun listenMessages(convId: String): Flow<List<Message>> {
      TODO("Not yet implemented")
    }
  }

  private class FakeOverViewConvRepo : OverViewConvRepository {

    private val data = mutableListOf<OverViewConversation>()

    override fun getNewUid(): String = "fake-overview-id"

    override suspend fun getOverViewConvUser(userId: String): List<OverViewConversation> {
      return data.filter { it.overViewOwnerId == userId }
    }

    override suspend fun addOverViewConvUser(overView: OverViewConversation) {
      data.removeAll { it.overViewId == overView.overViewId }
      data.add(overView)
    }

    override suspend fun deleteOverViewConvUser(convId: String) {
      data.removeAll { it.linkedConvId == convId }
    }

    override suspend fun deleteOverViewById(overViewId: String) {
      data.removeAll { it.overViewId == overViewId }
    }

    override fun listenOverView(userId: String): Flow<List<OverViewConversation>> {
      return flowOf(emptyList())
    }
  }


  private class FakeLocationRepo(
      private val results: List<Location> =
          listOf(Location(name = "Paris"), Location(name = "Rome"))
  ) : LocationRepository {
    var lastQuery: String? = null
    var searchCalled = false

    override suspend fun search(query: String): List<Location> {
      lastQuery = query
      searchCalled = true
      return if (query.isNotBlank()) results else emptyList()
    }
  }

  private class FakeBookingRepo : BookingRepository {
    override fun getNewUid(): String = "fake-booking-id"

    override suspend fun getAllBookings(): List<Booking> = emptyList()

    override suspend fun getBooking(bookingId: String): Booking? = null

    override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

    override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

    override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

    override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

    override suspend fun addBooking(booking: Booking) {}

    override suspend fun updateBooking(bookingId: String, booking: Booking) {}

    override suspend fun deleteBooking(bookingId: String) {}

    override suspend fun deleteAllBookingOfUser(userId: String) {}

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

    override suspend fun updatePaymentStatus(
        bookingId: String,
        paymentStatus: com.android.sample.model.booking.PaymentStatus
    ) {}

    override suspend fun confirmBooking(bookingId: String) {}

    override suspend fun completeBooking(bookingId: String) {}

    override suspend fun cancelBooking(bookingId: String) {}
  }

  // Minimal fake ListingRepository to satisfy the ViewModel dependency
  private class FakeListingRepo : ListingRepository {
    override fun getNewUid(): String = "fake-listing-id"

    override suspend fun getAllListings(): List<Listing> = emptyList()

    override suspend fun getProposals(): List<Proposal> = emptyList()

    override suspend fun getRequests(): List<Request> = emptyList()

    override suspend fun getListing(listingId: String): Listing? = null

    override suspend fun getListingsByUser(userId: String): List<Listing> = emptyList()

    override suspend fun addProposal(proposal: Proposal) {}

    override suspend fun addRequest(request: Request) {}

    override suspend fun updateListing(listingId: String, listing: Listing) {}

    override suspend fun deleteListing(listingId: String) {}

    override suspend fun deleteAllListingOfUser(userId: String) {}

    override suspend fun deactivateListing(listingId: String) {}

    override suspend fun searchBySkill(skill: Skill): List<Listing> = emptyList()

    override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> =
        emptyList()
  }

  private class FakeRatingRepos : RatingRepository {
    override fun getNewUid(): String = "fake-rating-id"

    override suspend fun hasRating(
        fromUserId: String,
        toUserId: String,
        ratingType: RatingType,
        targetObjectId: String
    ): Boolean {
      // For these VM tests we don't care about duplicates, so "no rating yet" is fine.
      return false
    }

    override suspend fun getAllRatings(): List<Rating> = emptyList()

    override suspend fun getRating(ratingId: String): Rating? = null

    override suspend fun getRatingsByFromUser(fromUserId: String): List<Rating> = emptyList()

    override suspend fun getRatingsByToUser(toUserId: String): List<Rating> =
        throw RuntimeException("Failed to load ratings.")

    override suspend fun getRatingsOfListing(listingId: String): List<Rating> = emptyList()

    override suspend fun addRating(rating: Rating) = Unit

    override suspend fun updateRating(ratingId: String, rating: Rating) = Unit

    override suspend fun deleteRating(ratingId: String) = Unit

    override suspend fun getTutorRatingsOfUser(userId: String): List<Rating> = emptyList()

    override suspend fun getStudentRatingsOfUser(userId: String): List<Rating> = emptyList()

    override suspend fun deleteAllRatingOfUser(userId: String) {}
  }

  private class SuccessGpsProvider(
      private val lat: Double = 12.34,
      private val lon: Double = 56.78
  ) : GpsLocationProvider(ApplicationProvider.getApplicationContext()) {
    override suspend fun getCurrentLocation(timeoutMs: Long): android.location.Location? {
      val loc = android.location.Location("test")
      loc.latitude = lat
      loc.longitude = lon
      return loc
    }
  }

  // -------- Helpers ------------------------------------------------------

  private fun makeProfile(
      id: String = "1",
      name: String = "Kendrick",
      email: String = "kdot@example.com",
      location: Location = Location(name = "Compton"),
      desc: String = "Rap tutor"
  ) = Profile(id, name, email, location = location, description = desc)

  private fun newVm(
      repo: ProfileRepository = FakeProfileRepo(),
      locRepo: LocationRepository = FakeLocationRepo(),
      listingRepo: ListingRepository = FakeListingRepo(),
      ratingRepo: RatingRepository = FakeRatingRepos(),
      bookingRepo: BookingRepository = FakeBookingRepo()
  ): MyProfileViewModel {
    return MyProfileViewModel(
        profileRepository = repo,
        locationRepository = locRepo,
        listingRepository = listingRepo,
        ratingsRepository = ratingRepo,
        bookingRepository = bookingRepo,
        sessionManager = UserSessionManager)
  }

  private class NullGpsProvider : GpsLocationProvider(ApplicationProvider.getApplicationContext()) {
    override suspend fun getCurrentLocation(timeoutMs: Long): android.location.Location? = null
  }

  private class SecurityExceptionGpsProvider :
      GpsLocationProvider(ApplicationProvider.getApplicationContext()) {
    override suspend fun getCurrentLocation(timeoutMs: Long): android.location.Location? {
      throw SecurityException("Permission denied")
    }
  }
  // -------- Tests --------------------------------------------------------

  @Test
  fun loadProfile_populatesUiState() = runTest {
    val profile = makeProfile()
    val repo = FakeProfileRepo(profile)
    val vm = newVm(repo)

    vm.loadProfile()
    advanceUntilIdle()

    val ui = vm.uiState.value
    assertEquals(profile.name, ui.name)
    assertEquals(profile.email, ui.email)
    assertEquals(profile.location, ui.selectedLocation)
    assertEquals(profile.description, ui.description)
    assertFalse(ui.isLoading)
    assertNull(ui.loadError)
    assertTrue(repo.getProfileCalled)
  }

  @Test
  fun setName_updatesName_and_setsErrorIfBlank() {
    val vm = newVm()

    vm.setName("K Dot")
    assertEquals("K Dot", vm.uiState.value.name)
    assertNull(vm.uiState.value.invalidNameMsg)

    vm.setName("")
    assertEquals(NAME_EMPTY_MSG, vm.uiState.value.invalidNameMsg)
  }

  @Test
  fun setEmail_validatesFormat_andRequired() {
    val vm = newVm()

    vm.setEmail("")
    assertEquals(EMAIL_EMPTY_MSG, vm.uiState.value.invalidEmailMsg)

    vm.setEmail("invalid-email")
    assertEquals(EMAIL_INVALID_MSG, vm.uiState.value.invalidEmailMsg)

    vm.setEmail("good@mail.com")
    assertNull(vm.uiState.value.invalidEmailMsg)
  }

  @Test
  fun setLocation_updatesLocation_andClearsError() {
    val vm = newVm()

    vm.setLocation(Location(name = "Paris"))
    val ui = vm.uiState.value
    assertEquals("Paris", ui.selectedLocation?.name)
    assertNull(ui.invalidLocationMsg)
  }

  @Test
  fun setDescription_updatesDesc_and_setsErrorIfBlank() {
    val vm = newVm()

    vm.setDescription("Music mentor")
    assertEquals("Music mentor", vm.uiState.value.description)
    assertNull(vm.uiState.value.invalidDescMsg)

    vm.setDescription("")
    assertEquals(DESC_EMPTY_MSG, vm.uiState.value.invalidDescMsg)
  }

  @Test
  fun setError_setsAllErrorMessages_whenFieldsInvalid() {
    val vm = newVm()
    vm.setError()

    val ui = vm.uiState.value
    assertEquals(NAME_EMPTY_MSG, ui.invalidNameMsg)
    assertEquals(EMAIL_EMPTY_MSG, ui.invalidEmailMsg)
    assertEquals(LOCATION_EMPTY_MSG, ui.invalidLocationMsg)
    assertEquals(DESC_EMPTY_MSG, ui.invalidDescMsg)
  }

  @Test
  fun isValid_returnsTrue_onlyWhenAllFieldsAreCorrect() {
    val vm = newVm()

    vm.setName("Test")
    vm.setEmail("test@mail.com")
    vm.setLocation(Location(name = "Paris"))
    vm.setDescription("Teacher")

    assertTrue(vm.uiState.value.isValid)

    vm.setEmail("wrong")
    assertFalse(vm.uiState.value.isValid)
  }

  @Test
  fun setLocationQuery_updatesQuery_andFetchesResults() = runTest {
    val locRepo = FakeLocationRepo()
    val vm = newVm(locRepo = locRepo)

    vm.setLocationQuery("Par")
    advanceUntilIdle()

    val ui = vm.uiState.value
    assertEquals("Par", ui.locationQuery)
    assertTrue(locRepo.searchCalled)
    assertEquals(2, ui.locationSuggestions.size)
    assertEquals("Paris", ui.locationSuggestions[0].name)
  }

  @Test
  fun setLocationQuery_emptyQuery_setsError_andClearsSuggestions() = runTest {
    val locRepo = FakeLocationRepo()
    val vm = newVm(locRepo = locRepo)

    vm.setLocationQuery("")
    advanceUntilIdle()

    val ui = vm.uiState.value
    assertEquals(LOCATION_EMPTY_MSG, ui.invalidLocationMsg)
    assertTrue(ui.locationSuggestions.isEmpty())
  }

  @Test
  fun editProfile_doesNotUpdate_whenInvalid() = runTest {
    val repo = FakeProfileRepo()
    val vm = newVm(repo)

    // invalid by default
    vm.editProfile()
    advanceUntilIdle()

    assertFalse(repo.updateCalled)
  }

  @Test
  fun editProfile_updatesRepository_whenValid() = runTest {
    val repo = FakeProfileRepo()
    val vm = newVm(repo)

    vm.setName("Kendrick Lamar")
    vm.setEmail("kdot@gmail.com")
    vm.setLocation(Location(name = "Compton"))
    vm.setDescription("Hip-hop tutor")

    vm.editProfile()
    advanceUntilIdle()

    assertTrue(repo.updateCalled)
    val updated = repo.updatedProfile!!
    assertEquals("Kendrick Lamar", updated.name)
    assertEquals("kdot@gmail.com", updated.email)
    assertEquals("Compton", updated.location.name)
    assertEquals("Hip-hop tutor", updated.description)
  }

  @Test
  fun editProfile_handlesRepositoryException_gracefully() = runTest {
    val failingRepo =
        object : FakeProfileRepo() {
          override suspend fun updateProfile(userId: String, profile: Profile) {
            throw RuntimeException("Update failed")
          }
        }
    val vm = newVm(failingRepo)

    vm.setName("Good")
    vm.setEmail("good@mail.com")
    vm.setLocation(Location(name = "LA"))
    vm.setDescription("Mentor")

    // Should not crash
    vm.editProfile()
    advanceUntilIdle()

    assertTrue(true)
  }

  @Test
  fun loadProfile_withUserId_loadsCorrectProfile() = runTest {
    // Given
    val profile = makeProfile()
    val repo = FakeProfileRepo(profile)
    val vm = newVm(repo)

    // When - load profile with specific userId
    vm.loadProfile("specificUserId")
    advanceUntilIdle()

    // Then - profile should be loaded
    val ui = vm.uiState.value
    assertEquals(profile.name, ui.name)
    assertEquals(profile.email, ui.email)
    assertEquals(profile.location, ui.selectedLocation)
    assertEquals(profile.description, ui.description)
    assertTrue(repo.getProfileCalled)
  }

  @Test
  fun loadProfile_storesUserIdInState() = runTest {
    // Given
    val profile = makeProfile()
    val repo = FakeProfileRepo(profile)
    UserSessionManager.setCurrentUserId("originalUserId")
    val vm = newVm(repo)

    // When - load profile with different userId
    vm.loadProfile("differentUserId")
    advanceUntilIdle()

    // Then - UI state should have the new userId
    val ui = vm.uiState.value
    assertEquals("differentUserId", ui.userId)
  }

  @Test
  fun loadProfile_withoutParameter_usesDefaultUserId() = runTest {
    // Given
    val profile = makeProfile()
    val repo = FakeProfileRepo(profile)
    UserSessionManager.setCurrentUserId("defaultUserId")
    val vm = newVm(repo)

    // When - load profile without parameter
    vm.loadProfile()
    advanceUntilIdle()

    // Then - UI state should have the default userId
    val ui = vm.uiState.value
    assertEquals("defaultUserId", ui.userId)
  }

  @Test
  fun editProfile_usesUserIdFromState() = runTest {
    // Given
    val profile = makeProfile()
    val repo = FakeProfileRepo(profile)
    UserSessionManager.setCurrentUserId("originalUserId")
    val vm = newVm(repo)

    // Load profile with different userId
    vm.loadProfile("targetUserId")
    advanceUntilIdle()

    // Set valid data
    vm.setName("New Name")
    vm.setEmail("new@email.com")
    vm.setLocation(Location(name = "New Location"))
    vm.setDescription("New Description")

    // When - edit profile
    vm.editProfile()
    advanceUntilIdle()

    // Then - should update with userId from state, not original VM userId
    val updated = repo.updatedProfile
    assertNotNull(updated)
    assertEquals("targetUserId", updated?.userId)
    assertEquals("New Name", updated?.name)
  }

  @Test
  fun fetchLocationFromGps_success_updatesSelectedLocation_andClearsError() = runTest {
    val vm = newVm()
    val provider = SuccessGpsProvider(12.34, 56.78)

    vm.fetchLocationFromGps(provider, context = ApplicationProvider.getApplicationContext())
    advanceUntilIdle()

    val ui = vm.uiState.value
    // use non-null assertion because the test expects a location to be set
    assertEquals(12.34, ui.selectedLocation!!.latitude, 0.0001)
    assertEquals(56.78, ui.selectedLocation!!.longitude, 0.0001)
    assertEquals("12.34, 56.78", ui.locationQuery)
    assertNull(ui.invalidLocationMsg)
  }

  @Test
  fun fetchLocationFromGps_nullResult_setsFailedToObtainError() = runTest {
    val vm = newVm()
    val provider = NullGpsProvider()

    vm.fetchLocationFromGps(provider, context = ApplicationProvider.getApplicationContext())
    advanceUntilIdle()

    val ui = vm.uiState.value
    assertEquals(GPS_FAILED_MSG, ui.invalidLocationMsg)
  }

  @Test
  fun fetchLocationFromGps_securityException_setsPermissionDeniedError() = runTest {
    val vm = newVm()
    val provider = SecurityExceptionGpsProvider()

    vm.fetchLocationFromGps(provider, context = ApplicationProvider.getApplicationContext())
    advanceUntilIdle()

    val ui = vm.uiState.value
    assertEquals(LOCATION_PERMISSION_DENIED_MSG, ui.invalidLocationMsg)
  }

  @Test
  fun loadUserListings_handlesRepositoryException_setsListingsError() = runTest {
    // Listing repo that throws to exercise the catch branch
    val failingListingRepo =
        object : ListingRepository by FakeListingRepo() {
          override suspend fun getListingsByUser(userId: String): List<Listing> {
            throw RuntimeException("Listings fetch failed")
          }
        }

    val repo = FakeProfileRepo(makeProfile())
    val vm = newVm(repo = repo, listingRepo = failingListingRepo)

    // Trigger listings load
    vm.loadUserListings("ownerId")
    advanceUntilIdle()

    val ui = vm.uiState.value
    assertTrue(ui.listings.isEmpty())
    assertFalse(ui.listingsLoading)
    assertEquals("Failed to load listings.", ui.listingsLoadError)
  }

  @Test
  fun setError_setsEmailFormatError_whenEmailMalformed_and_setsOtherErrors() {
    val vm = newVm()

    // Set malformed email and leave other fields empty
    vm.setEmail("not-an-email")
    vm.setError()

    val ui = vm.uiState.value
    assertEquals("Email is not in the right format", ui.invalidEmailMsg)
    assertEquals("Name cannot be empty", ui.invalidNameMsg)
    assertEquals("Location cannot be empty", ui.invalidLocationMsg)
    assertEquals("Description cannot be empty", ui.invalidDescMsg)
  }

  @Test
  fun isValid_false_whenMissingLocationOrDescription_and_true_afterSettingBoth() {
    val vm = newVm()

    vm.setName("Test")
    vm.setEmail("test@mail.com")
    // no location, no description -> invalid
    assertFalse(vm.uiState.value.isValid)

    vm.setDescription("Teacher")
    // still missing location -> invalid
    assertFalse(vm.uiState.value.isValid)

    vm.setLocation(Location(name = "Paris"))
    // now all required fields present and valid -> valid
    assertTrue(vm.uiState.value.isValid)
  }

  @Test
  fun permissionGranted_branch_executes_fetchLocationFromGps() {
    val repo = mock<ProfileRepository>()
    val listingRepo = mock<ListingRepository>()
    val context = mock<Context>()
    val ratingRepo = mock<RatingRepository>()

    val provider = GpsLocationProvider(context)
    UserSessionManager.setCurrentUserId("demo")
    val viewModel =
        MyProfileViewModel(
            repo,
            listingRepository = listingRepo,
            ratingsRepository = ratingRepo,
            sessionManager = UserSessionManager)

    viewModel.fetchLocationFromGps(provider, context)
  }

  @Test
  fun permissionDenied_branch_executes_onLocationPermissionDenied() = runTest {
    val repo = mock<ProfileRepository>()
    val listingRepo = mock<ListingRepository>()
    val ratingRepo = mock<RatingRepository>()
    UserSessionManager.setCurrentUserId("demo")

    val viewModel =
        MyProfileViewModel(
            repo,
            listingRepository = listingRepo,
            ratingsRepository = ratingRepo,
            sessionManager = UserSessionManager)

    viewModel.onLocationPermissionDenied()
  }

  @Test
  fun loadUserRatingFails_handlesRepositoryException_setsRatingsError() = runTest {
    val failingRatingRepo =
        object : RatingRepository by FakeRatingRepos() {
          override suspend fun getRatingsByToUser(toUserId: String): List<Rating> {
            throw RuntimeException("Ratings fetch failed")
          }
        }

    val repo = FakeProfileRepo(makeProfile())
    val vm = newVm(repo = repo, ratingRepo = failingRatingRepo)

    // Trigger ratings load
    vm.loadUserRatings("userId")
    advanceUntilIdle()

    val ui = vm.uiState.value
    assertTrue(ui.ratings.isEmpty())
    assertFalse(ui.ratingsLoading)
    assertEquals("Failed to load ratings.", ui.ratingsLoadError)
  }

  @Test
  fun clearUpdateSuccess_resetsFlag_afterSuccessfulUpdate() = runTest {
    val repo = FakeProfileRepo()
    val vm = newVm(repo)

    vm.setName("New Name")
    vm.setEmail("new@mail.com")
    vm.setLocation(Location(name = "Paris"))
    vm.setDescription("Desc")

    vm.editProfile()
    advanceUntilIdle()

    assertTrue(vm.uiState.value.updateSuccess)

    vm.clearUpdateSuccess()

    assertFalse(vm.uiState.value.updateSuccess)
  }

  @Test
  fun editProfile_doesNothing_whenNoFieldsChangedAfterLoad() = runTest {
    val stored =
        makeProfile(
            id = "u1",
            name = "Alice",
            email = "alice@mail.com",
            location = Location(name = "Lyon"),
            desc = "Tutor")
    val repo = FakeProfileRepo(storedProfile = stored)
    val vm = newVm(repo)

    vm.loadProfile()
    advanceUntilIdle()

    vm.editProfile()
    advanceUntilIdle()

    assertFalse(repo.updateCalled)
  }

  @Test
  fun editProfile_updates_whenAnyFieldChanges_afterLoad() = runTest {
    val stored =
        makeProfile(
            id = "u1",
            name = "Alice",
            email = "alice@mail.com",
            location = Location(name = "Lyon"),
            desc = "Tutor")
    val repo = FakeProfileRepo(stored)
    val vm = newVm(repo)

    vm.loadProfile()
    advanceUntilIdle()

    vm.setName("Alice Cooper")

    vm.editProfile()
    advanceUntilIdle()

    assertTrue(repo.updateCalled)
    val updated = repo.updatedProfile!!
    assertEquals("Alice Cooper", updated.name)
    assertEquals("alice@mail.com", updated.email)
    assertEquals("Lyon", updated.location.name)
    assertEquals("Tutor", updated.description)
  }

  @Test
  fun hasProfileChanged_false_whenProfilesAreIdentical() {
    val vm = newVm()
    val original =
        makeProfile(
            id = "u1",
            name = "A",
            email = "a@mail.com",
            location = Location(name = "Paris", latitude = 1.0, longitude = 2.0),
            desc = "Desc")
    val updated = original.copy()

    val m =
        MyProfileViewModel::class
            .java
            .getDeclaredMethod("hasProfileChanged", Profile::class.java, Profile::class.java)
    m.isAccessible = true

    val result = m.invoke(vm, original, updated) as Boolean
    assertFalse(result)
  }

  @Test
  fun hasProfileChanged_true_whenAnyFieldDiffers_includingLocationFields() {
    val vm = newVm()
    val original =
        makeProfile(
            id = "u1",
            name = "A",
            email = "a@mail.com",
            location = Location(name = "Paris", latitude = 1.0, longitude = 2.0),
            desc = "Desc")

    val changedName = original.copy(name = "B")
    val changedEmail = original.copy(email = "b@mail.com")
    val changedDesc = original.copy(description = "Other")
    val changedLocName = original.copy(location = original.location.copy(name = "Lyon"))
    val changedLat = original.copy(location = original.location.copy(latitude = 9.9))
    val changedLon = original.copy(location = original.location.copy(longitude = 8.8))

    val m =
        MyProfileViewModel::class
            .java
            .getDeclaredMethod("hasProfileChanged", Profile::class.java, Profile::class.java)
    m.isAccessible = true

    fun assertChanged(updated: Profile) {
      val result = m.invoke(vm, original, updated) as Boolean
      assertTrue(result)
    }

    assertChanged(changedName)
    assertChanged(changedEmail)
    assertChanged(changedDesc)
    assertChanged(changedLocName)
    assertChanged(changedLat)
    assertChanged(changedLon)
  }

  @Test
  fun loadUserBookings_catchesBookingException() = runTest {
    val failingBookingRepo =
        object : BookingRepository {
          override suspend fun getBookingsByUserId(userId: String): List<Booking> {
            throw RuntimeException("boom")
          }

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> {
            TODO("Not yet implemented")
          }

          override suspend fun getBookingsByListing(listingId: String): List<Booking> {
            TODO("Not yet implemented")
          }

          override suspend fun addBooking(booking: Booking) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBooking(bookingId: String, booking: Booking) {
            TODO("Not yet implemented")
          }

          override suspend fun deleteBooking(bookingId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
            TODO("Not yet implemented")
          }

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {
            TODO("Not yet implemented")
          }

          override suspend fun confirmBooking(bookingId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun completeBooking(bookingId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun cancelBooking(bookingId: String) {
            TODO("Not yet implemented")
          }

          override fun getNewUid() = "x"

          override suspend fun getAllBookings(): List<Booking> {
            TODO("Not yet implemented")
          }

          override suspend fun getBooking(bookingId: String): Booking? {
            TODO("Not yet implemented")
          }

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> {
            TODO("Not yet implemented")
          }
        }
    UserSessionManager.setCurrentUserId("demo")
    val vm =
        MyProfileViewModel(
            profileRepository = FakeProfileRepo(),
            listingRepository = FakeListingRepo(),
            ratingsRepository = FakeRatingRepos(),
            bookingRepository = failingBookingRepo,
            sessionManager = UserSessionManager)

    vm.loadUserBookings("demo")
  }

  @Test
  fun loadUserBookings_catchesProfileException() = runTest {
    val bookingRepo =
        object : BookingRepository {
          override suspend fun getBookingsByUserId(userId: String): List<Booking> =
              listOf(
                  Booking(
                      bookingId = "b1",
                      associatedListingId = "l1",
                      listingCreatorId = "tutor1",
                      bookerId = "demo",
                      status = BookingStatus.COMPLETED))

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> {
            TODO("Not yet implemented")
          }

          override suspend fun getBookingsByListing(listingId: String): List<Booking> {
            TODO("Not yet implemented")
          }

          override suspend fun addBooking(booking: Booking) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBooking(bookingId: String, booking: Booking) {
            TODO("Not yet implemented")
          }

          override suspend fun deleteBooking(bookingId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
            TODO("Not yet implemented")
          }

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {
            TODO("Not yet implemented")
          }

          override suspend fun confirmBooking(bookingId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun completeBooking(bookingId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun cancelBooking(bookingId: String) {
            TODO("Not yet implemented")
          }

          override fun getNewUid() = "x"

          override suspend fun getAllBookings(): List<Booking> {
            TODO("Not yet implemented")
          }

          override suspend fun getBooking(bookingId: String): Booking? {
            TODO("Not yet implemented")
          }

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> {
            TODO("Not yet implemented")
          }
        }

    val failingProfileRepo =
        object : ProfileRepository {
          override fun getNewUid() = "x"

          override fun getCurrentUserId() = "test-user-id"

          override suspend fun getProfile(userId: String): Profile {
            throw RuntimeException("boom")
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
            TODO("Not yet implemented")
          }

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

          override suspend fun updateTutorRatingFields(
              userId: String,
              averageRating: Double,
              totalRatings: Int
          ) {
            // not needed in this test
          }

          override suspend fun updateStudentRatingFields(
              userId: String,
              averageRating: Double,
              totalRatings: Int
          ) {
            // not needed in this test
          }
        }
    UserSessionManager.setCurrentUserId("demo")
    val vm =
        MyProfileViewModel(
            profileRepository = failingProfileRepo,
            listingRepository = FakeListingRepo(),
            ratingsRepository = FakeRatingRepos(),
            bookingRepository = bookingRepo,
            sessionManager = UserSessionManager)

    vm.loadUserBookings("demo")
  }

  @Test
  fun loadUserBookings_catchesListingException() = runTest {
    val bookingRepo =
        object : BookingRepository {
          override suspend fun getBookingsByUserId(userId: String): List<Booking> =
              listOf(
                  Booking(
                      bookingId = "b1",
                      associatedListingId = "l1",
                      listingCreatorId = "tutor1",
                      bookerId = "demo",
                      status = BookingStatus.COMPLETED))

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> {
            TODO("Not yet implemented")
          }

          override suspend fun getBookingsByListing(listingId: String): List<Booking> {
            TODO("Not yet implemented")
          }

          override suspend fun addBooking(booking: Booking) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBooking(bookingId: String, booking: Booking) {
            TODO("Not yet implemented")
          }

          override suspend fun deleteBooking(bookingId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
            TODO("Not yet implemented")
          }

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {
            TODO("Not yet implemented")
          }

          override suspend fun confirmBooking(bookingId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun completeBooking(bookingId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun cancelBooking(bookingId: String) {
            TODO("Not yet implemented")
          }

          override fun getNewUid() = "x"

          override suspend fun getAllBookings(): List<Booking> {
            TODO("Not yet implemented")
          }

          override suspend fun getBooking(bookingId: String): Booking? {
            TODO("Not yet implemented")
          }

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> {
            TODO("Not yet implemented")
          }
        }

    val failingListingRepo =
        object : ListingRepository {
          override suspend fun getListing(listingId: String): Listing {
            throw RuntimeException("boom")
          }

          override suspend fun getListingsByUser(userId: String): List<Listing> {
            TODO("Not yet implemented")
          }

          override suspend fun addProposal(proposal: Proposal) {
            TODO("Not yet implemented")
          }

          override suspend fun addRequest(request: Request) {
            TODO("Not yet implemented")
          }

          override suspend fun updateListing(listingId: String, listing: Listing) {
            TODO("Not yet implemented")
          }

          override suspend fun deleteListing(listingId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun deleteAllListingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun deactivateListing(listingId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun searchBySkill(skill: Skill): List<Listing> {
            TODO("Not yet implemented")
          }

          override suspend fun searchByLocation(
              location: Location,
              radiusKm: Double
          ): List<Listing> {
            TODO("Not yet implemented")
          }

          override fun getNewUid() = "x"

          override suspend fun getAllListings(): List<Listing> {
            TODO("Not yet implemented")
          }

          override suspend fun getProposals(): List<Proposal> {
            TODO("Not yet implemented")
          }

          override suspend fun getRequests(): List<Request> {
            TODO("Not yet implemented")
          }
        }
    UserSessionManager.setCurrentUserId("demo")
    val vm =
        MyProfileViewModel(
            profileRepository = FakeProfileRepo(),
            listingRepository = failingListingRepo,
            ratingsRepository = FakeRatingRepos(),
            bookingRepository = bookingRepo,
            sessionManager = UserSessionManager)

    vm.loadUserBookings("demo")
  }

  private object EmptyConversationManager : ConversationManagerInter {
    override suspend fun getOverViewConvUser(userId: String) = emptyList<OverViewConversation>()
    override suspend fun createConvAndOverviews(
      creatorId: String,
      otherUserId: String,
      convName: String
    ): String {
      TODO("Not yet implemented")
    }

    override suspend fun deleteConvAndOverviews(convId: String, deleterId: String, otherId: String) {}
    override suspend fun sendMessage(convId: String, message: Message) {}
    override suspend fun resetUnreadCount(convId: String, userId: String) {}
    override suspend fun getConv(convId: String) = null
    override fun listenMessages(convId: String) = emptyFlow<List<Message>>()
    override fun listenConversationOverviews(userId: String) = emptyFlow<List<OverViewConversation>>()
    override fun getMessageNewUid() = "x"
  }


  @Test
  fun deleteAccount_missingUserId_setsError() = runTest {
    val mockSession = mock(UserSessionManager::class.java)
    whenever(mockSession.getCurrentUserId()).thenReturn(null)

    val mockAuthRepo = mock(AuthenticationRepository::class.java)

    whenever(mockAuthRepo.deleteCurrentUser()).thenAnswer {
      throw IllegalStateException("deleteCurrentUser should NOT be called")
    }

    val vm =
      MyProfileViewModel(
        profileRepository = FakeProfileRepo(),
        listingRepository = FakeListingRepo(),
        ratingsRepository = FakeRatingRepos(),
        bookingRepository = FakeBookingRepo(),
        authRepository = mockAuthRepo,
        sessionManager = mockSession,
      )

    vm.deleteAccount()
    advanceUntilIdle()

    val ui = vm.uiState.value
    assertEquals("Unexpected error: missing user id.", ui.deleteAccountError)
    assertFalse(ui.deleteAccountSuccess)
  }


  @Test
  fun deleteAccount_success_updatesSuccessFlag() = runTest {
    UserSessionManager.setCurrentUserId("abc")

    val fakeAuthRepo = mockk<AuthenticationRepository>()
    coEvery { fakeAuthRepo.deleteCurrentUser() } returns Result.success(Unit)

    val vm = MyProfileViewModel(
      profileRepository = FakeProfileRepo(makeProfile("abc")),
      listingRepository = FakeListingRepo(),
      ratingsRepository = FakeRatingRepos(),
      bookingRepository = FakeBookingRepo(),
      authRepository = fakeAuthRepo,
      conversationManager = EmptyConversationManager,
      sessionManager = UserSessionManager
    )

    vm.loadProfile("abc")
    advanceUntilIdle()

    vm.deleteAccount()
    advanceUntilIdle()

    assertTrue(vm.uiState.value.deleteAccountSuccess)
    assertNull(vm.uiState.value.deleteAccountError)
  }

  @Test
  fun deleteAccount_failure_setsErrorFlag() = runTest {
    UserSessionManager.setCurrentUserId("abc")

    val fakeAuthRepo = mockk<AuthenticationRepository>()
    coEvery { fakeAuthRepo.deleteCurrentUser() } returns Result.failure(Exception("boom"))

    val vm = MyProfileViewModel(
      profileRepository = FakeProfileRepo(makeProfile("abc")),
      listingRepository = FakeListingRepo(),
      ratingsRepository = FakeRatingRepos(),
      bookingRepository = FakeBookingRepo(),
      authRepository = fakeAuthRepo,
      conversationManager = EmptyConversationManager,
      sessionManager = UserSessionManager
    )

    vm.loadProfile("abc")
    advanceUntilIdle()

    vm.deleteAccount()
    advanceUntilIdle()

    assertEquals("boom", vm.uiState.value.deleteAccountError)
    assertFalse(vm.uiState.value.deleteAccountSuccess)
  }
}
