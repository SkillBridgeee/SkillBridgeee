// package com.android.sample.model.booking
//
// import android.util.Log
// import com.google.firebase.firestore.DocumentSnapshot
// import com.google.firebase.firestore.FirebaseFirestore
// import kotlinx.coroutines.tasks.await
//
// const val BOOKINGS_COLLECTION_PATH = "bookings"
//
// class BookingRepositoryFirestore(private val db: FirebaseFirestore) : BookingRepository {
//
//  override fun getNewUid(): String {
//    return db.collection(BOOKINGS_COLLECTION_PATH).document().id
//  }
//
//  override suspend fun getAllBookings(): List<Booking> {
//    val snapshot = db.collection(BOOKINGS_COLLECTION_PATH).get().await()
//    return snapshot.mapNotNull { documentToBooking(it) }
//  }
//
//  override suspend fun getBooking(bookingId: String): Booking {
//    val document = db.collection(BOOKINGS_COLLECTION_PATH).document(bookingId).get().await()
//    return documentToBooking(document)
//        ?: throw Exception("BookingRepositoryFirestore: Booking not found")
//  }
//
//  override suspend fun getBookingsByProvider(providerId: String): List<Booking> {
//    val snapshot =
//        db.collection(BOOKINGS_COLLECTION_PATH).whereEqualTo("providerId",
// providerId).get().await()
//    return snapshot.mapNotNull { documentToBooking(it) }
//  }
//
//  override suspend fun getBookingsByReceiver(receiverId: String): List<Booking> {
//    val snapshot =
//        db.collection(BOOKINGS_COLLECTION_PATH).whereEqualTo("receiverId",
// receiverId).get().await()
//    return snapshot.mapNotNull { documentToBooking(it) }
//  }
//
//  override suspend fun getBookingsByListing(listingId: String): List<Booking> {
//    val snapshot =
//        db.collection(BOOKINGS_COLLECTION_PATH).whereEqualTo("listingId", listingId).get().await()
//    return snapshot.mapNotNull { documentToBooking(it) }
//  }
//
//  override suspend fun addBooking(booking: Booking) {
//    db.collection(BOOKINGS_COLLECTION_PATH).document(booking.bookingId).set(booking).await()
//  }
//
//  override suspend fun updateBooking(bookingId: String, booking: Booking) {
//    db.collection(BOOKINGS_COLLECTION_PATH).document(bookingId).set(booking).await()
//  }
//
//  override suspend fun deleteBooking(bookingId: String) {
//    db.collection(BOOKINGS_COLLECTION_PATH).document(bookingId).delete().await()
//  }
//
//  override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
//    db.collection(BOOKINGS_COLLECTION_PATH)
//        .document(bookingId)
//        .update("status", status.name)
//        .await()
//  }
//
//  override suspend fun confirmBooking(bookingId: String) {
//    updateBookingStatus(bookingId, BookingStatus.CONFIRMED)
//  }
//
//  override suspend fun completeBooking(bookingId: String) {
//    updateBookingStatus(bookingId, BookingStatus.COMPLETED)
//  }
//
//  override suspend fun cancelBooking(bookingId: String) {
//    updateBookingStatus(bookingId, BookingStatus.CANCELLED)
//  }
//
//  private fun documentToBooking(document: DocumentSnapshot): Booking? {
//    return try {
//      val bookingId = document.id
//      val listingId = document.getString("listingId") ?: return null
//      val providerId = document.getString("providerId") ?: return null
//      val receiverId = document.getString("receiverId") ?: return null
//      val sessionStart = document.getTimestamp("sessionStart")?.toDate() ?: return null
//      val sessionEnd = document.getTimestamp("sessionEnd")?.toDate() ?: return null
//      val statusString = document.getString("status") ?: return null
//      val status = BookingStatus.valueOf(statusString)
//      val price = document.getDouble("price") ?: 0.0
//
//      Booking(
//          bookingId = bookingId,
//          listingId = listingId,
//          providerId = providerId,
//          receiverId = receiverId,
//          sessionStart = sessionStart,
//          sessionEnd = sessionEnd,
//          status = status,
//          price = price)
//    } catch (e: Exception) {
//      Log.e("BookingRepositoryFirestore", "Error converting document to Booking", e)
//      null
//    }
//  }
// }
