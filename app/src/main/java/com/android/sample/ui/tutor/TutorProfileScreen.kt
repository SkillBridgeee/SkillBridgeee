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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Tutor
import com.android.sample.ui.components.RatingStars
import com.android.sample.ui.components.SkillChip

object TutorPageTestTags {
  const val GO_BACK = "TutorPageTestTags.GO_BACK" // kept for test parity (unused here)
  const val TOP_BAR_TITLE = "TutorPageTestTags.TOP_BAR_TITLE" // kept for test parity (unused here)
  const val PFP = "TutorPageTestTags.PFP"
  const val NAME = "TutorPageTestTags.NAME"
  const val RATING = "TutorPageTestTags.RATING"
  const val SKILLS_SECTION = "TutorPageTestTags.SKILLS_SECTION"
  const val SKILL = "TutorPageTestTags.SKILL"
  const val CONTACT_SECTION = "TutorPageTestTags.CONTACT_SECTION"
}

@Composable
fun TutorProfileScreen(
    tutorId: String,
    vm: TutorProfileViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
  LaunchedEffect(Unit) { vm.load(tutorId) }
  val state by vm.state.collectAsStateWithLifecycle()

  if (state.loading) {
    Box(
        modifier = modifier.fillMaxSize().padding(contentPadding),
        contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
  } else {
    state.tutor?.let { TutorContent(it, modifier, contentPadding) }
  }
}

@Composable
private fun TutorContent(tutor: Tutor, modifier: Modifier, padding: PaddingValues) {
  LazyColumn(
      contentPadding = PaddingValues(16.dp),
      modifier = modifier.fillMaxSize().padding(padding),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
          Surface(
              tonalElevation = 2.dp,
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
                          tutor.name,
                          style =
                              MaterialTheme.typography.titleLarge.copy(
                                  fontWeight = FontWeight.SemiBold),
                          modifier = Modifier.testTag(TutorPageTestTags.NAME))
                      RatingStars(
                          ratingOutOfFive = tutor.starRating,
                          modifier = Modifier.testTag(TutorPageTestTags.RATING))
                      Text("(${tutor.ratingNumber})", style = MaterialTheme.typography.bodyMedium)
                    }
              }
        }

        item {
          Column(modifier = Modifier.fillMaxWidth().testTag(TutorPageTestTags.SKILLS_SECTION)) {
            Text("Skills:", style = MaterialTheme.typography.titleMedium)
          }
        }

        items(tutor.skills) { s ->
          SkillChip(skill = s, modifier = Modifier.fillMaxWidth().testTag(TutorPageTestTags.SKILL))
        }

        item {
          Surface(
              tonalElevation = 1.dp,
              shape = MaterialTheme.shapes.large,
              modifier = Modifier.fillMaxWidth().testTag(TutorPageTestTags.CONTACT_SECTION)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MailOutline, contentDescription = "Email")
                    Spacer(Modifier.width(8.dp))
                    Text(tutor.email, style = MaterialTheme.typography.bodyMedium)
                  }
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    InstagramGlyph()
                    Spacer(Modifier.width(8.dp))
                    val handle = "@${tutor.name.replace(" ", "")}"
                    Text(handle, style = MaterialTheme.typography.bodyMedium)
                  }
                }
              }
        }
      }
}

private fun sampleTutor(): Tutor =
    Tutor(
        userId = "demo",
        name = "Kendrick Lamar",
        email = "kendrick@gmail.com",
        description = "Performer and mentor",
        skills =
            listOf(
                Skill(
                    userId = "demo",
                    mainSubject = MainSubject.MUSIC,
                    skill = "SINGING",
                    skillTime = 10.0,
                    expertise = ExpertiseLevel.EXPERT),
                Skill(
                    userId = "demo",
                    mainSubject = MainSubject.MUSIC,
                    skill = "DANCING",
                    skillTime = 5.0,
                    expertise = ExpertiseLevel.INTERMEDIATE),
                Skill(
                    userId = "demo",
                    mainSubject = MainSubject.MUSIC,
                    skill = "GUITAR",
                    skillTime = 7.0,
                    expertise = ExpertiseLevel.BEGINNER)),
        starRating = 5.0,
        ratingNumber = 23)

@Preview(showBackground = true)
@Composable
private fun Preview_TutorProfile_Light() {
  MaterialTheme {
    TutorContent(tutor = sampleTutor(), modifier = Modifier, padding = PaddingValues(0.dp))
  }
}

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
