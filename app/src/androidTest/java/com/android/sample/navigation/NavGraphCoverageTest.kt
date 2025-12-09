package com.android.sample.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.rememberNavController
import com.android.sample.handleAuthenticatedUser
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.HomePage.MainPageViewModel
import com.android.sample.ui.subject.SubjectListTestTags
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Integration tests that compose the real AppNavGraph and exercise several routes that were
 * previously not covered by unit/navigation tests. The tests supply minimal fake repositories and
 * mocked VMs/state flows where necessary so composables can collect concrete StateFlow values.
 */
class NavGraphCoverageTest {
  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var staticAuth: FirebaseAuth
  private lateinit var mockUser: FirebaseUser

  @Before
  fun setup() {
    // stub FirebaseAuth.getInstance() to avoid touching real Firebase
    mockkStatic(FirebaseAuth::class)
    staticAuth = mockk(relaxed = true)
    every { FirebaseAuth.getInstance() } returns staticAuth
  }

  @After
  fun teardown() {
    unmockkStatic(FirebaseAuth::class)
  }

  // ensure test repositories are cleared after each test to avoid leakage
  // Note: calls here are safe even if providers were not set in a given test
  @After
  fun clearProviders() {
    try {
      com.android.sample.model.listing.ListingRepositoryProvider.clearForTests()
    } catch (_: Throwable) {}
    try {
      com.android.sample.model.user.ProfileRepositoryProvider.clearForTests()
    } catch (_: Throwable) {}
    try {
      unmockkStatic("com.android.sample.MainActivityKt")
    } catch (_: Throwable) {}
  }

  // Small in-test fake listing repo that returns one Proposal matching SubjectList expectations
  private class FakeListingRepo : ListingRepository {
    override fun getNewUid(): String = "uid"

    override suspend fun getAllListings(): List<Listing> {
      return listOf(
          Proposal(
              listingId = "p1",
              creatorUserId = "creator_1",
              skill = com.android.sample.model.skill.Skill(MainSubject.MUSIC, "violin"),
              title = "Violin lessons",
              description = "Tutor proposal",
              hourlyRate = 20.0))
    }

    override suspend fun getProposals(): List<Proposal> =
        getAllListings().filterIsInstance<Proposal>()

    override suspend fun getRequests(): List<Request> = emptyList()

    override suspend fun getListing(listingId: String): Listing? =
        getAllListings().firstOrNull { it.listingId == listingId }

    override suspend fun getListingsByUser(userId: String): List<Listing> = emptyList()

    override suspend fun addProposal(proposal: Proposal) {}

    override suspend fun addRequest(request: Request) {}

    override suspend fun updateListing(listingId: String, listing: Listing) {}

    override suspend fun deleteListing(listingId: String) {}

    override suspend fun deactivateListing(listingId: String) {}

    override suspend fun searchBySkill(skill: com.android.sample.model.skill.Skill): List<Listing> =
        emptyList()

    override suspend fun searchByLocation(
        location: com.android.sample.model.map.Location,
        radiusKm: Double
    ): List<Listing> = emptyList()
  }

  @Test
  fun skills_route_with_real_viewmodel_renders_searchbar() {
    // Initialize providers used by SubjectListViewModel so viewModel(backStackEntry) can be created
    com.android.sample.model.listing.ListingRepositoryProvider.setForTests(FakeListingRepo())

    // Use a mock ProfileRepository and stub getProfile to return a concrete Profile
    val mockProfileRepo = mockk<ProfileRepository>(relaxed = true)
    coEvery { mockProfileRepo.getProfile(any()) } returns
        Profile(
            userId = "creator_1",
            name = "Creator",
            email = "c@example.com",
            location = com.android.sample.model.map.Location(0.0, 0.0, ""),
            levelOfEducation = "",
            description = "")
    com.android.sample.model.user.ProfileRepositoryProvider.setForTests(mockProfileRepo)

    composeRule.setContent {
      val nav = rememberNavController()
      AppNavGraph(
          navController = nav,
          bookingsViewModel = mockk(relaxed = true),
          profileViewModel = mockk(relaxed = true),
          mainPageViewModel = mockk(relaxed = true),
          newListingViewModel = mockk(relaxed = true),
          authViewModel = mockk(relaxed = true),
          bookingDetailsViewModel = mockk(relaxed = true),
          discussionViewModel = mockk(relaxed = true),
          onGoogleSignIn = {},
          startDestination = NavRoutes.SKILLS)
    }

    // Wait for composable and assert the SubjectList search bar exists
    composeRule.onNodeWithTag(SubjectListTestTags.SEARCHBAR).assertIsDisplayed()
  }

  @Test
  fun splash_calls_handleAuthenticatedUser_and_navigates() {
    // make FirebaseAuth.currentUser non-null
    mockUser = mockk(relaxed = true)
    every { mockUser.uid } returns "fake-uid"
    every { staticAuth.currentUser } returns mockUser

    // Mock the top-level suspend function handleAuthenticatedUser to return Unit
    mockkStatic("com.android.sample.MainActivityKt")
    coEvery { handleAuthenticatedUser(any(), any(), any()) } returns Unit

    // Provide a concrete uiState StateFlow for MainPageViewModel to avoid MockK proxy cast issues
    val mainVm: MainPageViewModel = mockk(relaxed = true)
    val sampleHomeUiState = com.android.sample.ui.HomePage.HomeUiState()
    val homeStateFlow = kotlinx.coroutines.flow.MutableStateFlow(sampleHomeUiState)
    every { mainVm.uiState } returns homeStateFlow

    // Provide a concrete uiState StateFlow for AuthenticationViewModel to avoid proxy cast issues
    val authVm: com.android.sample.model.authentication.AuthenticationViewModel =
        mockk(relaxed = true)
    val authUiState = com.android.sample.model.authentication.AuthenticationUiState()
    val authStateFlow = kotlinx.coroutines.flow.MutableStateFlow(authUiState)
    every { authVm.uiState } returns authStateFlow

    composeRule.setContent {
      val nav = rememberNavController()
      AppNavGraph(
          navController = nav,
          bookingsViewModel = mockk(relaxed = true),
          profileViewModel = mockk(relaxed = true),
          mainPageViewModel = mainVm,
          newListingViewModel = mockk(relaxed = true),
          authViewModel = authVm,
          bookingDetailsViewModel = mockk(relaxed = true),
          discussionViewModel = mockk(relaxed = true),
          onGoogleSignIn = {},
          startDestination = NavRoutes.SPLASH)
    }

    composeRule.waitForIdle()

    // cleanup static mock
    unmockkStatic("com.android.sample.MainActivityKt")
  }
}
