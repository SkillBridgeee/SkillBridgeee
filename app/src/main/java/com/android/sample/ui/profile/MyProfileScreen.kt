package com.android.sample.ui.profile

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.map.GpsLocationProvider
import com.android.sample.model.map.Location
import com.android.sample.model.user.Profile
import com.android.sample.ui.components.ListingCard
import com.android.sample.ui.components.LocationInputField

/**
 * Test tags used by UI tests and screenshot tests on the My Profile screen.
 *
 * Keep these stable — tests rely on the exact string constants below.
 */
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
  const val ROOT_LIST = "profile_list"
  const val LOGOUT_BUTTON = "logoutButton"
  const val ERROR_MSG = "errorMsg"
  const val PIN_CONTENT_DESC = "Use my location"
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
        Button(
            onClick = { profileViewModel.editProfile() },
            modifier = Modifier.testTag(MyProfileScreenTestTag.SAVE_BUTTON)) {
              Text("Save Profile Changes")
            }
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
  val fieldSpacing = 8.dp

  LazyColumn(
      modifier = Modifier.fillMaxWidth().testTag(MyProfileScreenTestTag.ROOT_LIST),
      contentPadding = pd) {
        item { ProfileHeader(name = ui.name) }

        item {
          Spacer(modifier = Modifier.height(12.dp))
          ProfileForm(ui = ui, profileViewModel = profileViewModel, fieldSpacing = fieldSpacing)
        }

        item { ProfileListings(ui = ui) }

        item { ProfileLogout(onLogout = onLogout) }
      }
}

@Composable
private fun ProfileHeader(name: String?) {
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
                  text = name?.firstOrNull()?.uppercase() ?: "",
                  style = MaterialTheme.typography.titleLarge,
                  color = Color.Black,
                  fontWeight = FontWeight.Bold)
            }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = name ?: "Your Name",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag(MyProfileScreenTestTag.NAME_DISPLAY))
        Text(
            text = "Student",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.testTag(MyProfileScreenTestTag.ROLE_BADGE))
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    errorMsg: String? = null,
    testTag: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      placeholder = { Text(placeholder) },
      isError = isError,
      supportingText = {
        errorMsg?.let {
          Text(text = it, modifier = Modifier.testTag(MyProfileScreenTestTag.ERROR_MSG))
        }
      },
      modifier = modifier.testTag(testTag),
      minLines = minLines)
}

@Composable
private fun SectionCard(
    title: String,
    titleTestTag: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
  Box(
      modifier =
          modifier
              .widthIn(max = 300.dp)
              .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
              .border(
                  width = 1.dp,
                  brush = Brush.linearGradient(colors = listOf(Color.Gray, Color.LightGray)),
                  shape = MaterialTheme.shapes.medium)
              .padding(16.dp)) {
        Column {
          Text(
              text = title,
              fontWeight = FontWeight.Bold,
              modifier = titleTestTag?.let { Modifier.testTag(it) } ?: Modifier)
          Spacer(modifier = Modifier.height(10.dp))
          content()
        }
      }
}

@Composable
private fun ProfileForm(
    ui: MyProfileUIState,
    profileViewModel: MyProfileViewModel,
    fieldSpacing: Dp = 8.dp
) {
  val context = LocalContext.current
  val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
  val permissionLauncher =
      rememberLauncherForActivityResult(RequestPermission()) { granted ->
        val provider = GpsLocationProvider(context)
        if (granted) {
          profileViewModel.fetchLocationFromGps(provider)
        } else {
          profileViewModel.onLocationPermissionDenied()
        }
      }

  Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.Center) {
        SectionCard(title = "Personal Details", titleTestTag = MyProfileScreenTestTag.CARD_TITLE) {
          ProfileTextField(
              value = ui.name ?: "",
              onValueChange = { profileViewModel.setName(it) },
              label = "Name",
              placeholder = "Enter Your Full Name",
              isError = ui.invalidNameMsg != null,
              errorMsg = ui.invalidNameMsg,
              testTag = MyProfileScreenTestTag.INPUT_PROFILE_NAME,
              modifier = Modifier.fillMaxWidth())

          Spacer(modifier = Modifier.height(fieldSpacing))

          ProfileTextField(
              value = ui.email ?: "",
              onValueChange = { profileViewModel.setEmail(it) },
              label = "Email",
              placeholder = "Enter Your Email",
              isError = ui.invalidEmailMsg != null,
              errorMsg = ui.invalidEmailMsg,
              testTag = MyProfileScreenTestTag.INPUT_PROFILE_EMAIL,
              modifier = Modifier.fillMaxWidth())

          Spacer(modifier = Modifier.height(fieldSpacing))

          ProfileTextField(
              value = ui.description ?: "",
              onValueChange = { profileViewModel.setDescription(it) },
              label = "Description",
              placeholder = "Info About You",
              isError = ui.invalidDescMsg != null,
              errorMsg = ui.invalidDescMsg,
              testTag = MyProfileScreenTestTag.INPUT_PROFILE_DESC,
              modifier = Modifier.fillMaxWidth(),
              minLines = 2)

          Spacer(modifier = Modifier.height(fieldSpacing))

          // Location input + pin icon overlay
          Box(modifier = Modifier.fillMaxWidth()) {
            LocationInputField(
                locationQuery = ui.locationQuery,
                locationSuggestions = ui.locationSuggestions,
                onLocationQueryChange = { profileViewModel.setLocationQuery(it) },
                errorMsg = ui.invalidLocationMsg,
                onLocationSelected = { location ->
                  profileViewModel.setLocationQuery(location.name)
                  profileViewModel.setLocation(location)
                },
                modifier = Modifier.fillMaxWidth())

            IconButton(
                onClick = {
                  val granted =
                      ContextCompat.checkSelfPermission(context, permission) ==
                          PackageManager.PERMISSION_GRANTED
                  if (granted) {
                    profileViewModel.fetchLocationFromGps(GpsLocationProvider(context))
                  } else {
                    permissionLauncher.launch(permission)
                  }
                },
                modifier = Modifier.align(Alignment.CenterEnd).size(36.dp)) {
                  Icon(
                      imageVector = Icons.Filled.MyLocation,
                      contentDescription = MyProfileScreenTestTag.PIN_CONTENT_DESC,
                      tint = MaterialTheme.colorScheme.primary)
                }
          }
        }
      }
}

@Composable
private fun ProfileListings(ui: MyProfileUIState) {
  Spacer(modifier = Modifier.height(16.dp))
  Text(
      text = "Your Listings",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 16.dp))
  Spacer(modifier = Modifier.height(8.dp))

  when {
    ui.listingsLoading -> {
      Box(
          modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
          contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
    }
    ui.listingsLoadError != null -> {
      Text(
          text = ui.listingsLoadError ?: "Failed to load listings.",
          style = MaterialTheme.typography.bodyMedium,
          color = Color.Red,
          modifier = Modifier.padding(horizontal = 16.dp))
    }
    ui.listings.isEmpty() -> {
      Text(
          text = "You don’t have any listings yet.",
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(horizontal = 16.dp))
    }
    else -> {
      val creatorProfile =
          Profile(
              userId = ui.userId ?: "",
              name = ui.name ?: "",
              email = ui.email ?: "",
              location = ui.selectedLocation ?: Location(),
              description = ui.description ?: "")
      ui.listings.forEach { listing ->
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
          ListingCard(listing = listing, creator = creatorProfile, onOpenListing = {}, onBook = {})
          Spacer(Modifier.height(8.dp))
        }
      }
    }
  }
}

@Composable
private fun ProfileLogout(onLogout: () -> Unit) {
  Spacer(modifier = Modifier.height(16.dp))
  Button(
      onClick = onLogout,
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 16.dp)
              .testTag(MyProfileScreenTestTag.LOGOUT_BUTTON)) {
        Text("Logout")
      }
  Spacer(modifier = Modifier.height(80.dp))
}
