package com.android.sample.ui.profile

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.map.GpsLocationProvider
import com.android.sample.model.map.Location
import com.android.sample.model.user.Profile
import com.android.sample.ui.components.ListingCard
import com.android.sample.ui.components.LocationInputField
import kotlinx.coroutines.delay

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

  const val INFO_RATING_BAR = "infoRankingBar"
  const val INFO_TAB = "infoTab"
  const val RATING_TAB = "rankingTab"

  const val RATING_COMING_SOON_TEXT = "rankingComingSoonText"
  const val TAB_INDICATOR = "tabIndicator"
}

enum class ProfileTab {
  INFO,
  RATING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Top-level composable for the My Profile screen.
 *
 * This sets up the Scaffold (including the floating Save button) and hosts the screen content.
 *
 * @param profileViewModel ViewModel providing UI state and actions. Defaults to `viewModel()`.
 * @param profileId Optional profile id to load (used when viewing other users). Passed to the
 *   content loader.
 * @param onLogout Callback invoked when the user taps the logout button.
 */
fun MyProfileScreen(
    profileViewModel: MyProfileViewModel = viewModel(),
    profileId: String,
    onLogout: () -> Unit = {}
) {
  val selectedTab = remember { mutableStateOf(ProfileTab.INFO) }
  Scaffold(
      topBar = {},
      bottomBar = {},
      floatingActionButton = {
        // Save profile edits
        // todo change the button and don't make it floating the rendering is very ugly
        if (selectedTab.value == ProfileTab.INFO) {
          Button(
              onClick = { profileViewModel.editProfile() },
              modifier = Modifier.testTag(MyProfileScreenTestTag.SAVE_BUTTON)) {
                Text("Save Profile Changes")
              }
        }
      },
      floatingActionButtonPosition = FabPosition.Center) { pd ->
        val ui by profileViewModel.uiState.collectAsState()
        LaunchedEffect(profileId) { profileViewModel.loadProfile(profileId) }
        LaunchedEffect(ui.updateSuccess) {
          if (ui.updateSuccess) {
            delay(5000)
            profileViewModel.clearUpdateSuccess()
          }
        }

        Column {
          InfoToRankingRow(selectedTab)
          Spacer(modifier = Modifier.height(16.dp))

          if (selectedTab.value == ProfileTab.INFO) {
            ProfileContent(pd, ui, profileViewModel, onLogout)
          } else {
            RatingContent(pd, ui)
          }
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Internal content host for the My Profile screen.
 *
 * Loads the profile when `profileId` changes, observes the `uiState` from the `profileViewModel`,
 * and composes the header, form, listings and logout sections inside a `LazyColumn`.
 *
 * @param pd Content padding from the parent Scaffold.
 * @param profileId Profile id to load.
 * @param profileViewModel ViewModel that exposes UI state and actions.
 * @param onLogout Callback invoked by the logout UI.
 */
private fun ProfileContent(
    pd: PaddingValues,
    ui: MyProfileUIState,
    profileViewModel: MyProfileViewModel,
    onLogout: () -> Unit
) {
  val profileId = ui.userId ?: ""
  LaunchedEffect(profileId) { profileViewModel.loadProfile(profileId) }
  val fieldSpacing = 8.dp

  LazyColumn(
      modifier = Modifier.fillMaxWidth().testTag(MyProfileScreenTestTag.ROOT_LIST),
      contentPadding = pd) {
        if (ui.updateSuccess) {
          item {
            Text(
                text = "Profile successfully updated!",
                color = Color(0xFF2E7D32),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
          }
        }
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
/**
 * Small header composable showing avatar initial, display name, and role badge.
 *
 * @param name Display name to show. The avatar shows the first character uppercased or empty if
 *   `null`.
 */
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
/**
 * Reusable small wrapper around `OutlinedTextField` used in this screen.
 *
 * Adds consistent `testTag` and supporting error text handling.
 *
 * @param value Current input value.
 * @param onValueChange Change callback.
 * @param label Label text.
 * @param placeholder Placeholder text.
 * @param isError True when field is invalid.
 * @param errorMsg Optional supporting error message to display.
 * @param testTag Test tag applied to the field root for UI tests.
 * @param modifier Modifier applied to the field.
 * @param minLines Minimum visible lines for the field.
 */
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
/**
 * Small reusable card-like container used for form sections.
 *
 * Provides consistent width, background, border and inner padding, and exposes a `Column` content
 * slot so callers can place fields inside.
 *
 * @param title Section title shown at the top of the card.
 * @param titleTestTag Optional test tag applied to the title `Text` for UI tests.
 * @param modifier Optional `Modifier` applied to the root container.
 * @param content Column-scoped composable content placed below the title.
 */
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
/**
 * The editable profile form containing name, email, description and location inputs.
 *
 * Uses [SectionCard] to reduce duplication for the card styling.
 *
 * @param ui Current UI state from the view model.
 * @param profileViewModel ViewModel instance used to update form fields.
 * @param fieldSpacing Vertical spacing between fields.
 */
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
          profileViewModel.fetchLocationFromGps(provider, context)
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
                    profileViewModel.fetchLocationFromGps(GpsLocationProvider(context), context)
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
/**
 * Listings section showing the user's created listings.
 *
 * Shows a localized loading UI while listings are being fetched so the rest of the profile remains
 * visible.
 *
 * @param ui Current UI state providing listings and profile data for the creator.
 */
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
/**
 * Logout section — presents a full-width logout button that triggers `onLogout`.
 *
 * The button includes a test tag so tests can find and click it.
 *
 * @param onLogout Callback invoked when the button is clicked.
 */
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

@Composable
fun InfoToRankingRow(selectedTab: MutableState<ProfileTab>) {
  val tabCount = 2
  val indicatorHeight = 3.dp

  Column(modifier = Modifier.fillMaxWidth()) {
    // --- Tabs Row ---
    Row(modifier = Modifier.fillMaxWidth().testTag(MyProfileScreenTestTag.INFO_RATING_BAR)) {
      // Info tab
      Box(
          modifier =
              Modifier.weight(1f)
                  .clickable { selectedTab.value = ProfileTab.INFO }
                  .padding(vertical = 12.dp)
                  .testTag(MyProfileScreenTestTag.INFO_TAB),
          contentAlignment = Alignment.Center) {
            Text(
                text = "Info",
                fontWeight =
                    if (selectedTab.value == ProfileTab.INFO) FontWeight.Bold
                    else FontWeight.Normal,
                color =
                    if (selectedTab.value == ProfileTab.INFO) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
          }

      // Ratings tab
      Box(
          modifier =
              Modifier.weight(1f)
                  .clickable { selectedTab.value = ProfileTab.RATING }
                  .padding(vertical = 12.dp)
                  .testTag(MyProfileScreenTestTag.RATING_TAB),
          contentAlignment = Alignment.Center) {
            Text(
                text = "Ratings",
                fontWeight =
                    if (selectedTab.value == ProfileTab.RATING) FontWeight.Bold
                    else FontWeight.Normal,
                color =
                    if (selectedTab.value == ProfileTab.RATING) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
          }
    }

    // --- Indicator Animation ---
    val transition = updateTransition(targetState = selectedTab.value, label = "tabIndicator")
    val offsetX by
        transition.animateDp(label = "tabIndicatorOffset") { tab ->
          when (tab) {
            ProfileTab.INFO -> 0.dp
            ProfileTab.RATING -> 0.5f.dp * LocalConfiguration.current.screenWidthDp
          }
        }

    Box(
        modifier =
            Modifier.fillMaxWidth()
                .height(indicatorHeight)
                .testTag(MyProfileScreenTestTag.TAB_INDICATOR)) {
          Box(
              modifier =
                  Modifier.offset(x = offsetX)
                      .width((LocalConfiguration.current.screenWidthDp / tabCount).dp)
                      .height(indicatorHeight)
                      .background(MaterialTheme.colorScheme.primary))
        }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun RatingContent(
    pd: PaddingValues,
    ui: MyProfileUIState,
) {

  Box(
      modifier =
          Modifier.fillMaxWidth()
              .padding(pd)
              .padding(16.dp)
              .testTag(MyProfileScreenTestTag.RATING_COMING_SOON_TEXT),
      contentAlignment = Alignment.Center) {
        Text(
            text = "Ratings Feature Coming Soon!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
      }
}
