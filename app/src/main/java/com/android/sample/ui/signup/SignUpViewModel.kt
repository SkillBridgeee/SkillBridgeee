package com.android.sample.ui.signup

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.HttpClientProvider
import com.android.sample.model.authentication.AuthenticationRepository
import com.android.sample.model.map.GpsLocationProvider
import com.android.sample.model.map.Location
import com.android.sample.model.map.LocationRepository
import com.android.sample.model.map.NominatimLocationRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Holds the state of individual password requirements. */
data class PasswordRequirements(
    val minLength: Boolean = false,
    val hasLetter: Boolean = false,
    val hasDigit: Boolean = false,
    val hasSpecial: Boolean = false
) {
  /** Returns true if all requirements are met */
  val allMet: Boolean
    get() = minLength && hasLetter && hasDigit && hasSpecial
}

data class SignUpUiState(
    val name: String = "",
    val surname: String = "",
    val address: String = "",
    val selectedLocation: Location? = null,
    val locationQuery: String = "",
    val locationSuggestions: List<Location> = emptyList(),
    val levelOfEducation: String = "",
    val description: String = "",
    val email: String = "",
    val password: String = "",
    val submitting: Boolean = false,
    val error: String? = null,
    val canSubmit: Boolean = false,
    val submitSuccess: Boolean = false,
    val verificationEmailSent: Boolean = false, // True when verification email has been sent
    val isGoogleSignUp: Boolean = false, // True if user is already authenticated via Google
    val passwordRequirements: PasswordRequirements = PasswordRequirements(),
    val isNavigating: Boolean = false, // True when navigating away after successful submit
    val isToSAccepted: Boolean = false // True when user has accepted Terms of Service
)

sealed interface SignUpEvent {

  data class NameChanged(val value: String) : SignUpEvent

  data class SurnameChanged(val value: String) : SignUpEvent

  data class AddressChanged(val value: String) : SignUpEvent

  data class LocationQueryChanged(val value: String) : SignUpEvent

  data class LocationSelected(val location: Location) : SignUpEvent

  data class LevelOfEducationChanged(val value: String) : SignUpEvent

  data class DescriptionChanged(val value: String) : SignUpEvent

  data class EmailChanged(val value: String) : SignUpEvent

  data class PasswordChanged(val value: String) : SignUpEvent

  data class ToSAcceptedChanged(val accepted: Boolean) : SignUpEvent

  object Submit : SignUpEvent
}

class SignUpViewModel(
    initialEmail: String? = null,
    private val authRepository: AuthenticationRepository = AuthenticationRepository(),
    private val signUpUseCase: SignUpUseCase =
        SignUpUseCase(AuthenticationRepository(), ProfileRepositoryProvider.repository),
    private val locationRepository: LocationRepository =
        NominatimLocationRepository(HttpClientProvider.client)
) : ViewModel() {

  companion object {
    private const val TAG = "SignUpViewModel"
    private const val GPS_FAILED_MSG = "Failed to obtain GPS location"
    private const val LOCATION_PERMISSION_DENIED_MSG = "Location permission denied"
    private const val LOCATION_DISABLED_MSG =
        "Location services are disabled. Please enable location in your device settings."
  }

  private val _state = MutableStateFlow(SignUpUiState())
  val state: StateFlow<SignUpUiState> = _state

  private var locationSearchJob: Job? = null
  private val locationSearchDelayTime: Long = 1000

  /**
   * Validates password and returns individual requirement states. Extracted to a helper function to
   * avoid duplication between UI and validation logic.
   */
  private fun validatePassword(password: String): PasswordRequirements {
    return PasswordRequirements(
        minLength = password.length >= 8,
        hasLetter = password.any { it.isLetter() },
        hasDigit = password.any { it.isDigit() },
        hasSpecial = Regex("[^A-Za-z0-9]").containsMatchIn(password))
  }

  init {
    // Check if user is already authenticated (Google Sign-In) and pre-fill email
    if (!initialEmail.isNullOrBlank()) {
      val isAuthenticated = authRepository.getCurrentUser() != null
      Log.d(TAG, "Init - Email: $initialEmail, User authenticated: $isAuthenticated")
      _state.update { it.copy(email = initialEmail, isGoogleSignUp = isAuthenticated) }
      validate()
    }
  }

  /** Called when user navigates away from signup without completing */
  fun onSignUpAbandoned() {
    // If this was a Google sign-up (user is authenticated but no profile was created)
    // sign them out so they go through the flow again next time
    val state = _state.value

    // Only sign out if:
    // 1. It's a Google sign-up (user is already authenticated)
    // 2. Sign-up was NOT successful (profile wasn't created)
    if (state.isGoogleSignUp && !state.submitSuccess) {
      Log.d(TAG, "Sign-up abandoned without completion - signing out Google user")
      authRepository.signOut()
    }
  }

  fun onEvent(e: SignUpEvent) {
    when (e) {
      is SignUpEvent.NameChanged -> _state.update { it.copy(name = e.value) }
      is SignUpEvent.SurnameChanged -> _state.update { it.copy(surname = e.value) }
      is SignUpEvent.AddressChanged -> _state.update { it.copy(address = e.value) }
      is SignUpEvent.LocationQueryChanged -> setLocationQuery(e.value)
      is SignUpEvent.LocationSelected -> setLocation(e.location)
      is SignUpEvent.LevelOfEducationChanged ->
          _state.update { it.copy(levelOfEducation = e.value) }
      is SignUpEvent.DescriptionChanged -> _state.update { it.copy(description = e.value) }
      is SignUpEvent.EmailChanged -> {
        // Don't allow email changes for Google sign-ups
        if (!_state.value.isGoogleSignUp) {
          _state.update { it.copy(email = e.value) }
        }
      }
      is SignUpEvent.PasswordChanged -> _state.update { it.copy(password = e.value) }
      is SignUpEvent.ToSAcceptedChanged -> _state.update { it.copy(isToSAccepted = e.accepted) }
      SignUpEvent.Submit -> submit()
    }
    validate()
  }

  private fun validate() {
    val namePattern = Regex("^[\\p{L} ]+\$") // Unicode letters and spaces only

    _state.update { s ->
      val nameTrim = s.name.trim()
      val surnameTrim = s.surname.trim()
      val nameOk = nameTrim.isNotEmpty() && namePattern.matches(nameTrim)
      val surnameOk = surnameTrim.isNotEmpty() && namePattern.matches(surnameTrim)

      val emailTrim = s.email.trim()
      val emailOk = run {
        // require exactly one '@', non-empty local and domain, and at least one dot in domain
        val atCount = emailTrim.count { it == '@' }
        if (atCount != 1) return@run false
        val (local, domain) = emailTrim.split("@", limit = 2)
        local.isNotEmpty() && domain.isNotEmpty() && domain.contains('.')
      }

      // Validate password and get requirements
      val passwordReqs = validatePassword(s.password)

      // Check if user is already authenticated (e.g., Google Sign-In)
      val isAuthenticated = authRepository.getCurrentUser() != null
      val passwordOk =
          if (isAuthenticated) {
            // Password not required for already authenticated users
            true
          } else {
            // All password requirements must be met for new sign-ups
            passwordReqs.allMet
          }

      val levelOk = s.levelOfEducation.trim().isNotEmpty()
      val ok = nameOk && surnameOk && emailOk && passwordOk && levelOk
      s.copy(canSubmit = ok, error = null, passwordRequirements = passwordReqs)
    }
  }

  /**
   * Fetches the current location using GPS and updates the UI state.
   *
   * @param provider The GPS location provider to use for fetching the location.
   * @param context The Android context used for geocoding.
   */
  @Suppress("DEPRECATION")
  fun fetchLocationFromGps(provider: GpsLocationProvider, context: Context) {
    viewModelScope.launch {
      // Check if location services are enabled
      if (!provider.isLocationEnabled()) {
        _state.update { it.copy(error = LOCATION_DISABLED_MSG) }
        return@launch
      }

      try {
        val androidLoc = provider.getCurrentLocation()
        if (androidLoc != null) {
          val geocoder = Geocoder(context, Locale.getDefault())
          val addresses: List<Address> =
              geocoder.getFromLocation(androidLoc.latitude, androidLoc.longitude, 1)?.toList()
                  ?: emptyList()

          val addressText =
              if (addresses.isNotEmpty()) {
                val address = addresses[0]
                listOfNotNull(address.locality, address.adminArea, address.countryName)
                    .joinToString(", ")
              } else {
                "${androidLoc.latitude}, ${androidLoc.longitude}"
              }

          val mapLocation =
              Location(
                  latitude = androidLoc.latitude,
                  longitude = androidLoc.longitude,
                  name = addressText)

          _state.update {
            it.copy(
                selectedLocation = mapLocation,
                locationQuery = addressText,
                address = addressText,
                error = null)
          }
        } else {
          _state.update { it.copy(error = GPS_FAILED_MSG) }
        }
      } catch (_: SecurityException) {
        _state.update { it.copy(error = LOCATION_PERMISSION_DENIED_MSG) }
      } catch (_: Exception) {
        _state.update { it.copy(error = GPS_FAILED_MSG) }
      }
    }
  }

  /** Handles the scenario when location permission is denied by the user. */
  fun onLocationPermissionDenied() {
    _state.update { it.copy(error = LOCATION_PERMISSION_DENIED_MSG) }
  }

  private fun submit() {
    // Early return if form validation fails or already submitting
    if (!_state.value.canSubmit || _state.value.submitting) {
      Log.d(
          TAG,
          "Submit blocked - canSubmit: ${_state.value.canSubmit}, submitting: ${_state.value.submitting}")
      return
    }

    Log.d(TAG, "Starting sign-up submission")
    viewModelScope.launch {
      _state.update {
        it.copy(
            submitting = true, error = null, submitSuccess = false, verificationEmailSent = false)
      }
      val current = _state.value

      // Create request object from current state
      val selectedLoc = current.selectedLocation
      val request =
          SignUpRequest(
              name = current.name,
              surname = current.surname,
              email = current.email,
              password = current.password,
              levelOfEducation = current.levelOfEducation,
              description = current.description,
              address = current.address,
              location = selectedLoc)

      // Execute sign-up through use case
      Log.d(TAG, "Executing sign-up use case")
      val result = signUpUseCase.execute(request)

      // Update UI state based on result
      when (result) {
        is SignUpResult.Success -> {
          // Success for Google Sign-In users who already have auth
          Log.d(TAG, "Sign-up SUCCESS - setting submitSuccess=true, isNavigating=true")
          _state.update {
            it.copy(
                submitting = false,
                submitSuccess = true,
                verificationEmailSent = false,
                isNavigating = true)
          }
          Log.d(
              TAG,
              "State updated - submitSuccess: ${_state.value.submitSuccess}, isNavigating: ${_state.value.isNavigating}")
        }
        is SignUpResult.VerificationEmailSent -> {
          // Verification email sent - show message to check email
          Log.d(TAG, "Verification email SENT - setting verificationEmailSent=true")
          _state.update {
            it.copy(
                submitting = false,
                submitSuccess = false,
                verificationEmailSent = true,
                isNavigating = true)
          }
        }
        is SignUpResult.Error -> {
          Log.e(TAG, "Sign-up ERROR: ${result.message}")
          _state.update {
            it.copy(submitting = false, error = result.message, verificationEmailSent = false)
          }
        }
      }
    }
  }

  /**
   * Updates the location query in the UI state and fetches matching location suggestions.
   *
   * This function updates the current `locationQuery` value and triggers a search operation if the
   * query is not empty. The search is performed asynchronously within the `viewModelScope` using
   * the [locationRepository].
   *
   * @param query The new location search query entered by the user.
   */
  private fun setLocationQuery(query: String) {
    _state.update { it.copy(locationQuery = query, address = query) }

    locationSearchJob?.cancel()

    if (query.isNotEmpty()) {
      locationSearchJob =
          viewModelScope.launch {
            delay(locationSearchDelayTime)
            try {
              val results = locationRepository.search(query)
              _state.update { it.copy(locationSuggestions = results) }
            } catch (_: Exception) {
              _state.update { it.copy(locationSuggestions = emptyList()) }
            }
          }
    } else {
      _state.update { it.copy(locationSuggestions = emptyList(), selectedLocation = null) }
    }
  }

  /**
   * Updates the selected location and the locationQuery.
   *
   * @param location The selected location object.
   */
  private fun setLocation(location: Location) {
    _state.update {
      it.copy(selectedLocation = location, locationQuery = location.name, address = location.name)
    }
  }
}
