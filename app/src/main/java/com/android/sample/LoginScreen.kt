package com.android.sample

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

enum class UserRole(string: String) {
  Learner("Learner"),
  Tutor("Tutor")
}

@Preview
@Composable
fun LoginScreen() {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var selectedRole by remember { mutableStateOf(UserRole.Learner)}




  Column(
      modifier = Modifier.fillMaxSize().padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
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
              onClick = { selectedRole = UserRole.Learner },
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor =
                          if (selectedRole == UserRole.Learner) Color(0xFF42A5F5)
                          else Color.LightGray),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag(SignInScreenTestTags.ROLE_LEARNER)) {
                Text("I'm a Learner")
              }
          Button(
              onClick = { selectedRole = UserRole.Tutor },
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor =
                          if (selectedRole == UserRole.Tutor) Color(0xFF42A5F5)
                          else Color.LightGray),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag(SignInScreenTestTags.ROLE_TUTOR)) {
                Text("I'm a Tutor")
              }
        }

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = {
              Icon(
                  painterResource(id = android.R.drawable.ic_dialog_email),
                  contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth().testTag(SignInScreenTestTags.EMAIL_INPUT))

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            leadingIcon = {
              Icon(
                  painterResource(id = android.R.drawable.ic_lock_idle_lock),
                  contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth().testTag(SignInScreenTestTags.PASSWORD_INPUT))

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "Forgot password?",
            modifier =
                Modifier.align(Alignment.End)
                    .clickable {}
                    .testTag(SignInScreenTestTags.FORGOT_PASSWORD),
            fontSize = 14.sp,
            color = Color.Gray)

        Spacer(modifier = Modifier.height(30.dp))

        // TODO: Replace with Nahuel's SignIn button when implemented
        Button(
            onClick = {},
            enabled = email.isNotEmpty() && password.isNotEmpty(),
            modifier =
                Modifier.fillMaxWidth().height(50.dp).testTag(SignInScreenTestTags.SIGN_IN_BUTTON),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
            shape = RoundedCornerShape(12.dp)) {
              Text("Sign In", fontSize = 18.sp)
            }

        Spacer(modifier = Modifier.height(20.dp))

        Text("or continue with", modifier = Modifier.testTag(SignInScreenTestTags.AUTH_SECTION))

        Spacer(modifier = Modifier.height(15.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
          Button(
              onClick = {},
              colors = ButtonDefaults.buttonColors(containerColor = Color.White),
              shape = RoundedCornerShape(12.dp),
              modifier =
                  Modifier.weight(1f)
                      .border(width = 2.dp, color = Color.Gray, shape = RoundedCornerShape(12.dp))
                      .testTag(SignInScreenTestTags.AUTH_GOOGLE)) {
                Text("Google", color = Color.Black)
              }
          Button(
              onClick = {},
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
              modifier = Modifier.clickable {}.testTag(SignInScreenTestTags.SIGNUP_LINK))
        }
      }
}
