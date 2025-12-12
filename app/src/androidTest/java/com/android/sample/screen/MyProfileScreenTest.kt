package com.android.sample.screen

import android.Manifest
import android.app.UiAutomation
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.booking.PaymentStatus
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
import com.android.sample.model.map.Location
import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.components.LocationInputFieldTestTags
import com.android.sample.ui.profile.MyProfileScreen
import com.android.sample.ui.profile.MyProfileScreenTestTag
import com.android.sample.ui.profile.MyProfileUIState
import com.android.sample.ui.profile.MyProfileViewModel
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MyProfileScreenTest {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  private val sampleProfile =
      Profile(
          userId = "demo",
          name = "Kendrick Lamar",
          email = "kendrick@gmail.com",
          description = "Performer and mentor",
          location = Location(name = "EPFL", longitude = 0.0, latitude = 0.0))

  private val sampleSkills =
      listOf(
          Skill(MainSubject.MUSIC, "SINGING", 10.0, ExpertiseLevel.EXPERT),
          Skill(MainSubject.MUSIC, "DANCING", 5.0, ExpertiseLevel.INTERMEDIATE),
          Skill(MainSubject.MUSIC, "GUITAR", 7.0, ExpertiseLevel.BEGINNER),
      )

  /** Fake repository for testing ViewModel logic */
  private class FakeRepo() : ProfileRepository {

    private val profiles = mutableMapOf<String, Profile>()
    private val skillsByUser = mutableMapOf<String, List<Skill>>()

    // observable test hooks
    var updateCalled: Boolean = false
    var updatedProfile: Profile? = null

    fun seed(profile: Profile, skills: List<Skill>) {
      profiles[profile.userId] = profile
      skillsByUser[profile.userId] = skills
    }

    override fun getNewUid() = "fake"

    override fun getCurrentUserId() = "current-user-id"

    override suspend fun getProfile(userId: String): Profile =
        profiles[userId] ?: error("No profile $userId")

    override suspend fun getProfileById(userId: String) = getProfile(userId)

    override suspend fun addProfile(profile: Profile) {
      profiles[profile.userId] = profile
    }

    override suspend fun updateProfile(userId: String, profile: Profile) {
      profiles[userId] = profile
      updateCalled = true
      updatedProfile = profile
    }

    override suspend fun deleteProfile(userId: String) {
      profiles.remove(userId)
      skillsByUser.remove(userId)
    }

    override suspend fun getAllProfiles(): List<Profile> = profiles.values.toList()

    override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
        emptyList<Profile>()

    override suspend fun getSkillsForUser(userId: String): List<Skill> =
        skillsByUser[userId] ?: emptyList()

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

  // Minimal Fake ListingRepository to avoid initializing real Firebase/Firestore in tests
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

    override suspend fun deleteAllListingOfUser(userId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun deactivateListing(listingId: String) {}

    override suspend fun searchBySkill(skill: Skill): List<Listing> = emptyList()

    override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> =
        emptyList()
  }

  private class FakeBookingRepo : BookingRepository {
    private val items = mutableListOf<Booking>()

    fun seed(vararg bookings: Booking) {
      items.clear()
      items.addAll(bookings.toList())
    }

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

    override suspend fun deleteAllBookingOfUser(userId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

    override suspend fun updatePaymentStatus(bookingId: String, paymentStatus: PaymentStatus) {}

    override suspend fun confirmBooking(bookingId: String) {}

    override suspend fun completeBooking(bookingId: String) {}

    override suspend fun cancelBooking(bookingId: String) {}
  }

  private class FakeRatingRepo : RatingRepository {

    override fun getNewUid(): String = "fake-rating-id"

    // NEW: required by RatingRepository
    override suspend fun hasRating(
        fromUserId: String,
        toUserId: String,
        ratingType: com.android.sample.model.rating.RatingType,
        targetObjectId: String
    ): Boolean {
      // MyProfileScreen tests don't care about this, so always "no rating yet" is fine.
      return false
    }

    override suspend fun getAllRatings(): List<Rating> = emptyList()

    override suspend fun getRating(ratingId: String): Rating? = null

    override suspend fun getRatingsByFromUser(fromUserId: String): List<Rating> = emptyList()

    override suspend fun getRatingsByToUser(toUserId: String): List<Rating> = emptyList()

    override suspend fun getRatingsOfListing(listingId: String): List<Rating> = emptyList()

    override suspend fun addRating(rating: Rating) {}

    override suspend fun updateRating(ratingId: String, rating: Rating) {}

    override suspend fun deleteRating(ratingId: String) {}

    /** Gets all tutor ratings for listings owned by this user */
    override suspend fun getTutorRatingsOfUser(userId: String): List<Rating> = emptyList()

    /** Gets all student ratings received by this user */
    override suspend fun getStudentRatingsOfUser(userId: String): List<Rating> = emptyList()

    override suspend fun deleteAllRatingOfUser(userId: String) {
      TODO("Not yet implemented")
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
    override fun getNewUid(): String {
      TODO("Not yet implemented")
    }

    override suspend fun getOverViewConvUser(userId: String): List<OverViewConversation> {
      TODO("Not yet implemented")
    }

    override suspend fun addOverViewConvUser(overView: OverViewConversation) {
      TODO("Not yet implemented")
    }

    override suspend fun deleteOverViewConvUser(convId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun deleteOverViewById(overViewId: String) {
      TODO("Not yet implemented")
    }

    override fun listenOverView(userId: String): Flow<List<OverViewConversation>> {
      TODO("Not yet implemented")
    }
  }

  private lateinit var viewModel: MyProfileViewModel
  private val logoutClicked = AtomicBoolean(false)
  private lateinit var repo: FakeRepo

  private lateinit var contentSlot: MutableState<@Composable () -> Unit>

  @Before
  fun setup() {
    BookingRepositoryProvider.setForTests(FakeBookingRepo())
    ConversationRepositoryProvider.setForTests(FakeConversationRepo())
    OverViewConvRepositoryProvider.setForTests(FakeOverViewConvRepo())
    repo = FakeRepo().apply { seed(sampleProfile, sampleSkills) }
    UserSessionManager.setCurrentUserId("demo")
    viewModel =
        MyProfileViewModel(
            repo,
            listingRepository = FakeListingRepo(),
            bookingRepository = FakeBookingRepo(),
            ratingsRepository = FakeRatingRepo(),
            sessionManager = UserSessionManager)

    // reset flag before each test and set content once per test
    logoutClicked.set(false)
    compose.setContent {
      val slot = remember {
        mutableStateOf<@Composable () -> Unit>({
          MyProfileScreen(
              profileViewModel = viewModel,
              profileId = "demo",
              onLogout = { logoutClicked.set(true) })
        })
      }
      // expose the remembered slot to the test class
      contentSlot = slot

      // render current content
      slot.value()
    }

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(MyProfileScreenTestTag.NAME_DISPLAY, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  @After
  fun tearDown() {
    UserSessionManager.clearSession()
  }

  // Helper: wait for the LazyColumn to appear and scroll it so the logout button becomes visible
  private fun ensureLogoutVisible() {
    // Wait until the LazyColumn (root list) is present in unmerged tree
    compose.waitUntil(timeoutMillis = 5_000) {
      compose
          .onAllNodesWithTag(MyProfileScreenTestTag.ROOT_LIST, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Scroll the LazyColumn to the logout button using the unmerged tree (targets LazyColumn)
    compose
        .onNodeWithTag(MyProfileScreenTestTag.ROOT_LIST, useUnmergedTree = true)
        .performScrollToNode(hasTestTag(MyProfileScreenTestTag.LOGOUT_BUTTON))

    // Wait for the merged tree to expose the logout button
    compose.waitUntil(timeoutMillis = 2_000) {
      compose
          .onAllNodesWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  // Helper: scroll so the delete account button becomes visible
  private fun ensureDeleteVisible() {
    compose.waitUntil(timeoutMillis = 5_000) {
      compose
          .onAllNodesWithTag(MyProfileScreenTestTag.ROOT_LIST, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    compose
        .onNodeWithTag(MyProfileScreenTestTag.ROOT_LIST, useUnmergedTree = true)
        .performScrollToNode(hasTestTag(MyProfileScreenTestTag.DELETE_ACCOUNT_BUTTON))

    compose.waitUntil(timeoutMillis = 2_000) {
      compose
          .onAllNodesWithTag(MyProfileScreenTestTag.DELETE_ACCOUNT_BUTTON)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  // --- TESTS ---

  @Test
  fun profileInfo_isDisplayedCorrectly() {
    compose.onNodeWithTag(MyProfileScreenTestTag.PROFILE_ICON).assertIsDisplayed()
    compose
        .onNodeWithTag(MyProfileScreenTestTag.NAME_DISPLAY)
        .assertIsDisplayed()
        .assertTextContains("Kendrick Lamar")
    compose.onNodeWithTag(MyProfileScreenTestTag.ROLE_BADGE).assertTextEquals("Student")
  }

  // ----------------------------------------------------------
  // NAME FIELD TESTS
  // ----------------------------------------------------------
  @Test
  fun nameField_displaysCorrectInitialValue() {
    compose
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME)
        .assertTextContains("Kendrick Lamar")
  }

  @Test
  fun nameField_canBeEdited() {
    val newName = "K Dot"
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).performTextClearance()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).performTextInput(newName)
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).assertTextContains(newName)
  }

  @Test
  fun nameField_showsError_whenEmpty() {
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).performTextClearance()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME).performTextInput("")
    compose
        .onNodeWithTag(MyProfileScreenTestTag.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  // ----------------------------------------------------------
  // EMAIL FIELD TESTS
  // ----------------------------------------------------------
  @Test
  fun emailField_displaysCorrectInitialValue() {
    compose
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL)
        .assertTextContains("kendrick@gmail.com")
  }

  @Test
  fun emailField_canBeEdited() {
    val newEmail = "kdot@gmail.com"
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL).performTextClearance()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL).performTextInput(newEmail)
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL).assertTextContains(newEmail)
  }

  @Test
  fun emailField_showsError_whenInvalid() {
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL).performTextClearance()
    compose
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL)
        .performTextInput("invalidEmail")
    compose
        .onNodeWithTag(MyProfileScreenTestTag.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  // ----------------------------------------------------------
  // LOCATION FIELD TESTS
  // ----------------------------------------------------------
  @Test
  fun locationField_displaysCorrectInitialValue() {
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).assertTextContains("EPFL")
  }

  @Test
  fun locationField_canBeEdited() {
    val newLocation = "Harvard University"
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextClearance()
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextInput(newLocation)
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).assertTextContains(newLocation)
  }

  @Test
  fun locationField_showsError_whenEmpty() {
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextClearance()
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextInput(" ")
    compose
        .onNodeWithTag(LocationInputFieldTestTags.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun clickingPin_whenPermissionGranted_executesGrantedBranch() {
    // Grant runtime permission before composing the screen.
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val uiAutomation: UiAutomation = instrumentation.uiAutomation
    val packageName = compose.activity.packageName

    try {
      uiAutomation.grantRuntimePermission(packageName, Manifest.permission.ACCESS_FINE_LOCATION)
    } catch (_: SecurityException) {}

    // Wait for UI to be ready
    compose.waitForIdle()

    // Click the pin - with permission granted the onClick should take the 'granted' branch.
    compose
        .onNodeWithContentDescription(MyProfileScreenTestTag.PIN_CONTENT_DESC)
        .assertExists()
        .performClick()

    // No crash + the branch was executed. Basic assertion to ensure UI still shows expected info.
    compose.onNodeWithTag(MyProfileScreenTestTag.NAME_DISPLAY).assertExists()
  }

  // ----------------------------------------------------------
  // DESCRIPTION FIELD TESTS
  // ----------------------------------------------------------
  @Test
  fun descriptionField_displaysCorrectInitialValue() {
    compose
        .onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC)
        .assertTextContains("Performer and mentor")
  }

  @Test
  fun descriptionField_canBeEdited() {
    val newDesc = "Artist and teacher"
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).performTextClearance()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).performTextInput(newDesc)
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).assertTextContains(newDesc)
  }

  @Test
  fun descriptionField_showsError_whenEmpty() {
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).performTextClearance()
    compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC).performTextInput("")
    compose
        .onNodeWithTag(MyProfileScreenTestTag.ERROR_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  // ----------------------------------------------------------
  // GPS PIN BUTTON + SAVE FLOW TESTS
  // ----------------------------------------------------------
  @Test
  fun pinButton_isDisplayed_and_clickable() {
    compose
        .onNodeWithContentDescription(MyProfileScreenTestTag.PIN_CONTENT_DESC)
        .assertExists()
        .assertHasClickAction()
  }

  @Test
  fun clickingPin_thenSave_persistsLocation() {
    val gpsName = "12.34, 56.78"
    compose.runOnIdle {
      viewModel.setLocation(Location(name = gpsName, latitude = 12.34, longitude = 56.78))
    }

    // UI should reflect the location query
    compose.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).assertTextContains(gpsName)

    // Click save
    compose.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON).performClick()

    // Wait until repo update is called

    assertEquals(gpsName, viewModel.uiState.value.locationQuery)
  }

  // ----------------------------------------------------------
  // LOGOUT BUTTON TESTS
  // ----------------------------------------------------------
  @Test
  fun logoutButton_isDisplayed() {
    ensureLogoutVisible()
    compose.onNodeWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun logoutButton_isClickable() {
    ensureLogoutVisible()
    compose.onNodeWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON).assertHasClickAction()
  }

  @Test
  fun logoutButton_hasCorrectText() {
    ensureLogoutVisible()
    compose.onNodeWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON).assertTextContains("Logout")
  }

  @Test
  fun logoutButton_triggersCallback() {
    ensureLogoutVisible()
    compose.onNodeWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON).performClick()
    compose.waitForIdle()
    assert(logoutClicked.get())
  }

  // ----------------------------------------------------------
  // SAVE BUTTON TESTS
  // ----------------------------------------------------------
  @Test
  fun saveButton_isDisplayed() {
    compose.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun saveButton_isClickable() {
    compose.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON).assertHasClickAction()
  }

  @Test
  fun saveButton_hasCorrectText() {
    compose
        .onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON)
        .assertTextContains("Save Profile Changes")
  }

  // ----------------------------------------------------------
  // PROFILE ICON TESTS
  // ----------------------------------------------------------
  @Test
  fun profileIcon_displaysFirstLetterOfName() {
    // The profile icon should display "K" from "Kendrick Lamar"
    compose.onNodeWithTag(MyProfileScreenTestTag.PROFILE_ICON).assertIsDisplayed()
  }

  // Edge case test for empty name is in MyProfileScreenEdgeCasesTest.kt

  // ----------------------------------------------------------
  // CARD TITLE TEST
  // ----------------------------------------------------------
  @Test
  fun cardTitle_isDisplayed() {
    compose
        .onNodeWithTag(MyProfileScreenTestTag.CARD_TITLE)
        .assertIsDisplayed()
        .assertTextEquals("Personal Details")
  }

  // ----------------------------------------------------------
  // ROLE BADGE TEST
  // ----------------------------------------------------------
  @Test
  fun roleBadge_displaysStudent() {
    compose
        .onNodeWithTag(MyProfileScreenTestTag.ROLE_BADGE)
        .assertIsDisplayed()
        .assertTextEquals("Student")
  }

  @Test
  fun tabBar_isDisplayed() {
    compose.onNodeWithTag(MyProfileScreenTestTag.INFO_RATING_BAR).assertIsDisplayed()
  }

  @Test
  fun ratingTabIsDisplayed() {
    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_TAB).assertIsDisplayed()
  }

  @Test
  fun infoTabIsDisplayed() {
    compose.onNodeWithTag(MyProfileScreenTestTag.INFO_TAB).assertIsDisplayed()
  }

  @Test
  fun ratingTabIsClickable() {
    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_TAB).assertHasClickAction()
  }

  @Test
  fun infoTabIsClickable() {
    compose.onNodeWithTag(MyProfileScreenTestTag.INFO_TAB).assertHasClickAction()
  }

  @Test
  fun ratingTabSwitchesContent() {

    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_TAB).assertIsDisplayed().performClick()

    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_SECTION).assertIsDisplayed()
  }

  @Test
  fun infoTabSwitchesContent() {
    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_TAB).assertIsDisplayed().performClick()

    compose.onNodeWithTag(MyProfileScreenTestTag.INFO_RATING_BAR).assertIsDisplayed()
  }

  @Test
  fun bothTabsAreClickable() {
    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_TAB).assertIsDisplayed().performClick()

    compose.onNodeWithTag(MyProfileScreenTestTag.RATING_SECTION).assertIsDisplayed()

    compose.onNodeWithTag(MyProfileScreenTestTag.INFO_TAB).assertIsDisplayed().performClick()

    compose.onNodeWithTag(MyProfileScreenTestTag.PROFILE_ICON).assertIsDisplayed()
  }

  @Test
  fun historyTab_isDisplayed() {
    compose.onNodeWithTag(MyProfileScreenTestTag.HISTORY_TAB).assertIsDisplayed()
  }

  @Test
  fun historyTab_isClickable() {
    compose.onNodeWithTag(MyProfileScreenTestTag.HISTORY_TAB).assertHasClickAction()
  }

  @Test
  fun historyTab_switchesContentToHistorySection() {
    val bookingRepo =
        FakeBookingRepo().apply {
          seed(
              Booking(
                  bookingId = "b1",
                  associatedListingId = "p1",
                  listingCreatorId = "demo",
                  bookerId = "demo",
                  status = BookingStatus.COMPLETED))
        }
    UserSessionManager.setCurrentUserId("demo")
    val vm =
        MyProfileViewModel(
            profileRepository = repo,
            listingRepository = FakeListingRepo(),
            ratingsRepository = FakeRatingRepo(),
            bookingRepository = bookingRepo,
            sessionManager = UserSessionManager)

    compose.runOnIdle {
      contentSlot.value = {
        MyProfileScreen(
            profileViewModel = vm, profileId = "demo", onLogout = { logoutClicked.set(true) })
      }
    }

    compose.onNodeWithTag(MyProfileScreenTestTag.HISTORY_TAB).performClick()
    compose.onNodeWithTag(MyProfileScreenTestTag.HISTORY_SECTION).assertIsDisplayed()
  }

  private class BlockingListingRepo : ListingRepository {
    val gate = CompletableDeferred<Unit>()

    override fun getNewUid(): String = "blocking"

    override suspend fun getAllListings() = emptyList<Listing>()

    override suspend fun getProposals() = emptyList<Proposal>()

    override suspend fun getRequests() = emptyList<Request>()

    override suspend fun getListing(listingId: String) = null

    override suspend fun getListingsByUser(userId: String): List<Listing> {
      gate.await()
      return emptyList()
    }

    override suspend fun addProposal(proposal: Proposal) {}

    override suspend fun addRequest(request: Request) {}

    override suspend fun updateListing(listingId: String, listing: Listing) {}

    override suspend fun deleteListing(listingId: String) {}

    override suspend fun deleteAllListingOfUser(userId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun deactivateListing(listingId: String) {}

    override suspend fun searchBySkill(skill: Skill) = emptyList<Listing>()

    override suspend fun searchByLocation(location: Location, radiusKm: Double) =
        emptyList<Listing>()
  }

  @Test
  fun listings_showsLoadingIndicator_whenLoadingTrue() {
    val blockingRepo = BlockingListingRepo()
    val ratingRepo = FakeRatingRepo()
    val pRepo = FakeRepo().apply { seed(sampleProfile, sampleSkills) }
    UserSessionManager.setCurrentUserId("demo")
    val vm =
        MyProfileViewModel(
            pRepo,
            listingRepository = blockingRepo,
            bookingRepository = FakeBookingRepo(),
            ratingsRepository = ratingRepo,
            sessionManager = UserSessionManager)

    compose.runOnIdle {
      contentSlot.value = {
        MyProfileScreen(
            profileViewModel = vm, profileId = "demo", onLogout = { logoutClicked.set(true) })
      }
    }

    // wait screen ready
    compose.onNodeWithTag(MyProfileScreenTestTag.LISTINGS_TAB).performClick()

    val progressMatcher = hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)

    compose.waitUntil(5_000) {
      compose.onAllNodes(progressMatcher, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    }

    compose.onNode(progressMatcher, useUnmergedTree = true).assertExists()

    // release the gate
    compose.runOnIdle { blockingRepo.gate.complete(Unit) }
  }

  private class ErrorListingRepo : ListingRepository {
    override fun getNewUid(): String = "error"

    override suspend fun getAllListings() = emptyList<Listing>()

    override suspend fun getProposals() = emptyList<Proposal>()

    override suspend fun getRequests() = emptyList<Request>()

    override suspend fun getListing(listingId: String) = null

    override suspend fun getListingsByUser(userId: String): List<Listing> {
      throw RuntimeException("test listings failure")
    }

    override suspend fun addProposal(proposal: Proposal) {}

    override suspend fun addRequest(request: Request) {}

    override suspend fun updateListing(listingId: String, listing: Listing) {}

    override suspend fun deleteListing(listingId: String) {}

    override suspend fun deleteAllListingOfUser(userId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun deactivateListing(listingId: String) {}

    override suspend fun searchBySkill(skill: Skill) = emptyList<Listing>()

    override suspend fun searchByLocation(location: Location, radiusKm: Double) =
        emptyList<Listing>()
  }

  @Test
  fun listings_showsErrorMessage_whenErrorPresent() {
    val errorRepo = ErrorListingRepo()
    val ratingRepo = FakeRatingRepo()
    val pRepo = FakeRepo().apply { seed(sampleProfile, sampleSkills) }
    UserSessionManager.setCurrentUserId("demo")
    val vm =
        MyProfileViewModel(
            pRepo,
            listingRepository = errorRepo,
            ratingsRepository = ratingRepo,
            sessionManager = UserSessionManager)

    compose.runOnIdle {
      contentSlot.value = {
        MyProfileScreen(
            profileViewModel = vm, profileId = "demo", onLogout = { logoutClicked.set(true) })
      }
    }

    compose.onNodeWithTag(MyProfileScreenTestTag.LISTINGS_TAB).performClick()

    compose.onNodeWithText("Failed to load listings.").assertExists()
  }

  private class OneItemListingRepo(private val listing: Listing) : ListingRepository {
    override fun getNewUid(): String = "one"

    override suspend fun getAllListings() = emptyList<Listing>()

    override suspend fun getProposals() = emptyList<Proposal>()

    override suspend fun getRequests() = emptyList<Request>()

    override suspend fun getListing(listingId: String) = null

    override suspend fun getListingsByUser(userId: String): List<Listing> = listOf(listing)

    override suspend fun addProposal(proposal: Proposal) {}

    override suspend fun addRequest(request: Request) {}

    override suspend fun updateListing(listingId: String, listing: Listing) {}

    override suspend fun deleteListing(listingId: String) {}

    override suspend fun deleteAllListingOfUser(userId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun deactivateListing(listingId: String) {}

    override suspend fun searchBySkill(skill: Skill) = emptyList<Listing>()

    override suspend fun searchByLocation(location: Location, radiusKm: Double) =
        emptyList<Listing>()
  }

  private fun makeTestListing(): Proposal =
      Proposal(
          listingId = "p1",
          creatorUserId = "demo",
          description = "Guitar Lessons",
          skill = Skill(mainSubject = MainSubject.MUSIC, skill = "GUITAR"),
          location = Location(name = "EPFL", latitude = 0.0, longitude = 0.0),
          hourlyRate = 25.0,
          isActive = true)

  @Test
  fun listings_rendersNonEmptyList_elseBranch() {
    val pRepo = FakeRepo().apply { seed(sampleProfile, sampleSkills) }
    val listing = makeTestListing()
    val rating = FakeRatingRepo()
    val oneItemRepo = OneItemListingRepo(listing)
    UserSessionManager.setCurrentUserId("demo")
    val vm =
        MyProfileViewModel(
            pRepo,
            listingRepository = oneItemRepo,
            ratingsRepository = rating,
            sessionManager = UserSessionManager)

    compose.runOnIdle {
      contentSlot.value = {
        MyProfileScreen(
            profileViewModel = vm, profileId = "demo", onLogout = { logoutClicked.set(true) })
      }
    }

    compose.onNodeWithTag(MyProfileScreenTestTag.LISTINGS_TAB).performClick()

    compose
        .onNodeWithText("You don’t have any listings yet.", useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  @Suppress("UNCHECKED_CAST")
  fun successMessage_isShown_whenUpdateSuccessTrue() {
    compose.runOnIdle {
      val current = viewModel.uiState.value
      viewModel.clearUpdateSuccess()
      viewModel.apply {
        val newState = current.copy(updateSuccess = true)
        val field = MyProfileViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        val stateFlow =
            field.get(this) as kotlinx.coroutines.flow.MutableStateFlow<MyProfileUIState>
        stateFlow.value = newState
      }
    }

    val successMatcher = hasText("Profile successfully updated!")
    compose.waitUntil(5_000) {
      compose.onAllNodes(successMatcher, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    }

    compose.onNode(successMatcher, useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun history_showsEmptyMessage() {
    val bookingRepo = FakeBookingRepo()
    UserSessionManager.setCurrentUserId("demo")
    val vm =
        MyProfileViewModel(
            profileRepository = repo,
            listingRepository = FakeListingRepo(),
            ratingsRepository = FakeRatingRepo(),
            bookingRepository = bookingRepo,
            sessionManager = UserSessionManager)

    compose.runOnIdle {
      contentSlot.value = {
        MyProfileScreen(
            profileViewModel = vm, profileId = "demo", onLogout = { logoutClicked.set(true) })
      }
    }

    compose.onNodeWithTag(MyProfileScreenTestTag.HISTORY_TAB).performClick()

    compose.onNodeWithText("You don’t have any completed bookings yet.").assertExists()
  }

  // ----------------------------------------------------------
  // DELETE ACCOUNT BUTTON & DIALOG TESTS
  // ----------------------------------------------------------
  @Test
  fun deleteAccountButton_isDisplayed() {
    ensureDeleteVisible()
    compose.onNodeWithTag(MyProfileScreenTestTag.DELETE_ACCOUNT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun deleteAccountButton_isClickable() {
    ensureDeleteVisible()
    compose.onNodeWithTag(MyProfileScreenTestTag.DELETE_ACCOUNT_BUTTON).assertHasClickAction()
  }

  @Test
  fun deleteAccountButton_opensConfirmationDialog() {
    ensureDeleteVisible()

    compose.onNodeWithTag(MyProfileScreenTestTag.DELETE_ACCOUNT_BUTTON).performClick()

    compose
        .onNodeWithTag(MyProfileScreenTestTag.DELETE_ACCOUNT_DIALOG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun deleteAccountDialog_showsConfirmAndCancelButtons() {
    ensureDeleteVisible()

    compose.onNodeWithTag(MyProfileScreenTestTag.DELETE_ACCOUNT_BUTTON).performClick()

    compose
        .onNodeWithTag(MyProfileScreenTestTag.DELETE_ACCOUNT_CONFIRM_BUTTON, useUnmergedTree = true)
        .assertIsDisplayed()
    compose
        .onNodeWithTag(MyProfileScreenTestTag.DELETE_ACCOUNT_CANCEL_BUTTON, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun deleteAccountDialog_dismissesOnCancel() {
    ensureDeleteVisible()

    compose.onNodeWithTag(MyProfileScreenTestTag.DELETE_ACCOUNT_BUTTON).performClick()

    // Dialog should be visible
    compose
        .onNodeWithTag(MyProfileScreenTestTag.DELETE_ACCOUNT_DIALOG, useUnmergedTree = true)
        .assertIsDisplayed()

    // Tap Cancel
    compose
        .onNodeWithTag(MyProfileScreenTestTag.DELETE_ACCOUNT_CANCEL_BUTTON, useUnmergedTree = true)
        .performClick()

    // Dialog should disappear
    compose
        .onNodeWithTag(MyProfileScreenTestTag.DELETE_ACCOUNT_DIALOG, useUnmergedTree = true)
        .assertDoesNotExist()
  }
}
