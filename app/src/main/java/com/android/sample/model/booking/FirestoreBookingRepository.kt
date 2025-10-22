package com.android.sample.model.booking

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID
import kotlinx.coroutines.tasks.await

const val BOOKINGS_COLLECTION_PATH = "bookings"

class FirestoreBookingRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : BookingRepository {

  // Helper property to get current user ID
  private val currentUserId: String
    get() = auth.currentUser?.uid ?: throw Exception("User not authenticated")

  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  override suspend fun getAllBookings(): List<Booking> {
    try {
      val snapshot =
          db.collection(BOOKINGS_COLLECTION_PATH)
              .whereEqualTo("bookerId", currentUserId)
              .orderBy("sessionStart", Query.Direction.ASCENDING)
              .get()
              .await()
      return snapshot.toObjects(Booking::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to fetch bookings: ${e.message}")
    }
  }

  override suspend fun getBooking(bookingId: String): Booking? {
    return try {
      val document = db.collection(BOOKINGS_COLLECTION_PATH).document(bookingId).get().await()

      if (document.exists()) {
        val booking =
            document.toObject(Booking::class.java)
                ?: throw Exception("Failed to parse Booking with ID $bookingId")

        // Verify user has access (either booker or listing creator)
        if (booking.bookerId != currentUserId && booking.listingCreatorId != currentUserId) {
          throw Exception("Access denied: This booking doesn't belong to current user")
        }
        booking
      } else {
        return null
      }
    } catch (e: Exception) {
      throw Exception("Failed to get booking: ${e.message}")
    }
  }

  override suspend fun getBookingsByTutor(tutorId: String): List<Booking> {
    try {
      val snapshot =
          db.collection(BOOKINGS_COLLECTION_PATH)
              .whereEqualTo("listingCreatorId", tutorId)
              .orderBy("sessionStart", Query.Direction.ASCENDING)
              .get()
              .await()
      return snapshot.toObjects(Booking::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to fetch bookings by tutor: ${e.message}")
    }
  }

  override suspend fun getBookingsByUserId(userId: String): List<Booking> {
    try {
      val snapshot =
          db.collection(BOOKINGS_COLLECTION_PATH)
              .whereEqualTo("bookerId", userId)
              .orderBy("sessionStart", Query.Direction.ASCENDING)
              .get()
              .await()
      return snapshot.toObjects(Booking::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to fetch bookings by user: ${e.message}")
    }
  }

  override suspend fun getBookingsByStudent(studentId: String): List<Booking> {
    return getBookingsByUserId(studentId)
  }

  override suspend fun getBookingsByListing(listingId: String): List<Booking> {
    try {
      val snapshot =
          db.collection(BOOKINGS_COLLECTION_PATH)
              .whereEqualTo("associatedListingId", listingId)
              .orderBy("sessionStart", Query.Direction.ASCENDING)
              .get()
              .await()
      return snapshot.toObjects(Booking::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to fetch bookings by listing: ${e.message}")
    }
  }

  override suspend fun addBooking(booking: Booking) {
    try {
      // Verify current user is the booker
      if (booking.bookerId != currentUserId) {
        throw Exception("Access denied: Can only create bookings for yourself")
      }

      db.collection(BOOKINGS_COLLECTION_PATH).document(booking.bookingId).set(booking).await()
    } catch (e: Exception) {
      throw Exception("Failed to add booking: ${e.message}")
    }
  }

  override suspend fun updateBooking(bookingId: String, booking: Booking) {
    try {
      val documentRef = db.collection(BOOKINGS_COLLECTION_PATH).document(bookingId)
      val documentSnapshot = documentRef.get().await()

      if (documentSnapshot.exists()) {
        val existingBooking = documentSnapshot.toObject(Booking::class.java)

        // Verify user has access
        if (existingBooking?.bookerId != currentUserId &&
            existingBooking?.listingCreatorId != currentUserId) {
          throw Exception(
              "Access denied: Cannot update booking that doesn't belong to current user")
        }

        documentRef.set(booking).await()
      } else {
        throw Exception("Booking with ID $bookingId not found")
      }
    } catch (e: Exception) {
      throw Exception("Failed to update booking: ${e.message}")
    }
  }

  override suspend fun deleteBooking(bookingId: String) {
    try {
      // val documentRef = db.collection(BOOKINGS_COLLECTION_PATH).document(bookingId)
      // val documentSnapshot = documentRef.get().await()

    } catch (e: Exception) {
      throw Exception("Failed to delete booking: ${e.message}")
    }
  }

  override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
    try {
      val documentRef = db.collection(BOOKINGS_COLLECTION_PATH).document(bookingId)
      val documentSnapshot = documentRef.get().await()

      if (documentSnapshot.exists()) {
        val booking = documentSnapshot.toObject(Booking::class.java)

        // Verify user has access
        if (booking?.bookerId != currentUserId && booking?.listingCreatorId != currentUserId) {
          throw Exception("Access denied: Cannot update booking status")
        }

        documentRef.update("status", status).await()
      } else {
        throw Exception("Booking with ID $bookingId not found")
      }
    } catch (e: Exception) {
      throw Exception("Failed to update booking status: ${e.message}")
    }
  }

  override suspend fun confirmBooking(bookingId: String) {
    updateBookingStatus(bookingId, BookingStatus.CONFIRMED)
  }

  override suspend fun completeBooking(bookingId: String) {
    updateBookingStatus(bookingId, BookingStatus.COMPLETED)
  }

  override suspend fun cancelBooking(bookingId: String) {
    updateBookingStatus(bookingId, BookingStatus.CANCELLED)
  }
}
