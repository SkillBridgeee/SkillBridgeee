// package com.android.sample.model.rating
//
// import android.util.Log
// import com.android.sample.model.listing.ListingRepository
// import com.android.sample.model.user.ProfileRepository
// import com.google.firebase.firestore.DocumentSnapshot
// import com.google.firebase.firestore.FirebaseFirestore
// import kotlinx.coroutines.tasks.await
//
// const val RATINGS_COLLECTION_PATH = "ratings"
//
// class RatingRepositoryFirestore(private val db: FirebaseFirestore) : RatingRepository {
//
//  override fun getNewUid(): String {
//    return db.collection(RATINGS_COLLECTION_PATH).document().id
//  }
//
//  override suspend fun getAllRatings(): List<Rating> {
//    val snapshot = db.collection(RATINGS_COLLECTION_PATH).get().await()
//    return snapshot.mapNotNull { documentToRating(it) }
//  }
//
//  override suspend fun getRating(ratingId: String): Rating {
//    val document = db.collection(RATINGS_COLLECTION_PATH).document(ratingId).get().await()
//    return documentToRating(document)
//        ?: throw Exception("RatingRepositoryFirestore: Rating not found")
//  }
//
//  override suspend fun getRatingsByFromUser(fromUserId: String): List<Rating> {
//    val snapshot =
//        db.collection(RATINGS_COLLECTION_PATH).whereEqualTo("fromUserId",
// fromUserId).get().await()
//    return snapshot.mapNotNull { documentToRating(it) }
//  }
//
//  override suspend fun getRatingsByToUser(toUserId: String): List<Rating> {
//    val snapshot =
//        db.collection(RATINGS_COLLECTION_PATH).whereEqualTo("toUserId", toUserId).get().await()
//    return snapshot.mapNotNull { documentToRating(it) }
//  }
//
//  override suspend fun getRatingsByListing(listingId: String): List<Rating> {
//    val snapshot =
//        db.collection(RATINGS_COLLECTION_PATH).whereEqualTo("listingId", listingId).get().await()
//    return snapshot.mapNotNull { documentToRating(it) }
//  }
//
//  override suspend fun getRatingsByBooking(bookingId: String): Rating? {
//    val snapshot =
//        db.collection(RATINGS_COLLECTION_PATH).whereEqualTo("bookingId", bookingId).get().await()
//    return snapshot.documents.firstOrNull()?.let { documentToRating(it) }
//  }
//
//  override suspend fun addRating(rating: Rating) {
//    db.collection(RATINGS_COLLECTION_PATH).document(rating.ratingId).set(rating).await()
//  }
//
//  override suspend fun updateRating(ratingId: String, rating: Rating) {
//    db.collection(RATINGS_COLLECTION_PATH).document(ratingId).set(rating).await()
//  }
//
//  override suspend fun deleteRating(ratingId: String) {
//    db.collection(RATINGS_COLLECTION_PATH).document(ratingId).delete().await()
//  }
//
//  override suspend fun getTutorRatingsForUser(
//      userId: String,
//      listingRepository: ListingRepository
//  ): List<Rating> {
//    // Get all listings owned by this user
//    val userListings = listingRepository.getListingsByUser(userId)
//    val listingIds = userListings.map { it.listingId }
//
//    if (listingIds.isEmpty()) return emptyList()
//
//    // Get all tutor ratings for these listings
//    val allRatings = mutableListOf<Rating>()
//    for (listingId in listingIds) {
//      val ratings = getRatingsByListing(listingId).filter { it.ratingType == RatingType.TUTOR }
//      allRatings.addAll(ratings)
//    }
//
//    return allRatings
//  }
//
//  override suspend fun getStudentRatingsForUser(userId: String): List<Rating> {
//    return getRatingsByToUser(userId).filter { it.ratingType == RatingType.STUDENT }
//  }
//
//  override suspend fun addRatingAndUpdateProfile(
//      rating: Rating,
//      profileRepository: ProfileRepository,
//      listingRepository: ListingRepository
//  ) {
//    addRating(rating)
//
//    when (rating.ratingType) {
//      RatingType.TUTOR -> {
//        // Recalculate tutor rating based on all their listing ratings
//        profileRepository.recalculateTutorRating(rating.toUserId, listingRepository, this)
//      }
//      RatingType.STUDENT -> {
//        // Recalculate student rating based on all their received ratings
//        profileRepository.recalculateStudentRating(rating.toUserId, this)
//      }
//    }
//  }
//
//  private fun documentToRating(document: DocumentSnapshot): Rating? {
//    return try {
//      val ratingId = document.id
//      val bookingId = document.getString("bookingId") ?: return null
//      val listingId = document.getString("listingId") ?: return null
//      val fromUserId = document.getString("fromUserId") ?: return null
//      val toUserId = document.getString("toUserId") ?: return null
//      val starRatingValue = (document.getLong("starRating") ?: return null).toInt()
//      val starRating = StarRating.fromInt(starRatingValue)
//      val comment = document.getString("comment") ?: ""
//      val ratingTypeString = document.getString("ratingType") ?: return null
//      val ratingType = RatingType.valueOf(ratingTypeString)
//
//      Rating(
//          ratingId = ratingId,
//          bookingId = bookingId,
//          listingId = listingId,
//          fromUserId = fromUserId,
//          toUserId = toUserId,
//          starRating = starRating,
//          comment = comment,
//          ratingType = ratingType)
//    } catch (e: Exception) {
//      Log.e("RatingRepositoryFirestore", "Error converting document to Rating", e)
//      null
//    }
//  }
// }
