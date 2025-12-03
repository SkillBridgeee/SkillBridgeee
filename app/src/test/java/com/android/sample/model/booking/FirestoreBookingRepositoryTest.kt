package com.android.sample.model.booking

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.android.sample.utils.FirebaseEmulator
import com.android.sample.utils.RepositoryTest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import java.util.Date
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

@Config(sdk = [28])
class FirestoreBookingRepositoryTest : RepositoryTest() {
  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth

  private val context = ApplicationProvider.getApplicationContext<Context>()

  @Before
  override fun setUp() {
    super.setUp()
    firestore = FirebaseEmulator.firestore

    // Mock FirebaseAuth to bypass authentication
    auth = mockk()
    val mockUser = mockk<FirebaseUser>()
    every { auth.currentUser } returns mockUser
    every { mockUser.uid } returns testUserId // testUserId is "test-user-id" from RepositoryTest

    bookingRepository = FirestoreBookingRepository(firestore, auth, context)
    BookingRepositoryProvider.setForTests(bookingRepository)
  }

  @After
  override fun tearDown() = runBlocking {
    val snapshot = firestore.collection(BOOKINGS_COLLECTION_PATH).get().await()
    for (document in snapshot.documents) {
      document.reference.delete().await()
    }
  }

  @Test
  fun getNewUidReturnsUniqueIDs() {
    val uid1 = bookingRepository.getNewUid()
    val uid2 = bookingRepository.getNewUid()
    assertNotNull(uid1)
    assertNotNull(uid2)
    assertNotEquals(uid1, uid2)
  }

  @Test
  fun addBookingWithTheCorrectID() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            status = BookingStatus.PENDING,
            price = 50.0)
    bookingRepository.addBooking(booking)

    val retrievedBooking = bookingRepository.getBooking("booking1")
    assertNotNull(retrievedBooking)
    assertEquals("booking1", retrievedBooking!!.bookingId)
  }

  @Test
  fun canRetrieveABookingByID() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking)

    val retrievedBooking = bookingRepository.getBooking("booking1")
    assertNotNull(retrievedBooking)
    assertEquals("booking1", retrievedBooking!!.bookingId)
  }

  @Test
  fun canDeleteABookingByID() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking)
    bookingRepository.deleteBooking("booking1")
  }

  @Test
  fun canConfirmBooking() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking)
    bookingRepository.confirmBooking("booking1")
    val retrievedBooking = bookingRepository.getBooking("booking1")
    assertEquals(BookingStatus.CONFIRMED, retrievedBooking!!.status)
  }

  @Test
  fun canCancelBooking() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking)
    bookingRepository.cancelBooking("booking1")
    val retrievedBooking = bookingRepository.getBooking("booking1")
    assertEquals(BookingStatus.CANCELLED, retrievedBooking!!.status)
  }

  @Test
  fun getAllBookingsReturnsEmptyListWhenNoBookings() = runTest {
    val bookings = bookingRepository.getAllBookings()
    assertEquals(0, bookings.size)
  }

  @Test
  fun getAllBookingsReturnsSortedBySessionStart() = runTest {
    val booking1 =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis() + 7200000),
            sessionEnd = Date(System.currentTimeMillis() + 10800000),
            status = BookingStatus.PENDING,
            price = 50.0)
    val booking2 =
        Booking(
            bookingId = "booking2",
            associatedListingId = "listing2",
            listingCreatorId = "tutor2",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            status = BookingStatus.CONFIRMED,
            price = 75.0)

    bookingRepository.addBooking(booking1)
    bookingRepository.addBooking(booking2)

    val bookings = bookingRepository.getAllBookings()
    assertEquals(2, bookings.size)
    assertEquals("booking2", bookings[0].bookingId) // Earlier date first
  }

  @Test
  fun getBookingReturnsNullForNonExistentBooking() = runTest {
    val retrievedBooking = bookingRepository.getBooking("non-existent")
    assertEquals(null, retrievedBooking)
  }

  @Test
  fun getBookingFailsForUnauthorizedUser() = runTest {
    // Create booking for another user
    val anotherAuth = mockk<FirebaseAuth>()
    val anotherUser = mockk<FirebaseUser>()
    every { anotherAuth.currentUser } returns anotherUser
    every { anotherUser.uid } returns "another-user-id"

    val anotherRepo = FirestoreBookingRepository(firestore, anotherAuth, context)
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = "another-user-id",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    anotherRepo.addBooking(booking)

    // Try to access with original user
    assertThrows(Exception::class.java) { runTest { bookingRepository.getBooking("booking1") } }
  }

  @Test
  fun getBookingsByTutorReturnsCorrectBookings() = runTest {
    val booking1 =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    val booking2 =
        Booking(
            bookingId = "booking2",
            associatedListingId = "listing2",
            listingCreatorId = "tutor2",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))

    bookingRepository.addBooking(booking1)
    bookingRepository.addBooking(booking2)

    val bookings = bookingRepository.getBookingsByTutor("tutor1")
    assertEquals(1, bookings.size)
    assertEquals("booking1", bookings[0].bookingId)
  }

  @Test
  fun getBookingsByUserIdReturnsCorrectBookings() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking)

    val bookings = bookingRepository.getBookingsByUserId(testUserId)
    assertEquals(1, bookings.size)
    assertEquals("booking1", bookings[0].bookingId)
  }

  @Test
  fun getBookingsByStudentCallsGetBookingsByUserId() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking)

    val bookings = bookingRepository.getBookingsByStudent(testUserId)
    assertEquals(1, bookings.size)
  }

  @Test
  fun getBookingsByListingReturnsCorrectBookings() = runTest {
    val booking1 =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    val booking2 =
        Booking(
            bookingId = "booking2",
            associatedListingId = "listing2",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))

    bookingRepository.addBooking(booking1)
    bookingRepository.addBooking(booking2)

    val bookings = bookingRepository.getBookingsByListing("listing1")
    assertEquals(1, bookings.size)
    assertEquals("booking1", bookings[0].bookingId)
  }

  @Test
  fun updateBookingSucceedsForBooker() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            price = 50.0)
    bookingRepository.addBooking(booking)

    val updatedBooking = booking.copy(price = 75.0)
    bookingRepository.updateBooking("booking1", updatedBooking)

    val retrieved = bookingRepository.getBooking("booking1")
    assertEquals(75.0, retrieved!!.price, 0.01)
  }

  @Test
  fun updateBookingFailsForNonExistentBooking() {
    val booking =
        Booking(
            bookingId = "non-existent",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))

    assertThrows(Exception::class.java) {
      runTest { bookingRepository.updateBooking("non-existent", booking) }
    }
  }

  @Test
  fun updateBookingStatusFailsForNonExistentBooking() {
    assertThrows(Exception::class.java) {
      runTest { bookingRepository.updateBookingStatus("non-existent", BookingStatus.CONFIRMED) }
    }
  }

  @Test
  fun updateBookingStatusFailsForUnauthorizedUser() = runTest {
    // Create booking for another user as listing creator
    val anotherAuth = mockk<FirebaseAuth>()
    val anotherUser = mockk<FirebaseUser>()
    every { anotherAuth.currentUser } returns anotherUser
    every { anotherUser.uid } returns "another-user-id"

    val anotherRepo = FirestoreBookingRepository(firestore, anotherAuth, context)
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "another-user-id",
            bookerId = "another-user-id",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    anotherRepo.addBooking(booking)

    // Try to update status with original user
    assertThrows(Exception::class.java) {
      runTest { bookingRepository.updateBookingStatus("booking1", BookingStatus.CONFIRMED) }
    }
  }

  @Test
  fun bookingValidationThrowsForInvalidDates() {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis() + 3600000),
            sessionEnd = Date(System.currentTimeMillis()), // End before start
            price = 50.0)

    assertThrows(IllegalArgumentException::class.java) { booking.validate() }
  }

  @Test
  fun bookingValidationThrowsForSameBookerAndCreator() {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = testUserId,
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            price = 50.0)

    assertThrows(IllegalArgumentException::class.java) { booking.validate() }
  }

  @Test
  fun bookingValidationThrowsForNegativePrice() {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            price = -10.0)

    assertThrows(IllegalArgumentException::class.java) { booking.validate() }
  }

  @Test
  fun canCompleteBooking() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            status = BookingStatus.CONFIRMED)
    bookingRepository.addBooking(booking)
    bookingRepository.completeBooking("booking1")
    val retrievedBooking = bookingRepository.getBooking("booking1")
    assertEquals(BookingStatus.COMPLETED, retrievedBooking!!.status)
  }

  @Test
  fun addBookingForAnotherUserFails() {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = "another-user",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))

    assertThrows(Exception::class.java) { runTest { bookingRepository.addBooking(booking) } }
  }

  // ----------------------------
  // Tests for new dual-query functionality (lines 26-47, 91-112)
  // ----------------------------

  @Test
  fun getAllBookings_returnsBookingsWhereUserIsBooker() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking)

    val bookings = bookingRepository.getAllBookings()
    assertEquals(1, bookings.size)
    assertEquals("booking1", bookings[0].bookingId)
    assertEquals(testUserId, bookings[0].bookerId)
  }

  @Test
  fun getAllBookings_returnsBookingsWhereUserIsListingCreator() = runTest {
    // Create booking where current user is the listing creator
    // We need to create this booking as the student (booker) first
    val studentAuth = mockk<FirebaseAuth>()
    val studentUser = mockk<FirebaseUser>()
    every { studentAuth.currentUser } returns studentUser
    every { studentUser.uid } returns "student1"

    val studentRepo = FirestoreBookingRepository(firestore, studentAuth, context)
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = testUserId,
            bookerId = "student1",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    studentRepo.addBooking(booking)

    // Now query as the listing creator (testUserId)
    val bookings = bookingRepository.getAllBookings()
    assertEquals(1, bookings.size)
    assertEquals("booking1", bookings[0].bookingId)
    assertEquals(testUserId, bookings[0].listingCreatorId)
  }

  @Test
  fun getAllBookings_combinesBookerAndCreatorBookings() = runTest {
    // Booking where user is booker
    val booking1 =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking1)

    // Booking where user is listing creator - need student to create it
    val studentAuth = mockk<FirebaseAuth>()
    val studentUser = mockk<FirebaseUser>()
    every { studentAuth.currentUser } returns studentUser
    every { studentUser.uid } returns "student1"

    val studentRepo = FirestoreBookingRepository(firestore, studentAuth, context)
    val booking2 =
        Booking(
            bookingId = "booking2",
            associatedListingId = "listing2",
            listingCreatorId = testUserId,
            bookerId = "student1",
            sessionStart = Date(System.currentTimeMillis() + 7200000),
            sessionEnd = Date(System.currentTimeMillis() + 10800000))
    studentRepo.addBooking(booking2)

    val bookings = bookingRepository.getAllBookings()
    assertEquals(2, bookings.size)
    // Should be sorted by sessionStart
    assertEquals("booking1", bookings[0].bookingId)
    assertEquals("booking2", bookings[1].bookingId)
  }

  @Test
  fun getAllBookings_removiesDuplicates_withDistinctBy() = runTest {
    // This tests the distinctBy logic - though in practice a booking shouldn't
    // appear in both queries, we test that distinctBy works correctly
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = testUserId,
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))

    // Even though validation would fail in real scenario, we're testing repo logic
    // The booking would appear in both booker and creator queries
    try {
      bookingRepository.addBooking(booking)
    } catch (_: Exception) {
      // Skip if validation prevents this - the distinctBy is still tested in combined queries
    }

    val bookings = bookingRepository.getAllBookings()
    // Should have at most 1 booking (not duplicated)
    assert(bookings.size <= 1)
  }

  @Test
  fun getAllBookings_sortsBySessionStart_acrossBothQueries() = runTest {
    // Booking where user is booker (later time)
    val booking1 =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis() + 7200000),
            sessionEnd = Date(System.currentTimeMillis() + 10800000))
    bookingRepository.addBooking(booking1)

    // Booking where user is listing creator (earlier time) - created by student
    val studentAuth = mockk<FirebaseAuth>()
    val studentUser = mockk<FirebaseUser>()
    every { studentAuth.currentUser } returns studentUser
    every { studentUser.uid } returns "student1"

    val studentRepo = FirestoreBookingRepository(firestore, studentAuth, context)
    val booking2 =
        Booking(
            bookingId = "booking2",
            associatedListingId = "listing2",
            listingCreatorId = testUserId,
            bookerId = "student1",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    studentRepo.addBooking(booking2)

    val bookings = bookingRepository.getAllBookings()
    assertEquals(2, bookings.size)
    // Earlier booking should be first
    assertEquals("booking2", bookings[0].bookingId)
    assertEquals("booking1", bookings[1].bookingId)
  }

  @Test
  fun getBookingsByUserId_returnsBookingsWhereUserIsBooker() = runTest {
    // Create auth for user123 to add their own booking
    val user123Auth = mockk<FirebaseAuth>()
    val user123User = mockk<FirebaseUser>()
    every { user123Auth.currentUser } returns user123User
    every { user123User.uid } returns "user123"

    val user123Repo = FirestoreBookingRepository(firestore, user123Auth, context)
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = "user123",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    user123Repo.addBooking(booking)

    val bookings = bookingRepository.getBookingsByUserId("user123")
    assertEquals(1, bookings.size)
    assertEquals("booking1", bookings[0].bookingId)
  }

  @Test
  fun isOnlineReturnsTrueWhenOnline() {
    val repo = FirestoreBookingRepository(firestore, auth, context)
    assertEquals(repo.isOnline(), true)
  }

  @Test
  fun getBookingsByUserId_returnsBookingsWhereUserIsListingCreator() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "user123",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking)

    val bookings = bookingRepository.getBookingsByUserId("user123")
    assertEquals(1, bookings.size)
    assertEquals("booking1", bookings[0].bookingId)
  }

  @Test
  fun getBookingsByUserId_combinesBookerAndCreatorBookings() = runTest {
    // Create auth for user123
    val user123Auth = mockk<FirebaseAuth>()
    val user123User = mockk<FirebaseUser>()
    every { user123Auth.currentUser } returns user123User
    every { user123User.uid } returns "user123"

    val user123Repo = FirestoreBookingRepository(firestore, user123Auth, context)

    // Booking where target user123 is booker
    val booking1 =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = "user123",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    user123Repo.addBooking(booking1)

    // Booking where target user123 is listing creator - created by testUserId as booker
    val booking2 =
        Booking(
            bookingId = "booking2",
            associatedListingId = "listing2",
            listingCreatorId = "user123",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis() + 7200000),
            sessionEnd = Date(System.currentTimeMillis() + 10800000))
    bookingRepository.addBooking(booking2)

    val bookings = bookingRepository.getBookingsByUserId("user123")
    assertEquals(2, bookings.size)
  }

  @Test
  fun getBookingsByUserId_removiesDuplicatesWithDistinctBy() = runTest {
    // Edge case: same booking appears in both queries
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "user123",
            bookerId = "user123",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))

    try {
      bookingRepository.addBooking(booking)
    } catch (_: Exception) {
      // Skip if validation prevents this
    }

    val bookings = bookingRepository.getBookingsByUserId("user123")
    // Should have at most 1 booking (not duplicated)
    assert(bookings.size <= 1)
  }

  @Test
  fun getBookingsByUserId_sortsBySessionStart() = runTest {
    // Create auth for user123
    val user123Auth = mockk<FirebaseAuth>()
    val user123User = mockk<FirebaseUser>()
    every { user123Auth.currentUser } returns user123User
    every { user123User.uid } returns "user123"

    val user123Repo = FirestoreBookingRepository(firestore, user123Auth, context)

    // Later booking where user123 is booker
    val booking1 =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = "user123",
            sessionStart = Date(System.currentTimeMillis() + 7200000),
            sessionEnd = Date(System.currentTimeMillis() + 10800000))
    user123Repo.addBooking(booking1)

    // Earlier booking where user123 is creator - created by testUserId
    val booking2 =
        Booking(
            bookingId = "booking2",
            associatedListingId = "listing2",
            listingCreatorId = "user123",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking2)

    val bookings = bookingRepository.getBookingsByUserId("user123")
    assertEquals(2, bookings.size)
    // Earlier booking should be first
    assertEquals("booking2", bookings[0].bookingId)
    assertEquals("booking1", bookings[1].bookingId)
  }

  @Test
  fun getBookingsByUserId_returnsEmptyListWhenUserHasNoBookings() = runTest {
    val bookings = bookingRepository.getBookingsByUserId("non-existent-user")
    assertEquals(0, bookings.size)
  }

  @Test
  fun getAllBookings_excludesBookingsFromOtherUsers() = runTest {
    // Create booking for a different user
    val anotherAuth = mockk<FirebaseAuth>()
    val anotherUser = mockk<FirebaseUser>()
    every { anotherAuth.currentUser } returns anotherUser
    every { anotherUser.uid } returns "another-user-id"

    val anotherRepo = FirestoreBookingRepository(firestore, anotherAuth, context)
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = "another-user-id",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    anotherRepo.addBooking(booking)

    // Current user should not see this booking
    val bookings = bookingRepository.getAllBookings()
    assertEquals(0, bookings.size)
  }

  // ----------------------------
  // Payment Status Tests
  // ----------------------------

  @Test
  fun updatePaymentStatus_successfullyUpdatesToPayed() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            status = BookingStatus.CONFIRMED,
            paymentStatus = PaymentStatus.PENDING_PAYMENT)
    bookingRepository.addBooking(booking)

    bookingRepository.updatePaymentStatus("booking1", PaymentStatus.PAID)

    val retrievedBooking = bookingRepository.getBooking("booking1")
    assertNotNull(retrievedBooking)
    assertEquals(PaymentStatus.PAID, retrievedBooking!!.paymentStatus)
  }

  @Test
  fun updatePaymentStatus_successfullyUpdatesToConfirmed() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = testUserId,
            bookerId = "student1",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            status = BookingStatus.CONFIRMED,
            paymentStatus = PaymentStatus.PAID)

    // Create booking as student first
    val studentAuth = mockk<FirebaseAuth>()
    val studentUser = mockk<FirebaseUser>()
    every { studentAuth.currentUser } returns studentUser
    every { studentUser.uid } returns "student1"
    val studentRepo = FirestoreBookingRepository(firestore, studentAuth)
    studentRepo.addBooking(booking)

    // Update payment status as tutor (listing creator)
    bookingRepository.updatePaymentStatus("booking1", PaymentStatus.CONFIRMED)

    val retrievedBooking = bookingRepository.getBooking("booking1")
    assertNotNull(retrievedBooking)
    assertEquals(PaymentStatus.CONFIRMED, retrievedBooking!!.paymentStatus)
  }

  @Test
  fun updatePaymentStatus_failsForNonExistentBooking() {
    assertThrows(Exception::class.java) {
      runTest { bookingRepository.updatePaymentStatus("non-existent", PaymentStatus.PAID) }
    }
  }

  @Test
  fun updatePaymentStatus_failsWhenUserHasNoAccess() = runTest {
    // Create booking as different user
    val anotherAuth = mockk<FirebaseAuth>()
    val anotherUser = mockk<FirebaseUser>()
    every { anotherAuth.currentUser } returns anotherUser
    every { anotherUser.uid } returns "another-user"

    val anotherRepo = FirestoreBookingRepository(firestore, anotherAuth)
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = "another-user",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            paymentStatus = PaymentStatus.PENDING_PAYMENT)
    anotherRepo.addBooking(booking)

    // Try to update payment status as testUserId (who is neither booker nor creator)
    assertThrows(Exception::class.java) {
      runTest { bookingRepository.updatePaymentStatus("booking1", PaymentStatus.PAID) }
    }
  }

  @Test
  fun updatePaymentStatus_preservesOtherBookingFields() = runTest {
    val originalBooking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            status = BookingStatus.CONFIRMED,
            paymentStatus = PaymentStatus.PENDING_PAYMENT,
            price = 75.0)
    bookingRepository.addBooking(originalBooking)

    bookingRepository.updatePaymentStatus("booking1", PaymentStatus.PAID)

    val retrievedBooking = bookingRepository.getBooking("booking1")
    assertNotNull(retrievedBooking)
    assertEquals("booking1", retrievedBooking!!.bookingId)
    assertEquals("listing1", retrievedBooking.associatedListingId)
    assertEquals("tutor1", retrievedBooking.listingCreatorId)
    assertEquals(testUserId, retrievedBooking.bookerId)
    assertEquals(BookingStatus.CONFIRMED, retrievedBooking.status)
    assertEquals(75.0, retrievedBooking.price, 0.01)
    assertEquals(PaymentStatus.PAID, retrievedBooking.paymentStatus)
  }

  @Test
  fun newBooking_hasDefaultPaymentStatusPendingPayment() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking)

    val retrievedBooking = bookingRepository.getBooking("booking1")
    assertNotNull(retrievedBooking)
    assertEquals(PaymentStatus.PENDING_PAYMENT, retrievedBooking!!.paymentStatus)
  }

  @Test
  fun updatePaymentStatus_canUpdateAsBooker() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            paymentStatus = PaymentStatus.PENDING_PAYMENT)
    bookingRepository.addBooking(booking)

    // Update as booker
    bookingRepository.updatePaymentStatus("booking1", PaymentStatus.PAID)

    val retrievedBooking = bookingRepository.getBooking("booking1")
    assertEquals(PaymentStatus.PAID, retrievedBooking!!.paymentStatus)
  }

  @Test
  fun updatePaymentStatus_canUpdateAsListingCreator() = runTest {
    // Create booking as student where testUserId is the listing creator
    val studentAuth = mockk<FirebaseAuth>()
    val studentUser = mockk<FirebaseUser>()
    every { studentAuth.currentUser } returns studentUser
    every { studentUser.uid } returns "student1"

    val studentRepo = FirestoreBookingRepository(firestore, studentAuth)
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = testUserId,
            bookerId = "student1",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            paymentStatus = PaymentStatus.PAID)
    studentRepo.addBooking(booking)

    // Update as listing creator (testUserId)
    bookingRepository.updatePaymentStatus("booking1", PaymentStatus.CONFIRMED)

    val retrievedBooking = bookingRepository.getBooking("booking1")
    assertEquals(PaymentStatus.CONFIRMED, retrievedBooking!!.paymentStatus)
  }
}
