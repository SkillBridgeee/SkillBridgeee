package com.android.sample.ui.signup

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.android.sample.model.map.GpsLocationProvider
import com.android.sample.ui.components.EllipsizingTextField
import com.android.sample.ui.components.EllipsizingTextFieldStyle
import com.android.sample.ui.components.RoundEdgedLocationInputField
import com.android.sample.ui.components.VerticalScrollHint
import com.android.sample.ui.theme.DisabledContent
import com.android.sample.ui.theme.FieldContainer
import com.android.sample.ui.theme.GrayE6
import com.android.sample.ui.theme.TurquoiseEnd
import com.android.sample.ui.theme.TurquoisePrimary
import com.android.sample.ui.theme.TurquoiseStart

object SignUpScreenTestTags {
  const val TITLE = "SignUpScreenTestTags.TITLE"
  const val SUBTITLE = "SignUpScreenTestTags.SUBTITLE"
  const val NAME = "SignUpScreenTestTags.NAME"
  const val SURNAME = "SignUpScreenTestTags.SURNAME"
  const val ADDRESS = "SignUpScreenTestTags.ADDRESS"
  const val LEVEL_OF_EDUCATION = "SignUpScreenTestTags.LEVEL_OF_EDUCATION"
  const val DESCRIPTION = "SignUpScreenTestTags.DESCRIPTION"
  const val EMAIL = "SignUpScreenTestTags.EMAIL"
  const val PASSWORD = "SignUpScreenTestTags.PASSWORD"
  const val SIGN_UP = "SignUpScreenTestTags.SIGN_UP"

  const val PIN_CONTENT_DESC = "Use my location"
}

@Composable
fun SignUpScreen(vm: SignUpViewModel, onSubmitSuccess: () -> Unit = {}, onNavigateToToS: () -> Unit) {
  val state by vm.state.collectAsState()

  // Navigate on success (Google Sign-In) or when verification email is sent (Email/Password)
  LaunchedEffect(state.submitSuccess, state.verificationEmailSent) {
    if (state.submitSuccess || state.verificationEmailSent) {
      onSubmitSuccess()
    }
  }

  // Clean up if user navigates away without completing signup
  DisposableEffect(Unit) { onDispose { vm.onSignUpAbandoned() } }

  val focusManager = LocalFocusManager.current

  val fieldShape = RoundedCornerShape(14.dp)
  val fieldColors =
      TextFieldDefaults.colors(
          focusedContainerColor = FieldContainer,
          unfocusedContainerColor = FieldContainer,
          disabledContainerColor = FieldContainer,
          focusedIndicatorColor = Color.Transparent,
          unfocusedIndicatorColor = Color.Transparent,
          disabledIndicatorColor = Color.Transparent,
          cursorColor = MaterialTheme.colorScheme.primary,
          focusedTextColor = MaterialTheme.colorScheme.onSurface,
          unfocusedTextColor = MaterialTheme.colorScheme.onSurface)

  val scrollState = rememberScrollState()
  var isToSChecked by remember { mutableStateOf(false) }

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text(
              "SkillBridge",
              modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.TITLE),
              textAlign = TextAlign.Center,
              style =
                  MaterialTheme.typography.headlineLarge.copy(
                      fontWeight = FontWeight.ExtraBold, color = TurquoisePrimary))

          Text(
              "Personal Information",
              modifier = Modifier.testTag(SignUpScreenTestTags.SUBTITLE),
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

          Box(modifier = Modifier.fillMaxWidth()) {
            EllipsizingTextField(
                value = state.name,
                onValueChange = { vm.onEvent(SignUpEvent.NameChanged(it)) },
                placeholder = "Enter your Name",
                modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.NAME),
                maxPreviewLength = 45,
                style =
                    EllipsizingTextFieldStyle(
                        shape = fieldShape, colors = fieldColors
                        // keyboardOptions = ... // not needed for name
                        ))
          }

          EllipsizingTextField(
              value = state.surname,
              onValueChange = { vm.onEvent(SignUpEvent.SurnameChanged(it)) },
              placeholder = "Enter your Surname",
              modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.SURNAME),
              maxPreviewLength = 45,
              style = EllipsizingTextFieldStyle(shape = fieldShape, colors = fieldColors))

          TextField(
              value = state.email,
              onValueChange = {
                if (!state.isGoogleSignUp) {
                  vm.onEvent(SignUpEvent.EmailChanged(it))
                }
              },
              modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.EMAIL),
              placeholder = { Text("Email Address") },
              singleLine = true,
              shape = fieldShape,
              colors = fieldColors,
              enabled = !state.isGoogleSignUp, // Disable email field if pre-filled from Google
              readOnly = state.isGoogleSignUp) // Make it read-only for Google sign-ups

          // Location input with Nominatim search and dropdown
          val context = LocalContext.current
          val permission = android.Manifest.permission.ACCESS_FINE_LOCATION

          val permissionLauncher =
              rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                  granted ->
                if (granted) {
                  vm.fetchLocationFromGps(GpsLocationProvider(context), context)
                } else {
                  vm.onLocationPermissionDenied()
                }
              }

          Box(modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.ADDRESS)) {
            RoundEdgedLocationInputField(
                locationQuery = state.locationQuery,
                locationSuggestions = state.locationSuggestions,
                onLocationQueryChange = { vm.onEvent(SignUpEvent.LocationQueryChanged(it)) },
                onLocationSelected = { location ->
                  vm.onEvent(SignUpEvent.LocationSelected(location))
                },
                shape = fieldShape,
                colors = fieldColors)

            IconButton(
                onClick = {
                  val granted =
                      ContextCompat.checkSelfPermission(context, permission) ==
                          PackageManager.PERMISSION_GRANTED
                  if (granted) {
                    vm.fetchLocationFromGps(GpsLocationProvider(context), context)
                  } else {
                    permissionLauncher.launch(permission)
                  }
                },
                modifier = Modifier.align(Alignment.CenterEnd).size(36.dp)) {
                  Icon(
                      imageVector = Icons.Filled.MyLocation,
                      contentDescription = SignUpScreenTestTags.PIN_CONTENT_DESC,
                      tint = MaterialTheme.colorScheme.primary)
                }
          }

          TextField(
              value = state.levelOfEducation,
              onValueChange = { vm.onEvent(SignUpEvent.LevelOfEducationChanged(it)) },
              modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION),
              placeholder = { Text("Major, Year (e.g. CS, 3rd year)") },
              singleLine = true,
              shape = fieldShape,
              colors = fieldColors)

          TextField(
              value = state.description,
              onValueChange = { vm.onEvent(SignUpEvent.DescriptionChanged(it)) },
              modifier =
                  Modifier.fillMaxWidth()
                      .heightIn(min = 112.dp)
                      .testTag(SignUpScreenTestTags.DESCRIPTION),
              placeholder = { Text("Short description of yourself") },
              shape = fieldShape,
              colors = fieldColors)

          // Only show password field if user is not signing up via Google
          if (!state.isGoogleSignUp) {
            TextField(
                value = state.password,
                onValueChange = { vm.onEvent(SignUpEvent.PasswordChanged(it)) },
                modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.PASSWORD),
                placeholder = { Text("Password") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                shape = fieldShape,
                colors = fieldColors,
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }))

            Spacer(Modifier.height(6.dp))

            // Password requirement checklist from ViewModel state
            val reqs = state.passwordRequirements

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
              RequirementItem(met = reqs.minLength, text = "At least 8 characters")
              RequirementItem(met = reqs.hasLetter, text = "Contains a letter")
              RequirementItem(met = reqs.hasDigit, text = "Contains a digit")
              RequirementItem(met = reqs.hasSpecial, text = "Contains a special character")
            }
          }

          // Display verification email sent message or error message if present
          if (state.verificationEmailSent) {
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(containerColor = TurquoisePrimary.copy(alpha = 0.1f))) {
                  Column(
                      modifier = Modifier.padding(16.dp),
                      verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "✓ Verification Email Sent!",
                            color = TurquoisePrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Text(
                            text =
                                "Please check your inbox at ${state.email} and click the verification link. After verifying, you can log in.",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium)
                      }
                }
          } else {
            state.error?.let { errorMessage ->
              Spacer(Modifier.height(8.dp))
              Text(
                  text = errorMessage,
                  color = MaterialTheme.colorScheme.error,
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp))
            }
          }

          Spacer(Modifier.height(6.dp))

          val gradient = Brush.horizontalGradient(listOf(TurquoiseStart, TurquoiseEnd))
          val disabledBrush = Brush.linearGradient(listOf(GrayE6, GrayE6))

          // For Google sign-up, password requirements don't apply
          val enabled =
              if (state.isGoogleSignUp) {
                state.canSubmit && !state.submitting
              } else {
                // Use passwordRequirements from ViewModel state
                state.canSubmit && state.passwordRequirements.allMet && !state.submitting
              }

          val buttonColors =
              ButtonDefaults.buttonColors(
                  containerColor = Color.Transparent,
                  contentColor = Color.White, // <-- white text when enabled
                  disabledContainerColor = Color.Transparent,
                  disabledContentColor = DisabledContent // <-- gray text when disabled
                  )

          Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(top = 8.dp)) {
            Checkbox(
                checked = isToSChecked,
                onCheckedChange = { isToSChecked = it },
                colors = CheckboxDefaults.colors(checkedColor = TurquoisePrimary))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "I have read and accept the ",
                style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Terms of Service",
                style = MaterialTheme.typography.bodyMedium.copy(color = TurquoisePrimary),
                modifier = Modifier.clickable { onNavigateToToS() })
          }

          Button(
              onClick = { vm.onEvent(SignUpEvent.Submit) },
              enabled = isToSChecked,
              modifier =
                  Modifier.fillMaxWidth()
                      .height(52.dp)
                      .clip(RoundedCornerShape(24.dp))
                      .background(
                          if (enabled) gradient else disabledBrush, RoundedCornerShape(24.dp))
                      .testTag(SignUpScreenTestTags.SIGN_UP),
              colors = buttonColors,
              contentPadding = PaddingValues(0.dp)) {
                Text(
                    if (state.submitting) "Submitting…" else "Sign Up",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
              }
        }
    // True if can scroll down further or false if at bottom of the page
    val showHint by remember { derivedStateOf { scrollState.value < scrollState.maxValue } }

    VerticalScrollHint(
        visible = showHint,
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp))
  }
}

@Composable
private fun RequirementItem(met: Boolean, text: String) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.CenterVertically) {
        val tint = if (met) MaterialTheme.colorScheme.primary else DisabledContent
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (met) MaterialTheme.colorScheme.onSurface else DisabledContent)
      }
}
