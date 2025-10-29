package com.android.sample.ui.login

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
import com.android.sample.ui.theme.extendedColors

object SignInScreenTestTags {
  const val TITLE = "title"
  const val EMAIL_INPUT = "emailInput"
  const val PASSWORD_INPUT = "passwordInput"
  const val SIGN_IN_BUTTON = "signInButton"
  const val AUTH_GOOGLE = "authGoogle"
  const val SIGNUP_LINK = "signUpLink"
  const val AUTH_GITHUB = "authGitHub"
  const val FORGOT_PASSWORD = "forgotPassword"
  const val AUTH_SECTION = "authSection"
  const val SUBTITLE = "subtitle"
}

@Composable
fun LoginScreen(
    viewModel: AuthenticationViewModel = AuthenticationViewModel(LocalContext.current),
    onGoogleSignIn: () -> Unit = {},
    onGitHubSignIn: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {} // Add this parameter
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val authResult by viewModel.authResult.collectAsStateWithLifecycle()

  Column(
      modifier = Modifier.fillMaxSize().padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        if (uiState.showSuccessMessage) {
          SuccessCard(
              authResult = authResult,
              onSignOut = {
                viewModel.showSuccessMessage(false)
                viewModel.signOut()
              })
        } else {
          LoginForm(
              uiState = uiState,
              viewModel = viewModel,
              onGoogleSignIn = onGoogleSignIn,
              onGitHubSignIn = onGitHubSignIn,
              onNavigateToSignUp)
        }
      }
}

@Composable
private fun SuccessCard(authResult: AuthResult?, onSignOut: () -> Unit) {
  val extendedColors = MaterialTheme.extendedColors

  Card(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      colors = CardDefaults.cardColors(containerColor = extendedColors.successGreen)) {
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
                  onClick = onSignOut,
                  colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                    Text("Sign Out", color = extendedColors.successGreen)
                  }
            }
      }
}

@Composable
private fun LoginForm(
    uiState: AuthenticationUiState,
    viewModel: AuthenticationViewModel,
    onGoogleSignIn: () -> Unit,
    onGitHubSignIn: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {}
) {
  LoginHeader()
  Spacer(modifier = Modifier.height(20.dp))

  EmailPasswordFields(
      email = uiState.email,
      password = uiState.password,
      onEmailChange = viewModel::updateEmail,
      onPasswordChange = viewModel::updatePassword)

  ErrorAndMessageDisplay(error = uiState.error, message = uiState.message)

  ForgotPasswordLink()
  Spacer(modifier = Modifier.height(30.dp))

  SignInButton(
      isLoading = uiState.isLoading,
      isEnabled = uiState.isSignInButtonEnabled,
      onClick = viewModel::signIn)
  Spacer(modifier = Modifier.height(20.dp))

  AlternativeAuthSection(
      isLoading = uiState.isLoading,
      onGoogleSignIn = onGoogleSignIn,
      onGitHubSignIn = onGitHubSignIn)
  Spacer(modifier = Modifier.height(20.dp))

  SignUpLink(onNavigateToSignUp = onNavigateToSignUp)
}

@Composable
private fun LoginHeader() {
  val extendedColors = MaterialTheme.extendedColors

  Text(
      text = "SkillBridge",
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold,
      color = extendedColors.loginTitleBlue,
      modifier = Modifier.testTag(SignInScreenTestTags.TITLE))
  Spacer(modifier = Modifier.height(10.dp))
  Text("Welcome back! Please sign in.", modifier = Modifier.testTag(SignInScreenTestTags.SUBTITLE))
}

@Composable
private fun EmailPasswordFields(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit
) {
  OutlinedTextField(
      value = email,
      onValueChange = onEmailChange,
      label = { Text("Email") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
      leadingIcon = {
        Icon(painterResource(id = android.R.drawable.ic_dialog_email), contentDescription = null)
      },
      modifier = Modifier.fillMaxWidth().testTag(SignInScreenTestTags.EMAIL_INPUT))

  Spacer(modifier = Modifier.height(10.dp))

  OutlinedTextField(
      value = password,
      onValueChange = onPasswordChange,
      label = { Text("Password") },
      visualTransformation = PasswordVisualTransformation(),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
      leadingIcon = {
        Icon(painterResource(id = android.R.drawable.ic_lock_idle_lock), contentDescription = null)
      },
      modifier = Modifier.fillMaxWidth().testTag(SignInScreenTestTags.PASSWORD_INPUT))
}

@Composable
private fun ErrorAndMessageDisplay(error: String?, message: String?) {
  val extendedColors = MaterialTheme.extendedColors

  error?.let { errorMessage ->
    Spacer(modifier = Modifier.height(10.dp))
    Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
  }

  message?.let { msg ->
    Spacer(modifier = Modifier.height(10.dp))
    Text(text = msg, color = extendedColors.messageGreen, fontSize = 14.sp)
  }
}

@Composable
private fun ForgotPasswordLink() {
  val extendedColors = MaterialTheme.extendedColors

  Spacer(modifier = Modifier.height(10.dp))
  Text(
      "Forgot password?",
      modifier =
          Modifier.fillMaxWidth()
              .wrapContentWidth(Alignment.End)
              .clickable { /* TODO: Implement when needed */}
              .testTag(SignInScreenTestTags.FORGOT_PASSWORD),
      fontSize = 14.sp,
      color = extendedColors.forgotPasswordGray)
}

@Composable
private fun SignInButton(isLoading: Boolean, isEnabled: Boolean, onClick: () -> Unit) {
  val extendedColors = MaterialTheme.extendedColors

  Button(
      onClick = onClick,
      enabled = isEnabled,
      modifier = Modifier.fillMaxWidth().height(50.dp).testTag(SignInScreenTestTags.SIGN_IN_BUTTON),
      colors = ButtonDefaults.buttonColors(containerColor = extendedColors.signInButtonTeal),
      shape = RoundedCornerShape(12.dp)) {
        if (isLoading) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
        } else {
          Text("Sign In", fontSize = 18.sp)
        }
      }
}

@Composable
private fun AlternativeAuthSection(
    isLoading: Boolean,
    onGoogleSignIn: () -> Unit,
    onGitHubSignIn: () -> Unit = {}
) {
  Text("or continue with", modifier = Modifier.testTag(SignInScreenTestTags.AUTH_SECTION))
  Spacer(modifier = Modifier.height(15.dp))

  Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
    AuthProviderButton(
        text = "Google",
        enabled = !isLoading,
        onClick = onGoogleSignIn,
        testTag = SignInScreenTestTags.AUTH_GOOGLE)
    AuthProviderButton(
        text = "GitHub",
        enabled = !isLoading,
        onClick = onGitHubSignIn, // This line is correct
        testTag = SignInScreenTestTags.AUTH_GITHUB)
  }
}

@Composable
private fun RowScope.AuthProviderButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
  val extendedColors = MaterialTheme.extendedColors

  Button(
      onClick = onClick,
      enabled = enabled,
      colors = ButtonDefaults.buttonColors(containerColor = Color.White),
      shape = RoundedCornerShape(12.dp),
      modifier =
          Modifier.weight(1f)
              .border(
                  width = 2.dp,
                  color = extendedColors.authButtonBorderGray,
                  shape = RoundedCornerShape(12.dp))
              .testTag(testTag)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
              Text(text, color = extendedColors.authProviderTextBlack)
            }
      }
}

@Composable
private fun SignUpLink(onNavigateToSignUp: () -> Unit = {}) {
  val extendedColors = MaterialTheme.extendedColors

  Row {
    Text("Don't have an account? ")
    Text(
        "Sign Up",
        color = extendedColors.signUpLinkBlue,
        fontWeight = FontWeight.Bold,
        modifier =
            Modifier.clickable { onNavigateToSignUp() }.testTag(SignInScreenTestTags.SIGNUP_LINK))
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
