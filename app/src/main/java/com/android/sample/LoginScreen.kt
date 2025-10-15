package com.android.sample

import androidx.activity.ComponentActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.sample.model.authentication.*

object SignInScreenTestTags {
  const val TITLE = "title"
  const val ROLE_LEARNER = "roleLearner"
  const val EMAIL_INPUT = "emailInput"
  const val PASSWORD_INPUT = "passwordInput"
  const val SIGN_IN_BUTTON = "signInButton"
  const val AUTH_GOOGLE = "authGoogle"
  const val SIGNUP_LINK = "signUpLink"
  const val AUTH_GITHUB = "authGitHub"
  const val FORGOT_PASSWORD = "forgotPassword"
  const val AUTH_SECTION = "authSection"
  const val ROLE_TUTOR = "roleTutor"
  const val SUBTITLE = "subtitle"
}

@Composable
fun LoginScreen(viewModel: AuthenticationViewModel, onGoogleSignIn: () -> Unit = {}) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val authResult by viewModel.authResult.collectAsStateWithLifecycle()

  // Handle authentication results
  LaunchedEffect(authResult) {
    when (authResult) {
      is AuthResult.Success -> {
        viewModel.showSuccessMessage(true)
      }
      is AuthResult.Error -> {
        // Error is handled in uiState
      }
      null -> {
        /* No action needed */
      }
    }
  }

  Column(
      modifier = Modifier.fillMaxSize().padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {

        // Show success message if authenticated
        if (uiState.showSuccessMessage) {
          Card(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                      Text(
                          text = "Authentication Successful!",
                          color = Color.White,
                          fontSize = 18.sp,
                          fontWeight = FontWeight.Bold)
                      Spacer(modifier = Modifier.height(8.dp))
                      Text(
                          text =
                              "Welcome ${authResult?.let { (it as? AuthResult.Success)?.user?.displayName ?: "User" }}",
                          color = Color.White,
                          fontSize = 14.sp)
                      Spacer(modifier = Modifier.height(16.dp))
                      Button(
                          onClick = {
                            viewModel.showSuccessMessage(false)
                            viewModel.signOut()
                          },
                          colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                            Text("Sign Out", color = Color(0xFF4CAF50))
                          }
                    }
              }
        } else {
          // Show login form when not showing success message
          // App name
          Text(
              text = "SkillBridge",
              fontSize = 28.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1E88E5),
              modifier = Modifier.testTag(SignInScreenTestTags.TITLE))

          Spacer(modifier = Modifier.height(10.dp))
          Text(
              "Welcome back! Please sign in.",
              modifier = Modifier.testTag(SignInScreenTestTags.SUBTITLE))

          Spacer(modifier = Modifier.height(20.dp))

          // Role buttons
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { viewModel.updateSelectedRole(UserRole.LEARNER) },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (uiState.selectedRole == UserRole.LEARNER) Color(0xFF42A5F5)
                            else Color.LightGray),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag(SignInScreenTestTags.ROLE_LEARNER)) {
                  Text("I'm a Learner")
                }
            Button(
                onClick = { viewModel.updateSelectedRole(UserRole.TUTOR) },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (uiState.selectedRole == UserRole.TUTOR) Color(0xFF42A5F5)
                            else Color.LightGray),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag(SignInScreenTestTags.ROLE_TUTOR)) {
                  Text("I'm a Tutor")
                }
          }

          Spacer(modifier = Modifier.height(30.dp))

          OutlinedTextField(
              value = uiState.email,
              onValueChange = viewModel::updateEmail,
              label = { Text("Email") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
              leadingIcon = {
                Icon(
                    painterResource(id = android.R.drawable.ic_dialog_email),
                    contentDescription = null)
              },
              modifier = Modifier.fillMaxWidth().testTag(SignInScreenTestTags.EMAIL_INPUT))

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
              value = uiState.password,
              onValueChange = viewModel::updatePassword,
              label = { Text("Password") },
              visualTransformation = PasswordVisualTransformation(),
              keyboardOptions =
                  KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrect = false),
              leadingIcon = {
                Icon(
                    painterResource(id = android.R.drawable.ic_lock_idle_lock),
                    contentDescription = null)
              },
              modifier = Modifier.fillMaxWidth().testTag(SignInScreenTestTags.PASSWORD_INPUT))

          // Show error message if exists
          uiState.error?.let { errorMessage ->
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
          }

          // Show success message for password reset
          uiState.message?.let { message ->
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = message, color = Color.Green, fontSize = 14.sp)
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
              "Forgot password?",
              modifier =
                  Modifier.align(Alignment.End)
                      .clickable { viewModel.sendPasswordReset() }
                      .testTag(SignInScreenTestTags.FORGOT_PASSWORD),
              fontSize = 14.sp,
              color = Color.Gray)

          Spacer(modifier = Modifier.height(30.dp))

          // Sign In Button with Firebase authentication
          Button(
              onClick = viewModel::signIn,
              enabled = uiState.isSignInButtonEnabled,
              modifier =
                  Modifier.fillMaxWidth()
                      .height(50.dp)
                      .testTag(SignInScreenTestTags.SIGN_IN_BUTTON),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
              shape = RoundedCornerShape(12.dp)) {
                if (uiState.isLoading) {
                  CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                  Text("Sign In", fontSize = 18.sp)
                }
              }

          Spacer(modifier = Modifier.height(20.dp))

          Text("or continue with", modifier = Modifier.testTag(SignInScreenTestTags.AUTH_SECTION))

          Spacer(modifier = Modifier.height(15.dp))

          Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            Button(
                onClick = onGoogleSignIn,
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier =
                    Modifier.weight(1f)
                        .border(width = 2.dp, color = Color.Gray, shape = RoundedCornerShape(12.dp))
                        .testTag(SignInScreenTestTags.AUTH_GOOGLE)) {
                  Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.Center) {
                        Text("Google", color = Color.Black)
                      }
                }
            Button(
                onClick = { /* TODO: GitHub auth */},
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier =
                    Modifier.weight(1f)
                        .border(width = 2.dp, color = Color.Gray, shape = RoundedCornerShape(12.dp))
                        .testTag(SignInScreenTestTags.AUTH_GITHUB)) {
                  Text("GitHub", color = Color.Black)
                }
          }

          Spacer(modifier = Modifier.height(20.dp))

          Row {
            Text("Don't have an account? ")
            Text(
                "Sign Up",
                color = Color.Blue,
                fontWeight = FontWeight.Bold,
                modifier =
                    Modifier.clickable {
                          // TODO: Navigate to sign up when implemented
                        }
                        .testTag(SignInScreenTestTags.SIGNUP_LINK))
          }
        }
      }
}

// Legacy composable for backward compatibility and proper ViewModel creation
@Preview
@Composable
fun LoginScreenPreview() {
  val context = LocalContext.current
  val activity = context as? ComponentActivity
  val viewModel: AuthenticationViewModel = remember { AuthenticationViewModel(context) }

  // Google Sign-In helper setup
  val googleSignInHelper =
      remember(activity) {
        activity?.let { act ->
          GoogleSignInHelper(act) { result -> viewModel.handleGoogleSignInResult(result) }
        }
      }

  LoginScreen(
      viewModel = viewModel,
      onGoogleSignIn = {
        googleSignInHelper?.signInWithGoogle()
            ?: run { viewModel.setError("Google Sign-In requires Activity context") }
      })
}
