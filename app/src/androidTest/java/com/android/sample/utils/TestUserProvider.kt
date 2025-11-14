// package com.android.sample.utils
//
// import android.content.Context
// import androidx.compose.runtime.Composable
// import androidx.test.core.app.ApplicationProvider
// import com.android.sample.model.authentication.AuthResult
// import com.android.sample.model.authentication.AuthenticationViewModel
// import com.android.sample.model.authentication.UserSessionManager
// import com.android.sample.MainApp
// import com.google.firebase.auth.FirebaseUser
// import io.mockk.every
// import io.mockk.mockk
// import kotlinx.coroutines.flow.MutableStateFlow
//
/// **
// * Utility class for creating fake authenticated users and launching the app in tests.
// */
// object TestUserProvider {
//
//    /**
//     * Creates a fake FirebaseUser with the given ID and email.
//     */
//    fun createFakeFirebaseUser(
//        userId: String = "test-user",
//        email: String = "test@example.com"
//    ): FirebaseUser {
//        val user = mockk<FirebaseUser>(relaxed = true)
//        every { user.uid } returns userId
//        every { user.email } returns email
//        return user
//    }
//
//    /**
//     * Creates an AuthenticationViewModel that is *already logged in*
//     * with a fake Firebase user.
//     */
//    fun createAuthenticatedViewModel(fakeUser: FirebaseUser): AuthenticationViewModel {
//        val context = ApplicationProvider.getApplicationContext<Context>()
//
//        val viewModel = AuthenticationViewModel(context)
//
//        // Replace internal authResult flow with a Success state
//        val privateField = AuthenticationViewModel::class.java.getDeclaredField("_authResult")
//        privateField.isAccessible = true
//        privateField.set(viewModel, MutableStateFlow<AuthResult>(AuthResult.Success(fakeUser)))
//
//        // Store user in session manager
//        UserSessionManager.setCurrentUser(fakeUser)
//
//        return viewModel
//    }
//
//    /**
//     * Returns a MainApp composable configured with a fake authenticated user.
//     */
//    @Composable
//    fun FakeLoggedInMainApp(
//        userId: String = "test-user",
//        email: String = "test@example.com"
//    ) {
//        val fakeUser = createFakeFirebaseUser(userId, email)
//        val authViewModel = createAuthenticatedViewModel(fakeUser)
//
//        MainApp(
//            authViewModel = authViewModel,
//            onGoogleSignIn = {}
//        )
//    }
// }
