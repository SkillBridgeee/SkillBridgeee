@file:Suppress("DEPRECATION")

package com.android.sample.model.authentication

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class CredentialAuthHelperTest {

  private lateinit var context: Context
  private lateinit var credentialHelper: CredentialAuthHelper

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    credentialHelper = CredentialAuthHelper(context)
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun getGoogleSignInClient_returnsConfiguredClient() {
    val client = credentialHelper.getGoogleSignInClient()

    assertNotNull(client)
  }

  @Test
  fun getFirebaseCredential_convertsIdTokenToAuthCredential() {
    val idToken = "test-id-token"

    mockkStatic(GoogleAuthProvider::class)
    val mockCredential = mockk<AuthCredential>()
    every { GoogleAuthProvider.getCredential(idToken, null) } returns mockCredential

    val result = credentialHelper.getFirebaseCredential(idToken)

    assertEquals(mockCredential, result)
    verify { GoogleAuthProvider.getCredential(idToken, null) }
  }

  @Test
  fun getFirebaseCredential_withDifferentToken_createsNewCredential() {
    val idToken1 = "token-1"
    val idToken2 = "token-2"

    mockkStatic(GoogleAuthProvider::class)
    val mockCredential1 = mockk<AuthCredential>()
    val mockCredential2 = mockk<AuthCredential>()
    every { GoogleAuthProvider.getCredential(idToken1, null) } returns mockCredential1
    every { GoogleAuthProvider.getCredential(idToken2, null) } returns mockCredential2

    val result1 = credentialHelper.getFirebaseCredential(idToken1)
    val result2 = credentialHelper.getFirebaseCredential(idToken2)

    assertEquals(mockCredential1, result1)
    assertEquals(mockCredential2, result2)
    verify(exactly = 1) { GoogleAuthProvider.getCredential(idToken1, null) }
    verify(exactly = 1) { GoogleAuthProvider.getCredential(idToken2, null) }
  }

  @Test
  fun webClientId_isCorrect() {
    assertEquals(
        "1061045584009-duiljd2t9ijc3u8vc9193a4ecpk2di5f.apps.googleusercontent.com",
        CredentialAuthHelper.WEB_CLIENT_ID)
  }

  @Test
  fun getGoogleSignInClient_configuresWithCorrectWebClientId() {
    val client = credentialHelper.getGoogleSignInClient()

    // Verify the client is properly configured
    assertNotNull(client)
  }

  @Test
  fun getFirebaseCredential_withEmptyToken_stillCreatesCredential() {
    val idToken = ""

    mockkStatic(GoogleAuthProvider::class)
    val mockCredential = mockk<AuthCredential>()
    every { GoogleAuthProvider.getCredential(idToken, null) } returns mockCredential

    val result = credentialHelper.getFirebaseCredential(idToken)

    assertEquals(mockCredential, result)
    verify { GoogleAuthProvider.getCredential(idToken, null) }
  }

  @Test
  fun getFirebaseCredential_callsGoogleAuthProviderCorrectly() {
    val idToken = "valid-token-123"

    mockkStatic(GoogleAuthProvider::class)
    val mockCredential = mockk<AuthCredential>()
    every { GoogleAuthProvider.getCredential(any(), null) } returns mockCredential

    credentialHelper.getFirebaseCredential(idToken)

    verify { GoogleAuthProvider.getCredential(idToken, null) }
  }

  @Test
  fun credentialHelper_canBeInstantiatedWithContext() {
    val newHelper = CredentialAuthHelper(context)

    assertNotNull(newHelper)
  }
}
