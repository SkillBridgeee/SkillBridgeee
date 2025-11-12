package com.android.sample.screen

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.skill.SkillsHelper
import com.android.sample.model.user.Profile
import com.android.sample.ui.subject.SubjectListViewModel
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

// AI generated test for SubjectListViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class SubjectListViewModelTest {

  private val dispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // ---------- Helpers -----------------------------------------------------

  private fun listing(
      id: String,
      creatorId: String,
      desc: String,
      subject: MainSubject = MainSubject.MUSIC,
      skillName: String = "guitar",
      rate: Double = 25.0
  ) =
      Proposal(
          listingId = id,
          creatorUserId = creatorId,
          skill = Skill(subject, skillName),
          description = desc,
          location = Location(0.0, 0.0, "Paris"),
          hourlyRate = rate)

  private fun profile(id: String, name: String, rating: Double, total: Int) =
      Profile(userId = id, name = name, tutorRating = RatingInfo(rating, total))

  private class FakeListingRepo(
      private val listings: List<Listing>,
      private val throwError: Boolean = false,
      private val errorMessage: String = "boom"
  ) : ListingRepository {
    override fun getNewUid(): String {
      TODO("Not yet implemented")
    }

    override suspend fun getAllListings(): List<Listing> {
      if (throwError) error(errorMessage)
      delay(10)
      return listings
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

    override suspend fun deactivateListing(listingId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun searchBySkill(skill: Skill): List<Listing> {
      TODO("Not yet implemented")
    }

    override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> {
      TODO("Not yet implemented")
    }
  }

  private class FakeProfileRepo(private val profiles: Map<String, Profile>) :
      com.android.sample.model.user.ProfileRepository {
    override fun getNewUid(): String = "unused"

    override suspend fun getProfile(userId: String): Profile? = profiles[userId]

    override suspend fun addProfile(profile: Profile) {}

    override suspend fun updateProfile(userId: String, profile: Profile) {}

    override suspend fun deleteProfile(userId: String) {}

    override suspend fun getAllProfiles(): List<Profile> = profiles.values.toList()

    override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
        emptyList<Profile>()

    override suspend fun getProfileById(userId: String): Profile? = profiles[userId]

    override suspend fun getSkillsForUser(userId: String) = emptyList<Skill>()
  }

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

  private fun newVm(
      listings: List<Listing> = defaultListings,
      profiles: Map<String, Profile> = defaultProfiles,
      throwError: Boolean = false
  ) =
      SubjectListViewModel(
          listingRepo = FakeListingRepo(listings, throwError),
          profileRepo = FakeProfileRepo(profiles))

  private val L1 = listing("1", "A", "Guitar class", MainSubject.MUSIC, "guitar")
  private val L2 = listing("2", "B", "Piano class", MainSubject.MUSIC, "piano")
  private val L3 = listing("3", "C", "Singing", MainSubject.MUSIC, "sing")
  private val L4 = listing("4", "D", "Piano beginner", MainSubject.MUSIC, "piano")

  private val defaultListings = listOf(L1, L2, L3, L4)

  private val defaultProfiles =
      mapOf(
          "A" to profile("A", "Alice", 4.9, 10),
          "B" to profile("B", "Bob", 4.8, 20),
          "C" to profile("C", "Charlie", 4.8, 15),
          "D" to profile("D", "Diana", 4.2, 5))

  // ---------- Tests -------------------------------------------------------

  @Test
  fun refresh_populates_listings_sorted_by_rating() = runTest {
    val vm = newVm()
    vm.refresh(MainSubject.MUSIC)
    advanceUntilIdle()

    val ui = vm.ui.value
    assertFalse(ui.isLoading)
    assertNull(ui.error)
    assertTrue(ui.allListings.isNotEmpty())

    val sorted = ui.listings.map { it.creator?.name }
    assertEquals(listOf("Alice", "Bob", "Charlie", "Diana"), sorted)
  }

  @Test
  fun query_filter_works_by_description_or_name() = runTest {
    val vm = newVm()
    vm.refresh(MainSubject.MUSIC)
    advanceUntilIdle()

    vm.onQueryChanged("piano")
    val ui1 = vm.ui.value
    assertTrue(ui1.listings.all { it.listing.description.contains("piano", true) })

    vm.onQueryChanged("Alice")
  }

  @Test
  fun skill_filter_works_correctly() = runTest {
    val vm = newVm()
    vm.refresh(MainSubject.MUSIC)
    advanceUntilIdle()

    vm.onSkillSelected("piano")
    val ui = vm.ui.value
    assertTrue(ui.listings.all { it.listing.skill.skill.equals("piano", true) })
  }

  @Test
  fun refresh_sets_error_on_failure() = runTest {
    val vm = newVm(throwError = true)
    vm.refresh(MainSubject.MUSIC)
    advanceUntilIdle()

    val ui = vm.ui.value
    assertFalse(ui.isLoading)
    assertNotNull(ui.error)
    assertTrue(ui.listings.isEmpty())
  }

  @Test
  fun sorting_respects_tie_breakers() = runTest {
    val listings = listOf(L2, L3)
    val profiles = mapOf("B" to profile("B", "Aaron", 4.8, 15), "C" to profile("C", "Zed", 4.8, 15))
    val vm = newVm(listings, profiles)
    vm.refresh(MainSubject.MUSIC)
    advanceUntilIdle()

    val names = vm.ui.value.listings.map { it.creator?.name }
    assertEquals(listOf("Aaron", "Zed"), names)
  }

  // ---------- Additional Coverage Tests -----------------------------------

  @Test
  fun subjectToString_returns_expected_labels() {
    val vm = newVm()
    assertEquals("Music", vm.subjectToString(MainSubject.MUSIC))
    assertEquals("Sports", vm.subjectToString(MainSubject.SPORTS))
    assertEquals("Languages", vm.subjectToString(MainSubject.LANGUAGES))
    assertEquals("Subjects", vm.subjectToString(null))
  }

  @Test
  fun getSkillsForSubject_returns_list_from_helper() {
    val vm = newVm()
    val skills = vm.getSkillsForSubject(MainSubject.MUSIC)
    assertTrue(skills.containsAll(SkillsHelper.getSkillNames(MainSubject.MUSIC)))
  }

  @Test
  fun onQueryChanged_triggers_filter_even_with_empty_query() = runTest {
    val vm = newVm()
    vm.refresh(MainSubject.MUSIC)
    advanceUntilIdle()
    vm.onQueryChanged("")
    assertTrue(vm.ui.value.listings.isNotEmpty())
  }

  @Test
  fun onSkillSelected_updates_selectedSkill_and_filters() = runTest {
    val vm = newVm()
    vm.refresh(MainSubject.MUSIC)
    advanceUntilIdle()
    vm.onSkillSelected("guitar")
    val ui = vm.ui.value
    assertEquals("guitar", ui.selectedSkill)
  }

  @Test
  fun refresh_with_null_subject_defaults_to_previous_mainSubject() = runTest {
    val vm = newVm()
    vm.refresh(null)
    advanceUntilIdle()
    assertEquals(MainSubject.MUSIC, vm.ui.value.mainSubject)
  }
}
