package com.android.sample.utils

import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.listing.ListingRepository
import com.github.se.bootcamp.utils.FirebaseEmulator
import com.google.firebase.FirebaseApp
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
abstract class RepositoryTest {

  // The repository is now a lateinit var, to be initialized by subclasses.
  protected lateinit var bookingRepository: BookingRepository
  protected lateinit var listingRepository: ListingRepository
  protected var testUserId = "test-user-id"

  @Before
  open fun setUp() {
    val appContext = RuntimeEnvironment.getApplication()
    if (FirebaseApp.getApps(appContext).isEmpty()) {
      FirebaseApp.initializeApp(appContext)
    }

    // Connect to emulators only after FirebaseApp is ready
    FirebaseEmulator.connect()

    // The repository will be set for the provider in the subclass's setUp method
  }

  @After
  open fun tearDown() {
    if (FirebaseEmulator.isRunning) {
      FirebaseEmulator.auth.signOut()
    }
  }
}
