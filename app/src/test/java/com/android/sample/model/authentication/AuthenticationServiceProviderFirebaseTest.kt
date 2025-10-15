package com.android.sample.model.authentication

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
class AuthenticationServiceProviderFirebaseTest {

  @get:Rule val firebaseRule = FirebaseTestRule()

  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    // Reset the provider before each test
    AuthenticationServiceProvider.resetAuthenticationService()
    // Disable test mode to use real Firebase (now properly initialized)
    AuthenticationServiceProvider.disableTestMode()
  }

  @After
  fun tearDown() {
    // Clean up after each test
    AuthenticationServiceProvider.resetAuthenticationService()
  }

  @Test
  fun getAuthenticationService_withFirebase_returnsSameInstance() {
    val service1 = AuthenticationServiceProvider.getAuthenticationService(context)
    val service2 = AuthenticationServiceProvider.getAuthenticationService(context)

    assertSame(service1, service2)
  }

  @Test
  fun getAuthenticationService_withFirebase_returnsNonNull() {
    val service = AuthenticationServiceProvider.getAuthenticationService(context)

    assertNotNull(service)
  }

  @Test
  fun resetAuthenticationService_withFirebase_clearsInstance() {
    val service1 = AuthenticationServiceProvider.getAuthenticationService(context)

    AuthenticationServiceProvider.resetAuthenticationService()
    val service2 = AuthenticationServiceProvider.getAuthenticationService(context)

    assertNotSame(service1, service2)
  }

  @Test
  fun isInTestMode_withFirebase_returnsFalse() {
    // Should be in production mode when using Firebase
    assertFalse(
        "Should not be in test mode when using Firebase",
        AuthenticationServiceProvider.isInTestMode())
  }
}
