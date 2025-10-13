package com.android.sample.ui.tutor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.ui.components.RatingStars
import com.android.sample.ui.components.SkillChip
import com.android.sample.ui.components.TopAppBar
import com.android.sample.ui.theme.White

/** Test tags for the Tutor Profile screen. */
object TutorPageTestTags {
  const val PFP = "TutorPageTestTags.PFP"
  const val NAME = "TutorPageTestTags.NAME"
  const val RATING = "TutorPageTestTags.RATING"
  const val SKILLS_SECTION = "TutorPageTestTags.SKILLS_SECTION"
  const val SKILL = "TutorPageTestTags.SKILL"
  const val CONTACT_SECTION = "TutorPageTestTags.CONTACT_SECTION"
  const val TOP_BAR = "TutorPageTestTags.TOP_BAR"
}

/**
 * The Tutor Profile screen displays detailed information about a tutor, including their name,
 * profile picture, skills, and contact information.
 *
 * @param tutorId The unique identifier of the tutor whose profile is to be displayed.
 * @param vm The ViewModel that provides the data for the screen.
 * @param navController The NavHostController for navigation actions.
 * @param modifier The modifier to be applied to the composable.
 */
@Composable
fun TutorProfileScreen(
    tutorId: String,
    vm: TutorProfileViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
  LaunchedEffect(tutorId) { vm.load(tutorId) }
  val state by vm.state.collectAsStateWithLifecycle()

  Scaffold(
      topBar = {
        Box(Modifier.fillMaxWidth().testTag(TutorPageTestTags.TOP_BAR)) {
          TopAppBar(navController = navController)
        }
      }) { innerPadding ->
        // Show a loading spinner while loading and the content when loaded
        if (state.loading) {
          Box(
              modifier = modifier.fillMaxSize().padding(innerPadding),
              contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
              }
        } else {
          val profile = state.profile
          if (profile != null) {
            TutorContent(
                profile = profile,
                skills = state.skills,
                modifier = modifier,
                padding = innerPadding)
          }
        }
      }
}

/**
 * The main content of the Tutor Profile screen, displaying the tutor's profile information, skills,
 * and contact details.
 *
 * @param profile The profile of the tutor.
 * @param skills The list of skills the tutor offers.
 * @param modifier The modifier to be applied to the composable.
 * @param padding The padding values to be applied to the content.
 */
@Composable
private fun TutorContent(
    profile: Profile,
    skills: List<Skill>,
    modifier: Modifier,
    padding: PaddingValues
) {
  LazyColumn(
      contentPadding = PaddingValues(16.dp),
      modifier = modifier.fillMaxSize().padding(padding),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
          Surface(
              color = White,
              shape = MaterialTheme.shapes.large,
              modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                      Box(
                          modifier = Modifier.fillMaxWidth().height(140.dp),
                          contentAlignment = Alignment.Center) {
                            Box(
                                Modifier.size(96.dp)
                                    .clip(MaterialTheme.shapes.extraLarge)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag(TutorPageTestTags.PFP))
                          }
                      Text(
                          profile.name,
                          style =
                              MaterialTheme.typography.titleLarge.copy(
                                  fontWeight = FontWeight.SemiBold),
                          modifier = Modifier.testTag(TutorPageTestTags.NAME))
                      RatingStars(
                          ratingOutOfFive = profile.tutorRating.averageRating,
                          modifier = Modifier.testTag(TutorPageTestTags.RATING))
                      Text(
                          "(${profile.tutorRating.totalRatings})",
                          style = MaterialTheme.typography.bodyMedium)
                    }
              }
        }

        item {
          Column(modifier = Modifier.fillMaxWidth().testTag(TutorPageTestTags.SKILLS_SECTION)) {
            Text("Skills:", style = MaterialTheme.typography.titleMedium)
          }
        }

        items(skills) { s ->
          SkillChip(skill = s, modifier = Modifier.fillMaxWidth().testTag(TutorPageTestTags.SKILL))
        }

        item {
          Surface(
              color = White,
              shape = MaterialTheme.shapes.large,
              modifier = Modifier.fillMaxWidth().testTag(TutorPageTestTags.CONTACT_SECTION)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MailOutline, contentDescription = "Email")
                    Spacer(Modifier.width(8.dp))
                    Text(profile.email, style = MaterialTheme.typography.bodyMedium)
                  }
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    InstagramGlyph()
                    Spacer(Modifier.width(8.dp))
                    val handle = "@${profile.name.replace(" ", "")}"
                    Text(handle, style = MaterialTheme.typography.bodyMedium)
                  }
                }
              }
        }
      }
}

/**
 * A simple Instagram glyph drawn using Canvas (Ai generated).
 *
 * @param modifier The modifier to be applied to the composable.
 */
@Composable
private fun InstagramGlyph(modifier: Modifier = Modifier) {
  val color = LocalContentColor.current
  Canvas(modifier.size(24.dp)) {
    val w = size.width
    val h = size.height
    val stroke = w * 0.12f
    // Rounded square outline
    drawRoundRect(
        color = color,
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.22f, h * 0.22f),
        style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    // Camera lens
    drawCircle(
        color = color,
        radius = w * 0.22f,
        center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f),
        style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    // Small dot
    drawCircle(
        color = color,
        radius = w * 0.06f,
        center = androidx.compose.ui.geometry.Offset(w * 0.78f, h * 0.22f),
        style = Fill)
  }
}
