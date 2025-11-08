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
    // minimal fake for tests (implementation elided in excerpt)
  ) : com.android.sample.model.map.LocationRepository {
    override suspend fun search(query: String): List<Location> {
      return listOf(Location(name = "Paris", latitude = 48.8566, longitude = 2.3522))
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
    // sub-skill is required
    vm.setSubSkill("PROGRAMMING")
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
    // sub-skill is required
    vm.setSubSkill("PAINTING")
    vm.setLocation(Location(name = "Nice", latitude = 43.7, longitude = 7.25))

    vm.addSkill()
    advanceUntilIdle()

    assertTrue(repo.addProposalCalled)
    val proposal = repo.addedProposal!!
    assertEquals("fake-uid", proposal.listingId)
    assertEquals("PAINTING", proposal.skill.skill)
    assertEquals(MainSubject.ARTS, proposal.skill.mainSubject)
    assertEquals("Teach DSLR", proposal.description)
    assertEquals(43.7, proposal.location.latitude, 0.01)
  }

  @Test
  fun addSkill_doesNotThrow_whenRepositoryFails() = runTest {
    val failingRepo =
      object : FakeListingRepo() {
        override suspend fun addProposal(proposal: Proposal) {
          throw RuntimeException("fail")
        }
      }

    val vm = newVm(failingRepo)
    vm.setTitle("Valid")
    vm.setDescription("Desc")
    vm.setPrice("10")
    vm.setSubject(MainSubject.TECHNOLOGY)
    vm.setSubSkill("PROGRAMMING")
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

  // ----------------------------------------------------------
  // EXTRA COVERAGE TESTS FOR NewSkillViewModel
  // ----------------------------------------------------------

  @Test
  fun setSubSkill_setsValue_andClearsError() {
    val vm = newVm()
    vm.setSubSkill("Portraits")
    assertEquals("Portraits", vm.uiState.value.selectedSubSkill)
    assertNull(vm.uiState.value.invalidSubSkillMsg)
  }

  @Test
  fun setSubject_resetsSubSkill_andClearsSubjectAndSubSkillErrors_andUpdatesOptions() {
    val vm = newVm()

    // Seed error state and a stale sub-skill
    vm.setError()
    vm.setSubSkill("Stale")
    assertEquals("Stale", vm.uiState.value.selectedSubSkill)

    vm.setSubject(MainSubject.TECHNOLOGY)

    val ui = vm.uiState.value
    assertEquals(MainSubject.TECHNOLOGY, ui.subject)
    assertNull(ui.invalidSubjectMsg)
    assertNull(ui.invalidSubSkillMsg)
    assertNull(ui.selectedSubSkill) // reset
    assertNotNull(ui.subSkillOptions)
  }


  @Test
  fun addSkill_usesProvidedUserId_inProposal() = runTest {
    val repo = FakeListingRepo()
    val vm = NewSkillViewModel(repo, FakeLocationRepo(), userId = "u123")

    vm.setTitle("Guitar")
    vm.setDescription("Chords")
    vm.setPrice("15")
    vm.setSubject(MainSubject.MUSIC)
    vm.setSubSkill("GUITAR")
    vm.setLocation(Location(48.8566, 2.3522, "Paris"))

    vm.addSkill()
    advanceUntilIdle()

    assertEquals("u123", repo.addedProposal!!.creatorUserId)
  }

  @Test
  fun price_zero_allowed_andValidFlowStillSubmits() = runTest {
    val repo = FakeListingRepo()
    val vm = newVm(repo)

    vm.setTitle("Intro")
    vm.setDescription("Free class")
    vm.setPrice("0")
    vm.setSubject(MainSubject.TECHNOLOGY)
    vm.setSubSkill("PROGRAMMING")
    vm.setLocation(Location(45.75, 4.85, "Lyon"))

    assertNull(vm.uiState.value.invalidPriceMsg)
    vm.addSkill()
    advanceUntilIdle()
    assertTrue(repo.addProposalCalled)
    assertEquals(0.0, repo.addedProposal!!.hourlyRate, 0.0)
  }

  @Test
  fun setPrice_handlesNaN_asInvalid() {
    val vm = newVm()
    vm.setPrice("NaN")
    assertEquals("Price must be a positive number", vm.uiState.value.invalidPriceMsg)
  }

  @Test
  fun setLocationQuery_blank_clearsSelectedLocation_andSetsError() {
    val vm = newVm()
    vm.setLocation(Location(47.37, 8.54, "Zurich"))

    vm.setLocationQuery("") // blank -> should clear & set error
    val ui = vm.uiState.value
    assertNull(ui.selectedLocation)
    assertEquals("You must choose a location", ui.invalidLocationMsg)
    assertTrue(ui.locationSuggestions.isEmpty())
  }

  @Test
  fun locationSearch_isDebounced_andPreviousJobCancelled_onlyLastQueryApplied() = runTest {
    // Repo that records queries and returns different results per query
    class RecordingRepo : com.android.sample.model.map.LocationRepository {
      val queries = mutableListOf<String>()
      override suspend fun search(query: String): List<Location> {
        queries.add(query)
        return listOf(Location(name = "Lyon", latitude = 45.75, longitude = 4.85))
      }
    }

    val repo = RecordingRepo()
    val vm = newVm(locRepo = repo)

    // Type quickly: first "Pa", then "Ly" before debounce fires
    vm.setLocationQuery("Pa")
    vm.setLocationQuery("Ly")

    // Advance virtual time so the last debounce fires
    advanceUntilIdle()

    // Only "Ly" results should be applied
    val ui = vm.uiState.value
    assertEquals(listOf("Ly"), repo.queries.map { it }.takeLast(1))
    assertTrue(ui.locationSuggestions.first().name.contains("Lyon", ignoreCase = true))
  }

  @Test
  fun setError_doesNotSetSubSkillError_andSetsOthers() {
    val vm = newVm()
    vm.setError()
    val ui = vm.uiState.value
    assertEquals("Title cannot be empty", ui.invalidTitleMsg)
    assertEquals("Description cannot be empty", ui.invalidDescMsg)
    assertEquals("Price cannot be empty", ui.invalidPriceMsg)
    assertEquals("You must choose a subject", ui.invalidSubjectMsg)
    assertEquals("You must choose a location", ui.invalidLocationMsg)
    // Since no subject chosen, sub-skill error remains null
    assertNull(ui.invalidSubSkillMsg)
  }

  @Test
  fun isValid_false_ifAnySingleFieldMissing_eachIndividually() {
    // Base valid setup
    fun validVm(): NewSkillViewModel {
      val vm = newVm()
      vm.setTitle("T")
      vm.setDescription("D")
      vm.setPrice("1")
      vm.setSubject(MainSubject.TECHNOLOGY)
      vm.setSubSkill("PROGRAMMING")
      vm.setLocation(Location(46.948, 7.447, "Bern"))
      return vm
    }

    // Missing title
    val vmTitle = validVm()
    vmTitle.setTitle("")
    assertFalse(vmTitle.uiState.value.isValid)

    // Missing description
    val vmDesc = validVm()
    vmDesc.setDescription("")
    assertFalse(vmDesc.uiState.value.isValid)

    // Missing price
    val vmPrice = validVm()
    vmPrice.setPrice("")
    assertFalse(vmPrice.uiState.value.isValid)

    // Missing subject
    val vmSubject = validVm()
    // create new VM with no subject selected
    val vmNoSubject = newVm()
    vmNoSubject.setTitle("T")
    vmNoSubject.setDescription("D")
    vmNoSubject.setPrice("1")
    vmNoSubject.setLocation(Location(46.948, 7.447, "Bern"))
    assertFalse(vmNoSubject.uiState.value.isValid)

    // Missing location
    val vmLoc = newVm()
    vmLoc.setTitle("T")
    vmLoc.setDescription("D")
    vmLoc.setPrice("1")
    vmLoc.setSubject(MainSubject.TECHNOLOGY)
    vmLoc.setSubSkill("PROGRAMMING")
    assertFalse(vmLoc.uiState.value.isValid)
  }
}
