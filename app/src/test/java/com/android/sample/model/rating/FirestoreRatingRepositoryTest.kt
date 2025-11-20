package com.android.sample.model.rating

import com.android.sample.utils.FirebaseEmulator
import com.android.sample.utils.RepositoryTest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

@Config(sdk = [28])
class FirestoreRatingRepositoryTest : RepositoryTest() {
  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth

  private val otherUserId = "other-user-id"

  @Before
  override fun setUp() {
    super.setUp()
    firestore = FirebaseEmulator.firestore

    // Mock FirebaseAuth
    auth = mockk()
    val mockUser = mockk<FirebaseUser>()
    every { auth.currentUser } returns mockUser
    every { mockUser.uid } returns testUserId // from RepositoryTest

    ratingRepository = FirestoreRatingRepository(firestore, auth)
  }

  @After
  override fun tearDown() = runBlocking {
    val snapshot = firestore.collection("ratings").get().await()
    for (document in snapshot.documents) {
      document.reference.delete().await()
    }
  }

  @Test
  fun `getNewUid returns unique non-null IDs`() {
    val uid1 = ratingRepository.getNewUid()
    val uid2 = ratingRepository.getNewUid()
    assertNotNull(uid1)
    assertNotNull(uid2)
    assertNotEquals(uid1, uid2)
  }

  @Test
  fun `addRating and getRating work correctly`() = runTest {
    val rating =
        Rating(
            ratingId = "rating1",
            fromUserId = testUserId,
            toUserId = otherUserId,
            starRating = StarRating.FOUR,
            comment = "Great tutor!",
            ratingType = RatingType.TUTOR,
            targetObjectId = "listing1")
    ratingRepository.addRating(rating)

    val retrieved = ratingRepository.getRating("rating1")
    assertNotNull(retrieved)
    assertEquals("rating1", retrieved?.ratingId)
    assertEquals(StarRating.FOUR, retrieved?.starRating)
    assertEquals(RatingType.TUTOR, retrieved?.ratingType)
    assertEquals("listing1", retrieved?.targetObjectId)
  }

  @Test
  fun `getRating for non-existent ID returns null`() = runTest {
    val retrieved = ratingRepository.getRating("non-existent-id")
    assertNull(retrieved)
  }

  @Test
  fun `getAllRatings returns only ratings from current user`() = runTest {
    val rating1 =
        Rating(
            ratingId = "rating1",
            fromUserId = testUserId,
            toUserId = otherUserId,
            ratingType = RatingType.TUTOR,
            targetObjectId = "listing1")
    val rating2 =
        Rating(
            ratingId = "rating2",
            fromUserId = otherUserId,
            toUserId = testUserId,
            ratingType = RatingType.STUDENT,
            targetObjectId = otherUserId)
    // Add directly to bypass security check for test setup
    firestore.collection("ratings").document(rating1.ratingId).set(rating1).await()
    firestore.collection("ratings").document(rating2.ratingId).set(rating2).await()

    val allRatings = ratingRepository.getAllRatings()
    assertEquals(1, allRatings.size)
    assertEquals("rating1", allRatings[0].ratingId)
  }

  @Test
  fun `addRating throws when fromUserId is not current user`() = runTest {
    val rating =
        Rating(
            ratingId = "rating1",
            fromUserId = otherUserId, // not current user
            toUserId = testUserId,
            ratingType = RatingType.TUTOR,
            targetObjectId = "listing1")

    val exception =
        assertThrows(Exception::class.java) { runBlocking { ratingRepository.addRating(rating) } }
    assertTrue(exception.message?.contains("Access denied") == true)
  }

  @Test
  fun `addRating throws when rating yourself`() = runTest {
    val rating =
        Rating(
            ratingId = "rating1",
            fromUserId = testUserId,
            toUserId = testUserId, // rating yourself
            ratingType = RatingType.TUTOR,
            targetObjectId = "listing1")

    val exception =
        assertThrows(Exception::class.java) { runBlocking { ratingRepository.addRating(rating) } }
    assertTrue(exception.message?.contains("cannot rate yourself") == true)
  }

  @Test
  fun `getRating throws when user has no access to rating`() = runTest {
    val rating =
        Rating(
            ratingId = "rating1",
            fromUserId = otherUserId,
            toUserId = "third-user-id",
            ratingType = RatingType.TUTOR,
            targetObjectId = "listing1")
    // Add directly to firestore bypassing repository
    firestore.collection("ratings").document(rating.ratingId).set(rating).await()

    val exception =
        assertThrows(Exception::class.java) {
          runBlocking { ratingRepository.getRating("rating1") }
        }
    assertTrue(exception.message?.contains("Access denied") == true)
  }

  @Test
  fun `updateRating throws when updating rating not created by current user`() = runTest {
    val rating =
        Rating(
            ratingId = "rating1",
            fromUserId = otherUserId,
            toUserId = testUserId,
            ratingType = RatingType.STUDENT,
            targetObjectId = "listing1")
    // Add directly to firestore
    firestore.collection("ratings").document(rating.ratingId).set(rating).await()

    val updatedRating = rating.copy(starRating = StarRating.FIVE)
    val exception =
        assertThrows(Exception::class.java) {
          runBlocking { ratingRepository.updateRating("rating1", updatedRating) }
        }
    assertTrue(exception.message?.contains("Access denied") == true)
  }

  @Test
  fun `deleteRating throws when deleting rating not created by current user`() = runTest {
    val rating =
        Rating(
            ratingId = "rating1",
            fromUserId = otherUserId,
            toUserId = testUserId,
            ratingType = RatingType.STUDENT,
            targetObjectId = "listing1")
    // Add directly to firestore
    firestore.collection("ratings").document(rating.ratingId).set(rating).await()

    val exception =
        assertThrows(Exception::class.java) {
          runBlocking { ratingRepository.deleteRating("rating1") }
        }
    assertTrue(exception.message?.contains("Access denied") == true)
  }

  @Test
  fun `getTutorRatingsOfUser returns only tutor ratings`() = runTest {
    val rating1 =
        Rating(
            ratingId = "rating1",
            fromUserId = otherUserId,
            toUserId = testUserId,
            ratingType = RatingType.TUTOR,
            targetObjectId = testUserId)
    val rating2 =
        Rating(
            ratingId = "rating2",
            fromUserId = otherUserId,
            toUserId = testUserId,
            ratingType = RatingType.STUDENT,
            targetObjectId = testUserId)
    // Add directly
    firestore.collection("ratings").document(rating1.ratingId).set(rating1).await()
    firestore.collection("ratings").document(rating2.ratingId).set(rating2).await()

    val tutorRatings = ratingRepository.getTutorRatingsOfUser(testUserId)
    assertEquals(1, tutorRatings.size)
    assertEquals(RatingType.TUTOR, tutorRatings[0].ratingType)
  }

  @Test
  fun `getStudentRatingsOfUser returns only student ratings`() = runTest {
    val rating1 =
        Rating(
            ratingId = "rating1",
            fromUserId = otherUserId,
            toUserId = testUserId,
            ratingType = RatingType.STUDENT,
            targetObjectId = testUserId)
    val rating2 =
        Rating(
            ratingId = "rating2",
            fromUserId = otherUserId,
            toUserId = testUserId,
            ratingType = RatingType.TUTOR,
            targetObjectId = testUserId)
    // Add directly
    firestore.collection("ratings").document(rating1.ratingId).set(rating1).await()
    firestore.collection("ratings").document(rating2.ratingId).set(rating2).await()

    val studentRatings = ratingRepository.getStudentRatingsOfUser(testUserId)
    assertEquals(1, studentRatings.size)
    assertEquals(RatingType.STUDENT, studentRatings[0].ratingType)
  }

  @Test
  fun `currentUserId throws when user not authenticated`() {
    val authNoUser = mockk<FirebaseAuth>()
    every { authNoUser.currentUser } returns null
    val repo = FirestoreRatingRepository(firestore, authNoUser)

    val exception = assertThrows(Exception::class.java) { runBlocking { repo.getAllRatings() } }
    assertTrue(exception.message?.contains("not authenticated") == true)
  }

  @Test
  fun `updateRating works when rating exists and user has access`() = runTest {
    val rating =
        Rating(
            ratingId = "rating1",
            fromUserId = testUserId,
            toUserId = otherUserId,
            starRating = StarRating.TWO,
            ratingType = RatingType.LISTING,
            targetObjectId = "listing1")
    ratingRepository.addRating(rating)

    val updated = rating.copy(starRating = StarRating.FIVE)
    ratingRepository.updateRating("rating1", updated)

    val retrieved = ratingRepository.getRating("rating1")
    assertEquals(StarRating.FIVE, retrieved?.starRating)
  }

  @Test
  fun `deleteRating works when rating exists and user has access`() = runTest {
    val rating =
        Rating(
            ratingId = "rating1",
            fromUserId = testUserId,
            toUserId = otherUserId,
            ratingType = RatingType.LISTING,
            targetObjectId = "listing1")
    ratingRepository.addRating(rating)

    ratingRepository.deleteRating("rating1")
    val retrieved = ratingRepository.getRating("rating1")
    assertNull(retrieved)
  }

  @Test
  fun `getRatingsByFromUser returns correct ratings`() = runTest {
    val rating1 =
        Rating(
            ratingId = "rating1",
            fromUserId = testUserId,
            toUserId = otherUserId,
            ratingType = RatingType.TUTOR,
            targetObjectId = "listing1")
    val rating2 =
        Rating(
            ratingId = "rating2",
            fromUserId = otherUserId,
            toUserId = testUserId,
            ratingType = RatingType.STUDENT,
            targetObjectId = otherUserId)
    // Add directly to bypass security check for test setup
    firestore.collection("ratings").document(rating1.ratingId).set(rating1).await()
    firestore.collection("ratings").document(rating2.ratingId).set(rating2).await()

    val fromUserRatings = ratingRepository.getRatingsByFromUser(testUserId)
    assertEquals(1, fromUserRatings.size)
    assertEquals("rating1", fromUserRatings[0].ratingId)
  }

  @Test
  fun `getRatingsByToUser returns correct ratings`() = runTest {
    val rating1 =
        Rating(
            ratingId = "rating1",
            fromUserId = testUserId,
            toUserId = otherUserId,
            ratingType = RatingType.TUTOR,
            targetObjectId = "listing1")
    val rating2 =
        Rating(
            ratingId = "rating2",
            fromUserId = otherUserId,
            toUserId = testUserId,
            ratingType = RatingType.STUDENT,
            targetObjectId = otherUserId)
    // Add directly to bypass security check for test setup
    firestore.collection("ratings").document(rating1.ratingId).set(rating1).await()
    firestore.collection("ratings").document(rating2.ratingId).set(rating2).await()

    val toUserRatings = ratingRepository.getRatingsByToUser(testUserId)
    assertEquals(1, toUserRatings.size)
    assertEquals("rating2", toUserRatings[0].ratingId)
  }

  @Test
  fun `getRatingsOfListing returns correct ratings`() = runTest {
    val rating1 =
        Rating(
            ratingId = "rating1",
            fromUserId = testUserId,
            toUserId = otherUserId,
            ratingType = RatingType.LISTING,
            targetObjectId = "listing1")
    val rating2 =
        Rating(
            ratingId = "rating2",
            fromUserId = otherUserId,
            toUserId = testUserId,
            ratingType = RatingType.LISTING,
            targetObjectId = "listing2")
    // Add directly to bypass security check for test setup
    firestore.collection("ratings").document(rating1.ratingId).set(rating1).await()
    firestore.collection("ratings").document(rating2.ratingId).set(rating2).await()

    val listingRatings = ratingRepository.getRatingsOfListing("listing1")
    assertEquals(1, listingRatings.size)
    assertEquals("rating1", listingRatings[0].ratingId)
  }

  @Test
  fun `updateRating modifies existing rating`() = runTest {
    val originalRating =
        Rating(
            ratingId = "rating1",
            fromUserId = testUserId,
            toUserId = otherUserId,
            starRating = StarRating.THREE,
            comment = "Okay",
            ratingType = RatingType.TUTOR,
            targetObjectId = "listing1")
    ratingRepository.addRating(originalRating)

    val updatedRating = originalRating.copy(starRating = StarRating.FIVE, comment = "Excellent!")
    ratingRepository.updateRating("rating1", updatedRating)

    val retrieved = ratingRepository.getRating("rating1")
    assertNotNull(retrieved)
    assertEquals(StarRating.FIVE, retrieved?.starRating)
    assertEquals("Excellent!", retrieved?.comment)
  }

  @Test
  fun `deleteRating removes the rating`() = runTest {
    val rating =
        Rating(
            ratingId = "rating1",
            fromUserId = testUserId,
            toUserId = otherUserId,
            ratingType = RatingType.TUTOR,
            targetObjectId = "listing1")
    ratingRepository.addRating(rating)

    var retrieved = ratingRepository.getRating("rating1")
    assertNotNull(retrieved)

    ratingRepository.deleteRating("rating1")
    retrieved = ratingRepository.getRating("rating1")
    assertNull(retrieved)
  }

  @Test
  fun `hasRating returns true when matching rating exists`() = runTest {
    val rating =
        Rating(
            ratingId = "rating-has-1",
            fromUserId = testUserId,
            toUserId = otherUserId,
            starRating = StarRating.FOUR,
            comment = "Great!",
            ratingType = RatingType.TUTOR,
            targetObjectId = "listing-has-1",
        )

    // Insert directly in Firestore so hasRating queries it
    firestore.collection(RATINGS_COLLECTION_PATH).document(rating.ratingId).set(rating).await()

    val exists =
        ratingRepository.hasRating(
            fromUserId = testUserId,
            toUserId = otherUserId,
            ratingType = RatingType.TUTOR,
            targetObjectId = "listing-has-1",
        )

    assertTrue(exists)
  }

  @Test
  fun `hasRating returns false when no matching rating exists`() = runTest {
    // Make sure collection is empty or contains only non-matching ratings
    val rating =
        Rating(
            ratingId = "rating-has-2",
            fromUserId = testUserId,
            toUserId = otherUserId,
            starRating = StarRating.THREE,
            comment = "Irrelevant",
            ratingType = RatingType.TUTOR,
            targetObjectId = "some-other-listing",
        )

    firestore.collection(RATINGS_COLLECTION_PATH).document(rating.ratingId).set(rating).await()

    val exists =
        ratingRepository.hasRating(
            fromUserId = testUserId,
            toUserId = otherUserId,
            ratingType = RatingType.TUTOR,
            targetObjectId = "listing-has-1", // different target
        )

    assertFalse(exists)
  }
}
