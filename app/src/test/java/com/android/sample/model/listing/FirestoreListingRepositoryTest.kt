package com.android.sample.model.listing

import com.android.sample.model.skill.Skill
import com.android.sample.utils.RepositoryTest
import com.github.se.bootcamp.utils.FirebaseEmulator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import java.util.Date
import kotlin.text.set
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class FirestoreListingRepositoryTest : RepositoryTest() {
  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var repository: ListingRepository

  private val testProposal =
      Proposal(
          listingId = "proposal1",
          creatorUserId = testUserId,
          skill = Skill(skill = "Android"),
          description = "Android proposal",
          createdAt = Date())

  private val testRequest =
      Request(
          listingId = "request1",
          creatorUserId = testUserId,
          skill = Skill(skill = "iOS"),
          description = "iOS request",
          createdAt = Date())

  @Before
  override fun setUp() {
    super.setUp()
    firestore = FirebaseEmulator.firestore

    auth = mockk(relaxed = true)
    val mockUser = mockk<FirebaseUser>()
    every { auth.currentUser } returns mockUser
    every { mockUser.uid } returns testUserId

    repository = FirestoreListingRepository(firestore, auth)
  }

  @After
  override fun tearDown() = runBlocking {
    val snapshot = firestore.collection(LISTINGS_COLLECTION_PATH).get().await()
    for (document in snapshot.documents) {
      document.reference.delete().await()
    }
  }

  @Test
  fun addAndGetProposal() = runTest {
    repository.addProposal(testProposal)
    val retrieved = repository.getListing("proposal1")
    assertEquals(testProposal, retrieved)
  }

  @Test
  fun getNewUidReturnsUniqueIds() {
    val uid1 = repository.getNewUid()
    val uid2 = repository.getNewUid()
    assertNotNull(uid1)
    assertNotNull(uid2)
    assertTrue(uid1 != uid2)
  }

  @Test
  fun getAllListingsReturnsEmptyListWhenNoListings() = runTest {
    val listings = repository.getAllListings()
    assertEquals(0, listings.size)
  }

  @Test
  fun getProposalsReturnsEmptyListWhenNoProposals() = runTest {
    repository.addRequest(testRequest)
    val proposals = repository.getProposals()
    assertEquals(0, proposals.size)
  }

  @Test
  fun getRequestsReturnsEmptyListWhenNoRequests() = runTest {
    repository.addProposal(testProposal)
    val requests = repository.getRequests()
    assertEquals(0, requests.size)
  }

  @Test
  fun getListingsByUserReturnsEmptyListWhenNoListings() = runTest {
    val listings = repository.getListingsByUser(testUserId)
    assertEquals(0, listings.size)
  }

  @Test
  fun updateNonExistentListingThrowsException() {
    val updatedProposal = testProposal.copy(description = "Updated")
    assertThrows(Exception::class.java) {
      runTest { repository.updateListing("non-existent", updatedProposal) }
    }
  }

  @Test
  fun updateListingOfAnotherUserThrowsException() = runTest {
    // Add a listing as another user
    every { auth.currentUser?.uid } returns "another-user"
    repository.addProposal(testProposal.copy(creatorUserId = "another-user", listingId = "p1"))

    // Switch back to the main test user and try to update it
    every { auth.currentUser?.uid } returns testUserId
    val updatedProposal = testProposal.copy(listingId = "p1", description = "Hacked")
    assertThrows(Exception::class.java) {
      runTest { repository.updateListing("p1", updatedProposal) }
    }
  }

  @Test
  fun deleteNonExistentListingThrowsException() {
    assertThrows(Exception::class.java) { runTest { repository.deleteListing("non-existent") } }
  }

  @Test
  fun deactivateNonExistentListingThrowsException() {
    assertThrows(Exception::class.java) { runTest { repository.deactivateListing("non-existent") } }
  }

  @Test
  fun deactivateListingOfAnotherUserThrowsException() = runTest {
    // Add a listing as another user
    every { auth.currentUser?.uid } returns "another-user"
    repository.addProposal(testProposal.copy(creatorUserId = "another-user", listingId = "p1"))

    // Switch back to the main test user and try to deactivate it
    every { auth.currentUser?.uid } returns testUserId
    assertThrows(Exception::class.java) { runTest { repository.deactivateListing("p1") } }
  }

  @Test
  fun searchBySkillReturnsEmptyListWhenNoMatches() = runTest {
    repository.addProposal(testProposal)
    val results = repository.searchBySkill(Skill(skill = "Python"))
    assertEquals(0, results.size)
  }

  @Test
  fun searchBySkillReturnsMultipleMatches() = runTest {
    val proposal1 = testProposal.copy(listingId = "p1")
    val proposal2 = testProposal.copy(listingId = "p2")
    repository.addProposal(proposal1)
    repository.addProposal(proposal2)

    val results = repository.searchBySkill(Skill(skill = "Android"))
    assertEquals(2, results.size)
  }

  @Test
  fun searchByLocationThrowsNotImplementedException() {
    assertThrows(NotImplementedError::class.java) {
      runTest {
        repository.searchByLocation(com.android.sample.model.map.Location(0.0, 0.0, "Test"), 10.0)
      }
    }
  }

  @Test
  fun addProposalThrowsExceptionWhenUserNotAuthenticated() {
    every { auth.currentUser } returns null

    assertThrows(Exception::class.java) { runTest { repository.addProposal(testProposal) } }
  }

  @Test
  fun addRequestThrowsExceptionWhenUserNotAuthenticated() {
    every { auth.currentUser } returns null

    assertThrows(Exception::class.java) { runTest { repository.addRequest(testRequest) } }
  }

  @Test
  fun getListingsHandlesInvalidTypeInDatabase() = runTest {
    // Manually insert a document with an invalid type
    firestore
        .collection(LISTINGS_COLLECTION_PATH)
        .document("invalid1")
        .set(
            mapOf(
                "listingId" to "invalid1",
                "creatorUserId" to testUserId,
                "type" to "INVALID_TYPE",
                "description" to "Invalid"))
        .await()

    val listings = repository.getAllListings()
    // The invalid listing should be filtered out
    assertEquals(0, listings.size)
  }

  @Test
  fun getListingsHandlesMissingTypeField() = runTest {
    // Manually insert a document without a type field
    firestore
        .collection(LISTINGS_COLLECTION_PATH)
        .document("notype1")
        .set(
            mapOf(
                "listingId" to "notype1",
                "creatorUserId" to testUserId,
                "description" to "No type"))
        .await()

    val listings = repository.getAllListings()
    // The document without type should be filtered out
    assertEquals(0, listings.size)
  }

  @Test
  fun getListingsByUserWithMultipleListings() = runTest {
    val proposal1 = testProposal.copy(listingId = "p1")
    val proposal2 = testProposal.copy(listingId = "p2")
    val request1 = testRequest.copy(listingId = "r1")

    repository.addProposal(proposal1)
    repository.addProposal(proposal2)
    repository.addRequest(request1)

    val userListings = repository.getListingsByUser(testUserId)
    assertEquals(3, userListings.size)
  }

  @Test
  fun updateListingPreservesListingId() = runTest {
    repository.addProposal(testProposal)
    val updatedProposal =
        testProposal.copy(
            listingId = "different-id", // Try to change ID
            description = "Updated")
    repository.updateListing("proposal1", updatedProposal)

    // Original ID should still exist
    val retrieved = repository.getListing("proposal1")
    assertNotNull(retrieved)
    assertEquals("Updated", retrieved?.description)
  }

  @Test
  fun addAndGetRequest() = runTest {
    repository.addRequest(testRequest)
    val retrieved = repository.getListing("request1")
    assertEquals(testRequest, retrieved)
  }

  @Test
  fun getNonExistentListingReturnsNull() = runTest {
    val retrieved = repository.getListing("non-existent")
    assertNull(retrieved)
  }

  @Test
  fun getAllListingsReturnsAllTypes() = runTest {
    repository.addProposal(testProposal)
    repository.addRequest(testRequest)
    val allListings = repository.getAllListings()
    assertEquals(2, allListings.size)
    assertTrue(allListings.contains(testProposal))
    assertTrue(allListings.contains(testRequest))
  }

  @Test
  fun getProposalsReturnsOnlyProposals() = runTest {
    repository.addProposal(testProposal)
    repository.addRequest(testRequest)
    val proposals = repository.getProposals()
    assertEquals(1, proposals.size)
    assertEquals(testProposal, proposals[0])
  }

  @Test
  fun getRequestsReturnsOnlyRequests() = runTest {
    repository.addProposal(testProposal)
    repository.addRequest(testRequest)
    val requests = repository.getRequests()
    assertEquals(1, requests.size)
    assertEquals(testRequest, requests[0])
  }

  @Test
  fun getListingsByUser() = runTest {
    repository.addProposal(testProposal)
    val otherProposal = testProposal.copy(listingId = "proposal2", creatorUserId = "other-user")

    // Mock auth for the other user to add their listing
    every { auth.currentUser?.uid } returns "other-user"
    repository.addProposal(otherProposal)

    // Switch back to the original test user
    every { auth.currentUser?.uid } returns testUserId

    val userListings = repository.getListingsByUser(testUserId)
    assertEquals(1, userListings.size)
    assertEquals(testProposal, userListings[0])
  }

  @Test
  fun deleteListing() = runTest {
    repository.addProposal(testProposal)
    assertNotNull(repository.getListing("proposal1"))
    repository.deleteListing("proposal1")
    assertNull(repository.getListing("proposal1"))
  }

  @Test
  fun deactivateListing() = runTest {
    repository.addProposal(testProposal)
    repository.deactivateListing("proposal1")
    // Re-fetch the document directly to check the raw value
    val doc = firestore.collection(LISTINGS_COLLECTION_PATH).document("proposal1").get().await()
    assertNotNull(doc)
    assertFalse(doc.getBoolean("isActive")!!)
  }

  @Test
  fun updateListing() = runTest {
    repository.addProposal(testProposal)
    val updatedProposal = testProposal.copy(description = "Updated description")
    repository.updateListing("proposal1", updatedProposal)
    val retrieved = repository.getListing("proposal1")
    assertEquals(updatedProposal, retrieved)
  }

  @Test
  fun searchBySkill() = runTest {
    repository.addProposal(testProposal)
    repository.addRequest(testRequest)
    val results = repository.searchBySkill(Skill(skill = "Android"))
    assertEquals(1, results.size)
    assertEquals(testProposal, results[0])
  }

  @Test
  fun addListingForAnotherUserThrowsException() {
    val anotherUserProposal = testProposal.copy(creatorUserId = "another-user")
    assertThrows(Exception::class.java) { runTest { repository.addProposal(anotherUserProposal) } }
  }

  @Test
  fun deleteListingOfAnotherUserThrowsException() = runTest {
    // Add a listing as another user
    every { auth.currentUser?.uid } returns "another-user"
    repository.addProposal(testProposal.copy(creatorUserId = "another-user", listingId = "p1"))

    // Switch back to the main test user and try to delete it
    every { auth.currentUser?.uid } returns testUserId
    assertThrows(Exception::class.java) { runTest { repository.deleteListing("p1") } }
  }
}
