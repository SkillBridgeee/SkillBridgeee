package com.android.sample.model.rating

import com.android.sample.utils.RepositoryTest
import com.github.se.bootcamp.utils.FirebaseEmulator
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
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
}
