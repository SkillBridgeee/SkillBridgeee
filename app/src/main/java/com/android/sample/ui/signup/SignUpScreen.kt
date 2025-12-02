package com.android.sample.ui.signup

import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
fun SignUpScreen(
    vm: SignUpViewModel,
    onSubmitSuccess: () -> Unit = {},
    onGoogleSignUpSuccess: () -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
  val state by vm.state.collectAsState()

  // Handle back button press for Google signup - sign out if not completed
  BackHandler(
      enabled = state.isGoogleSignUp && !state.submitSuccess && !state.verificationEmailSent) {
        vm.onSignUpAbandoned()
        onBackPressed()
      }

  // Navigate based on signup type
  LaunchedEffect(state.submitSuccess, state.verificationEmailSent, state.isGoogleSignUp) {
    when {
      state.submitSuccess && state.isGoogleSignUp -> onGoogleSignUpSuccess()
      state.verificationEmailSent -> onSubmitSuccess()
    }
  }

  val focusManager = LocalFocusManager.current
  val fieldStyle = rememberFieldStyle()
  val scrollState = rememberScrollState()

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
          SignUpHeader()

          PersonalInfoFields(state = state, vm = vm, fieldStyle = fieldStyle)

          LocationField(state = state, vm = vm, fieldStyle = fieldStyle)

          LevelAndDescriptionFields(state = state, vm = vm, fieldStyle = fieldStyle)

          PasswordSection(
              state = state, vm = vm, fieldStyle = fieldStyle, focusManager = focusManager)

          StatusMessages(state = state)

          SubmitButton(state = state, vm = vm)
        }

    VerticalScrollHint(
        visible = scrollState.value < scrollState.maxValue,
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp))
  }
}

/** Data class to hold field styling configuration */
data class FieldStyle(val shape: Shape, val colors: TextFieldColors)

/** Remember field style configuration */
@Composable
private fun rememberFieldStyle(): FieldStyle {
  val shape = RoundedCornerShape(14.dp)
  val colors =
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
  return FieldStyle(shape, colors)
}

@Composable
private fun SignUpHeader() {
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
}

@Composable
private fun PersonalInfoFields(state: SignUpUiState, vm: SignUpViewModel, fieldStyle: FieldStyle) {
  Box(modifier = Modifier.fillMaxWidth()) {
    EllipsizingTextField(
        value = state.name,
        onValueChange = { vm.onEvent(SignUpEvent.NameChanged(it)) },
        placeholder = "Enter your Name",
        modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.NAME),
        maxPreviewLength = 45,
        style =
            EllipsizingTextFieldStyle(
                shape = fieldStyle.shape as RoundedCornerShape, colors = fieldStyle.colors))
  }

  EllipsizingTextField(
      value = state.surname,
      onValueChange = { vm.onEvent(SignUpEvent.SurnameChanged(it)) },
      placeholder = "Enter your Surname",
      modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.SURNAME),
      maxPreviewLength = 45,
      style =
          EllipsizingTextFieldStyle(
              shape = fieldStyle.shape as RoundedCornerShape, colors = fieldStyle.colors))

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
      shape = fieldStyle.shape,
      colors = fieldStyle.colors,
      enabled = !state.isGoogleSignUp,
      readOnly = state.isGoogleSignUp)
}

@Composable
private fun LocationField(state: SignUpUiState, vm: SignUpViewModel, fieldStyle: FieldStyle) {
  val context = LocalContext.current
  val permission = android.Manifest.permission.ACCESS_FINE_LOCATION

  val permissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
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
        onLocationSelected = { vm.onEvent(SignUpEvent.LocationSelected(it)) },
        shape = fieldStyle.shape,
        colors = fieldStyle.colors)

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
}

@Composable
private fun LevelAndDescriptionFields(
    state: SignUpUiState,
    vm: SignUpViewModel,
    fieldStyle: FieldStyle
) {
  TextField(
      value = state.levelOfEducation,
      onValueChange = { vm.onEvent(SignUpEvent.LevelOfEducationChanged(it)) },
      modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION),
      placeholder = { Text("Major, Year (e.g. CS, 3rd year)") },
      singleLine = true,
      shape = fieldStyle.shape,
      colors = fieldStyle.colors)

  TextField(
      value = state.description,
      onValueChange = { vm.onEvent(SignUpEvent.DescriptionChanged(it)) },
      modifier =
          Modifier.fillMaxWidth().heightIn(min = 112.dp).testTag(SignUpScreenTestTags.DESCRIPTION),
      placeholder = { Text("Short description of yourself") },
      shape = fieldStyle.shape,
      colors = fieldStyle.colors)
}

@Composable
private fun PasswordSection(
    state: SignUpUiState,
    vm: SignUpViewModel,
    fieldStyle: FieldStyle,
    focusManager: FocusManager
) {
  if (!state.isGoogleSignUp) {
    TextField(
        value = state.password,
        onValueChange = { vm.onEvent(SignUpEvent.PasswordChanged(it)) },
        modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.PASSWORD),
        placeholder = { Text("Password") },
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        visualTransformation = PasswordVisualTransformation(),
        shape = fieldStyle.shape,
        colors = fieldStyle.colors,
        keyboardOptions =
            KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }))

    Spacer(Modifier.height(6.dp))

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
      val reqs = state.passwordRequirements
      RequirementItem(met = reqs.minLength, text = "At least 8 characters")
      RequirementItem(met = reqs.hasLetter, text = "Contains a letter")
      RequirementItem(met = reqs.hasDigit, text = "Contains a digit")
      RequirementItem(met = reqs.hasSpecial, text = "Contains a special character")
    }
  }
}

@Composable
private fun StatusMessages(state: SignUpUiState) {
  if (state.verificationEmailSent) {
    Spacer(Modifier.height(8.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TurquoisePrimary.copy(alpha = 0.1f))) {
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
}

@Composable
private fun SubmitButton(state: SignUpUiState, vm: SignUpViewModel) {
  Spacer(Modifier.height(6.dp))

  val gradient = Brush.horizontalGradient(listOf(TurquoiseStart, TurquoiseEnd))
  val disabledBrush = Brush.linearGradient(listOf(GrayE6, GrayE6))

  val enabled =
      if (state.isGoogleSignUp) {
        state.canSubmit && !state.submitting
      } else {
        state.canSubmit && state.passwordRequirements.allMet && !state.submitting
      }

  val buttonColors =
      ButtonDefaults.buttonColors(
          containerColor = Color.Transparent,
          contentColor = Color.White,
          disabledContainerColor = Color.Transparent,
          disabledContentColor = DisabledContent)

  Button(
      onClick = { vm.onEvent(SignUpEvent.Submit) },
      enabled = enabled,
      modifier =
          Modifier.fillMaxWidth()
              .height(52.dp)
              .clip(RoundedCornerShape(24.dp))
              .background(if (enabled) gradient else disabledBrush, RoundedCornerShape(24.dp))
              .testTag(SignUpScreenTestTags.SIGN_UP),
      colors = buttonColors,
      contentPadding = PaddingValues(0.dp)) {
        Text(
            if (state.submitting) "Submitting…" else "Sign Up",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold)
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
