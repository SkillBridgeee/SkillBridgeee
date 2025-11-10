package com.android.sample.model.authentication

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("CONTEXT_RECEIVER_MEMBER_IS_DEPRECATED")
class AuthenticationViewModel(
    @Suppress("StaticFieldLeak") private val context: Context,
    private val repository: AuthenticationRepository = AuthenticationRepository(),
    private val credentialHelper: CredentialAuthHelper = CredentialAuthHelper(context),
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository
) : ViewModel() {

  companion object {
    private const val TAG = "AuthViewModel"
  }

  private val _uiState = MutableStateFlow(AuthenticationUiState())
  val uiState: StateFlow<AuthenticationUiState> = _uiState.asStateFlow()

  private val _authResult = MutableStateFlow<AuthResult?>(null)
  val authResult: StateFlow<AuthResult?> = _authResult.asStateFlow()

  // Firebase auth and listener to react to programmatic sign-ins (e.g. tests)
  private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
  private val authStateListener =
      FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        if (user == null) {
          _authResult.value = null
          return@AuthStateListener
        }

        viewModelScope.launch {
          val profile =
              try {
                profileRepository.getProfile(user.uid)
              } catch (_: Exception) {
                null
              }

          if (profile == null) {
            _authResult.value = AuthResult.RequiresSignUp(user.email ?: "", user)
            clearLoading()
          } else {
            _authResult.value = AuthResult.Success(user)
            clearLoading()
          }
        }
      }

  init {
    // register listener so late sign-ins are observed
    firebaseAuth.addAuthStateListener(authStateListener)

    // Optionally perform an immediate check in case currentUser is already present
    firebaseAuth.currentUser?.let { existingUser ->
      viewModelScope.launch {
        val profile =
            try {
              profileRepository.getProfile(existingUser.uid)
            } catch (_: Exception) {
              null
            }

        if (profile == null) {
          _authResult.value = AuthResult.RequiresSignUp(existingUser.email ?: "", existingUser)
          clearLoading()
        } else {
          _authResult.value = AuthResult.Success(existingUser)
          clearLoading()
        }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    firebaseAuth.removeAuthStateListener(authStateListener)
  }

  // --- rest of the class unchanged (helpers, signIn, handleGoogleSignInResult, etc.) ---
  private fun setLoading() {
    _uiState.update { it.copy(isLoading = true, error = null) }
  }

  private fun clearLoading() {
    _uiState.update { it.copy(isLoading = false, error = null) }
  }

  private fun setErrorState(errorMessage: String) {
    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
  }

  fun updateEmail(email: String) {
    _uiState.update { it.copy(email = email, error = null, message = null) }
  }

  fun updatePassword(password: String) {
    _uiState.update { it.copy(password = password, error = null, message = null) }
  }

  fun signIn() {
    val email = _uiState.value.email
    val password = _uiState.value.password

    if (email.isBlank() || password.isBlank()) {
      _uiState.update { it.copy(error = "Email and password cannot be empty") }
      return
    }

    setLoading()

    viewModelScope.launch {
      val result = repository.signInWithEmail(email, password)
      result.fold(
          onSuccess = { user ->
            _authResult.value = AuthResult.Success(user)
            clearLoading()
          },
          onFailure = { exception ->
            val errorMessage = exception.message ?: "Sign in failed"
            _authResult.value = AuthResult.Error(errorMessage)
            setErrorState(errorMessage)
          })
    }
  }

  @Suppress("DEPRECATION")
  fun handleGoogleSignInResult(result: ActivityResult) {
    setLoading()

    try {
      val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
      val account = task.getResult(ApiException::class.java)

      account.idToken?.let { idToken ->
        val firebaseCredential = credentialHelper.getFirebaseCredential(idToken)

        viewModelScope.launch {
          val authResult = repository.signInWithCredential(firebaseCredential)
          authResult.fold(
              onSuccess = { user ->
                val profile =
                    try {
                      profileRepository.getProfile(user.uid)
                    } catch (_: Exception) {
                      null
                    }

                if (profile == null) {
                  val email = user.email ?: account.email ?: ""
                  Log.d(
                      TAG,
                      "User needs sign up. Firebase email: ${user.email}, Google email: ${account.email}, Final email: $email")
                  _authResult.value = AuthResult.RequiresSignUp(email, user)
                  clearLoading()
                } else {
                  _authResult.value = AuthResult.Success(user)
                  clearLoading()
                }
              },
              onFailure = { exception ->
                val errorMessage = exception.message ?: "Google sign in failed"
                _authResult.value = AuthResult.Error(errorMessage)
                setErrorState(errorMessage)
              })
        }
      }
          ?: run {
            _authResult.value = AuthResult.Error("No ID token received")
            setErrorState("No ID token received")
          }
    } catch (e: ApiException) {
      val errorMessage = "Google sign in failed: ${e.message}"
      _authResult.value = AuthResult.Error(errorMessage)
      setErrorState(errorMessage)
    }
  }

  fun getGoogleSignInClient() = credentialHelper.getGoogleSignInClient()

  fun getSavedCredential() {
    setLoading()

    viewModelScope.launch {
      val result = credentialHelper.getPasswordCredential()
      result.fold(
          onSuccess = { passwordCredential ->
            _uiState.update {
              it.copy(
                  email = passwordCredential.id,
                  password = passwordCredential.password,
                  isLoading = false,
                  message = "Credential loaded")
            }
          },
          onFailure = { exception -> _uiState.update { it.copy(isLoading = false) } })
    }
  }

  fun signOut() {
    repository.signOut()
    credentialHelper.getGoogleSignInClient().signOut()
    _authResult.value = null
    _uiState.update { AuthenticationUiState() }
  }

  fun setError(message: String) {
    _uiState.update { it.copy(error = message, isLoading = false) }
  }

  fun showSuccessMessage(show: Boolean) {
    _uiState.update { it.copy(showSuccessMessage = show) }
  }
}
