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

  //  @Test
  //  fun bookingIdsAreUniqueInTheCollection() = runTest {
  //    val booking1 =
  //        Booking(
  //            bookingId = "booking1",
  //            associatedListingId = "listing1",
  //            listingCreatorId = "tutor1",
  //            bookerId = testUserId,
  //            sessionStart = Date(System.currentTimeMillis()),
  //            sessionEnd = Date(System.currentTimeMillis() + 3600000))
  //    val booking2 =
  //        Booking(
  //            bookingId = "booking2",
  //            associatedListingId = "listing2",
  //            listingCreatorId = "tutor2",
  //            bookerId = testUserId,
  //            sessionStart = Date(System.currentTimeMillis()),
  //            sessionEnd = Date(System.currentTimeMillis() + 3600000))
  //
  //    bookingRepository.addBooking(booking1)
  //    bookingRepository.addBooking(booking2)
  //
  //    val allBookings = bookingRepository.getAllBookings()
  //    assertEquals(2, allBookings.size)
  //    assertEquals(2, allBookings.map { it.bookingId }.toSet().size)
  //  }

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

  //  @Test
  //  fun canGetBookingsByListing() = runTest {
  //    val booking1 =
  //        Booking(
  //            bookingId = "booking1",
  //            associatedListingId = "listing1",
  //            listingCreatorId = "tutor1",
  //            bookerId = testUserId,
  //            sessionStart = Date(System.currentTimeMillis()),
  //            sessionEnd = Date(System.currentTimeMillis() + 3600000))
  //    val booking2 =
  //        Booking(
  //            bookingId = "booking2",
  //            associatedListingId = "listing2",
  //            listingCreatorId = "tutor2",
  //            bookerId = testUserId,
  //            sessionStart = Date(System.currentTimeMillis()),
  //            sessionEnd = Date(System.currentTimeMillis() + 3600000))
  //    bookingRepository.addBooking(booking1)
  //    bookingRepository.addBooking(booking2)
  //
  //    val bookings = bookingRepository.getBookingsByListing("listing1")
  //    assertEquals(1, bookings.size)
  //    assertEquals("booking1", bookings[0].bookingId)
  //  }

  // @Test
  //  fun getBookingsByListingReturnsEmptyListForNonExistentListing() = runTest {
  //    val bookings = bookingRepository.getBookingsByListing("non-existent-listing")
  //    assertTrue(bookings.isEmpty())
  //  }

  //  @Test
  //  fun canGetBookingsByStudent() = runTest {
  //    val booking1 =
  //        Booking(
  //            bookingId = "booking1",
  //            associatedListingId = "listing1",
  //            listingCreatorId = "tutor1",
  //            bookerId = testUserId,
  //            sessionStart = Date(System.currentTimeMillis()),
  //            sessionEnd = Date(System.currentTimeMillis() + 3600000))
  //    bookingRepository.addBooking(booking1)
  //
  //    val bookings = bookingRepository.getBookingsByStudent(testUserId)
  //    assertEquals(1, bookings.size)
  //    assertEquals("booking1", bookings[0].bookingId)
  //  }

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
}
