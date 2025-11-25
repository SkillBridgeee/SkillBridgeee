package com.android.sample.ui.newListing

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.android.sample.model.listing.ListingType
import com.android.sample.model.map.GpsLocationProvider
import com.android.sample.model.skill.MainSubject
import com.android.sample.ui.components.AppButton
import com.android.sample.ui.components.LocationInputField
import com.android.sample.ui.navigation.NavRoutes

object NewListingScreenTestTag {
  const val BUTTON_SAVE_LISTING = "buttonSaveListing"
  const val CREATE_LESSONS_TITLE = "createLessonsTitle"
  const val INPUT_COURSE_TITLE = "inputCourseTitle"
  const val INVALID_TITLE_MSG = "invalidTitleMsg"
  const val INPUT_DESCRIPTION = "inputDescription"
  const val INVALID_DESC_MSG = "invalidDescMsg"
  const val INPUT_PRICE = "inputPrice"
  const val INVALID_PRICE_MSG = "invalidPriceMsg"
  const val SUBJECT_FIELD = "subjectField"
  const val SUBJECT_DROPDOWN = "subjectDropdown"
  const val SUBJECT_DROPDOWN_ITEM_PREFIX = "subjectItem"
  const val INVALID_SUBJECT_MSG = "invalidSubjectMsg"
  const val SUB_SKILL_FIELD = "subSkillField"
  const val SUB_SKILL_DROPDOWN = "subSkillDropdown"
  const val SUB_SKILL_DROPDOWN_ITEM_PREFIX = "subSkillItem"
  const val INVALID_SUB_SKILL_MSG = "invalidSubSkillMsg"
  const val LISTING_TYPE_FIELD = "listingTypeField"
  const val LISTING_TYPE_DROPDOWN = "listingTypeDropdown"
  const val LISTING_TYPE_DROPDOWN_ITEM_PREFIX = "listingTypeItem"
  const val INVALID_LISTING_TYPE_MSG = "invalidListingTypeMsg"
  const val BUTTON_USE_MY_LOCATION = "buttonUseMyLocation"

  const val INPUT_LOCATION_FIELD = "inputLocationField"

  const val SCROLLABLE_SCREEN = "scrollNewListing"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewListingScreen(
    skillViewModel: NewListingViewModel = viewModel(),
    profileId: String,
    listingId: String?,
    navController: NavController,
) {
  val listingUIState by skillViewModel.uiState.collectAsState()
  val isEditMode = listingId != null

  LaunchedEffect(listingUIState.addSuccess) {
    if (listingUIState.addSuccess) {
      if (isEditMode) {
        navController.navigate(NavRoutes.createProfileRoute(profileId)) {
          popUpTo(NavRoutes.createProfileRoute(profileId)) { inclusive = true }
        }
      } else {
        navController.popBackStack()
      }
      skillViewModel.clearAddSuccess()
    }
  }

  val buttonText =
      if (isEditMode) "Save Changes"
      else
          when (listingUIState.listingType) {
            ListingType.PROPOSAL -> "Create Proposal"
            ListingType.REQUEST -> "Create Request"
            null -> "Create Listing"
          }

  val titleText = if (isEditMode) "Edit Listing" else "Create Your Listing"

  Scaffold(
      floatingActionButton = {
        AppButton(
            text = buttonText,
            onClick = { skillViewModel.addListing() },
            // enabled = !listingUIState.isSaving,
            testTag = NewListingScreenTestTag.BUTTON_SAVE_LISTING)
      },
      floatingActionButtonPosition = FabPosition.Center) { pd ->
        ListingContent(
            pd = pd,
            listingId = listingId,
            listingViewModel = skillViewModel,
            titleText = titleText)
      }
}

/**
 * The content of the New Listing screen, including all input fields and dropdowns.
 *
 * @param pd The padding values provided by the Scaffold.
 * @param listingId The ID of the listing being edited, or null if creating a new listing.
 * @param listingViewModel The ViewModel managing the state of the listing.
 * @param titleText The title text to display at the top of the screen.
 */
@Composable
fun ListingContent(
    pd: PaddingValues,
    listingId: String?,
    listingViewModel: NewListingViewModel,
    titleText: String
) {
  val listingUIState by listingViewModel.uiState.collectAsState()

  ListingLoader(listingId = listingId, listingViewModel = listingViewModel)

  val scrollState = rememberScrollState()

  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier =
          Modifier.fillMaxWidth()
              .padding(pd)
              .verticalScroll(scrollState)
              .testTag(NewListingScreenTestTag.SCROLLABLE_SCREEN)) {
        Spacer(Modifier.height(20.dp))

        Box(
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.9f)
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(Color.Gray, Color.LightGray)),
                        shape = MaterialTheme.shapes.medium)
                    .padding(16.dp)) {
              Column {
                Text(
                    text = titleText,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag(NewListingScreenTestTag.CREATE_LESSONS_TITLE))

                Spacer(Modifier.height(10.dp))

                ListingTypeMenu(
                    selectedListingType = listingUIState.listingType,
                    onListingTypeSelected = { listingViewModel.setListingType(it) },
                    errorMsg = listingUIState.invalidListingTypeMsg)

                Spacer(Modifier.height(8.dp))

                TitleField(
                    title = listingUIState.title,
                    invalidTitleMsg = listingUIState.invalidTitleMsg,
                    onTitleChange = listingViewModel::setTitle,
                )

                Spacer(Modifier.height(8.dp))

                DescriptionField(
                    description = listingUIState.description,
                    invalidDescMsg = listingUIState.invalidDescMsg,
                    onDescriptionChange = listingViewModel::setDescription,
                )

                Spacer(Modifier.height(8.dp))

                PriceField(
                    price = listingUIState.price,
                    invalidPriceMsg = listingUIState.invalidPriceMsg,
                    onPriceChange = listingViewModel::setPrice,
                )

                Spacer(Modifier.height(8.dp))

                SubjectMenu(
                    selectedSubject = listingUIState.subject,
                    onSubjectSelected = listingViewModel::setSubject,
                    errorMsg = listingUIState.invalidSubjectMsg)

                if (listingUIState.subject != null) {
                  Spacer(Modifier.height(8.dp))

                  SubSkillMenu(
                      selectedSubSkill = listingUIState.selectedSubSkill,
                      options = listingUIState.subSkillOptions,
                      onSubSkillSelected = listingViewModel::setSubSkill,
                      errorMsg = listingUIState.invalidSubSkillMsg)
                }

                LocationSection(listingViewModel = listingViewModel)
              }
            }
      }
}

/**
 * Loads the listing data if editing an existing listing, or initializes for creation if no ID is
 * provided.
 *
 * @param listingId The ID of the listing to load, or null to create a new listing.
 * @param listingViewModel The ViewModel responsible for managing the listing state.
 */
@Composable
private fun ListingLoader(
    listingId: String?,
    listingViewModel: NewListingViewModel,
) {
  LaunchedEffect(listingId) {
      listingViewModel.load(listingId)
  }
}

/**
 * Composable function for the Title input field in the New Listing screen.
 *
 * @param title The current title value.
 * @param invalidTitleMsg An optional error message to display if the title is invalid.
 * @param onTitleChange Callback function to handle title changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TitleField(
    title: String,
    invalidTitleMsg: String?,
    onTitleChange: (String) -> Unit,
) {
  OutlinedTextField(
      value = title,
      onValueChange = onTitleChange,
      label = { Text("Course Title") },
      placeholder = { Text("Title") },
      isError = invalidTitleMsg != null,
      supportingText = {
        invalidTitleMsg?.let {
          Text(text = it, modifier = Modifier.testTag(NewListingScreenTestTag.INVALID_TITLE_MSG))
        }
      },
      modifier = Modifier.fillMaxWidth().testTag(NewListingScreenTestTag.INPUT_COURSE_TITLE))
}

/**
 * Composable function for the Description input field in the New Listing screen.
 *
 * @param description The current description value.
 * @param invalidDescMsg An optional error message to display if the description is invalid.
 * @param onDescriptionChange Callback function to handle description changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DescriptionField(
    description: String,
    invalidDescMsg: String?,
    onDescriptionChange: (String) -> Unit,
) {
  OutlinedTextField(
      value = description,
      onValueChange = onDescriptionChange,
      label = { Text("Description") },
      placeholder = { Text("Description of the skill") },
      isError = invalidDescMsg != null,
      supportingText = {
        invalidDescMsg?.let {
          Text(text = it, modifier = Modifier.testTag(NewListingScreenTestTag.INVALID_DESC_MSG))
        }
      },
      modifier = Modifier.fillMaxWidth().testTag(NewListingScreenTestTag.INPUT_DESCRIPTION))
}

/**
 * Composable function for the Price input field in the New Listing screen.
 *
 * @param price The current price value.
 * @param invalidPriceMsg An optional error message to display if the price is invalid.
 * @param onPriceChange Callback function to handle price changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriceField(
    price: String,
    invalidPriceMsg: String?,
    onPriceChange: (String) -> Unit,
) {
  OutlinedTextField(
      value = price,
      onValueChange = onPriceChange,
      label = { Text("Hourly Rate") },
      placeholder = { Text("Price per Hour") },
      isError = invalidPriceMsg != null,
      supportingText = {
        invalidPriceMsg?.let {
          Text(text = it, modifier = Modifier.testTag(NewListingScreenTestTag.INVALID_PRICE_MSG))
        }
      },
      modifier = Modifier.fillMaxWidth().testTag(NewListingScreenTestTag.INPUT_PRICE))
}

/**
 * Composable function for the Location section in the New Listing screen. Includes a location input
 * field and a button to use the device's current location.
 *
 * @param listingViewModel The ViewModel managing the state of the listing.
 */
@Composable
private fun LocationSection(
    listingViewModel: NewListingViewModel,
) {
  val listingUIState by listingViewModel.uiState.collectAsState()

  val context = LocalContext.current
  val permission = android.Manifest.permission.ACCESS_FINE_LOCATION

  val permissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
          listingViewModel.fetchLocationFromGps(GpsLocationProvider(context), context)
        } else {
          listingViewModel.onLocationPermissionDenied()
        }
      }

  Column {
    Box(modifier = Modifier.testTag(NewListingScreenTestTag.INPUT_LOCATION_FIELD)) {
      LocationInputField(
          locationQuery = listingUIState.locationQuery,
          locationSuggestions = listingUIState.locationSuggestions,
          onLocationQueryChange = listingViewModel::setLocationQuery,
          errorMsg = listingUIState.invalidLocationMsg,
          onLocationSelected = { location ->
            listingViewModel.setLocationQuery(location.name)
            listingViewModel.setLocation(location)
          })

      IconButton(
          onClick = {
            val granted =
                ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED

            if (granted) {
              listingViewModel.fetchLocationFromGps(GpsLocationProvider(context), context)
            } else {
              permissionLauncher.launch(permission)
            }
          },
          modifier =
              Modifier.align(Alignment.CenterEnd)
                  .offset(y = (-5).dp)
                  .size(36.dp)
                  .testTag(NewListingScreenTestTag.BUTTON_USE_MY_LOCATION)) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Use my location",
                tint = MaterialTheme.colorScheme.primary)
          }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectMenu(
    selectedSubject: MainSubject?,
    onSubjectSelected: (MainSubject) -> Unit,
    errorMsg: String?
) {
  var expanded by remember { mutableStateOf(false) }
  val subjects = MainSubject.entries

  ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = it },
      modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedSubject?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Subject") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            isError = errorMsg != null,
            supportingText = {
              errorMsg?.let {
                Text(
                    text = it,
                    modifier = Modifier.testTag(NewListingScreenTestTag.INVALID_SUBJECT_MSG))
              }
            },
            modifier =
                Modifier.testTag(NewListingScreenTestTag.SUBJECT_FIELD).menuAnchor().fillMaxWidth())

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag(NewListingScreenTestTag.SUBJECT_DROPDOWN)) {
              subjects.forEachIndexed { index, subject ->
                DropdownMenuItem(
                    text = { Text(subject.name) },
                    onClick = {
                      onSubjectSelected(subject)
                      expanded = false
                    },
                    modifier =
                        Modifier.testTag(
                            "${NewListingScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX}_$index"))
              }
            }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingTypeMenu(
    selectedListingType: ListingType?,
    onListingTypeSelected: (ListingType) -> Unit,
    errorMsg: String?
) {
  var expanded by remember { mutableStateOf(false) }
  val listingTypes = ListingType.entries

  ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = it },
      modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedListingType?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Listing Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            isError = errorMsg != null,
            supportingText = {
              errorMsg?.let {
                Text(
                    text = it,
                    modifier = Modifier.testTag(NewListingScreenTestTag.INVALID_LISTING_TYPE_MSG))
              }
            },
            modifier =
                Modifier.testTag(NewListingScreenTestTag.LISTING_TYPE_FIELD)
                    .menuAnchor()
                    .fillMaxWidth())

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag(NewListingScreenTestTag.LISTING_TYPE_DROPDOWN)) {
              listingTypes.forEachIndexed { index, type ->
                DropdownMenuItem(
                    text = { Text(type.name) },
                    onClick = {
                      onListingTypeSelected(type)
                      expanded = false
                    },
                    modifier =
                        Modifier.testTag(
                            "${NewListingScreenTestTag.LISTING_TYPE_DROPDOWN_ITEM_PREFIX}_$index"))
              }
            }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubSkillMenu(
    selectedSubSkill: String?,
    options: List<String>,
    onSubSkillSelected: (String) -> Unit,
    errorMsg: String?
) {
  var expanded by remember { mutableStateOf(false) }

  ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = it },
      modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedSubSkill ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Sub-Subject") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            isError = errorMsg != null,
            supportingText = {
              errorMsg?.let {
                Text(
                    text = it,
                    modifier = Modifier.testTag(NewListingScreenTestTag.INVALID_SUB_SKILL_MSG))
              }
            },
            modifier =
                Modifier.testTag(NewListingScreenTestTag.SUB_SKILL_FIELD)
                    .menuAnchor()
                    .fillMaxWidth())

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag(NewListingScreenTestTag.SUB_SKILL_DROPDOWN)) {
              options.forEachIndexed { index, opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                      onSubSkillSelected(opt)
                      expanded = false
                    },
                    modifier =
                        Modifier.testTag(
                            "${NewListingScreenTestTag.SUB_SKILL_DROPDOWN_ITEM_PREFIX}_$index"))
              }
            }
      }
}
