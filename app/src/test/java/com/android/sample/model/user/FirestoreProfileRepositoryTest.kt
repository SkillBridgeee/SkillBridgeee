package com.android.sample.model.user

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

@Config(sdk = [28])
class FirestoreProfileRepositoryTest : RepositoryTest() {
  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth

  private val context = ApplicationProvider.getApplicationContext<Context>()

  @Before
  override fun setUp() {
    super.setUp()
    firestore = FirebaseEmulator.firestore

    auth = mockk()
    val mockUser = mockk<FirebaseUser>()
    every { auth.currentUser } returns mockUser
    every { mockUser.uid } returns testUserId

    profileRepository = FirestoreProfileRepository(firestore, auth, context)
  }

  @After
  override fun tearDown() = runBlocking {
    val snapshot = firestore.collection(PROFILES_COLLECTION_PATH).get().await()
    for (document in snapshot.documents) {
      document.reference.delete().await()
    }
  }

  @Test
  fun getNewUidReturnsUniqueIDs() {
    val uid1 = profileRepository.getNewUid()
    val uid2 = profileRepository.getNewUid()
    assertNotNull(uid1)
    assertNotNull(uid2)
    assertNotEquals(uid1, uid2)
  }

  @Test
  fun isOnlineReturnsTrueWhenOnline() {
    val repo = FirestoreProfileRepository(firestore, auth, context)
    assertTrue(repo.isOnline())
  }

  @Test
  fun addAndGetProfileWorkCorrectly() = runTest {
    val profile =
        Profile(
            userId = testUserId,
            name = "John Doe",
            email = "john.doe@example.com",
            location = Location(46.519653, 6.632273),
            hourlyRate = "50",
            description = "Experienced tutor.",
            tutorRating = RatingInfo(0.0, 0),
            studentRating = RatingInfo(0.0, 0))
    profileRepository.addProfile(profile)

    val retrievedProfile = profileRepository.getProfile(testUserId)
    assertNotNull(retrievedProfile)
    assertEquals("John Doe", retrievedProfile!!.name)
  }

  @Test
  fun addProfileForAnotherUserFails() {
    val profile = Profile(userId = "another-user-id", name = "Jane Doe")
    assertThrows(Exception::class.java) { runTest { profileRepository.addProfile(profile) } }
  }

  @Test
  fun updateProfileWorksCorrectly() = runTest {
    val originalProfile = Profile(userId = testUserId, name = "John Doe")
    profileRepository.addProfile(originalProfile)

    val updatedProfileData = Profile(userId = testUserId, name = "John Updated")
    profileRepository.updateProfile(testUserId, updatedProfileData)

    val retrievedProfile = profileRepository.getProfile(testUserId)
    assertNotNull(retrievedProfile)
    assertEquals("John Updated", retrievedProfile!!.name)
  }

  @Test
  fun updateProfileForAnotherUserFails() {
    val profile = Profile(userId = "another-user-id", name = "Jane Doe")
    assertThrows(Exception::class.java) {
      runTest { profileRepository.updateProfile("another-user-id", profile) }
    }
  }

  @Test
  fun deleteProfileWorksCorrectly() = runTest {
    val profile = Profile(userId = testUserId, name = "John Doe")
    profileRepository.addProfile(profile)

    profileRepository.deleteProfile(testUserId)
    val retrievedProfile = profileRepository.getProfile(testUserId)
    assertNull(retrievedProfile)
  }

  @Test
  fun deleteProfileForAnotherUserFails() {
    assertThrows(Exception::class.java) {
      runTest { profileRepository.deleteProfile("another-user-id") }
    }
  }

  @Test
  fun getAllProfilesReturnsAllProfiles() = runTest {
    val profile1 = Profile(userId = testUserId, name = "John Doe")
    val profile2 =
        Profile(
            userId = "user2",
            name = "Jane Smith") // Note: addProfile checks current user, so this won't work
    // directly. We'll add to Firestore manually for this test.
    firestore.collection(PROFILES_COLLECTION_PATH).document(testUserId).set(profile1).await()
    firestore.collection(PROFILES_COLLECTION_PATH).document("user2").set(profile2).await()

    val profiles = profileRepository.getAllProfiles()
    assertEquals(2, profiles.size)
    assertTrue(profiles.any { it.name == "John Doe" })
    assertTrue(profiles.any { it.name == "Jane Smith" })
  }

  @Test
  fun getProfileByIdIsSameAsGetProfile() = runTest {
    val profile = Profile(userId = testUserId, name = "John Doe")
    profileRepository.addProfile(profile)

    val profileById = profileRepository.getProfileById(testUserId)
    val profileByGet = profileRepository.getProfile(testUserId)
    assertEquals(profileByGet, profileById)
  }

  @Test
  fun searchByLocationIsNotImplemented() {
    assertThrows(NotImplementedError::class.java) {
      runTest { profileRepository.searchProfilesByLocation(Location(), 10.0) }
    }
  }

  @Test
  fun getSkillsForUserReturnsEmptyListWhenNoSkills() = runTest {
    val profile = Profile(userId = testUserId, name = "John Doe")
    profileRepository.addProfile(profile)

    val skills = profileRepository.getSkillsForUser(testUserId)
    assertTrue(skills.isEmpty())
  }

  @Test
  fun updateTutorRatingFields_updatesValuesCorrectly() = runTest {
    // Arrange
    val profile =
        Profile(
            userId = testUserId,
            name = "John Doe",
            tutorRating = RatingInfo(1.0, 1),
            studentRating = RatingInfo(2.0, 3))
    profileRepository.addProfile(profile)

    // Act
    profileRepository.updateTutorRatingFields(
        userId = testUserId, averageRating = 4.7, totalRatings = 12)

    // Assert
    val updated = profileRepository.getProfile(testUserId)
    assertNotNull(updated)
    assertEquals(4.7, updated!!.tutorRating.averageRating, 0.001)
    assertEquals(12, updated.tutorRating.totalRatings)

    // Student rating must remain unchanged
    assertEquals(2.0, updated.studentRating.averageRating, 0.001)
    assertEquals(3, updated.studentRating.totalRatings)
  }

  @Test
  fun updateStudentRatingFields_updatesValuesCorrectly() = runTest {
    // Arrange
    val profile =
        Profile(
            userId = testUserId,
            name = "John Doe",
            tutorRating = RatingInfo(3.0, 4),
            studentRating = RatingInfo(1.5, 1))
    profileRepository.addProfile(profile)

    // Act
    profileRepository.updateStudentRatingFields(
        userId = testUserId, averageRating = 4.2, totalRatings = 15)

    // Assert
    val updated = profileRepository.getProfile(testUserId)
    assertNotNull(updated)
    assertEquals(4.2, updated!!.studentRating.averageRating, 0.001)
    assertEquals(15, updated.studentRating.totalRatings)

    // Tutor rating must remain unchanged
    assertEquals(3.0, updated.tutorRating.averageRating, 0.001)
    assertEquals(4, updated.tutorRating.totalRatings)
  }

  @Test
  fun updateStudentRatingFields_throwsExceptionWhenFirestoreFails() = runTest {
    // using a fake userId that doesn't exist causes the Firestore update to fail
    assertThrows(Exception::class.java) {
      runBlocking {
        profileRepository.updateStudentRatingFields(
            userId = "nonexistent-user", averageRating = 4.0, totalRatings = 10)
      }
    }
  }
}
