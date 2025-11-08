package com.android.sample.ui.screens.newSkill

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.sample.model.skill.MainSubject
import com.android.sample.ui.components.AppButton
import com.android.sample.ui.components.LocationInputField

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSkillScreen(skillViewModel: NewSkillViewModel = NewSkillViewModel(), profileId: String) {

  Scaffold(
      floatingActionButton = {
        AppButton(
            text = "Save New Skill",
            onClick = { skillViewModel.addSkill() },
            testTag = NewSkillScreenTestTag.BUTTON_SAVE_SKILL)
      },
      floatingActionButtonPosition = FabPosition.Center,
      content = { pd -> SkillsContent(pd, profileId, skillViewModel) })
}

@Composable
fun SkillsContent(pd: PaddingValues, profileId: String, skillViewModel: NewSkillViewModel) {

  val textSpace = 8.dp

  LaunchedEffect(profileId) { skillViewModel.load() }
  val skillUIState by skillViewModel.uiState.collectAsState()

  val locationSuggestions = skillUIState.locationSuggestions
  val locationQuery = skillUIState.locationQuery
  val locationErrorMsg: String? = skillUIState.invalidLocationMsg

  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth().padding(pd)) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.9f)
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(colors = listOf(Color.Gray, Color.LightGray)),
                        shape = MaterialTheme.shapes.medium)
                    .padding(16.dp)) {
              Column {
                Text(
                    text = "Create Your Lessons !",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag(NewSkillScreenTestTag.CREATE_LESSONS_TITLE))

                Spacer(modifier = Modifier.height(10.dp))

                // Title Input
                OutlinedTextField(
                    value = skillUIState.title,
                    onValueChange = { skillViewModel.setTitle(it) },
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

                Spacer(modifier = Modifier.height(textSpace))

                // Desc Input
                OutlinedTextField(
                    value = skillUIState.description,
                    onValueChange = { skillViewModel.setDescription(it) },
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

                Spacer(modifier = Modifier.height(textSpace))

                // Price Input
                OutlinedTextField(
                    value = skillUIState.price,
                    onValueChange = { skillViewModel.setPrice(it) },
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

                Spacer(modifier = Modifier.height(textSpace))

                SubjectMenu(
                    selectedSubject = skillUIState.subject,
                    skillViewModel = skillViewModel,
                    skillUIState = skillUIState)

                // Sub-skill dropdown, visible when a subject is selected
                if (skillUIState.subject != null) {
                  Spacer(modifier = Modifier.height(textSpace))
                  SubSkillMenu(
                      selectedSubSkill = skillUIState.selectedSubSkill,
                      options = skillUIState.subSkillOptions,
                      skillViewModel = skillViewModel,
                      skillUIState = skillUIState)
                }

                // Location Input with dropdown
                LocationInputField(
                    locationQuery = locationQuery,
                    locationSuggestions = locationSuggestions,
                    onLocationQueryChange = { skillViewModel.setLocationQuery(it) },
                    errorMsg = locationErrorMsg,
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
    skillViewModel: NewSkillViewModel,
    skillUIState: SkillUIState
) {
  var expanded by remember { mutableStateOf(false) }
  val subjects = MainSubject.entries.toTypedArray()

  ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = it },
      modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedSubject?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Subject") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = skillUIState.invalidSubjectMsg != null,
            supportingText = {
              skillUIState.invalidSubjectMsg?.let {
                Text(
                    text = it,
                    modifier = Modifier.testTag(NewSkillScreenTestTag.INVALID_SUBJECT_MSG))
              }
            },
            modifier =
                Modifier.menuAnchor().fillMaxWidth().testTag(NewSkillScreenTestTag.SUBJECT_FIELD))
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN)) {
              subjects.forEach { subject ->
                DropdownMenuItem(
                    text = { Text(subject.name) },
                    onClick = {
                      skillViewModel.setSubject(subject)
                      expanded = false
                    },
                    modifier = Modifier.testTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX))
              }
            }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubSkillMenu(
    selectedSubSkill: String?,
    options: List<String>,
    skillViewModel: NewSkillViewModel,
    skillUIState: SkillUIState
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
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = skillUIState.invalidSubSkillMsg != null,
            supportingText = {
              skillUIState.invalidSubSkillMsg?.let {
                Text(
                    text = it,
                    modifier = Modifier.testTag(NewSkillScreenTestTag.INVALID_SUB_SKILL_MSG))
              }
            },
            modifier =
                Modifier.menuAnchor().fillMaxWidth().testTag(NewSkillScreenTestTag.SUB_SKILL_FIELD))
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag(NewSkillScreenTestTag.SUB_SKILL_DROPDOWN)) {
              options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                      skillViewModel.setSubSkill(opt)
                      expanded = false
                    },
                    modifier =
                        Modifier.testTag(NewSkillScreenTestTag.SUB_SKILL_DROPDOWN_ITEM_PREFIX))
              }
            }
      }
}
