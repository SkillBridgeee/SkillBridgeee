package com.android.sample.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.map.Location
import com.android.sample.model.user.Profile
import com.android.sample.ui.components.AppButton
import com.android.sample.ui.components.ListingCard
import com.android.sample.ui.components.LocationInputField

object MyProfileScreenTestTag {
  const val PROFILE_ICON = "profileIcon"
  const val NAME_DISPLAY = "nameDisplay"
  const val ROLE_BADGE = "roleBadge"
  const val CARD_TITLE = "cardTitle"
  const val INPUT_PROFILE_NAME = "inputProfileName"
  const val INPUT_PROFILE_EMAIL = "inputProfileEmail"
  const val INPUT_PROFILE_LOCATION = "inputProfileLocation"
  const val INPUT_PROFILE_DESC = "inputProfileDesc"
  const val SAVE_BUTTON = "saveButton"
  const val LOGOUT_BUTTON = "logoutButton"
  const val ERROR_MSG = "errorMsg"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    profileViewModel: MyProfileViewModel = viewModel(),
    profileId: String,
    onLogout: () -> Unit = {}
) {
  Scaffold(
      topBar = {},
      bottomBar = {},
      floatingActionButton = {
        AppButton(
            text = "Save Profile Changes",
            onClick = { profileViewModel.editProfile() },
            testTag = MyProfileScreenTestTag.SAVE_BUTTON)
      },
      floatingActionButtonPosition = FabPosition.Center) { pd ->
        ProfileContent(pd, profileId, profileViewModel, onLogout)
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    pd: PaddingValues,
    profileId: String,
    profileViewModel: MyProfileViewModel,
    onLogout: () -> Unit
) {
  LaunchedEffect(profileId) { profileViewModel.loadProfile(profileId) }

  val ui by profileViewModel.uiState.collectAsState()

  val creatorProfile =
      Profile(
          userId = ui.userId ?: "",
          name = ui.name,
          email = ui.email ?: "",
          location = ui.selectedLocation ?: Location(),
          description = ui.description ?: "")

  val fieldSpacing = 8.dp
  val locationSuggestions = ui.locationSuggestions
  val locationQuery = ui.locationQuery

  LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = pd) {
    // Header: avatar + name + role
    item {
      Column(
          modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
          horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier =
                    Modifier.size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color.Blue, CircleShape)
                        .testTag(MyProfileScreenTestTag.PROFILE_ICON),
                contentAlignment = Alignment.Center) {
                  Text(
                      text = ui.name?.firstOrNull()?.uppercase() ?: "",
                      style = MaterialTheme.typography.titleLarge,
                      color = Color.Black,
                      fontWeight = FontWeight.Bold)
                }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = ui.name ?: "Your Name",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.testTag(MyProfileScreenTestTag.NAME_DISPLAY))
            Text(
                text = "Student",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.testTag(MyProfileScreenTestTag.ROLE_BADGE))
          }
    }

    // Form box (centered)
    item {
      Spacer(modifier = Modifier.height(12.dp))

      Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.Center) {
            Box(
                modifier =
                    Modifier.widthIn(max = 300.dp)
                        .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                        .border(
                            width = 1.dp,
                            brush =
                                Brush.linearGradient(colors = listOf(Color.Gray, Color.LightGray)),
                            shape = MaterialTheme.shapes.medium)
                        .padding(16.dp)) {
                  Column {
                    Text(
                        text = "Personal Details",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag(MyProfileScreenTestTag.CARD_TITLE))

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = ui.name ?: "",
                        onValueChange = { profileViewModel.setName(it) },
                        label = { Text("Name") },
                        placeholder = { Text("Enter Your Full Name") },
                        isError = ui.invalidNameMsg != null,
                        supportingText = {
                          ui.invalidNameMsg?.let {
                            Text(
                                text = it,
                                modifier = Modifier.testTag(MyProfileScreenTestTag.ERROR_MSG))
                          }
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                                .testTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME))

                    Spacer(modifier = Modifier.height(fieldSpacing))

                    OutlinedTextField(
                        value = ui.email ?: "",
                        onValueChange = { profileViewModel.setEmail(it) },
                        label = { Text("Email") },
                        placeholder = { Text("Enter Your Email") },
                        isError = ui.invalidEmailMsg != null,
                        supportingText = {
                          ui.invalidEmailMsg?.let {
                            Text(
                                text = it,
                                modifier = Modifier.testTag(MyProfileScreenTestTag.ERROR_MSG))
                          }
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                                .testTag(MyProfileScreenTestTag.INPUT_PROFILE_EMAIL))

                    Spacer(modifier = Modifier.height(fieldSpacing))

                    OutlinedTextField(
                        value = ui.description ?: "",
                        onValueChange = { profileViewModel.setDescription(it) },
                        label = { Text("Description") },
                        placeholder = { Text("Info About You") },
                        isError = ui.invalidDescMsg != null,
                        supportingText = {
                          ui.invalidDescMsg?.let {
                            Text(
                                text = it,
                                modifier = Modifier.testTag(MyProfileScreenTestTag.ERROR_MSG))
                          }
                        },
                        minLines = 2,
                        modifier =
                            Modifier.fillMaxWidth()
                                .testTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC))

                    Spacer(modifier = Modifier.height(fieldSpacing))

                    LocationInputField(
                        locationQuery = locationQuery,
                        locationSuggestions = locationSuggestions,
                        onLocationQueryChange = { profileViewModel.setLocationQuery(it) },
                        errorMsg = ui.invalidLocationMsg,
                        onLocationSelected = { location ->
                          profileViewModel.setLocationQuery(location.name)
                          profileViewModel.setLocation(location)
                        })
                  }
                }
          }
    }

    // Listings header
    item {
      Spacer(modifier = Modifier.height(16.dp))
      Text(
          text = "Your Listings",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 16.dp))
      Spacer(modifier = Modifier.height(8.dp))
    }

    // Listings – empty state or items
    if (ui.listings.isEmpty()) {
      item {
        Text(
            text = "You don’t have any listings yet.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp))
      }
    } else {
      items(items = ui.listings, key = { it.listingId }) { listing ->
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
          ListingCard(
              listing = listing,
              creator = creatorProfile,
              onOpenListing = {}, // no-op to satisfy static analysis
              onBook = {})
          Spacer(Modifier.height(8.dp))
        }
      }
    }

    // Logout button at the bottom
    item {
      Spacer(modifier = Modifier.height(16.dp))
      AppButton(text = "Logout", onClick = onLogout, testTag = MyProfileScreenTestTag.LOGOUT_BUTTON)
      Spacer(modifier = Modifier.height(80.dp)) // room above FAB
    }
  }
}
