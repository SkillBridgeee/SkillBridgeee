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

  // -------- Fake Repository ------------------------------------------------------

  private open class FakeRepo : ListingRepository {
    var addProposalCalled = false
    var addedProposal: Proposal? = null
    var generatedUid = "fake-uid"

    override fun getNewUid(): String = generatedUid

    override suspend fun addProposal(proposal: Proposal) {
      addProposalCalled = true
      addedProposal = proposal
    }

    // --- Unused methods in this ViewModel ---
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

  // -------- Helpers ------------------------------------------------------

  private fun newVm(repo: ListingRepository = FakeRepo()) = NewSkillViewModel(repo)

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
  fun isValid_trueOnlyWhenAllFieldsValid() {
    val vm = newVm()

    vm.setTitle("T")
    vm.setDescription("D")
    vm.setPrice("10")
    vm.setSubject(MainSubject.TECHNOLOGY)

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
    assertFalse(ui.isValid)
  }

  @Test
  fun setError_clearsErrorsWhenAllValid() {
    val vm = newVm()

    vm.setTitle("Good")
    vm.setDescription("Desc")
    vm.setPrice("10")
    vm.setSubject(MainSubject.TECHNOLOGY)

    vm.setError()

    val ui = vm.uiState.value
    assertNull(ui.invalidTitleMsg)
    assertNull(ui.invalidDescMsg)
    assertNull(ui.invalidPriceMsg)
    assertNull(ui.invalidSubjectMsg)
    assertTrue(ui.isValid)
  }

  @Test
  fun addSkill_doesNotAdd_whenInvalid() = runTest {
    val repo = FakeRepo()
    val vm = newVm(repo)

    vm.setTitle("Only title") // invalid, missing desc/price/subject
    vm.addSkill("user123")
    advanceUntilIdle()

    assertFalse(repo.addProposalCalled)
    val ui = vm.uiState.value
    assertEquals("Description cannot be empty", ui.invalidDescMsg)
    assertEquals("Price cannot be empty", ui.invalidPriceMsg)
    assertEquals("You must choose a subject", ui.invalidSubjectMsg)
  }

  @Test
  fun addSkill_callsRepository_whenValid() = runTest {
    val repo = FakeRepo()
    val vm = newVm(repo)

    vm.setTitle("Photography")
    vm.setDescription("Teach how to use DSLR")
    vm.setPrice("50")
    vm.setSubject(MainSubject.ARTS)

    vm.addSkill("user123")
    advanceUntilIdle()

    assertTrue(repo.addProposalCalled)
    val proposal = repo.addedProposal!!
    assertEquals("user123", proposal.creatorUserId)
    assertEquals("fake-uid", proposal.listingId)
    assertEquals("Photography", proposal.skill.skill)
    assertEquals(MainSubject.ARTS, proposal.skill.mainSubject)
    assertEquals("Teach how to use DSLR", proposal.description)
  }

  @Test
  fun addSkill_doesNotThrow_whenRepositoryFails() = runTest {
    val failingRepo =
        object : FakeRepo() {
          override suspend fun addProposal(proposal: Proposal) {
            throw RuntimeException("Network error")
          }
        }

    val vm = newVm(failingRepo)
    vm.setTitle("Valid")
    vm.setDescription("Desc")
    vm.setPrice("10")
    vm.setSubject(MainSubject.TECHNOLOGY)

    // Should not crash
    vm.addSkill("user123")
    advanceUntilIdle()
  }

  @Test
  fun load_doesNothing_butDoesNotCrash() {
    val vm = newVm()
    vm.load()
  }
}
