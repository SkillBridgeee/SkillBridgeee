package com.android.sample.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
  Scaffold(
      topBar = {},
      bottomBar = {},
      floatingActionButton = {
        // Save profile edits
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
    profileId: String,
    profileViewModel: MyProfileViewModel,
    onLogout: () -> Unit
) {
  LaunchedEffect(profileId) { profileViewModel.loadProfile(profileId) }
  val ui by profileViewModel.uiState.collectAsState()
  val fieldSpacing = 8.dp
  val locationSuggestions = ui.locationSuggestions
  val locationQuery = ui.locationQuery

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

/* ------- Small private composables kept inside the same file ------- */

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

          LocationInputField(
              locationQuery = ui.locationQuery,
              locationSuggestions = ui.locationSuggestions,
              onLocationQueryChange = { profileViewModel.setLocationQuery(it) },
              errorMsg = ui.invalidLocationMsg,
              onLocationSelected = { location ->
                profileViewModel.setLocationQuery(location.name)
                profileViewModel.setLocation(location)
              })
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

  // Use a Button here and attach the testTag to the clickable element so tests can find it.
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
