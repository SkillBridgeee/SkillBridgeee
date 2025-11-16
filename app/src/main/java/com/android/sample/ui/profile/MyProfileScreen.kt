package com.android.sample.ui.profile

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.map.GpsLocationProvider
import com.android.sample.ui.components.BookingCard
import com.android.sample.ui.components.LocationInputField
import com.android.sample.ui.components.ProposalCard
import com.android.sample.ui.components.RatingCard
import com.android.sample.ui.components.RequestCard

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
  const val INPUT_PROFILE_DESC = "inputProfileDesc"
  const val SAVE_BUTTON = "saveButton"
  const val ROOT_LIST = "profile_list"
  const val LOGOUT_BUTTON = "logoutButton"
  const val ERROR_MSG = "errorMsg"
  const val PIN_CONTENT_DESC = "Use my location"

  const val INFO_RATING_BAR = "infoRankingBar"
  const val INFO_TAB = "infoTab"
  const val RATING_TAB = "rankingTab"
  const val RATING_SECTION = "ratingSection"
  const val LISTINGS_TAB = "listingsTab"

  const val HISTORY_TAB = "historyTab"
  const val LISTINGS_SECTION = "listingsSection"
  const val HISTORY_SECTION = "historySection"
}

enum class ProfileTab {
  INFO,
  LISTINGS,
  RATING,
  HISTORY
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
    onLogout: () -> Unit = {},
    onListingClick: (String) -> Unit = {}
) {
  val selectedTab = remember { mutableStateOf(ProfileTab.INFO) }
  Scaffold { pd ->
    val ui by profileViewModel.uiState.collectAsState()
    LaunchedEffect(profileId) { profileViewModel.loadProfile(profileId) }

    Column {
      SelectionRow(selectedTab)
      Spacer(modifier = Modifier.height(4.dp))

      when (selectedTab.value) {
        ProfileTab.INFO -> MyProfileContent(pd, ui, profileViewModel, onLogout, onListingClick)
        ProfileTab.RATING -> RatingContent(ui)
        ProfileTab.LISTINGS -> ProfileListings(ui, onListingClick)
        ProfileTab.HISTORY -> ProfileHistory(ui, onListingClick)
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
 * @param ui Current UI state from the view model.
 * @param profileViewModel ViewModel that exposes UI state and actions.
 * @param onLogout Callback invoked by the logout UI.
 * @param onListingClick Callback when a listing card is clicked.
 */
private fun MyProfileContent(
    pd: PaddingValues,
    ui: MyProfileUIState,
    profileViewModel: MyProfileViewModel,
    onLogout: () -> Unit,
    onListingClick: (String) -> Unit
) {
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
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    errorMsg: String? = null,
    testTag: String,
    minLines: Int = 1
) {
  val focusedState = remember { mutableStateOf(false) }
  val focused = focusedState.value
  val maxPreview = 30

  // keep REAL value; only change what is drawn
  val ellipsizeTransformation = VisualTransformation { text ->
    if (!focused && text.text.length > maxPreview) {
      val short = text.text.take(maxPreview) + "..."
      TransformedText(AnnotatedString(short), OffsetMapping.Identity)
    } else {
      TransformedText(text, OffsetMapping.Identity)
    }
  }

  OutlinedTextField(
      value = value, // ← real value, not truncated
      onValueChange = onValueChange,
      label = { Text(label) },
      placeholder = { Text(placeholder) },
      isError = isError,
      supportingText = {
        errorMsg?.let {
          Text(text = it, modifier = Modifier.testTag(MyProfileScreenTestTag.ERROR_MSG))
        }
      },
      modifier =
          modifier
              .onFocusChanged { focusedState.value = it.isFocused }
              .semantics {
                // when visually ellipsized, expose full text for TalkBack
                if (!focused && value.isNotEmpty()) contentDescription = value
              }
              .testTag(testTag),
      minLines = minLines,
      singleLine = (minLines == 1), // ← only single-line when requested
      visualTransformation = ellipsizeTransformation)
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
    modifier: Modifier = Modifier,
    title: String,
    titleTestTag: String? = null,
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
  var nameChanged by remember { mutableStateOf(false) }
  var emailChanged by remember { mutableStateOf(false) }
  var descriptionChanged by remember { mutableStateOf(false) }
  var locationChanged by remember { mutableStateOf(false) }

  Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.Center) {
        SectionCard(title = "Personal Details", titleTestTag = MyProfileScreenTestTag.CARD_TITLE) {
          ProfileTextField(
              value = ui.name ?: "",
              onValueChange = {
                profileViewModel.setName(it)
                nameChanged = true
              },
              label = "Name",
              placeholder = "Enter Your Full Name",
              isError = ui.invalidNameMsg != null,
              errorMsg = ui.invalidNameMsg,
              testTag = MyProfileScreenTestTag.INPUT_PROFILE_NAME,
              modifier = Modifier.fillMaxWidth())

          Spacer(modifier = Modifier.height(fieldSpacing))

          ProfileTextField(
              value = ui.email ?: "",
              onValueChange = {
                profileViewModel.setEmail(it)
                emailChanged = true
              },
              label = "Email",
              placeholder = "Enter Your Email",
              isError = ui.invalidEmailMsg != null,
              errorMsg = ui.invalidEmailMsg,
              testTag = MyProfileScreenTestTag.INPUT_PROFILE_EMAIL,
              modifier = Modifier.fillMaxWidth())

          Spacer(modifier = Modifier.height(fieldSpacing))

          ProfileTextField(
              value = ui.description ?: "",
              onValueChange = {
                profileViewModel.setDescription(it)
                descriptionChanged = true
              },
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
                onLocationQueryChange = {
                  profileViewModel.setLocationQuery(it)
                  locationChanged = true
                },
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
          Spacer(modifier = Modifier.height(fieldSpacing))

          Button(
              onClick = {
                profileViewModel.editProfile()
                nameChanged = false
                emailChanged = false
                descriptionChanged = false
                locationChanged = false
              },
              modifier = Modifier.testTag(MyProfileScreenTestTag.SAVE_BUTTON).fillMaxWidth(),
              enabled = (nameChanged || emailChanged || descriptionChanged || locationChanged)) {
                Text("Save Profile Changes")
              }
        }
      }
}

/**
 * Listings section showing the user's created listings.
 *
 * Shows a localized loading UI while listings are being fetched so the rest of the profile remains
 * visible.
 *
 * @param ui Current UI state providing listings and profile data for the creator.
 * @param onListingClick Callback when a listing card is clicked.
 */
@Composable
private fun ProfileListings(ui: MyProfileUIState, onListingClick: (String) -> Unit) {
  Column(modifier = Modifier.fillMaxWidth().testTag(MyProfileScreenTestTag.LISTINGS_SECTION)) {
    Text(
        text = "Your Listings",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier =
            Modifier.padding(horizontal = 16.dp).testTag(MyProfileScreenTestTag.LISTINGS_SECTION))
  }

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
          text = ui.listingsLoadError,
          color = Color.Red,
          modifier = Modifier.padding(horizontal = 16.dp))
    }
    ui.listings.isEmpty() -> {
      Text(
          text = "You don’t have any listings yet.",
          modifier = Modifier.padding(horizontal = 16.dp))
    }
    else -> {
      LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        items(ui.listings) { listing ->
          when (listing) {
            is com.android.sample.model.listing.Proposal -> {
              ProposalCard(proposal = listing, onClick = onListingClick)
            }
            is com.android.sample.model.listing.Request -> {
              RequestCard(request = listing, onClick = onListingClick)
            }
          }
          Spacer(Modifier.height(8.dp))
        }
      }
    }
  }
}

/**
 * History section showing the user's completed listings.
 *
 * @param ui Current UI state providing listings and profile data for the creator.
 * @param onListingClick Callback when a listing card is clicked.
 */
@Composable
private fun ProfileHistory(
    ui: MyProfileUIState,
    onListingClick: (String) -> Unit,
) {
  val historyBookings = ui.bookings.filter { it.status == BookingStatus.COMPLETED }

  Column(modifier = Modifier.fillMaxWidth().testTag(MyProfileScreenTestTag.HISTORY_SECTION)) {
    Text(
        text = "Your History",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp))
  }

  when {
    historyBookings.isEmpty() -> {
      Text(
          text = "You don’t have any completed bookings yet.",
          modifier = Modifier.padding(horizontal = 16.dp))
    }
    else -> {
      LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        items(historyBookings) { booking ->
          val listing = ui.listings.firstOrNull { it.listingId == booking.associatedListingId }
          val creator = ui.profilesById[booking.listingCreatorId]

          if (creator != null && listing != null) {
            BookingCard(
                booking = booking,
                listing = listing,
                creator = creator,
                onClickBookingCard = { onListingClick(listing.listingId) })
          }
        }
      }
    }
  }
}

/**
 * Logout section — presents a full-width logout button that triggers `onLogout`.
 *
 * The button includes a test tag so tests can find and click it.
 *
 * @param onLogout Callback invoked when the button is clicked.
 */
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

/**
 * Top tab row for selecting between Info, Listings, Ratings, and History tabs.
 *
 * Shows an animated indicator below the selected tab.
 *
 * @param selectedTab Mutable state holding the currently selected tab. Updated when the user
 *   selects a different tab.
 */
@Composable
fun SelectionRow(selectedTab: MutableState<ProfileTab>) {
  val tabCount = 4
  val indicatorHeight = 3.dp

  val density = LocalDensity.current
  val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
  val tabWidthPx = screenWidthPx / tabCount

  val tabLabels = listOf("Info", "Listings", "Ratings", "History")

  val textWidthsPx = remember { mutableStateListOf(0f, 0f, 0f, 0f) }

  /**
   * Returns the index of the given [tab].
   *
   * @param tab The [ProfileTab] whose index is to be found.
   */
  fun tabIndex(tab: ProfileTab) =
      when (tab) {
        ProfileTab.INFO -> 0
        ProfileTab.LISTINGS -> 1
        ProfileTab.RATING -> 2
        ProfileTab.HISTORY -> 3
      }

  Column(Modifier.fillMaxWidth()) {
    Row(modifier = Modifier.fillMaxWidth().testTag(MyProfileScreenTestTag.INFO_RATING_BAR)) {
      tabLabels.forEachIndexed { index, label ->
        val tab = ProfileTab.entries[index]

        val tabTestTag =
            when (tab) {
              ProfileTab.INFO -> MyProfileScreenTestTag.INFO_TAB
              ProfileTab.LISTINGS -> MyProfileScreenTestTag.LISTINGS_TAB
              ProfileTab.RATING -> MyProfileScreenTestTag.RATING_TAB
              ProfileTab.HISTORY -> MyProfileScreenTestTag.HISTORY_TAB
            }

        Box(
            modifier =
                Modifier.weight(1f)
                    .clickable { selectedTab.value = tab }
                    .padding(vertical = 12.dp)
                    .testTag(tabTestTag),
            contentAlignment = Alignment.Center) {
              Text(
                  text = label,
                  fontWeight = if (selectedTab.value == tab) FontWeight.Bold else FontWeight.Normal,
                  color =
                      if (selectedTab.value == tab) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                  modifier =
                      Modifier.onGloballyPositioned {
                        textWidthsPx[index] = it.size.width.toFloat()
                      })
            }
      }
    }

    // When the selected tab changes, animate the indicator's position and width
    val transition = updateTransition(targetState = selectedTab.value, label = "tabIndicator")

    // Calculate the indicator's offset and width based on the selected tab
    val indicatorOffsetPx by
        transition.animateFloat(label = "offsetAnim") { tab ->
          val index = tabIndex(tab)
          val textWidth = textWidthsPx[index]
          tabWidthPx * index + (tabWidthPx - textWidth) / 2f
        }

    // Calculate the indicator's width based on the selected tab
    val indicatorWidthPx by
        transition.animateFloat(label = "widthAnim") { tab -> textWidthsPx[tabIndex(tab)] }

    Box(modifier = Modifier.fillMaxWidth().height(indicatorHeight)) {
      // Draw the animated indicator
      Box(
          modifier =
              Modifier.offset { IntOffset(indicatorOffsetPx.toInt(), 0) }
                  .width(with(density) { indicatorWidthPx.toDp() })
                  .height(indicatorHeight)
                  .background(MaterialTheme.colorScheme.primary))
    }

    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun RatingContent(ui: MyProfileUIState) {

  Text(
      text = "Your Ratings",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      modifier =
          Modifier.padding(horizontal = 16.dp).testTag(MyProfileScreenTestTag.RATING_SECTION))
  Spacer(modifier = Modifier.height(8.dp))

  when {
    ui.ratingsLoading -> {
      Box(
          modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
          contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
    }
    ui.ratingsLoadError != null -> {
      Text(
          text = ui.ratingsLoadError,
          style = MaterialTheme.typography.bodyMedium,
          color = Color.Red,
          modifier = Modifier.padding(horizontal = 16.dp))
    }
    ui.ratings.isEmpty() -> {
      Text(
          text = "You don’t have any ratings yet.",
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(horizontal = 16.dp))
    }
    else -> {
      val creatorProfile = ui.toProfile

      LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        items(ui.ratings) { rating ->
          RatingCard(rating = rating, creator = creatorProfile)
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }
  }
}
