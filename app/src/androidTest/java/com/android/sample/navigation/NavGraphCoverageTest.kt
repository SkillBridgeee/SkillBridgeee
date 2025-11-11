@Before
fun setUp() {
  // Sign out first to ensure clean state
  try {
    Firebase.auth.signOut()
  } catch (_: Exception) {}

  // Configure emulators
  try {
    Firebase.firestore.useEmulator("10.0.2.2", 8080)
    Firebase.auth.useEmulator("10.0.2.2", 9099)
  } catch (_: IllegalStateException) {}

  // Initialize repositories
  val ctx = InstrumentationRegistry.getInstrumentation().targetContext
  try {
    ProfileRepositoryProvider.init(ctx)
    ListingRepositoryProvider.init(ctx)
    BookingRepositoryProvider.init(ctx)
    RatingRepositoryProvider.init(ctx)
  } catch (e: Exception) {
    e.printStackTrace()
  }

  RouteStackManager.clear()

  // Sign in with Google (this will trigger the signup flow)
  testEmail = "test.user.${System.currentTimeMillis()}@example.com"
  var userId: String? = null

  runBlocking {
    try {
      Log.d(TAG, "Signing in as Google user with email: $testEmail")
      TestAuthHelpers.signInAsGoogleUser(email = testEmail, displayName = "Test User")

      val currentUser = Firebase.auth.currentUser
      require(currentUser != null) { "User not signed in after signInAsGoogleUser" }
      userId = currentUser.uid
      Log.d(TAG, "User signed in: ${currentUser.uid}, email: ${currentUser.email}")
    } catch (e: Exception) {
      Log.e(TAG, "Sign in failed in setUp", e)
      throw e
    }
  }

  // Wait for navigation to signup screen
  composeTestRule.waitUntil(timeoutMillis = 10_000) {
    val route = RouteStackManager.getCurrentRoute()
    Log.d(TAG, "Waiting for SIGNUP, current route: $route")
    route?.startsWith(NavRoutes.SIGNUP_BASE) == true
  }

  // Complete signup through UI
  Log.d(TAG, "Completing signup through UI")
  TestUiHelpers.signUpThroughUi(
      composeTestRule = composeTestRule,
      password = "TestPassw0rd!",
      name = "Test",
      surname = "User",
      levelOfEducation = "CS, 3rd year",
      description = "Test user for navigation tests",
      addressQuery = "Test Location",
      timeoutMs = 15_000L)

  // Wait for signup to complete (check if profile exists in Firestore)
  runBlocking {
    try {
      Log.d(TAG, "Waiting for profile to be created")
      var profileCreated = false
      val startTime = System.currentTimeMillis()

      while (!profileCreated && System.currentTimeMillis() - startTime < 15_000) {
        try {
          val profile = ProfileRepositoryProvider.repository.getProfile(userId!!)
          if (profile != null) {
            Log.d(TAG, "Profile verified: ${profile.name}")
            profileCreated = true
          }
        } catch (_: Exception) {}

        if (!profileCreated) {
          delay(500)
        }
      }

      require(profileCreated) { "Profile not created after signup" }
    } catch (e: Exception) {
      Log.e(TAG, "Profile verification failed", e)
      throw e
    }
  }

  // Re-authenticate to trigger AuthStateListener
  runBlocking {
    try {
      Log.d(TAG, "Re-authenticating after signup")
      TestAuthHelpers.signInAsGoogleUser(email = testEmail, displayName = "Test User")

      val currentUser = Firebase.auth.currentUser
      require(currentUser != null) { "User not signed in after re-authentication" }
      Log.d(TAG, "Re-authenticated: ${currentUser.uid}")

      delay(1000) // Give AuthStateListener time to fire
    } catch (e: Exception) {
      Log.e(TAG, "Re-authentication failed", e)
      throw e
    }
  }

  // Wait for home screen
  waitForHome(timeoutMs = 15_000L)
}
