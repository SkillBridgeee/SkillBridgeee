package com.android.sample.ui.subject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepositoryLocal
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.components.RatingStars
import com.android.sample.ui.theme.TealChip
import com.android.sample.ui.theme.White

object SubjectListTestTags {
  const val SEARCHBAR = "SubjectListTestTags.SEARCHBAR"
  const val CATEGORY_SELECTOR = "SubjectListTestTags.CATEGORY_SELECTOR"
  const val TOP_TUTORS_SECTION = "SubjectListTestTags.TOP_TUTORS_SECTION"
  const val TUTOR_LIST = "SubjectListTestTags.TUTOR_LIST"
  const val TUTOR_CARD = "SubjectListTestTags.TUTOR_CARD"
  const val TUTOR_BOOK_BUTTON = "SubjectListTestTags.TUTOR_BOOK_BUTTON"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectListScreen(
    viewModel: SubjectListViewModel,
    onBookTutor: (Profile) -> Unit = {},
) {
  val ui by viewModel.ui.collectAsState()

  LaunchedEffect(Unit) { viewModel.refresh() }

  Scaffold { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
      // Search
      OutlinedTextField(
          value = ui.query,
          onValueChange = viewModel::onQueryChanged,
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          placeholder = { Text("Find a tutor about...") },
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
                value = ui.selectedSkill?.replace('_', ' ') ?: "e.g. instrument, sing, mix, ...",
                onValueChange = {},
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier =
                    Modifier.menuAnchor()
                        .fillMaxWidth()
                        .testTag(SubjectListTestTags.CATEGORY_SELECTOR))

            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
              // "All" option
              DropdownMenuItem(
                  text = { Text("All") },
                  onClick = {
                    viewModel.onSkillSelected(null)
                    expanded = false
                  })
              ui.skillsForSubject.forEach { skillName ->
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
          "All ${ui.mainSubject.name.lowercase()} lessons",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold)

      Spacer(Modifier.height(8.dp))

      // Top Rated section
      if (ui.topTutors.isNotEmpty()) {
        Column(modifier = Modifier.testTag(SubjectListTestTags.TOP_TUTORS_SECTION)) {
          Text(
              "Top-Rated Tutors",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(vertical = 8.dp))
          ui.topTutors.forEach { p ->
            TutorCard(profile = p, onBook = onBookTutor)
            Spacer(Modifier.height(8.dp))
          }
        }
      }
      Spacer(Modifier.height(8.dp))

      if (ui.isLoading) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
      } else if (ui.error != null) {
        Text(ui.error!!, color = MaterialTheme.colorScheme.error)
      }

      LazyColumn(
          modifier = Modifier.fillMaxSize().testTag(SubjectListTestTags.TUTOR_LIST),
          contentPadding = PaddingValues(bottom = 24.dp)) {
            items(ui.tutors) { p ->
              TutorCard(profile = p, onBook = onBookTutor)
              Spacer(Modifier.height(16.dp))
            }
          }
    }
  }
}

/** Small helper to show a tutor card in both sections. */
@Composable
private fun TutorCard(profile: Profile, onBook: (Profile) -> Unit) {
  ElevatedCard(
      shape = MaterialTheme.shapes.large,
      colors = CardDefaults.elevatedCardColors(containerColor = White),
      modifier = Modifier.fillMaxWidth().testTag(SubjectListTestTags.TUTOR_CARD)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(16.dp)) {
              // Avatar placeholder
              Box(
                  modifier =
                      Modifier.size(44.dp)
                          .clip(MaterialTheme.shapes.extraLarge)
                          .background(MaterialTheme.colorScheme.surfaceVariant))

              Spacer(Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name.ifBlank { "Tutor" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)

                // Secondary line (could be top skill; we don’t have it here, so show description)
                val secondary = profile.description.ifBlank { "Lessons" }
                Text(
                    text = secondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)

                Spacer(Modifier.height(4.dp))

                RatingRow(rating = profile.tutorRating)
              }

              Spacer(Modifier.width(8.dp))

              Column(horizontalAlignment = Alignment.End) {
                // Price is not available in Profile; show placeholder.
                Text(text = "—/hr", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = { onBook(profile) },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = TealChip,
                            contentColor = White,
                            disabledContainerColor = TealChip.copy(alpha = 0.38f),
                            disabledContentColor = White.copy(alpha = 0.38f)),
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.testTag(SubjectListTestTags.TUTOR_BOOK_BUTTON)) {
                      Text("Book")
                    }
              }
            }
      }
}

@Composable
private fun RatingRow(rating: RatingInfo) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    RatingStars(ratingOutOfFive = rating.averageRating)
    Spacer(Modifier.width(6.dp))
    Text(
        "(${rating.totalRatings})",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Preview(showBackground = true)
@Composable
private fun SubjectListScreenPreview() {
  val previous = ProfileRepositoryProvider.repository
  DisposableEffect(Unit) {
    ProfileRepositoryProvider.repository = ProfileRepositoryLocal()
    onDispose { ProfileRepositoryProvider.repository = previous }
  }

  val vm: SubjectListViewModel = viewModel()
  LaunchedEffect(Unit) { vm.refresh() }

  MaterialTheme { Surface { SubjectListScreen(viewModel = vm) } }
}
