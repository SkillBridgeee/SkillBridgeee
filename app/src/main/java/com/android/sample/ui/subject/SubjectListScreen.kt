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
import com.android.sample.ui.components.ProposalCard
import com.android.sample.ui.components.RequestCard

/** Test tags for the different elements of the SubjectListScreen */
object SubjectListTestTags {
  const val SEARCHBAR = "SubjectListTestTags.SEARCHBAR"
  const val CATEGORY_SELECTOR = "SubjectListTestTags.CATEGORY_SELECTOR"
  const val LISTING_LIST = "SubjectListTestTags.LISTING_LIST"
  const val LISTING_CARD = "SubjectListTestTags.LISTING_CARD"
  const val LISTING_BOOK_BUTTON = "SubjectListTestTags.LISTING_BOOK_BUTTON"
}

/**
 * Generates a placeholder text for the category selector based on available skills.
 */
private fun getCategoryPlaceholder(skillsForSubject: List<String>): String {
  return if (skillsForSubject.isNotEmpty()) {
    val sampleSkills = skillsForSubject.take(3).joinToString(", ") { it.lowercase() }
    "e.g. $sampleSkills, ..."
  } else {
    "e.g. Maths, Violin, Python, ..."
  }
}

/**
 * Composable for displaying the loading indicator or error message.
 */
@Composable
private fun LoadingOrErrorSection(isLoading: Boolean, error: String?) {
  if (isLoading) {
    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
  } else if (error != null) {
    Text(error, color = MaterialTheme.colorScheme.error)
  }
}

/**
 * Composable for rendering a listing item (Proposal or Request card).
 */
@Composable
private fun ListingItem(
    listing: com.android.sample.model.listing.Listing,
    onListingClick: (String) -> Unit
) {
  when (listing) {
    is com.android.sample.model.listing.Proposal -> {
      ProposalCard(
          proposal = listing,
          onClick = onListingClick,
          testTag = SubjectListTestTags.LISTING_CARD)
    }
    is com.android.sample.model.listing.Request -> {
      RequestCard(
          request = listing,
          onClick = onListingClick,
          testTag = SubjectListTestTags.LISTING_CARD)
    }
  }
}

/**
 * Screen showing a list of tutors for a specific subject, with search and category filter.
 *
 * @param viewModel ViewModel providing the data
 * @param subject The main subject to display listings for
 * @param onListingClick Callback when a listing is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectListScreen(
    viewModel: SubjectListViewModel,
    subject: MainSubject?,
    onListingClick: (String) -> Unit = {}
) {
  val ui by viewModel.ui.collectAsState()
  LaunchedEffect(subject) {
    subject?.let { viewModel.refresh(it) }
  }

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
                        ?: getCategoryPlaceholder(skillsForSubject),
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

      Text(
          "All $mainSubjectString lessons",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold)

      Spacer(Modifier.height(8.dp))

      // Loading indicator or error message, if neither, this block shows nothing
      LoadingOrErrorSection(ui.isLoading, ui.error)

      // List of listings
      LazyColumn(
          modifier = Modifier.fillMaxSize().testTag(SubjectListTestTags.LISTING_LIST),
          contentPadding = PaddingValues(bottom = 24.dp)) {
            items(ui.listings) { item ->
              ListingItem(listing = item.listing, onListingClick = onListingClick)
              Spacer(Modifier.height(16.dp))
            }
          }
    }
  }
}
