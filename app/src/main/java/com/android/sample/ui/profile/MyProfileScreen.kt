package com.android.sample.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.ui.theme.SampleAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    profileViewModel: MyProfileViewModel = viewModel(),
    profileId: String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                actions = {}
            )
        },
        bottomBar = {
            // TODO: Implement bottom navigation bar
            Text("BotBar")
        },
        floatingActionButton = {},
        content = { pd ->
            ProfileContent(pd, profileId, profileViewModel)
        }
    )
}

@Composable
private fun ProfileContent(pd: PaddingValues, profileId: String, profileViewModel: MyProfileViewModel) {

    LaunchedEffect(profileId) { profileViewModel.loadProfile() }
    val profileUIState by profileViewModel.uiState.collectAsState()

    val fieldSpacing = 8.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(pd)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color.Blue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = profileUIState.name.firstOrNull()?.uppercase() ?: "",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = profileUIState.name,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Student",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )


        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .align(Alignment.CenterHorizontally)
                .padding(pd)
                .background(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.shapes.medium)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Gray, Color.LightGray)
                    ),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Personal Details",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = profileUIState.name,
                    onValueChange = { profileViewModel.setName(it) },
                    label = { Text("Name") },
                    placeholder = { Text("Enter Your Full Name") },
                    isError = profileUIState.invalidNameMsg != null,
                    supportingText = {
                        profileUIState.invalidNameMsg?.let {
                            Text(it)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(fieldSpacing))

                OutlinedTextField(
                    value = profileUIState.email,
                    onValueChange = { profileViewModel.setEmail(it) },
                    label = { Text("Email") },
                    placeholder = { Text("Enter Your Email") },
                    isError = profileUIState.invalidEmailMsg != null,
                    supportingText = {
                        profileUIState.invalidEmailMsg?.let {
                            Text(it)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(fieldSpacing))

                OutlinedTextField(
                    value = profileUIState.location,
                    onValueChange = { profileViewModel.setLocation(it) },
                    label = { Text("Location / Campus") },
                    placeholder = { Text("Enter Your Location or University") },
                    isError = profileUIState.invalidLocationMsg != null,
                    supportingText = {
                        profileUIState.invalidLocationMsg?.let {
                            Text(it)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(fieldSpacing))

                OutlinedTextField(
                    value = profileUIState.bio,
                    onValueChange = { profileViewModel.setBio(it) },
                    label = { Text("Bio") },
                    placeholder = { Text("Info About You") },
                    isError = profileUIState.invalidBioMsg != null,
                    supportingText = {
                        profileUIState.invalidBioMsg?.let {
                            Text(it)
                        }
                    },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun MyProfilePreview() {
    SampleAppTheme {
        MyProfileScreen(profileId = "")
    }
}
