package com.android.sample.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.sample.model.skill.Skill

private fun yearsText(years: Double): String {
  val y = if (years % 1.0 == 0.0) years.toInt().toString() else years.toString()
  return "$y years"
}

@Composable
fun SkillChip(skill: Skill, modifier: Modifier = Modifier) {
  val level = skill.expertise.name.lowercase()
  val name = skill.skill.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
  AssistChip(
      onClick = {},
      label = { Text("$name: ${yearsText(skill.skillTime)}, $level") },
      modifier = modifier.padding(vertical = 4.dp),
      colors = AssistChipDefaults.assistChipColors())
}
