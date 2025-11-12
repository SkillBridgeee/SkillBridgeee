package com.android.sample.ui.newListing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.android.sample.model.listing.ListingType
import com.android.sample.model.skill.MainSubject
import com.android.sample.ui.components.AppButton
import com.android.sample.ui.components.LocationInputField
import com.android.sample.ui.screens.newSkill.NewSkillViewModel

object NewSkillScreenTestTag {
  const val BUTTON_SAVE_SKILL = "buttonSaveSkill"
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewListingScreen(
    skillViewModel: NewSkillViewModel = viewModel(),
    profileId: String,
    navController: NavController
) {
  val skillUIState by skillViewModel.uiState.collectAsState()

  LaunchedEffect(skillUIState.addSuccess) {
    if (skillUIState.addSuccess) {
      navController.popBackStack()
      skillViewModel.clearAddSuccess()
    }
  }

  val buttonText =
      when (skillUIState.listingType) {
        ListingType.PROPOSAL -> "Create Proposal"
        ListingType.REQUEST -> "Create Request"
        null -> "Create Listing"
      }

  Scaffold(
      floatingActionButton = {
        AppButton(
            text = buttonText,
            onClick = { skillViewModel.addListing() },
            testTag = NewSkillScreenTestTag.BUTTON_SAVE_SKILL)
      },
      floatingActionButtonPosition = FabPosition.Center) { pd ->
        ListingContent(pd = pd, profileId = profileId, skillViewModel = skillViewModel)
      }
}

@Composable
fun ListingContent(pd: PaddingValues, profileId: String, skillViewModel: NewSkillViewModel) {
  val skillUIState by skillViewModel.uiState.collectAsState()

  LaunchedEffect(profileId) { skillViewModel.load() }

  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth().padding(pd)) {
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
                    text = "Create Your Listing",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag(NewSkillScreenTestTag.CREATE_LESSONS_TITLE))

                Spacer(Modifier.height(10.dp))

                ListingTypeMenu(
                    selectedListingType = skillUIState.listingType,
                    onListingTypeSelected = { skillViewModel.setListingType(it) },
                    errorMsg = skillUIState.invalidListingTypeMsg)

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = skillUIState.title,
                    onValueChange = skillViewModel::setTitle,
                    label = { Text("Course Title") },
                    placeholder = { Text("Title") },
                    isError = skillUIState.invalidTitleMsg != null,
                    supportingText = {
                      skillUIState.invalidTitleMsg?.let {
                        Text(
                            text = it,
                            modifier = Modifier.testTag(NewSkillScreenTestTag.INVALID_TITLE_MSG))
                      }
                    },
                    modifier =
                        Modifier.fillMaxWidth().testTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE))

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = skillUIState.description,
                    onValueChange = skillViewModel::setDescription,
                    label = { Text("Description") },
                    placeholder = { Text("Description of the skill") },
                    isError = skillUIState.invalidDescMsg != null,
                    supportingText = {
                      skillUIState.invalidDescMsg?.let {
                        Text(
                            text = it,
                            modifier = Modifier.testTag(NewSkillScreenTestTag.INVALID_DESC_MSG))
                      }
                    },
                    modifier =
                        Modifier.fillMaxWidth().testTag(NewSkillScreenTestTag.INPUT_DESCRIPTION))

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = skillUIState.price,
                    onValueChange = skillViewModel::setPrice,
                    label = { Text("Hourly Rate") },
                    placeholder = { Text("Price per Hour") },
                    isError = skillUIState.invalidPriceMsg != null,
                    supportingText = {
                      skillUIState.invalidPriceMsg?.let {
                        Text(
                            text = it,
                            modifier = Modifier.testTag(NewSkillScreenTestTag.INVALID_PRICE_MSG))
                      }
                    },
                    modifier = Modifier.fillMaxWidth().testTag(NewSkillScreenTestTag.INPUT_PRICE))

                Spacer(Modifier.height(8.dp))

                SubjectMenu(
                    selectedSubject = skillUIState.subject,
                    onSubjectSelected = skillViewModel::setSubject,
                    errorMsg = skillUIState.invalidSubjectMsg)

                if (skillUIState.subject != null) {
                  Spacer(Modifier.height(8.dp))

                  SubSkillMenu(
                      selectedSubSkill = skillUIState.selectedSubSkill,
                      options = skillUIState.subSkillOptions,
                      onSubSkillSelected = skillViewModel::setSubSkill,
                      errorMsg = skillUIState.invalidSubSkillMsg)
                }

                LocationInputField(
                    locationQuery = skillUIState.locationQuery,
                    locationSuggestions = skillUIState.locationSuggestions,
                    onLocationQueryChange = skillViewModel::setLocationQuery,
                    errorMsg = skillUIState.invalidLocationMsg,
                    onLocationSelected = { location ->
                      skillViewModel.setLocationQuery(location.name)
                      skillViewModel.setLocation(location)
                    })
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
                    modifier = Modifier.testTag(NewSkillScreenTestTag.INVALID_SUBJECT_MSG))
              }
            },
            modifier =
                Modifier.testTag(NewSkillScreenTestTag.SUBJECT_FIELD).menuAnchor().fillMaxWidth())

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN)) {
              subjects.forEachIndexed { index, subject ->
                DropdownMenuItem(
                    text = { Text(subject.name) },
                    onClick = {
                      onSubjectSelected(subject)
                      expanded = false
                    },
                    modifier =
                        Modifier.testTag(
                            "${NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX}_$index"))
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
                    modifier = Modifier.testTag(NewSkillScreenTestTag.INVALID_LISTING_TYPE_MSG))
              }
            },
            modifier =
                Modifier.testTag(NewSkillScreenTestTag.LISTING_TYPE_FIELD)
                    .menuAnchor()
                    .fillMaxWidth())

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag(NewSkillScreenTestTag.LISTING_TYPE_DROPDOWN)) {
              listingTypes.forEachIndexed { index, type ->
                DropdownMenuItem(
                    text = { Text(type.name) },
                    onClick = {
                      onListingTypeSelected(type)
                      expanded = false
                    },
                    modifier =
                        Modifier.testTag(
                            "${NewSkillScreenTestTag.LISTING_TYPE_DROPDOWN_ITEM_PREFIX}_$index"))
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
                    modifier = Modifier.testTag(NewSkillScreenTestTag.INVALID_SUB_SKILL_MSG))
              }
            },
            modifier =
                Modifier.testTag(NewSkillScreenTestTag.SUB_SKILL_FIELD).menuAnchor().fillMaxWidth())

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag(NewSkillScreenTestTag.SUB_SKILL_DROPDOWN)) {
              options.forEachIndexed { index, opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                      onSubSkillSelected(opt)
                      expanded = false
                    },
                    modifier =
                        Modifier.testTag(
                            "${NewSkillScreenTestTag.SUB_SKILL_DROPDOWN_ITEM_PREFIX}_$index"))
              }
            }
      }
}
