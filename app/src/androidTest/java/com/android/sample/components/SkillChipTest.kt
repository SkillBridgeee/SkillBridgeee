package com.android.sample.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.ui.components.SkillChip
import com.android.sample.ui.components.SkillChipTestTags
import org.junit.Rule
import org.junit.Test

class SkillChipTest {

  @get:Rule val compose = createComposeRule()

  @Test
  fun chip_is_displayed() {
    val skill = Skill(MainSubject.MUSIC, "PIANO", 2.0, ExpertiseLevel.INTERMEDIATE)
    compose.setContent { SkillChip(skill = skill) }

    compose.onNodeWithTag(SkillChipTestTags.CHIP, useUnmergedTree = true).assertIsDisplayed()
    compose.onNodeWithTag(SkillChipTestTags.TEXT, useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun formats_integer_years_and_level_lowercase() {
    val skill = Skill(MainSubject.MUSIC, "DATA_SCIENCE", 10.0, ExpertiseLevel.EXPERT)
    compose.setContent { SkillChip(skill = skill) }

    compose
        .onNodeWithTag(SkillChipTestTags.TEXT, useUnmergedTree = true)
        .assertTextEquals("Data science: 10 years, expert")
  }

  @Test
  fun formats_decimal_years_and_capitalizes_name() {
    val skill = Skill(MainSubject.MUSIC, "VOCAL_TRAINING", 1.5, ExpertiseLevel.BEGINNER)
    compose.setContent { SkillChip(skill = skill) }

    compose
        .onNodeWithTag(SkillChipTestTags.TEXT, useUnmergedTree = true)
        .assertTextEquals("Vocal training: 1.5 years, beginner")
  }
}
