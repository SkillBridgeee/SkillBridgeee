package com.android.sample.model.rating

import java.util.UUID

class FakeRatingRepository(private val initial: List<Rating> = emptyList()) : RatingRepository {

  private val ratings =
      mutableMapOf<String, Rating>().apply { initial.forEach { put(getIdOrGenerate(it), it) } }

  override fun getNewUid(): String = UUID.randomUUID().toString()

  override suspend fun getAllRatings(): List<Rating> =
      synchronized(ratings) { ratings.values.toList() }

  override suspend fun getRating(ratingId: String): Rating =
      synchronized(ratings) {
        ratings[ratingId] ?: throw NoSuchElementException("Rating $ratingId not found")
      }

  override suspend fun getRatingsByFromUser(fromUserId: String): List<Rating> =
      synchronized(ratings) {
        ratings.values.filter { r ->
          val v = findValueOn(r, listOf("fromUserId", "fromUser", "authorId", "creatorId"))
          v?.toString() == fromUserId
        }
      }

  override suspend fun getRatingsByToUser(toUserId: String): List<Rating> =
      synchronized(ratings) {
        ratings.values.filter { r ->
          val v = findValueOn(r, listOf("toUserId", "toUser", "receiverId", "targetId"))
          v?.toString() == toUserId
        }
      }

  override suspend fun getRatingsOfListing(listingId: String): Rating? =
      synchronized(ratings) {
        ratings.values.firstOrNull { r ->
          val v = findValueOn(r, listOf("listingId", "associatedListingId", "listing_id"))
          v?.toString() == listingId
        }
      }

  override suspend fun addRating(rating: Rating) {
    synchronized(ratings) { ratings[getIdOrGenerate(rating)] = rating }
  }

  override suspend fun updateRating(ratingId: String, rating: Rating) {
    synchronized(ratings) {
      if (!ratings.containsKey(ratingId)) throw NoSuchElementException("Rating $ratingId not found")
      ratings[ratingId] = rating
    }
  }

  override suspend fun deleteRating(ratingId: String) {
    synchronized(ratings) { ratings.remove(ratingId) }
  }

  override suspend fun getTutorRatingsOfUser(userId: String): List<Rating> =
      synchronized(ratings) {
        // Heuristic: ratings for tutors related to listings owned by this user OR ratings targeting
        // the user.
        ratings.values.filter { r ->
          val owner = findValueOn(r, listOf("listingOwnerId", "listingOwner", "ownerId"))
          val toUser = findValueOn(r, listOf("toUserId", "toUser", "receiverId"))
          owner?.toString() == userId || toUser?.toString() == userId
        }
      }

  override suspend fun getStudentRatingsOfUser(userId: String): List<Rating> =
      synchronized(ratings) {
        // Heuristic: ratings received by this user as a student (targeted to the user)
        ratings.values.filter { r ->
          val toUser = findValueOn(r, listOf("toUserId", "toUser", "receiverId", "targetId"))
          toUser?.toString() == userId
        }
      }

  // --- Helpers ---

  private fun getIdOrGenerate(rating: Rating): String {
    val v = findValueOn(rating, listOf("ratingId", "id", "rating_id"))
    return v?.toString() ?: UUID.randomUUID().toString()
  }

  private fun findValueOn(obj: Any, names: List<String>): Any? {
    try {
      // try getters / isX first
      for (name in names) {
        val getter = "get" + name.replaceFirstChar { it.uppercaseChar() }
        val isMethod = "is" + name.replaceFirstChar { it.uppercaseChar() }
        val method =
            obj.javaClass.methods.firstOrNull { m ->
              m.parameterCount == 0 &&
                  (m.name.equals(getter, true) ||
                      m.name.equals(name, true) ||
                      m.name.equals(isMethod, true))
            }
        if (method != null) {
          try {
            val v = method.invoke(obj)
            if (v != null) return v
          } catch (_: Throwable) {
            /* ignore */
          }
        }
      }

      // try declared fields
      for (name in names) {
        try {
          val field = obj.javaClass.getDeclaredField(name)
          field.isAccessible = true
          val v = field.get(obj)
          if (v != null) return v
        } catch (_: Throwable) {
          /* ignore */
        }
      }
    } catch (_: Throwable) {
      // ignore reflection failures
    }
    return null
  }
}
