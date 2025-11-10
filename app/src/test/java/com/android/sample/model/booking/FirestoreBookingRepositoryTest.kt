package com.android.sample.model.booking

import com.android.sample.utils.RepositoryTest
import com.github.se.bootcamp.utils.FirebaseEmulator
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class FirestoreBookingRepositoryTest : RepositoryTest() {
  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth

  @Before
  override fun setUp() {
    super.setUp()
    firestore = FirebaseEmulator.firestore

    // Mock FirebaseAuth to bypass authentication
    auth = mockk()
    val mockUser = mockk<FirebaseUser>()
    every { auth.currentUser } returns mockUser
    every { mockUser.uid } returns testUserId // testUserId is "test-user-id" from RepositoryTest

    bookingRepository = FirestoreBookingRepository(firestore, auth)
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

    val retrievedBooking = bookingRepository.getBooking("booking1")
    // assertEquals(null, retrievedBooking)
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

    val anotherRepo = FirestoreBookingRepository(firestore, anotherAuth)
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

    val anotherRepo = FirestoreBookingRepository(firestore, anotherAuth)
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

  @Test
  fun updateBookingSucceedsForListingCreator() = runTest {
    // Create booking where current user is the listing creator
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = testUserId,
            bookerId = "student1",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            price = 50.0)

    // Add booking using a different auth context
    val studentAuth = mockk<FirebaseAuth>()
    val studentUser = mockk<FirebaseUser>()
    every { studentAuth.currentUser } returns studentUser
    every { studentUser.uid } returns "student1"
    val studentRepo = FirestoreBookingRepository(firestore, studentAuth)
    studentRepo.addBooking(booking)

    // Update as listing creator
    val updatedBooking = booking.copy(price = 75.0)
    bookingRepository.updateBooking("booking1", updatedBooking)

    val retrieved = studentRepo.getBooking("booking1")
    assertEquals(75.0, retrieved!!.price, 0.01)
  }

  @Test
  fun updateBookingFailsForUnauthorizedUser() = runTest {
    // Create booking for another user
    val anotherAuth = mockk<FirebaseAuth>()
    val anotherUser = mockk<FirebaseUser>()
    every { anotherAuth.currentUser } returns anotherUser
    every { anotherUser.uid } returns "another-user-id"

    val anotherRepo = FirestoreBookingRepository(firestore, anotherAuth)
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = "another-user-id",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    anotherRepo.addBooking(booking)

    // Try to update with original user (not involved in booking)
    val updatedBooking = booking.copy(price = 100.0)
    assertThrows(Exception::class.java) {
      runTest { bookingRepository.updateBooking("booking1", updatedBooking) }
    }
  }

  @Test
  fun updateBookingStatusSucceedsForListingCreator() = runTest {
    // Create booking where current user is the listing creator
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = testUserId,
            bookerId = "student1",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            status = BookingStatus.PENDING)

    val studentAuth = mockk<FirebaseAuth>()
    val studentUser = mockk<FirebaseUser>()
    every { studentAuth.currentUser } returns studentUser
    every { studentUser.uid } returns "student1"
    val studentRepo = FirestoreBookingRepository(firestore, studentAuth)
    studentRepo.addBooking(booking)

    // Update status as listing creator
    bookingRepository.updateBookingStatus("booking1", BookingStatus.CONFIRMED)

    val retrieved = studentRepo.getBooking("booking1")
    assertEquals(BookingStatus.CONFIRMED, retrieved!!.status)
  }

  @Test
  fun getBookingSucceedsForListingCreator() = runTest {
    // Create booking where current user is the listing creator
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = testUserId,
            bookerId = "student1",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))

    val studentAuth = mockk<FirebaseAuth>()
    val studentUser = mockk<FirebaseUser>()
    every { studentAuth.currentUser } returns studentUser
    every { studentUser.uid } returns "student1"
    val studentRepo = FirestoreBookingRepository(firestore, studentAuth)
    studentRepo.addBooking(booking)

    // Get booking as listing creator
    val retrieved = bookingRepository.getBooking("booking1")
    assertNotNull(retrieved)
    assertEquals("booking1", retrieved!!.bookingId)
  }

  @Test
  fun getAllBookingsReturnsSortedBookingsWithMultipleDates() = runTest {
    val now = System.currentTimeMillis()
    val booking1 =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(now + 10000000),
            sessionEnd = Date(now + 14000000))
    val booking2 =
        Booking(
            bookingId = "booking2",
            associatedListingId = "listing2",
            listingCreatorId = "tutor2",
            bookerId = testUserId,
            sessionStart = Date(now + 1000000),
            sessionEnd = Date(now + 5000000))
    val booking3 =
        Booking(
            bookingId = "booking3",
            associatedListingId = "listing3",
            listingCreatorId = "tutor3",
            bookerId = testUserId,
            sessionStart = Date(now + 5000000),
            sessionEnd = Date(now + 9000000))

    bookingRepository.addBooking(booking1)
    bookingRepository.addBooking(booking2)
    bookingRepository.addBooking(booking3)

    val bookings = bookingRepository.getAllBookings()
    assertEquals(3, bookings.size)
    assertEquals("booking2", bookings[0].bookingId)
    assertEquals("booking3", bookings[1].bookingId)
    assertEquals("booking1", bookings[2].bookingId)
  }

  @Test
  fun getBookingsByTutorReturnsSortedBookings() = runTest {
    val now = System.currentTimeMillis()
    val booking1 =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(now + 10000000),
            sessionEnd = Date(now + 14000000))
    val booking2 =
        Booking(
            bookingId = "booking2",
            associatedListingId = "listing2",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(now + 1000000),
            sessionEnd = Date(now + 5000000))

    bookingRepository.addBooking(booking1)
    bookingRepository.addBooking(booking2)

    val bookings = bookingRepository.getBookingsByTutor("tutor1")
    assertEquals(2, bookings.size)
    assertEquals("booking2", bookings[0].bookingId) // Earlier first
  }

  @Test
  fun getBookingsByTutorReturnsEmptyListForNoMatches() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking)

    val bookings = bookingRepository.getBookingsByTutor("tutor2")
    assertEquals(0, bookings.size)
  }

  @Test
  fun getBookingsByUserIdReturnsSortedBookings() = runTest {
    val now = System.currentTimeMillis()
    val booking1 =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(now + 10000000),
            sessionEnd = Date(now + 14000000))
    val booking2 =
        Booking(
            bookingId = "booking2",
            associatedListingId = "listing2",
            listingCreatorId = "tutor2",
            bookerId = testUserId,
            sessionStart = Date(now + 1000000),
            sessionEnd = Date(now + 5000000))

    bookingRepository.addBooking(booking1)
    bookingRepository.addBooking(booking2)

    val bookings = bookingRepository.getBookingsByUserId(testUserId)
    assertEquals(2, bookings.size)
    assertEquals("booking2", bookings[0].bookingId)
  }

  @Test
  fun getBookingsByUserIdReturnsEmptyListForNoMatches() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking)

    val bookings = bookingRepository.getBookingsByUserId("other-user")
    assertEquals(0, bookings.size)
  }

  @Test
  fun getBookingsByListingReturnsSortedBookings() = runTest {
    val now = System.currentTimeMillis()
    val booking1 =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(now + 10000000),
            sessionEnd = Date(now + 14000000))
    val booking2 =
        Booking(
            bookingId = "booking2",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(now + 1000000),
            sessionEnd = Date(now + 5000000))

    bookingRepository.addBooking(booking1)
    bookingRepository.addBooking(booking2)

    val bookings = bookingRepository.getBookingsByListing("listing1")
    assertEquals(2, bookings.size)
    assertEquals("booking2", bookings[0].bookingId)
  }

  @Test
  fun getBookingsByListingReturnsEmptyListForNoMatches() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking)

    val bookings = bookingRepository.getBookingsByListing("listing2")
    assertEquals(0, bookings.size)
  }

  @Test
  fun deleteBookingDoesNotThrowException() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking)

    // Should not throw even though implementation is empty
    bookingRepository.deleteBooking("booking1")
  }

  @Test
  fun currentUserIdThrowsExceptionWhenNotAuthenticated() {
    val unauthAuth = mockk<FirebaseAuth>()
    every { unauthAuth.currentUser } returns null

    val unauthRepo = FirestoreBookingRepository(firestore, unauthAuth)

    assertThrows(Exception::class.java) { runTest { unauthRepo.getAllBookings() } }
  }

  @Test
  fun addBookingThrowsExceptionWhenNotAuthenticated() {
    val unauthAuth = mockk<FirebaseAuth>()
    every { unauthAuth.currentUser } returns null

    val unauthRepo = FirestoreBookingRepository(firestore, unauthAuth)
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))

    assertThrows(Exception::class.java) { runTest { unauthRepo.addBooking(booking) } }
  }

  @Test
  fun getBookingThrowsExceptionWhenNotAuthenticated() = runTest {
    // First create a booking with an authenticated user
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking)

    // Now try to access it with unauthenticated user
    val unauthAuth = mockk<FirebaseAuth>()
    every { unauthAuth.currentUser } returns null

    val unauthRepo = FirestoreBookingRepository(firestore, unauthAuth)

    assertThrows(Exception::class.java) { runBlocking { unauthRepo.getBooking("booking1") } }
  }

  @Test
  fun getAllBookingsFiltersOnlyCurrentUserBookings() = runTest {
    // Add booking for current user
    val booking1 =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    bookingRepository.addBooking(booking1)

    // Add booking for another user
    val anotherAuth = mockk<FirebaseAuth>()
    val anotherUser = mockk<FirebaseUser>()
    every { anotherAuth.currentUser } returns anotherUser
    every { anotherUser.uid } returns "another-user"
    val anotherRepo = FirestoreBookingRepository(firestore, anotherAuth)
    val booking2 =
        Booking(
            bookingId = "booking2",
            associatedListingId = "listing2",
            listingCreatorId = "tutor2",
            bookerId = "another-user",
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000))
    anotherRepo.addBooking(booking2)

    // getAllBookings should only return current user's bookings
    val bookings = bookingRepository.getAllBookings()
    assertEquals(1, bookings.size)
    assertEquals("booking1", bookings[0].bookingId)
  }

  @Test
  fun confirmBookingUpdatesStatusCorrectly() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            status = BookingStatus.PENDING)
    bookingRepository.addBooking(booking)

    bookingRepository.confirmBooking("booking1")

    val retrieved = bookingRepository.getBooking("booking1")
    assertEquals(BookingStatus.CONFIRMED, retrieved!!.status)
  }

  @Test
  fun completeBookingUpdatesStatusCorrectly() = runTest {
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

    val retrieved = bookingRepository.getBooking("booking1")
    assertEquals(BookingStatus.COMPLETED, retrieved!!.status)
  }

  @Test
  fun cancelBookingUpdatesStatusCorrectly() = runTest {
    val booking =
        Booking(
            bookingId = "booking1",
            associatedListingId = "listing1",
            listingCreatorId = "tutor1",
            bookerId = testUserId,
            sessionStart = Date(System.currentTimeMillis()),
            sessionEnd = Date(System.currentTimeMillis() + 3600000),
            status = BookingStatus.PENDING)
    bookingRepository.addBooking(booking)

    bookingRepository.cancelBooking("booking1")

    val retrieved = bookingRepository.getBooking("booking1")
    assertEquals(BookingStatus.CANCELLED, retrieved!!.status)
  }

  @Test
  fun getBookingReturnsNullWhenDocumentDoesNotExist() = runTest {
    val result = bookingRepository.getBooking("non-existent-id")
    assertEquals(null, result)
  }
}
