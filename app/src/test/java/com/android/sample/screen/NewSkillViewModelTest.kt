package com.android.sample.screen

import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.ui.screens.newSkill.NewSkillViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewSkillViewModelTest {

  private val dispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // -------- Fake Repositories ------------------------------------------------------

  private open class FakeListingRepo : ListingRepository {
    var addProposalCalled = false
    var addedProposal: Proposal? = null
    var generatedUid = "fake-uid"

    override fun getNewUid(): String = generatedUid

    override suspend fun addProposal(proposal: Proposal) {
      addProposalCalled = true
      addedProposal = proposal
    }

    // --- Unused methods ---
    override suspend fun getAllListings(): List<Listing> = emptyList()

    override suspend fun getProposals(): List<Proposal> = emptyList()

    override suspend fun getRequests(): List<Request> = emptyList()

    override suspend fun getListing(listingId: String): Listing? = null

    override suspend fun getListingsByUser(userId: String): List<Listing> = emptyList()

    override suspend fun addRequest(request: Request) {}

    override suspend fun updateListing(listingId: String, listing: Listing) {}

    override suspend fun deleteListing(listingId: String) {}

    override suspend fun deactivateListing(listingId: String) {}

    override suspend fun searchBySkill(skill: Skill): List<Listing> = emptyList()

    override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> =
        emptyList()
  }

  private class FakeLocationRepo(
      val shouldFail: Boolean = false,
      val results: List<Location> =
          listOf(Location(name = "Paris", latitude = 48.8566, longitude = 2.3522))
  ) : com.android.sample.model.map.LocationRepository {
    override suspend fun search(query: String): List<Location> {
      if (shouldFail) throw RuntimeException("Network error")
      return results.filter { it.name.contains(query, ignoreCase = true) }
    }
  }

  // -------- Helpers ------------------------------------------------------

  private fun newVm(
      repo: ListingRepository = FakeListingRepo(),
      locRepo: com.android.sample.model.map.LocationRepository = FakeLocationRepo()
  ) = NewSkillViewModel(repo, locRepo, userId = "")

  // -------- Tests --------------------------------------------------------

  @Test
  fun setTitle_updatesValue_andSetsErrorIfBlank() {
    val vm = newVm()

    vm.setTitle("Maths")
    assertEquals("Maths", vm.uiState.value.title)
    assertNull(vm.uiState.value.invalidTitleMsg)

    vm.setTitle("")
    assertEquals("Title cannot be empty", vm.uiState.value.invalidTitleMsg)
  }

  @Test
  fun setDescription_updatesValue_andSetsErrorIfBlank() {
    val vm = newVm()

    vm.setDescription("Teach algebra")
    assertEquals("Teach algebra", vm.uiState.value.description)
    assertNull(vm.uiState.value.invalidDescMsg)

    vm.setDescription("")
    assertEquals("Description cannot be empty", vm.uiState.value.invalidDescMsg)
  }

  @Test
  fun setPrice_validatesValue_correctly() {
    val vm = newVm()

    vm.setPrice("")
    assertEquals("Price cannot be empty", vm.uiState.value.invalidPriceMsg)

    vm.setPrice("abc")
    assertEquals("Price must be a positive number", vm.uiState.value.invalidPriceMsg)

    vm.setPrice("-5")
    assertEquals("Price must be a positive number", vm.uiState.value.invalidPriceMsg)

    vm.setPrice("12.5")
    assertNull(vm.uiState.value.invalidPriceMsg)
  }

  @Test
  fun setSubject_updatesSubject() {
    val vm = newVm()
    val subject = MainSubject.TECHNOLOGY
    vm.setSubject(subject)
    assertEquals(subject, vm.uiState.value.subject)
  }

  @Test
  fun setLocation_updatesSelectedLocation() {
    val vm = newVm()
    val location = Location(name = "Paris", latitude = 48.8566, longitude = 2.3522)
    vm.setLocation(location)
    assertEquals(location, vm.uiState.value.selectedLocation)
    assertEquals("Paris", vm.uiState.value.locationQuery)
  }

  @Test
  fun setLocationQuery_updatesSuggestions_whenValid() = runTest {
    val repo = FakeLocationRepo()
    val vm = newVm(locRepo = repo)

    vm.setLocationQuery("Par")
    advanceUntilIdle()

    val suggestions = vm.uiState.value.locationSuggestions
    assertTrue(suggestions.isNotEmpty())
    assertEquals("Paris", suggestions.first().name)
  }

  @Test
  fun setLocationQuery_handlesError_whenRepoFails() = runTest {
    val repo = FakeLocationRepo(shouldFail = true)
    val vm = newVm(locRepo = repo)

    vm.setLocationQuery("Something")
    advanceUntilIdle()

    assertTrue(vm.uiState.value.locationSuggestions.isEmpty())
  }

  @Test
  fun setLocationQuery_setsError_whenEmptyQuery() {
    val vm = newVm()
    vm.setLocationQuery("")
    assertEquals("You must choose a location", vm.uiState.value.invalidLocationMsg)
  }

  @Test
  fun isValid_trueOnlyWhenAllFieldsValid() {
    val vm = newVm()

    vm.setTitle("T")
    vm.setDescription("D")
    vm.setPrice("10")
    vm.setSubject(MainSubject.TECHNOLOGY)
    vm.setLocation(Location(name = "Lyon", latitude = 45.75, longitude = 4.85))

    assertTrue(vm.uiState.value.isValid)

    vm.setPrice("")
    assertFalse(vm.uiState.value.isValid)
  }

  @Test
  fun setError_setsAllErrorMessagesWhenInvalid() {
    val vm = newVm()

    vm.setError()

    val ui = vm.uiState.value
    assertEquals("Title cannot be empty", ui.invalidTitleMsg)
    assertEquals("Description cannot be empty", ui.invalidDescMsg)
    assertEquals("Price cannot be empty", ui.invalidPriceMsg)
    assertEquals("You must choose a subject", ui.invalidSubjectMsg)
    assertEquals("You must choose a location", ui.invalidLocationMsg)
    assertFalse(ui.isValid)
  }

  @Test
  fun addSkill_doesNotAdd_whenInvalid() = runTest {
    val repo = FakeListingRepo()
    val vm = newVm(repo)

    vm.setTitle("Only title") // invalid, missing desc/price/subject/location
    vm.addSkill()
    advanceUntilIdle()

    assertFalse(repo.addProposalCalled)
    val ui = vm.uiState.value
    assertEquals("Description cannot be empty", ui.invalidDescMsg)
    assertEquals("Price cannot be empty", ui.invalidPriceMsg)
    assertEquals("You must choose a subject", ui.invalidSubjectMsg)
    assertEquals("You must choose a location", ui.invalidLocationMsg)
  }

  @Test
  fun addSkill_callsRepository_whenValid() = runTest {
    val repo = FakeListingRepo()
    val vm = newVm(repo)

    vm.setTitle("Photography")
    vm.setDescription("Teach DSLR")
    vm.setPrice("50")
    vm.setSubject(MainSubject.ARTS)
    vm.setLocation(Location(name = "Nice", latitude = 43.7, longitude = 7.25))

    vm.addSkill()
    advanceUntilIdle()

    assertTrue(repo.addProposalCalled)
    val proposal = repo.addedProposal!!
    assertEquals("fake-uid", proposal.listingId)
    assertEquals("Photography", proposal.skill.skill)
    assertEquals(MainSubject.ARTS, proposal.skill.mainSubject)
    assertEquals("Teach DSLR", proposal.description)
    assertEquals(43.7, proposal.location.latitude, 0.01)
  }

  @Test
  fun addSkill_doesNotThrow_whenRepositoryFails() = runTest {
    val failingRepo =
        object : FakeListingRepo() {
          override suspend fun addProposal(proposal: Proposal) {
            throw RuntimeException("Network error")
          }
        }

    val vm = newVm(failingRepo)
    vm.setTitle("Valid")
    vm.setDescription("Desc")
    vm.setPrice("10")
    vm.setSubject(MainSubject.TECHNOLOGY)
    vm.setLocation(Location(name = "Lille", latitude = 50.63, longitude = 3.06))

    // Should not crash
    vm.addSkill()
    advanceUntilIdle()
  }

  @Test
  fun load_doesNothing_butDoesNotCrash() {
    val vm = newVm()
    vm.load()
  }
}

// package com.android.sample.screen
//
// import com.android.sample.model.listing.Listing
// import com.android.sample.model.listing.ListingRepository
// import com.android.sample.model.listing.Proposal
// import com.android.sample.model.listing.Request
// import com.android.sample.model.map.Location
// import com.android.sample.model.skill.MainSubject
// import com.android.sample.model.skill.Skill
// import com.android.sample.ui.screens.newSkill.NewSkillViewModel
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.ExperimentalCoroutinesApi
// import kotlinx.coroutines.test.StandardTestDispatcher
// import kotlinx.coroutines.test.advanceUntilIdle
// import kotlinx.coroutines.test.resetMain
// import kotlinx.coroutines.test.runTest
// import kotlinx.coroutines.test.setMain
// import org.junit.After
// import org.junit.Assert.*
// import org.junit.Before
// import org.junit.Test
//
// @OptIn(ExperimentalCoroutinesApi::class)
// class NewSkillViewModelTest {
//
//  private val dispatcher = StandardTestDispatcher()
//
//  @Before
//  fun setUp() {
//    Dispatchers.setMain(dispatcher)
//  }
//
//  @After
//  fun tearDown() {
//    Dispatchers.resetMain()
//  }
//
//  // -------- Fake Repository ------------------------------------------------------
//
//  private open class FakeRepo : ListingRepository {
//    var addProposalCalled = false
//    var addedProposal: Proposal? = null
//    var generatedUid = "fake-uid"
//
//    override fun getNewUid(): String = generatedUid
//
//    override suspend fun addProposal(proposal: Proposal) {
//      addProposalCalled = true
//      addedProposal = proposal
//    }
//
//    // --- Unused methods in this ViewModel ---
//    override suspend fun getAllListings(): List<Listing> = emptyList()
//
//    override suspend fun getProposals(): List<Proposal> = emptyList()
//
//    override suspend fun getRequests(): List<Request> = emptyList()
//
//    override suspend fun getListing(listingId: String): Listing? = null
//
//    override suspend fun getListingsByUser(userId: String): List<Listing> = emptyList()
//
//    override suspend fun addRequest(request: Request) {}
//
//    override suspend fun updateListing(listingId: String, listing: Listing) {}
//
//    override suspend fun deleteListing(listingId: String) {}
//
//    override suspend fun deactivateListing(listingId: String) {}
//
//    override suspend fun searchBySkill(skill: Skill): List<Listing> = emptyList()
//
//    override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> =
//        emptyList()
//  }
//
//  // -------- Helpers ------------------------------------------------------
//
//  private fun newVm(repo: ListingRepository = FakeRepo()) = NewSkillViewModel(repo)
//
//  // -------- Tests --------------------------------------------------------
//
//  @Test
//  fun setTitle_updatesValue_andSetsErrorIfBlank() {
//    val vm = newVm()
//
//    vm.setTitle("Maths")
//    assertEquals("Maths", vm.uiState.value.title)
//    assertNull(vm.uiState.value.invalidTitleMsg)
//
//    vm.setTitle("")
//    assertEquals("Title cannot be empty", vm.uiState.value.invalidTitleMsg)
//  }
//
//  @Test
//  fun setDescription_updatesValue_andSetsErrorIfBlank() {
//    val vm = newVm()
//
//    vm.setDescription("Teach algebra")
//    assertEquals("Teach algebra", vm.uiState.value.description)
//    assertNull(vm.uiState.value.invalidDescMsg)
//
//    vm.setDescription("")
//    assertEquals("Description cannot be empty", vm.uiState.value.invalidDescMsg)
//  }
//
//  @Test
//  fun setPrice_validatesValue_correctly() {
//    val vm = newVm()
//
//    vm.setPrice("")
//    assertEquals("Price cannot be empty", vm.uiState.value.invalidPriceMsg)
//
//    vm.setPrice("abc")
//    assertEquals("Price must be a positive number", vm.uiState.value.invalidPriceMsg)
//
//    vm.setPrice("-5")
//    assertEquals("Price must be a positive number", vm.uiState.value.invalidPriceMsg)
//
//    vm.setPrice("12.5")
//    assertNull(vm.uiState.value.invalidPriceMsg)
//  }
//
//  @Test
//  fun setSubject_updatesSubject() {
//    val vm = newVm()
//    val subject = MainSubject.TECHNOLOGY
//    vm.setSubject(subject)
//    assertEquals(subject, vm.uiState.value.subject)
//  }
//
//  @Test
//  fun isValid_trueOnlyWhenAllFieldsValid() {
//    val vm = newVm()
//
//    vm.setTitle("T")
//    vm.setDescription("D")
//    vm.setPrice("10")
//    vm.setSubject(MainSubject.TECHNOLOGY)
//
//    assertTrue(vm.uiState.value.isValid)
//
//    vm.setPrice("")
//    assertFalse(vm.uiState.value.isValid)
//  }
//
//  @Test
//  fun setError_setsAllErrorMessagesWhenInvalid() {
//    val vm = newVm()
//
//    vm.setError()
//
//    val ui = vm.uiState.value
//    assertEquals("Title cannot be empty", ui.invalidTitleMsg)
//    assertEquals("Description cannot be empty", ui.invalidDescMsg)
//    assertEquals("Price cannot be empty", ui.invalidPriceMsg)
//    assertEquals("You must choose a subject", ui.invalidSubjectMsg)
//    assertFalse(ui.isValid)
//  }
//
//  @Test
//  fun setError_clearsErrorsWhenAllValid() {
//    val vm = newVm()
//
//    vm.setTitle("Good")
//    vm.setDescription("Desc")
//    vm.setPrice("10")
//    vm.setSubject(MainSubject.TECHNOLOGY)
//
//    vm.setError()
//
//    val ui = vm.uiState.value
//    assertNull(ui.invalidTitleMsg)
//    assertNull(ui.invalidDescMsg)
//    assertNull(ui.invalidPriceMsg)
//    assertNull(ui.invalidSubjectMsg)
//    assertTrue(ui.isValid)
//  }
//
//  @Test
//  fun addSkill_doesNotAdd_whenInvalid() = runTest {
//    val repo = FakeRepo()
//    val vm = newVm(repo)
//
//    vm.setTitle("Only title") // invalid, missing desc/price/subject
//    vm.addSkill()
//    advanceUntilIdle()
//
//    assertFalse(repo.addProposalCalled)
//    val ui = vm.uiState.value
//    assertEquals("Description cannot be empty", ui.invalidDescMsg)
//    assertEquals("Price cannot be empty", ui.invalidPriceMsg)
//    assertEquals("You must choose a subject", ui.invalidSubjectMsg)
//  }
//
//  @Test
//  fun addSkill_callsRepository_whenValid() = runTest {
//    val repo = FakeRepo()
//    val vm = newVm(repo)
//
//    vm.setTitle("Photography")
//    vm.setDescription("Teach how to use DSLR")
//    vm.setPrice("50")
//    vm.setSubject(MainSubject.ARTS)
//
//    vm.addSkill()
//    advanceUntilIdle()
//
//    assertTrue(repo.addProposalCalled)
//    val proposal = repo.addedProposal!!
//    assertEquals("user123", proposal.creatorUserId)
//    assertEquals("fake-uid", proposal.listingId)
//    assertEquals("Photography", proposal.skill.skill)
//    assertEquals(MainSubject.ARTS, proposal.skill.mainSubject)
//    assertEquals("Teach how to use DSLR", proposal.description)
//  }
//
//  @Test
//  fun addSkill_doesNotThrow_whenRepositoryFails() = runTest {
//    val failingRepo =
//        object : FakeRepo() {
//          override suspend fun addProposal(proposal: Proposal) {
//            throw RuntimeException("Network error")
//          }
//        }
//
//    val vm = newVm(failingRepo)
//    vm.setTitle("Valid")
//    vm.setDescription("Desc")
//    vm.setPrice("10")
//    vm.setSubject(MainSubject.TECHNOLOGY)
//
//    // Should not crash
//    vm.addSkill()
//    advanceUntilIdle()
//  }
//
//  @Test
//  fun load_doesNothing_butDoesNotCrash() {
//    val vm = newVm()
//    vm.load()
//  }
// }
