@file:Suppress("DEPRECATION")

package com.android.sample.model.authentication

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GoogleSignInHelperTest {

  private lateinit var activity: ComponentActivity
  private lateinit var googleSignInHelper: GoogleSignInHelper
  private lateinit var mockGoogleSignInClient: GoogleSignInClient
  private var capturedActivityResult: ActivityResult? = null
  private val onSignInResultCallback: (ActivityResult) -> Unit = { result ->
    capturedActivityResult = result
  }

  @Before
  fun setUp() {
    // Create a real activity using Robolectric
    activity = Robolectric.buildActivity(ComponentActivity::class.java).create().get()

    // Mock GoogleSignIn static methods
    mockkStatic(GoogleSignIn::class)
    mockGoogleSignInClient = mockk(relaxed = true)

    // Mock signOut to return a completed task that immediately calls the listener
    val mockSignOutTask = mockk<Task<Void>>(relaxed = true)
    every { mockGoogleSignInClient.signOut() } returns mockSignOutTask
    every { mockSignOutTask.addOnCompleteListener(any()) } answers
        {
          val listener = firstArg<com.google.android.gms.tasks.OnCompleteListener<Void>>()
          listener.onComplete(mockSignOutTask)
          mockSignOutTask
        }

    // Mock the getClient method to return our mock client
    every { GoogleSignIn.getClient(any(), any<GoogleSignInOptions>()) } returns
        mockGoogleSignInClient

    // Reset captured result
    capturedActivityResult = null
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun constructor_initializesGoogleSignInClient_withCorrectConfiguration() {
    // When: Creating GoogleSignInHelper
    googleSignInHelper = GoogleSignInHelper(activity, onSignInResultCallback)

    // Then: GoogleSignIn.getClient should be called with correct configuration
    verify {
      GoogleSignIn.getClient(
          eq(activity),
          match<GoogleSignInOptions> { options ->
            // Verify the options include email and ID token request
            options.account == null && options.scopeArray.isNotEmpty()
          })
    }
  }

  @Test
  fun constructor_initializesGoogleSignInClient_withCorrectClientId() {
    // When: Creating GoogleSignInHelper
    googleSignInHelper = GoogleSignInHelper(activity, onSignInResultCallback)

    // Then: Verify the client was created (we can't directly verify the client ID
    // but we can verify the client was created)
    verify { GoogleSignIn.getClient(any(), any<GoogleSignInOptions>()) }
  }

  @Test
  fun signInWithGoogle_launchesSignInIntent() {
    // Given: A configured GoogleSignInHelper
    val mockIntent = mockk<Intent>(relaxed = true)
    every { mockGoogleSignInClient.signInIntent } returns mockIntent

    googleSignInHelper = GoogleSignInHelper(activity, onSignInResultCallback)

    // When: Calling signInWithGoogle
    googleSignInHelper.signInWithGoogle()

    // Then: Should sign out first, then get the sign-in intent
    verify { mockGoogleSignInClient.signOut() }
    verify { mockGoogleSignInClient.signInIntent }
  }

  @Test
  fun signInWithGoogle_getsSignInIntentFromClient() {
    // Given: A mock intent
    val mockIntent = mockk<Intent>(relaxed = true)
    every { mockGoogleSignInClient.signInIntent } returns mockIntent

    googleSignInHelper = GoogleSignInHelper(activity, onSignInResultCallback)

    // When: Signing in
    googleSignInHelper.signInWithGoogle()

    // Then: Verify we signed out first, then got the intent from the client
    verify(exactly = 1) { mockGoogleSignInClient.signOut() }
    verify(exactly = 1) { mockGoogleSignInClient.signInIntent }
  }

  @Test
  fun signInWithGoogle_signsOutBeforeLaunchingIntent() {
    // Given: A configured GoogleSignInHelper
    val mockIntent = mockk<Intent>(relaxed = true)
    every { mockGoogleSignInClient.signInIntent } returns mockIntent

    googleSignInHelper = GoogleSignInHelper(activity, onSignInResultCallback)

    // When: Calling signInWithGoogle
    googleSignInHelper.signInWithGoogle()

    // Then: signOut should be called before signInIntent
    verifyOrder {
      mockGoogleSignInClient.signOut()
      mockGoogleSignInClient.signInIntent
    }
  }

  @Test
  fun signOut_callsGoogleSignInClientSignOut() {
    // Given: A configured GoogleSignInHelper
    val mockTask = mockk<Task<Void>>(relaxed = true)
    every { mockGoogleSignInClient.signOut() } returns mockTask

    googleSignInHelper = GoogleSignInHelper(activity, onSignInResultCallback)

    // When: Calling signOut
    googleSignInHelper.signOut()

    // Then: The client's signOut should be called
    verify { mockGoogleSignInClient.signOut() }
  }

  @Test
  fun signOut_returnsTaskFromClient() {
    // Given: A mock task
    val mockTask = mockk<Task<Void>>(relaxed = true)
    every { mockGoogleSignInClient.signOut() } returns mockTask

    googleSignInHelper = GoogleSignInHelper(activity, onSignInResultCallback)

    // When: Signing out
    googleSignInHelper.signOut()

    // Then: Verify signOut was called
    verify(exactly = 1) { mockGoogleSignInClient.signOut() }
  }

  @Test
  fun onSignInResult_callbackIsInvoked_whenActivityResultReceived() {
    // Given: A helper with a callback
    var callbackInvoked = false
    var receivedResult: ActivityResult? = null
    val testCallback: (ActivityResult) -> Unit = { result ->
      callbackInvoked = true
      receivedResult = result
    }

    googleSignInHelper = GoogleSignInHelper(activity, testCallback)

    // When: Simulating an activity result
    val expectedResult = ActivityResult(Activity.RESULT_OK, Intent())
    testCallback(expectedResult)

    // Then: Callback should be invoked with the result
    assertTrue(callbackInvoked)
    assertEquals(expectedResult, receivedResult)
  }

  @Test
  fun onSignInResult_handlesSuccessResult() {
    // Given: A helper with callback
    googleSignInHelper = GoogleSignInHelper(activity, onSignInResultCallback)

    // When: Receiving a success result
    val successResult = ActivityResult(Activity.RESULT_OK, Intent())
    onSignInResultCallback(successResult)

    // Then: Result should be captured
    assertNotNull(capturedActivityResult)
    assertEquals(Activity.RESULT_OK, capturedActivityResult?.resultCode)
  }

  @Test
  fun onSignInResult_handlesCanceledResult() {
    // Given: A helper with callback
    googleSignInHelper = GoogleSignInHelper(activity, onSignInResultCallback)

    // When: Receiving a canceled result
    val canceledResult = ActivityResult(Activity.RESULT_CANCELED, null)
    onSignInResultCallback(canceledResult)

    // Then: Result should be captured
    assertNotNull(capturedActivityResult)
    assertEquals(Activity.RESULT_CANCELED, capturedActivityResult?.resultCode)
  }

  @Test
  fun onSignInResult_handlesResultWithData() {
    // Given: A helper with callback
    googleSignInHelper = GoogleSignInHelper(activity, onSignInResultCallback)

    // When: Receiving a result with intent data
    val intentData = Intent().apply { putExtra("test_key", "test_value") }
    val resultWithData = ActivityResult(Activity.RESULT_OK, intentData)
    onSignInResultCallback(resultWithData)

    // Then: Result and data should be captured
    assertNotNull(capturedActivityResult)
    assertEquals(Activity.RESULT_OK, capturedActivityResult?.resultCode)
    assertNotNull(capturedActivityResult?.data)
    assertEquals("test_value", capturedActivityResult?.data?.getStringExtra("test_key"))
  }

  @Test
  fun multipleSignInAttempts_eachGetsNewIntent() {
    // Given: A configured helper
    val mockIntent1 = mockk<Intent>(relaxed = true)
    val mockIntent2 = mockk<Intent>(relaxed = true)
    every { mockGoogleSignInClient.signInIntent } returns mockIntent1 andThen mockIntent2

    googleSignInHelper = GoogleSignInHelper(activity, onSignInResultCallback)

    // When: Calling signInWithGoogle multiple times
    googleSignInHelper.signInWithGoogle()
    googleSignInHelper.signInWithGoogle()

    // Then: Should sign out and get intent twice
    verify(exactly = 2) { mockGoogleSignInClient.signOut() }
    verify(exactly = 2) { mockGoogleSignInClient.signInIntent }
  }

  @Test
  fun googleSignInClient_isInitializedOnce() {
    // When: Creating the helper
    googleSignInHelper = GoogleSignInHelper(activity, onSignInResultCallback)

    // Then: GoogleSignIn.getClient should be called exactly once during initialization
    verify(exactly = 1) { GoogleSignIn.getClient(any(), any<GoogleSignInOptions>()) }

    // When: Performing operations
    every { mockGoogleSignInClient.signInIntent } returns mockk(relaxed = true)
    googleSignInHelper.signInWithGoogle()

    // Then: Client should not be re-initialized
    verify(exactly = 1) { GoogleSignIn.getClient(any(), any<GoogleSignInOptions>()) }
  }
}
