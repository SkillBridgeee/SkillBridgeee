package com.android.sample.model.authentication

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/** Firebase implementation of AuthenticationRepository */
class FirebaseAuthenticationRepository(private val context: Context) : AuthenticationRepository {

  private val firebaseAuth = FirebaseAuth.getInstance()
  internal val googleSignInClient: GoogleSignInClient by lazy {
    val gso =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.android.sample.R.string.default_web_client_id))
            .requestEmail()
            .build()
    GoogleSignIn.getClient(context, gso)
  }

  override suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult {
    return try {
      val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
      val user = result.user
      if (user != null) {
        AuthResult.Success(
            AuthUser(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName,
                photoUrl = user.photoUrl?.toString()))
      } else {
        AuthResult.Error(Exception("Sign in failed: User is null"))
      }
    } catch (e: Exception) {
      AuthResult.Error(e)
    }
  }

  override suspend fun signUpWithEmailAndPassword(
      email: String,
      password: String,
      name: String
  ): AuthResult {
    return try {
      val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
      val user = result.user
      if (user != null) {
        // Update display name in Firebase Auth
        val profileUpdates =
            com.google.firebase.auth.UserProfileChangeRequest.Builder().setDisplayName(name).build()
        user.updateProfile(profileUpdates).await()

        AuthResult.Success(
            AuthUser(
                uid = user.uid,
                email = user.email,
                displayName = name,
                photoUrl = user.photoUrl?.toString()))
      } else {
        AuthResult.Error(Exception("Sign up failed: User is null"))
      }
    } catch (e: Exception) {
      AuthResult.Error(e)
    }
  }

  override suspend fun signInWithGoogle(): AuthResult {
    // For direct token-based sign-in, we need the token to be passed somehow
    // This is a limitation of the current interface design
    return AuthResult.Error(
        Exception("Use signInWithGoogleToken(idToken) instead for direct token-based sign-in"))
  }

  /**
   * Direct Google Sign-In with token (similar to old project approach) This bypasses the complex
   * GoogleSignInHelper flow
   */
  suspend fun signInWithGoogleToken(idToken: String): AuthResult {
    return try {
      val credential = GoogleAuthProvider.getCredential(idToken, null)
      val result = firebaseAuth.signInWithCredential(credential).await()
      val user = result.user
      if (user != null) {
        AuthResult.Success(
            AuthUser(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName,
                photoUrl = user.photoUrl?.toString()))
      } else {
        AuthResult.Error(Exception("Google sign in failed: User is null"))
      }
    } catch (e: Exception) {
      AuthResult.Error(e)
    }
  }

  /**
   * Handle Google Sign-In result (to be called from Activity/Fragment) This is essentially the same
   * as signInWithGoogleToken but kept for backward compatibility
   */
  suspend fun handleGoogleSignInResult(idToken: String): AuthResult {
    return signInWithGoogleToken(idToken)
  }

  override suspend fun signOut() {
    firebaseAuth.signOut()
    googleSignInClient.signOut().await()
  }

  override fun getCurrentUser(): AuthUser? {
    val user = firebaseAuth.currentUser
    return user?.let {
      AuthUser(
          uid = it.uid,
          email = it.email,
          displayName = it.displayName,
          photoUrl = it.photoUrl?.toString())
    }
  }

  override fun isUserSignedIn(): Boolean {
    return firebaseAuth.currentUser != null
  }

  override suspend fun sendPasswordResetEmail(email: String): Boolean {
    return try {
      firebaseAuth.sendPasswordResetEmail(email).await()
      true
    } catch (e: Exception) {
      false
    }
  }

  override suspend fun deleteAccount(): Boolean {
    return try {
      firebaseAuth.currentUser?.delete()?.await()
      true
    } catch (e: Exception) {
      false
    }
  }
}
