package com.android.sample.model.authentication

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GoogleSignInHelperTest {

  @get:Rule val firebaseRule = FirebaseTestRule()

  private val mockActivity = mockk<ComponentActivity>(relaxed = true)
  private val mockGoogleSignInClient = mockk<GoogleSignInClient>()
  private val mockLifecycleOwner = mockk<LifecycleOwner>()
  private val mockLifecycleCoroutineScope = mockk<LifecycleCoroutineScope>()

  private var capturedOnSignInResult: ((AuthResult) -> Unit)? = null
  private lateinit var googleSignInHelper: GoogleSignInHelper

  @Before
  fun setUp() {
    // Mock the FirebaseAuthenticationRepository constructor
    mockkConstructor(FirebaseAuthenticationRepository::class)
    every { anyConstructed<FirebaseAuthenticationRepository>().googleSignInClient } returns
        mockGoogleSignInClient

    // Mock activity lifecycle properly with Robolectric
    val lifecycleRegistry = LifecycleRegistry(mockLifecycleOwner)
    every { mockActivity.lifecycle } returns lifecycleRegistry

    // Mock lifecycleScope with the proper LifecycleCoroutineScope type
    every { mockActivity.lifecycleScope } returns mockLifecycleCoroutineScope

    // Set lifecycle state after all mocks are in place
    lifecycleRegistry.currentState = Lifecycle.State.RESUMED

    // Capture the onSignInResult callback
    googleSignInHelper =
        GoogleSignInHelper(mockActivity) { result -> capturedOnSignInResult?.invoke(result) }
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun constructor_initializesCorrectly() {
    assertNotNull(googleSignInHelper)
  }

  @Test
  fun signInWithGoogle_launchesSignInIntent() {
    val mockIntent = mockk<Intent>()
    every { mockGoogleSignInClient.signInIntent } returns mockIntent

    googleSignInHelper.signInWithGoogle()

    verify { mockGoogleSignInClient.signInIntent }
  }

  @Test
  fun signInWithGoogle_callsGoogleSignInClient() {
    val mockIntent = mockk<Intent>()
    every { mockGoogleSignInClient.signInIntent } returns mockIntent

    googleSignInHelper.signInWithGoogle()

    verify(exactly = 1) { mockGoogleSignInClient.signInIntent }
  }

  @Test
  fun helper_usesFirebaseAuthRepository() {
    // Verify that it accesses the googleSignInClient when signing in
    val mockIntent = mockk<Intent>()
    every { mockGoogleSignInClient.signInIntent } returns mockIntent

    googleSignInHelper.signInWithGoogle()

    verify { anyConstructed<FirebaseAuthenticationRepository>().googleSignInClient }
  }
}
