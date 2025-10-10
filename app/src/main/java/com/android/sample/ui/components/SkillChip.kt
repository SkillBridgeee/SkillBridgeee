package com.android.sample.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.sample.model.skill.Skill
import com.android.sample.ui.theme.TealChip
import com.android.sample.ui.theme.White

/** Test tags for the [SkillChip] composable. */
object SkillChipTestTags {
  const val CHIP = "SkillChipTestTags.CHIP"
  const val TEXT = "SkillChipTestTags.TEXT"
}

/**
 * Formats the years of experience as a string, ensuring that whole numbers are displayed without
 * decimal places.
 */
private fun yearsText(years: Double): String {
  val y = if (years % 1.0 == 0.0) years.toInt().toString() else years.toString()
  return "$y years"
}

/**
 * A chip that displays a skill with its name, years of experience, and expertise level.
 *
 * @param skill The skill to be displayed.
 * @param modifier The modifier to be applied to the composable.
 */
@Composable
fun SkillChip(skill: Skill, modifier: Modifier = Modifier) {
  val level = skill.expertise.name.lowercase()
  val name = skill.skill.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
  Surface(
      color = TealChip,
      shape = MaterialTheme.shapes.large,
      modifier = modifier.padding(vertical = 4.dp).fillMaxWidth().testTag(SkillChipTestTags.CHIP),
      tonalElevation = 0.dp) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart) {
              Text(
                  text = "$name: ${yearsText(skill.skillTime)}, $level",
                  color = White,
                  style = MaterialTheme.typography.bodyMedium,
                  textAlign = TextAlign.Start,
                  modifier = Modifier.testTag(SkillChipTestTags.TEXT))
            }
      }
}
