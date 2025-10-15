package com.android.sample.ui.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.sample.ui.theme.DisabledContent
import com.android.sample.ui.theme.FieldContainer
import com.android.sample.ui.theme.GrayE6
import com.android.sample.ui.theme.SampleAppTheme
import com.android.sample.ui.theme.TurquoiseEnd
import com.android.sample.ui.theme.TurquoisePrimary
import com.android.sample.ui.theme.TurquoiseStart

object SignUpScreenTestTags {
  const val TITLE = "SignUpScreenTestTags.TITLE"
  const val SUBTITLE = "SignUpScreenTestTags.SUBTITLE"
  const val LEARNER = "SignUpScreenTestTags.LEARNER"
  const val TUTOR = "SignUpScreenTestTags.TUTOR"
  const val NAME = "SignUpScreenTestTags.NAME"
  const val SURNAME = "SignUpScreenTestTags.SURNAME"
  const val ADDRESS = "SignUpScreenTestTags.ADDRESS"
  const val LEVEL_OF_EDUCATION = "SignUpScreenTestTags.LEVEL_OF_EDUCATION"
  const val DESCRIPTION = "SignUpScreenTestTags.DESCRIPTION"
  const val EMAIL = "SignUpScreenTestTags.EMAIL"
  const val PASSWORD = "SignUpScreenTestTags.PASSWORD"
  const val SIGN_UP = "SignUpScreenTestTags.SIGN_UP"
}

@Composable
fun SignUpScreen(vm: SignUpViewModel, onSubmitSuccess: () -> Unit = {}) {
  val state by vm.state.collectAsState()

  LaunchedEffect(state.submitSuccess) { if (state.submitSuccess) onSubmitSuccess() }

  val fieldShape = RoundedCornerShape(14.dp)
  val fieldColors =
      TextFieldDefaults.colors(
          focusedContainerColor = FieldContainer,
          unfocusedContainerColor = FieldContainer,
          disabledContainerColor = FieldContainer,
          focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
          unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
          disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
          cursorColor = MaterialTheme.colorScheme.primary,
          focusedTextColor = MaterialTheme.colorScheme.onSurface,
          unfocusedTextColor = MaterialTheme.colorScheme.onSurface)

  val scrollState = rememberScrollState()

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
            "Personal Informations",
            modifier = Modifier.testTag(SignUpScreenTestTags.SUBTITLE),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          FilterChip(
              selected = state.role == Role.LEARNER,
              onClick = { vm.onEvent(SignUpEvent.RoleChanged(Role.LEARNER)) },
              label = { Text("I’m a Learner") },
              modifier = Modifier.testTag(SignUpScreenTestTags.LEARNER),
              shape = RoundedCornerShape(20.dp))
          FilterChip(
              selected = state.role == Role.TUTOR,
              onClick = { vm.onEvent(SignUpEvent.RoleChanged(Role.TUTOR)) },
              label = { Text("I’m a Tutor") },
              modifier = Modifier.testTag(SignUpScreenTestTags.TUTOR),
              shape = RoundedCornerShape(20.dp))
        }

        TextField(
            value = state.name,
            onValueChange = { vm.onEvent(SignUpEvent.NameChanged(it)) },
            modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.NAME),
            placeholder = { Text("Enter your Name", fontWeight = FontWeight.Bold) },
            singleLine = true,
            shape = fieldShape,
            colors = fieldColors)

        TextField(
            value = state.surname,
            onValueChange = { vm.onEvent(SignUpEvent.SurnameChanged(it)) },
            modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.SURNAME),
            placeholder = { Text("Enter your Surname", fontWeight = FontWeight.Bold) },
            singleLine = true,
            shape = fieldShape,
            colors = fieldColors)

        TextField(
            value = state.address,
            onValueChange = { vm.onEvent(SignUpEvent.AddressChanged(it)) },
            modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.ADDRESS),
            placeholder = { Text("Address", fontWeight = FontWeight.Bold) },
            singleLine = true,
            shape = fieldShape,
            colors = fieldColors)

        TextField(
            value = state.levelOfEducation,
            onValueChange = { vm.onEvent(SignUpEvent.LevelOfEducationChanged(it)) },
            modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION),
            placeholder = { Text("Major, Year (e.g. CS, 3rd year)", fontWeight = FontWeight.Bold) },
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
            placeholder = { Text("Short description of yourself", fontWeight = FontWeight.Bold) },
            shape = fieldShape,
            colors = fieldColors)

        TextField(
            value = state.email,
            onValueChange = { vm.onEvent(SignUpEvent.EmailChanged(it)) },
            modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.EMAIL),
            placeholder = { Text("Email Address", fontWeight = FontWeight.Bold) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            shape = fieldShape,
            colors = fieldColors)

        TextField(
            value = state.password,
            onValueChange = { vm.onEvent(SignUpEvent.PasswordChanged(it)) },
            modifier = Modifier.fillMaxWidth().testTag(SignUpScreenTestTags.PASSWORD),
            placeholder = { Text("Password", fontWeight = FontWeight.Bold) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            shape = fieldShape,
            colors = fieldColors)

        Spacer(Modifier.height(6.dp))

        // Password requirement checklist computed from the entered password
        val pw = state.password
        val minLength = pw.length >= 8
        val hasLetter = pw.any { it.isLetter() }
        val hasDigit = pw.any { it.isDigit() }
        val hasSpecial = Regex("[^A-Za-z0-9]").containsMatchIn(pw)

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
          RequirementItem(met = minLength, text = "At least 8 characters")
          RequirementItem(met = hasLetter, text = "Contains a letter")
          RequirementItem(met = hasDigit, text = "Contains a digit")
          RequirementItem(met = hasSpecial, text = "Contains a special character")
        }

        val gradient = Brush.horizontalGradient(listOf(TurquoiseStart, TurquoiseEnd))
        val disabledBrush = Brush.linearGradient(listOf(GrayE6, GrayE6))
        // Require the ViewModel's passwordRequirements to be satisfied (includes special character)
        val enabled =
            state.canSubmit && minLength && hasLetter && hasDigit && hasSpecial && !state.submitting

        Button(
            onClick = { vm.onEvent(SignUpEvent.Submit) },
            enabled = enabled,
            modifier =
                Modifier.fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (enabled) gradient else disabledBrush, RoundedCornerShape(24.dp))
                    .testTag(SignUpScreenTestTags.SIGN_UP),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = DisabledContent,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = DisabledContent),
            contentPadding = PaddingValues(0.dp)) {
              Text(
                  if (state.submitting) "Submitting…" else "Sign Up",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold)
            }
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

@Preview(showBackground = true)
@Composable
private fun PreviewSignUpScreen() {
  SampleAppTheme { SignUpScreen(vm = SignUpViewModel()) }
}
