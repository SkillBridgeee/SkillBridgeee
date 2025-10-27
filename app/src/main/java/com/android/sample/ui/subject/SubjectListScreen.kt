package com.android.sample.ui.subject

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.user.Profile
import com.android.sample.ui.components.TutorCard

/** Test tags for the different elements of the SubjectListScreen */
object SubjectListTestTags {
  const val SEARCHBAR = "SubjectListTestTags.SEARCHBAR"
  const val CATEGORY_SELECTOR = "SubjectListTestTags.CATEGORY_SELECTOR"
  const val TOP_TUTORS_SECTION = "SubjectListTestTags.TOP_TUTORS_SECTION"
  const val TUTOR_LIST = "SubjectListTestTags.TUTOR_LIST"
  const val TUTOR_CARD = "SubjectListTestTags.TUTOR_CARD"
  const val TUTOR_BOOK_BUTTON = "SubjectListTestTags.TUTOR_BOOK_BUTTON"
}

/**
 * Screen showing a list of tutors for a specific subject, with search and category filter.
 *
 * @param viewModel ViewModel providing the data
 * @param onBookTutor Callback when the "Book" button is pressed on a tutor card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectListScreen(
    viewModel: SubjectListViewModel,
    onBookTutor: (Profile) -> Unit = {},
    subject: MainSubject?
) {
  val ui by viewModel.ui.collectAsState()
  LaunchedEffect(Unit) { viewModel.refresh() }
  val skillsForSubject = viewModel.getSkillsForSubject(subject)
  val mainSubjectString = viewModel.subjectToString(subject)

  Scaffold { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
      // Search
      OutlinedTextField(
          value = ui.query,
          onValueChange = viewModel::onQueryChanged,
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          placeholder = { Text("Find a tutor about $mainSubjectString") },
          singleLine = true,
          modifier =
              Modifier.fillMaxWidth().padding(top = 8.dp).testTag(SubjectListTestTags.SEARCHBAR))

      Spacer(Modifier.height(12.dp))

      // Category selector (skills for current main subject)
      var expanded by remember { mutableStateOf(false) }
      ExposedDropdownMenuBox(
          expanded = expanded,
          onExpandedChange = { expanded = !expanded },
          modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                readOnly = true,
                onValueChange = {},
                value =
                    ui.selectedSkill?.replace('_', ' ')
                        ?: buildString {
                          append("e.g. ")
                          skillsForSubject.take(3).forEachIndexed { index, skill ->
                            append(skill.lowercase())
                            if (index < skillsForSubject.take(3).lastIndex) append(", ")
                          }
                          append(", ...")
                        },
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier =
                    Modifier.menuAnchor()
                        .fillMaxWidth()
                        .testTag(SubjectListTestTags.CATEGORY_SELECTOR))

            // Hide the menu when a dismiss happens (expanded = false)
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
              // "All" option -> no skill filter
              DropdownMenuItem(
                  text = { Text("All") },
                  onClick = {
                    viewModel.onSkillSelected(null)
                    expanded = false
                  })
              skillsForSubject.forEach { skillName ->
                DropdownMenuItem(
                    text = {
                      Text(
                          skillName.replace('_', ' ').lowercase().replaceFirstChar {
                            it.titlecase()
                          })
                    },
                    onClick = {
                      viewModel.onSkillSelected(skillName)
                      expanded = false
                    })
              }
            }
          }

      Spacer(Modifier.height(16.dp))

      // All tutors list
      Text(
          "All $mainSubjectString lessons",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold)

      Spacer(Modifier.height(8.dp))

      // Loading indicator or error message, if neither, this block shows nothing
      if (ui.isLoading) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
      } else if (ui.error != null) {
        Text(ui.error!!, color = MaterialTheme.colorScheme.error)
      }

      // Tutors list
      LazyColumn(
          modifier = Modifier.fillMaxSize().testTag(SubjectListTestTags.TUTOR_LIST),
          contentPadding = PaddingValues(bottom = 24.dp)) {
            items(ui.tutors) { p ->
              // Reuse TutorCard from components
              TutorCard(
                  profile = p,
                  pricePerHour = null,
                  onPrimaryAction = onBookTutor,
                  cardTestTag = SubjectListTestTags.TUTOR_CARD,
                  buttonTestTag = SubjectListTestTags.TUTOR_BOOK_BUTTON)
              Spacer(Modifier.height(16.dp))
            }
          }
    }
  }
}
