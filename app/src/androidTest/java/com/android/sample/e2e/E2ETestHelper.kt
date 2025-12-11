package com.android.sample.e2e

import android.content.Context
import android.util.Log
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.communication.ConversationManager
import com.android.sample.model.communication.conversation.ConversationRepositoryProvider
import com.android.sample.model.communication.overViewConv.OverViewConvRepositoryProvider
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepositoryProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

/**
 * Helper object containing utility functions for end-to-end tests. Provides common functionality
 * for authentication, user management, and test setup.
 */
object E2ETestHelper {

  private const val TAG = "E2ETestHelper"

  /**
   * Initializes all repositories required for E2E tests. Should be called in @Before setup method.
   * Only catches IllegalStateException for "already initialized" cases.
   *
   * @param context The application context
   * @throws Exception if initialization fails for reasons other than already being initialized
   */
  fun initializeRepositories(context: Context) {
    initializeRepository("ProfileRepository") { ProfileRepositoryProvider.init(context) }
    initializeRepository("ListingRepository") { ListingRepositoryProvider.init(context) }
    initializeRepository("BookingRepository") { BookingRepositoryProvider.init(context) }
    initializeRepository("RatingRepository") { RatingRepositoryProvider.init(context) }
    initializeRepository("OverViewConvRepository") { OverViewConvRepositoryProvider.init(context) }
    initializeRepository("ConversationRepository") { ConversationRepositoryProvider.init(context) }
  }

  /** Helper to initialize a single repository, catching only "already initialized" exceptions. */
  private fun initializeRepository(name: String, init: () -> Unit) {
    try {
      init()
    } catch (e: IllegalStateException) {
      // Expected if already initialized - safe to ignore
      Log.d(TAG, "$name already initialized")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to initialize $name", e)
      throw e
    }
  }

  /**
   * Data class to hold the result of creating a test listing.
   *
   * @param listingId The ID of the created listing
   * @param proposal The created Proposal object
   */
  data class TestListingResult(val listingId: String, val proposal: Proposal)

  /**
   * Creates a test listing (Proposal) in Firestore.
   *
   * @param creatorUserId The user ID of the listing creator (must not be blank)
   * @param skill The skill for the listing
   * @param title The listing title (must not be blank)
   * @param description The listing description (must not be blank)
   * @param hourlyRate The hourly rate (default: 40.0)
   * @return TestListingResult containing both the listing ID and the created Proposal
   * @throws IllegalArgumentException if required parameters are blank
   */
  suspend fun createTestListing(
      creatorUserId: String,
      skill: Skill,
      title: String,
      description: String,
      hourlyRate: Double = 40.0
  ): String {
    // Validate parameters
    require(creatorUserId.isNotBlank()) { "creatorUserId must not be blank" }
    require(title.isNotBlank()) { "title must not be blank" }
    require(description.isNotBlank()) { "description must not be blank" }

    val listingId = ListingRepositoryProvider.repository.getNewUid()
    val proposal =
        Proposal(
            listingId = listingId,
            creatorUserId = creatorUserId,
            skill = skill,
            title = title,
            description = description,
            location = Location(latitude = 46.5197, longitude = 6.6323),
            hourlyRate = hourlyRate,
            isActive = true)
    ListingRepositoryProvider.repository.addProposal(proposal)
    waitForDocument("listings", listingId, timeoutMs = 5000L)
    return listingId
  }

  /**
   * Creates a test listing and returns both the ID and the Proposal object. Use this when you need
   * to inspect the created data in assertions.
   *
   * @param creatorUserId The user ID of the listing creator (must not be blank)
   * @param skill The skill for the listing
   * @param title The listing title (must not be blank)
   * @param description The listing description (must not be blank)
   * @param hourlyRate The hourly rate (default: 40.0)
   * @return TestListingResult containing both the listing ID and the created Proposal
   * @throws IllegalArgumentException if required parameters are blank
   */
  suspend fun createTestListingWithResult(
      creatorUserId: String,
      skill: Skill,
      title: String,
      description: String,
      hourlyRate: Double = 40.0
  ): TestListingResult {
    require(creatorUserId.isNotBlank()) { "creatorUserId must not be blank" }
    require(title.isNotBlank()) { "title must not be blank" }
    require(description.isNotBlank()) { "description must not be blank" }

    val listingId = ListingRepositoryProvider.repository.getNewUid()
    val proposal =
        Proposal(
            listingId = listingId,
            creatorUserId = creatorUserId,
            skill = skill,
            title = title,
            description = description,
            location = Location(latitude = 46.5197, longitude = 6.6323),
            hourlyRate = hourlyRate,
            isActive = true)
    ListingRepositoryProvider.repository.addProposal(proposal)
    waitForDocument("listings", listingId, timeoutMs = 5000L)
    return TestListingResult(listingId, proposal)
  }

  /**
   * Deletes a test listing from Firestore.
   *
   * @param listingId The ID of the listing to delete
   */
  suspend fun deleteTestListing(listingId: String) {
    try {
      ListingRepositoryProvider.repository.deleteListing(listingId)
      Log.d(TAG, "Deleted test listing: $listingId")
    } catch (e: Exception) {
      Log.w(TAG, "Could not delete test listing $listingId: ${e.message}")
    }
  }

  /**
   * Creates a conversation between two users using ConversationManager.
   *
   * @param creatorId The user ID of the conversation creator (must not be blank)
   * @param otherUserId The user ID of the other participant (must not be blank)
   * @param convName The name of the conversation (must not be blank)
   * @return The created conversation ID
   * @throws IllegalArgumentException if required parameters are blank
   */
  suspend fun createTestConversation(
      creatorId: String,
      otherUserId: String,
      convName: String
  ): String {
    require(creatorId.isNotBlank()) { "creatorId must not be blank" }
    require(otherUserId.isNotBlank()) { "otherUserId must not be blank" }
    require(convName.isNotBlank()) { "convName must not be blank" }

    val conversationManager =
        ConversationManager(
            convRepo = ConversationRepositoryProvider.repository,
            overViewRepo = OverViewConvRepositoryProvider.repository)
    return conversationManager.createConvAndOverviews(
        creatorId = creatorId, otherUserId = otherUserId, convName = convName)
  }

  /**
   * Deletes a test conversation and its associated overview entries from Firestore.
   *
   * @param convId The ID of the conversation to delete
   */
  suspend fun deleteTestConversation(convId: String) {
    try {
      val conversationManager =
          ConversationManager(
              convRepo = ConversationRepositoryProvider.repository,
              overViewRepo = OverViewConvRepositoryProvider.repository)
      conversationManager.deleteConvAndOverviews(convId)
      Log.d(TAG, "Deleted test conversation: $convId")
    } catch (e: Exception) {
      Log.w(TAG, "Could not delete test conversation $convId: ${e.message}")
    }
  }

  /**
   * Creates a test profile for a user in Firestore. This simulates an existing user who has already
   * signed up.
   *
   * @param userId The Firebase user ID
   * @param email The user's email address
   * @param name The user's name
   * @param surname The user's surname (optional)
   */
  suspend fun createTestProfile(userId: String, email: String, name: String, surname: String = "") {
    val fullName = if (surname.isNotEmpty()) "$name $surname" else name

    val profile =
        Profile(
            userId = userId,
            name = fullName,
            email = email,
            levelOfEducation = "Bachelor",
            location = Location(latitude = 0.0, longitude = 0.0),
            hourlyRate = "25",
            description = "Test user for E2E testing",
            tutorRating = RatingInfo(averageRating = 5.0, totalRatings = 0),
            studentRating = RatingInfo(averageRating = 5.0, totalRatings = 0))

    ProfileRepositoryProvider.repository.addProfile(profile)
  }

  /**
   * Cleans up test data by deleting a user profile from Firestore.
   *
   * @param userId The Firebase user ID to clean up
   */
  suspend fun cleanupTestProfile(userId: String) {
    try {
      val firestore = FirebaseFirestore.getInstance()
      firestore.collection("profiles").document(userId).delete().await()
    } catch (_: Exception) {
      // Ignore cleanup errors
    }
  }

  /**
   * Cleans up a Firebase Auth user.
   *
   * @param user The FirebaseUser to delete
   */
  suspend fun cleanupFirebaseUser(user: FirebaseUser?) {
    try {
      user?.delete()?.await()
    } catch (_: Exception) {
      // Ignore cleanup errors
    }
  }

  /** Signs out the current user from Firebase Auth. */
  fun signOutCurrentUser() {
    FirebaseAuth.getInstance().signOut()
  }

  /**
   * Forces email verification for a test user using Cloud Functions. This function calls the
   * `forceVerifyTestUser` Cloud Function which uses Firebase Admin SDK to mark the user's email as
   * verified.
   *
   * Security: Only works with @example.test email addresses.
   *
   * @param email The email address of the test user (must end with @example.test)
   * @throws Exception if the Cloud Function call fails
   */
  suspend fun forceEmailVerification(email: String) {
    val functions = Firebase.functions(TestConfig.FUNCTIONS_REGION)
    // Ensure emulator usage (no-op if already set)
    functions.useEmulator(TestConfig.EMULATOR_HOST, TestConfig.FUNCTIONS_PORT)

    val data = mapOf("email" to email)
    val result = functions.getHttpsCallable("forceVerifyTestUser").call(data).await()

    @Suppress("UNCHECKED_CAST") val resultData = result.data as? Map<String, Any>
    val success = resultData?.get("success") as? Boolean ?: false
    val message = resultData?.get("message") as? String ?: "Unknown result"

    if (!success) {
      throw Exception("forceVerifyTestUser failed: $message")
    }

    // Reload current user (if present) to pick up verified flag
    FirebaseAuth.getInstance().currentUser?.reload()?.await()
  }

  /**
   * Waits for a semantic node to be displayed.
   *
   * @param composeTestRule The ComposeTestRule for the test
   * @param node The SemanticsNodeInteraction to wait for
   * @param timeoutMillis Maximum time to wait in milliseconds (default: 10 seconds)
   */
  @Suppress("unused")
  fun waitForNode(
      composeTestRule: ComposeTestRule,
      node: SemanticsNodeInteraction,
      timeoutMillis: Long = 10_000L
  ) {
    composeTestRule.waitUntil(timeoutMillis = timeoutMillis) {
      try {
        node.assertExists()
        true
      } catch (_: AssertionError) {
        false
      }
    }
  }

  /**
   * Waits for a Firestore document to exist by polling the database. This is useful in tests after
   * programmatic creation of profiles, listings, or bookings to avoid arbitrary sleep calls and
   * instead wait for actual data availability.
   *
   * @param collection The Firestore collection name
   * @param docId The document ID to wait for
   * @param timeoutMs Maximum time to wait in milliseconds (default: 5 seconds)
   * @param pollIntervalMs Interval between polling attempts in milliseconds (default: 200ms)
   * @throws kotlinx.coroutines.TimeoutCancellationException if document is not found within timeout
   */
  suspend fun waitForDocument(
      collection: String,
      docId: String,
      timeoutMs: Long = 5000L,
      pollIntervalMs: Long = 200L
  ) {
    withTimeout(timeoutMs) {
      val firestore = FirebaseFirestore.getInstance()
      while (true) {
        val snap = firestore.collection(collection).document(docId).get().await()
        if (snap.exists()) return@withTimeout
        delay(pollIntervalMs)
      }
    }
  }

  /**
   * Gets the target application context.
   *
   * @return The application context
   */
  @Suppress("unused") fun getContext() = InstrumentationRegistry.getInstrumentation().targetContext

  /**
   * Initializes Firebase emulators for testing. This is automatically done in TestRunner, but can
   * be called again if needed.
   */
  @Suppress("unused")
  fun initializeFirebaseEmulators() {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // Use emulators
    try {
      firestore.useEmulator(TestConfig.EMULATOR_HOST, TestConfig.FIRESTORE_PORT)
      auth.useEmulator(TestConfig.EMULATOR_HOST, TestConfig.AUTH_PORT)
    } catch (_: IllegalStateException) {
      // Already initialized, ignore
    }
  }

  /** Clears all Firestore data in the emulator. WARNING: Only use in test environments! */
  @Suppress("unused")
  suspend fun clearFirestoreData() {
    try {
      val firestore = FirebaseFirestore.getInstance()
      // Delete all profiles
      val profiles = firestore.collection("profiles").get().await()
      profiles.documents.forEach { it.reference.delete().await() }
    } catch (_: Exception) {
      // Ignore errors during cleanup
    }
  }

  /**
   * Creates a Google Sign-In test account and authenticates with Firebase. This creates a real
   * Firebase user that can be used for E2E testing.
   *
   * IMPORTANT: This function simulates a Google-authenticated user. Google users are automatically
   * email-verified, so we need to handle this.
   *
   * @param email The email for the test account
   * @param displayName The display name for the test account (not used in current implementation)
   * @return The authenticated FirebaseUser
   */
  suspend fun createAndAuthenticateGoogleUser(
      email: String,
      @Suppress("UNUSED_PARAMETER") displayName: String
  ): FirebaseUser {
    val auth = FirebaseAuth.getInstance()

    // Create a temporary email/password account that simulates a Google-signed-in user
    try {
      val result = auth.createUserWithEmailAndPassword(email, "TestPassword123!").await()
      val user = result.user!!

      // Google users are automatically verified, so we send a verification email
      // In the Firebase emulator, this marks the email as verified
      try {
        user.sendEmailVerification().await()
        // Reload to get the updated verification status
        user.reload().await()
      } catch (_: Exception) {
        // Ignore verification errors in emulator
      }

      return user
    } catch (_: Exception) {
      // If account already exists, sign in
      val result = auth.signInWithEmailAndPassword(email, "TestPassword123!").await()
      val user = result.user!!

      // Ensure email is verified
      if (!user.isEmailVerified) {
        try {
          user.sendEmailVerification().await()
          user.reload().await()
        } catch (_: Exception) {
          // Ignore verification errors
        }
      }

      return user
    }
  }
}
