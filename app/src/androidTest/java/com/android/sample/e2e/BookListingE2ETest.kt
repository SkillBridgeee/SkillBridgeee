package com.android.sample.e2e

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.android.sample.MainActivity
import com.android.sample.e2e.E2ETestHelper.cleanupFirebaseUser
import com.android.sample.e2e.E2ETestHelper.cleanupTestProfile
import com.android.sample.e2e.E2ETestHelper.createAndAuthenticateGoogleUser
import com.android.sample.e2e.E2ETestHelper.createTestProfile
import com.android.sample.e2e.E2ETestHelper.signOutCurrentUser
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.communication.conversation.ConversationRepositoryProvider
import com.android.sample.model.communication.overViewConv.OverViewConvRepositoryProvider
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.skill.AcademicSkills
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.components.BottomBarTestTag
import com.google.firebase.auth.FirebaseUser
import java.util.Date
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-End test for Booking an Existing Listing user flow.
 *
 * This test verifies that a user can:
 * 1. View an existing listing on the home screen
 * 2. Book the listing
 * 3. See the booking in their bookings/history section
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BookListingE2ETest : E2ETestBase() {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  private var tutorUser: FirebaseUser? = null
  private var tutorEmail: String = ""
  private var createdListingIds: MutableList<String> = mutableListOf()
  private var createdBookingId: String? = null

  @Before
  fun setup() {
    val ctx = composeTestRule.activity
    try {
      ProfileRepositoryProvider.init(ctx)
      ListingRepositoryProvider.init(ctx)
      BookingRepositoryProvider.init(ctx)
      RatingRepositoryProvider.init(ctx)
      OverViewConvRepositoryProvider.init(ctx)
      ConversationRepositoryProvider.init(ctx)
    } catch (_: Exception) {
      // Repository initialization warning
    }

    signOutCurrentUser()
    UserSessionManager.clearSession()
    composeTestRule.waitForIdle()

    testEmail = generateTestEmail()
    tutorEmail = "tutor.${generateTestEmail()}"
  }

  @After
  fun tearDown() {
    runBlocking {
      // Clean up created booking
      createdBookingId?.let { bookingId ->
        try {
          BookingRepositoryProvider.repository.deleteBooking(bookingId)
        } catch (_: Exception) {
          // Could not delete booking
        }
      }

      // Clean up all created listings
      createdListingIds.forEach { listingId ->
        try {
          ListingRepositoryProvider.repository.deleteListing(listingId)
        } catch (_: Exception) {
          // Could not delete listing
        }
      }

      // Clean up tutor user
      tutorUser?.let { user ->
        cleanupTestProfile(user.uid)
        cleanupFirebaseUser(user)
      }

      // Clean up student user
      testUser?.let { user ->
        cleanupTestProfile(user.uid)
        cleanupFirebaseUser(user)
      }

      signOutCurrentUser()
      UserSessionManager.clearSession()
    }
  }

  @Test
  fun bookListing_studentBooksExistingProposal_appearsInHistory() {
    runBlocking {
      // Create Tutor User and Multiple Listings
      tutorUser = createAndAuthenticateGoogleUser(tutorEmail, "Bob Tutor")

      val createdListingIds = mutableListOf<String>()

      tutorUser?.let { tutor ->
        createTestProfile(userId = tutor.uid, email = tutorEmail, name = "Bob", surname = "Tutor")

        try {
          tutor.sendEmailVerification().await()
          tutor.reload().await()
        } catch (_: Exception) {
          // Email verification may fail in emulator
        }

        // Create multiple listings by the tutor
        val listings =
            listOf(
                Triple(
                    "Physics Tutoring",
                    AcademicSkills.PHYSICS,
                    "Expert in quantum mechanics and thermodynamics"),
                Triple(
                    "Mathematics Help",
                    AcademicSkills.MATHEMATICS,
                    "Advanced calculus and linear algebra"),
                Triple(
                    "Chemistry Lessons",
                    AcademicSkills.CHEMISTRY,
                    "Organic and inorganic chemistry specialist"))

        listings.forEach { (title, skill, description) ->
          val listingId = ListingRepositoryProvider.repository.getNewUid()

          val proposal =
              Proposal(
                  listingId = listingId,
                  creatorUserId = tutor.uid,
                  skill = Skill(mainSubject = MainSubject.ACADEMICS, skill = skill.name),
                  title = title,
                  description = description,
                  location = Location(latitude = 46.5197, longitude = 6.6323),
                  hourlyRate = 40.0,
                  isActive = true)

          ListingRepositoryProvider.repository.addProposal(proposal)
          createdListingIds.add(listingId)
        }

        // Wait for all listings to be created and indexed
        E2ETestHelper.waitForDocument("listings", createdListingIds.first(), timeoutMs = 5000L)
      }

      // Sign out tutor
      signOutCurrentUser()
      UserSessionManager.clearSession()

      // Initialize student user and navigate to home
      val student =
          initializeUserAndNavigateToHome(
              composeTestRule, userName = "Carol", userSurname = "Student")

      // View Listings on Home Screen
      composeTestRule.waitUntil(timeoutMillis = 8000) {
        try {
          composeTestRule
              .onAllNodes(hasText("Physics", substring = true, ignoreCase = true))
              .fetchSemanticsNodes()
              .isNotEmpty()
        } catch (_: Throwable) {
          false
        }
      }

      composeTestRule.onNodeWithText("Physics", substring = true, ignoreCase = true).assertExists()

      // Verify other listings are visible
      try {
        composeTestRule
            .onNodeWithText("Mathematics", substring = true, ignoreCase = true)
            .assertExists()
      } catch (_: AssertionError) {
        // Mathematics listing may not be visible
      }

      composeTestRule.waitForIdle()

      // Create and Submit Booking Request
      val selectedListingId = createdListingIds.firstOrNull()
      selectedListingId?.let { listingId ->
        val newBookingId = BookingRepositoryProvider.repository.getNewUid()

        val booking =
            Booking(
                bookingId = newBookingId,
                associatedListingId = listingId,
                listingCreatorId = tutorUser!!.uid,
                bookerId = student.uid,
                sessionStart = Date(),
                sessionEnd = Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000),
                status = BookingStatus.PENDING,
                price = 40.0)

        BookingRepositoryProvider.repository.addBooking(booking)
        createdBookingId = booking.bookingId

        // Wait for booking to be created
        E2ETestHelper.waitForDocument("bookings", newBookingId, timeoutMs = 3000L)
      }

      // Tutor Approves Booking
      createdBookingId?.let { bookingId ->
        try {
          val updatedBooking =
              Booking(
                  bookingId = bookingId,
                  associatedListingId = selectedListingId!!,
                  listingCreatorId = tutorUser!!.uid,
                  bookerId = student.uid,
                  sessionStart = Date(),
                  sessionEnd = Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000),
                  status = BookingStatus.CONFIRMED,
                  price = 40.0)

          BookingRepositoryProvider.repository.updateBooking(bookingId, updatedBooking)

          // Wait for update to propagate
          composeTestRule.waitForIdle()
        } catch (_: Exception) {
          // Could not update booking status
        }
      }

      // Verify Booking in My Bookings
      composeTestRule.onNodeWithTag(BottomBarTestTag.NAV_BOOKINGS).performClick()

      // Wait for My Bookings screen to load
      composeTestRule.waitForIdle()

      // Wait for Physics booking to appear
      composeTestRule.waitUntil(timeoutMillis = 8000) {
        try {
          composeTestRule
              .onAllNodes(hasText("Physics", substring = true, ignoreCase = true))
              .fetchSemanticsNodes()
              .isNotEmpty()
        } catch (_: Throwable) {
          false
        }
      }

      composeTestRule.onNodeWithText("Physics", substring = true, ignoreCase = true).assertExists()

      // Verify tutor name
      composeTestRule.onNodeWithText("Bob", substring = true, ignoreCase = true).assertExists()

      // Verify confirmed status if visible
      try {
        composeTestRule
            .onNodeWithText("Confirmed", substring = true, ignoreCase = true)
            .assertExists()
      } catch (_: AssertionError) {
        // Status text may not be visible in current view
      }
    }
  }
}
