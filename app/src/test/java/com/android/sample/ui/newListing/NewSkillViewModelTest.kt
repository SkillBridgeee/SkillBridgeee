package com.android.sample.ui.newListing

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingType
import com.android.sample.model.map.Location
import com.android.sample.model.map.LocationRepository
import com.android.sample.model.skill.MainSubject
import com.android.sample.ui.screens.newSkill.NewListingViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewListingViewModelTest {

  @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var mockListingRepository: ListingRepository
  private lateinit var mockLocationRepository: LocationRepository
  private lateinit var viewModel: NewListingViewModel

  private val testUserId = "test-user-123"
  private val testLocation =
      Location(latitude = 46.5196535, longitude = 6.6322734, name = "Lausanne")

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    mockListingRepository = mockk(relaxed = true)
    mockLocationRepository = mockk(relaxed = true)

    every { mockListingRepository.getNewUid() } returns "listing-123"

    viewModel =
        NewListingViewModel(
            listingRepository = mockListingRepository,
            locationRepository = mockLocationRepository,
            userId = testUserId)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  // ========== Initial State Tests ==========

  @Test
  fun initialState_hasCorrectDefaults() = runTest {
    val state = viewModel.uiState.first()

    assertEquals("", state.title)
    assertEquals("", state.description)
    assertEquals("", state.price)
    assertNull(state.subject)
    assertNull(state.listingType)
    assertNull(state.selectedLocation)
    assertEquals("", state.locationQuery)
    assertTrue(state.locationSuggestions.isEmpty())
    assertNull(state.invalidTitleMsg)
    assertNull(state.invalidDescMsg)
    assertNull(state.invalidPriceMsg)
    assertNull(state.invalidSubjectMsg)
    assertNull(state.invalidListingTypeMsg)
    assertNull(state.invalidLocationMsg)
    assertFalse(state.isValid)
  }

  // ========== Field Update Tests ==========

  @Test
  fun setTitle_updatesStateCorrectly() = runTest {
    viewModel.setTitle("Math Tutoring")

    val state = viewModel.uiState.first()

    assertEquals("Math Tutoring", state.title)
    assertNull(state.invalidTitleMsg)
  }

  @Test
  fun setTitle_withBlankValue_setsErrorMessage() = runTest {
    viewModel.setTitle("")

    val state = viewModel.uiState.first()

    assertEquals("", state.title)
    assertEquals("Title cannot be empty", state.invalidTitleMsg)
  }

  @Test
  fun setDescription_updatesStateCorrectly() = runTest {
    viewModel.setDescription("Expert in calculus and algebra")

    val state = viewModel.uiState.first()

    assertEquals("Expert in calculus and algebra", state.description)
    assertNull(state.invalidDescMsg)
  }

  @Test
  fun setDescription_withBlankValue_setsErrorMessage() = runTest {
    viewModel.setDescription("")

    val state = viewModel.uiState.first()

    assertEquals("", state.description)
    assertEquals("Description cannot be empty", state.invalidDescMsg)
  }

  @Test
  fun setPrice_withValidPrice_updatesStateCorrectly() = runTest {
    viewModel.setPrice("25.50")

    val state = viewModel.uiState.first()

    assertEquals("25.50", state.price)
    assertNull(state.invalidPriceMsg)
  }

  @Test
  fun setPrice_withZeroPrice_isValid() = runTest {
    viewModel.setPrice("0")

    val state = viewModel.uiState.first()

    assertEquals("0", state.price)
    assertNull(state.invalidPriceMsg)
  }

  @Test
  fun setPrice_withBlankValue_setsErrorMessage() = runTest {
    viewModel.setPrice("")

    val state = viewModel.uiState.first()

    assertEquals("", state.price)
    assertEquals("Price cannot be empty", state.invalidPriceMsg)
  }

  @Test
  fun setPrice_withNegativeValue_setsErrorMessage() = runTest {
    viewModel.setPrice("-10")

    val state = viewModel.uiState.first()

    assertEquals("-10", state.price)
    assertEquals("Price must be a positive number", state.invalidPriceMsg)
  }

  @Test
  fun setPrice_withInvalidFormat_setsErrorMessage() = runTest {
    viewModel.setPrice("abc")

    val state = viewModel.uiState.first()

    assertEquals("abc", state.price)
    assertEquals("Price must be a positive number", state.invalidPriceMsg)
  }

  @Test
  fun setSubject_updatesStateCorrectly() = runTest {
    viewModel.setSubject(MainSubject.ACADEMICS)

    val state = viewModel.uiState.first()

    assertEquals(MainSubject.ACADEMICS, state.subject)
    assertNull(state.invalidSubjectMsg)
  }

  @Test
  fun setListingType_withProposal_updatesStateCorrectly() = runTest {
    viewModel.setListingType(ListingType.PROPOSAL)

    val state = viewModel.uiState.first()

    assertEquals(ListingType.PROPOSAL, state.listingType)
    assertNull(state.invalidListingTypeMsg)
  }

  @Test
  fun setListingType_withRequest_updatesStateCorrectly() = runTest {
    viewModel.setListingType(ListingType.REQUEST)

    val state = viewModel.uiState.first()

    assertEquals(ListingType.REQUEST, state.listingType)
    assertNull(state.invalidListingTypeMsg)
  }

  @Test
  fun setLocation_updatesStateCorrectly() = runTest {
    viewModel.setLocation(testLocation)

    val state = viewModel.uiState.first()

    assertEquals(testLocation, state.selectedLocation)
    assertEquals("Lausanne", state.locationQuery)
  }

  // ========== Location Search Tests ==========

  @Test
  fun setLocationQuery_withValidQuery_triggersSearch() = runTest {
    val searchResults =
        listOf(Location(46.5196535, 6.6322734, "Lausanne"), Location(46.2044, 6.1432, "Geneva"))
    coEvery { mockLocationRepository.search("Switz") } returns searchResults

    viewModel.setLocationQuery("Switz")
    testDispatcher.scheduler.advanceTimeBy(1100) // Wait for debounce delay

    val state = viewModel.uiState.first()

    assertEquals("Switz", state.locationQuery)
    assertEquals(searchResults, state.locationSuggestions)
    assertNull(state.invalidLocationMsg)
  }

  @Test
  fun setLocationQuery_withBlankQuery_clearsResults() = runTest {
    viewModel.setLocationQuery("")

    val state = viewModel.uiState.first()

    assertEquals("", state.locationQuery)
    assertTrue(state.locationSuggestions.isEmpty())
    assertEquals("You must choose a location", state.invalidLocationMsg)
    assertNull(state.selectedLocation)
  }

  @Test
  fun setLocationQuery_whenSearchFails_clearsSuggestions() = runTest {
    coEvery { mockLocationRepository.search(any()) } throws Exception("Network error")

    viewModel.setLocationQuery("Test")
    testDispatcher.scheduler.advanceTimeBy(1100)

    val state = viewModel.uiState.first()

    assertEquals("Test", state.locationQuery)
    assertTrue(state.locationSuggestions.isEmpty())
  }

  // ========== Validation Tests ==========

  @Test
  fun isValid_returnsFalse_whenFieldsAreEmpty() = runTest {
    val state = viewModel.uiState.first()

    assertFalse(state.isValid)
  }

  @Test
  fun isValid_returnsTrue_whenAllFieldsAreValid() = runTest {
    viewModel.setTitle("Math Tutoring")
    viewModel.setDescription("Expert tutor")
    viewModel.setPrice("25.00")
    viewModel.setSubject(MainSubject.ACADEMICS)
    viewModel.setListingType(ListingType.PROPOSAL)
    viewModel.setSubSkill("Algebra")
    viewModel.setLocation(testLocation)

    val state = viewModel.uiState.first()

    assertTrue(state.isValid)
  }

  @Test
  fun setError_setsAllErrorMessages_forInvalidFields() = runTest {
    viewModel.setError()

    val state = viewModel.uiState.first()

    assertEquals("Title cannot be empty", state.invalidTitleMsg)
    assertEquals("Description cannot be empty", state.invalidDescMsg)
    assertEquals("Price cannot be empty", state.invalidPriceMsg)
    assertEquals("You must choose a subject", state.invalidSubjectMsg)
    assertEquals("You must choose a listing type", state.invalidListingTypeMsg)
    assertEquals("You must choose a location", state.invalidLocationMsg)
  }

  @Test
  fun setError_doesNotSetErrors_forValidFields() = runTest {
    viewModel.setTitle("Valid Title")
    viewModel.setDescription("Valid Description")
    viewModel.setPrice("25.00")
    viewModel.setSubject(MainSubject.ACADEMICS)
    viewModel.setListingType(ListingType.PROPOSAL)
    viewModel.setLocation(testLocation)

    viewModel.setError()

    val state = viewModel.uiState.first()

    assertNull(state.invalidTitleMsg)
    assertNull(state.invalidDescMsg)
    assertNull(state.invalidPriceMsg)
    assertNull(state.invalidSubjectMsg)
    assertNull(state.invalidListingTypeMsg)
    assertNull(state.invalidLocationMsg)
  }

  // ========== Add Listing Tests ==========

  @Test
  fun addListing_withInvalidState_doesNotCallRepository() = runTest {
    viewModel.addListing()

    coVerify(exactly = 0) { mockListingRepository.addProposal(any()) }
    coVerify(exactly = 0) { mockListingRepository.addRequest(any()) }
  }

  @Test
  fun addListing_withValidProposal_callsAddProposal() = runTest {
    val mainDispatcher = StandardTestDispatcher(testScheduler)
    Dispatchers.setMain(mainDispatcher)
    try {
      // construct ViewModel after setting Main so viewModelScope uses the test dispatcher
      viewModel =
          NewListingViewModel(
              listingRepository = mockListingRepository,
              locationRepository = mockLocationRepository,
              userId = testUserId)

      // Arrange
      viewModel.setTitle("Math Tutoring")
      viewModel.setDescription("Expert tutor")
      viewModel.setPrice("30.00")
      viewModel.setListingType(ListingType.PROPOSAL)
      viewModel.setSubject(MainSubject.ACADEMICS)
      viewModel.setSubSkill("Algebra")
      viewModel.setLocation(testLocation)

      // Act
      viewModel.addListing()

      // Let scheduled coroutines run on the test scheduler
      advanceUntilIdle()

      // Assert
      coVerify(exactly = 1) {
        mockListingRepository.addProposal(
            match { proposal ->
              proposal.creatorUserId == testUserId &&
                  proposal.description == "Expert tutor" &&
                  proposal.hourlyRate == 30.00 &&
                  proposal.skill.mainSubject == MainSubject.ACADEMICS &&
                  proposal.location == testLocation
            })
      }
    } finally {
      Dispatchers.resetMain()
    }
  }

  @Test
  fun addListing_withValidRequest_callsAddRequest() = runTest {
    // Arrange: populate ViewModel with the inputs the ViewModel maps into the Request
    viewModel.setTitle("Need Math Help")
    viewModel.setDescription("Looking for algebra tutor")
    viewModel.setPrice("25.00")
    viewModel.setListingType(ListingType.REQUEST)
    viewModel.setSubject(MainSubject.ACADEMICS)
    viewModel.setSubSkill("Algebra")
    viewModel.setLocation(testLocation)

    // Act: trigger addListing (which launches a coroutine on viewModelScope)
    viewModel.addListing()

    // Allow the ViewModelScope coroutine on Dispatchers.Main / test dispatcher to run
    testDispatcher.scheduler.advanceUntilIdle()
    advanceUntilIdle()

    // Assert: repository was called with a Request that matches the mapped fields
    coVerify(exactly = 1) {
      mockListingRepository.addRequest(
          match { request ->
            request.creatorUserId == testUserId &&
                request.skill.mainSubject == MainSubject.ACADEMICS &&
                request.skill.skill == "Algebra" &&
                request.description == "Looking for algebra tutor" &&
                request.hourlyRate == 25.00 &&
                request.location == testLocation
          })
    }
  }

  @Test
  fun addListing_whenRepositoryThrowsException_doesNotCrash() = runTest {
    val mainDispatcher = StandardTestDispatcher(testScheduler)
    Dispatchers.setMain(mainDispatcher)
    try {
      // Make repo throw when adding a proposal
      coEvery { mockListingRepository.addProposal(any()) } throws RuntimeException("boom")

      // construct ViewModel after setting Main so viewModelScope uses the test dispatcher
      viewModel =
          NewListingViewModel(
              listingRepository = mockListingRepository,
              locationRepository = mockLocationRepository,
              userId = testUserId)

      // Arrange valid state
      viewModel.setTitle("Math Tutoring")
      viewModel.setDescription("Expert tutor")
      viewModel.setPrice("30.00")
      viewModel.setListingType(ListingType.PROPOSAL)
      viewModel.setSubject(MainSubject.ACADEMICS)
      viewModel.setSubSkill("Algebra")
      viewModel.setLocation(testLocation)

      // Act
      viewModel.addListing()

      // Let scheduled coroutines run (the thrown exception will be caught inside VM)
      advanceUntilIdle()

      // Assert repository was invoked (exception was handled by ViewModel)
      coVerify(exactly = 1) { mockListingRepository.addProposal(any()) }
    } finally {
      Dispatchers.resetMain()
    }
  }
  // ========== Edge Cases ==========

  @Test
  fun multipleFieldUpdates_maintainState() = runTest {
    viewModel.setTitle("Title 1")
    viewModel.setTitle("Title 2")
    viewModel.setDescription("Desc 1")
    viewModel.setDescription("Desc 2")

    val state = viewModel.uiState.first()

    assertEquals("Title 2", state.title)
    assertEquals("Desc 2", state.description)
  }

  @Test
  fun locationQueryDebounce_cancelsOnNewInput() = runTest {
    val results1 = listOf(Location(0.0, 0.0, "Location1"))
    val results2 = listOf(Location(0.0, 0.0, "Location2"))

    coEvery { mockLocationRepository.search("First") } returns results1
    coEvery { mockLocationRepository.search("Second") } returns results2

    viewModel.setLocationQuery("First")
    testDispatcher.scheduler.advanceTimeBy(500) // Less than debounce time
    viewModel.setLocationQuery("Second")
    testDispatcher.scheduler.advanceTimeBy(1100)

    val state = viewModel.uiState.first()

    // Should only have results from the second search
    assertEquals("Second", state.locationQuery)
    assertEquals(results2, state.locationSuggestions)

    // Verify first search was never executed (cancelled)
    coVerify(exactly = 0) { mockLocationRepository.search("First") }
    coVerify(exactly = 1) { mockLocationRepository.search("Second") }
  }
}
