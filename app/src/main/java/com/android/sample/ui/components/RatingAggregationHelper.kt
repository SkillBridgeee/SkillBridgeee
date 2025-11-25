package com.android.sample.ui.components

import android.util.Log
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.rating.StarRating
import com.android.sample.model.user.ProfileRepository

/**
 * Helper for recomputing aggregate (average, count) ratings and storing them on user profiles.
 *
 * Used by:
 * - BookingDetailsViewModel (tutor aggregate rating)
 * - ListingViewModel (student aggregate rating)
 */
object RatingAggregationHelper {

  private const val TAG = "RatingAggregationHelper"

  /**
   * Recomputes and stores the aggregated *tutor* rating for the given user.
   * - Fetches all tutor ratings of that user.
   * - Computes average stars and total number of ratings.
   * - Writes the result into `tutorRating` fields on the profile.
   *
   * Any repository exception is caught and logged so UI flows do not crash.
   */
  suspend fun recomputeTutorAggregateRating(
      tutorUserId: String,
      ratingRepo: RatingRepository,
      profileRepo: ProfileRepository,
      logTag: String = TAG
  ) {
    try {
      val ratings = ratingRepo.getTutorRatingsOfUser(tutorUserId)
      val count = ratings.size

      if (count == 0) {
        profileRepo.updateTutorRatingFields(
            userId = tutorUserId, averageRating = 0.0, totalRatings = 0)
        return
      }

      val sum = ratings.sumOf { it.starRating.toInt() }
      val avg = sum.toDouble() / count.toDouble()

      profileRepo.updateTutorRatingFields(
          userId = tutorUserId, averageRating = avg, totalRatings = count)
    } catch (e: Exception) {
      Log.w(logTag, "Failed to recompute tutor rating for $tutorUserId", e)
    }
  }

  /**
   * Recomputes and stores the aggregated *student* rating for the given user.
   * - Fetches all student ratings received by that user.
   * - Computes average stars and total number of ratings.
   * - Writes the result into `studentRating` fields on the profile.
   *
   * Any repository exception is caught and logged so UI flows do not crash.
   */
  suspend fun recomputeStudentAggregateRating(
      studentUserId: String,
      ratingRepo: RatingRepository,
      profileRepo: ProfileRepository,
      logTag: String = TAG
  ) {
    try {
      val ratings = ratingRepo.getStudentRatingsOfUser(studentUserId)
      val count = ratings.size

      if (count == 0) {
        profileRepo.updateStudentRatingFields(
            userId = studentUserId, averageRating = 0.0, totalRatings = 0)
        return
      }

      val sum = ratings.sumOf { it.starRating.toInt() }
      val avg = sum.toDouble() / count.toDouble()

      profileRepo.updateStudentRatingFields(
          userId = studentUserId, averageRating = avg, totalRatings = count)
    } catch (e: Exception) {
      Log.w(logTag, "Failed to recompute student rating for $studentUserId", e)
    }
  }

  /** Maps a [StarRating] enum to its numeric 1–5 representation. */
  private fun StarRating.toInt(): Int =
      when (this) {
        StarRating.ONE -> 1
        StarRating.TWO -> 2
        StarRating.THREE -> 3
        StarRating.FOUR -> 4
        StarRating.FIVE -> 5
      }
}
