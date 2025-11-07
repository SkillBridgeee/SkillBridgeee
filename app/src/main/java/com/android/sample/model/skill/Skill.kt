package com.android.sample.model.skill

import androidx.compose.ui.graphics.Color
import com.android.sample.ui.theme.academicsColor
import com.android.sample.ui.theme.artsColor
import com.android.sample.ui.theme.craftsColor
import com.android.sample.ui.theme.languagesColor
import com.android.sample.ui.theme.musicColor
import com.android.sample.ui.theme.sportsColor
import com.android.sample.ui.theme.technologyColor

/** Enum representing main subject categories */
enum class MainSubject {
  ACADEMICS,
  SPORTS,
  MUSIC,
  ARTS,
  TECHNOLOGY,
  LANGUAGES,
  CRAFTS
}

/** Enum representing academic skills */
enum class AcademicSkills {
  MATHEMATICS,
  PHYSICS,
  CHEMISTRY,
  BIOLOGY,
  HISTORY,
  GEOGRAPHY,
  LITERATURE,
  ECONOMICS,
  PSYCHOLOGY,
  PHILOSOPHY
}

/** Enum representing sports skills */
enum class SportsSkills {
  FOOTBALL,
  BASKETBALL,
  TENNIS,
  SWIMMING,
  RUNNING,
  SOCCER,
  VOLLEYBALL,
  BASEBALL,
  GOLF,
  CYCLING
}

/** Enum representing music skills */
enum class MusicSkills {
  PIANO,
  GUITAR,
  VIOLIN,
  DRUMS,
  SINGING,
  SAXOPHONE,
  FLUTE,
  TRUMPET,
  CELLO,
  BASS
}

/** Enum representing arts skills */
enum class ArtsSkills {
  PAINTING,
  DRAWING,
  SCULPTURE,
  PHOTOGRAPHY,
  DIGITAL_ART,
  POTTERY,
  GRAPHIC_DESIGN,
  ILLUSTRATION,
  CALLIGRAPHY,
  ANIMATION
}

/** Enum representing technology skills */
enum class TechnologySkills {
  PROGRAMMING,
  WEB_DEVELOPMENT,
  MOBILE_DEVELOPMENT,
  DATA_SCIENCE,
  CYBERSECURITY,
  AI_MACHINE_LEARNING,
  DATABASE_MANAGEMENT,
  CLOUD_COMPUTING,
  NETWORKING,
  GAME_DEVELOPMENT
}

/** Enum representing language skills */
enum class LanguageSkills {
  ENGLISH,
  SPANISH,
  FRENCH,
  GERMAN,
  ITALIAN,
  CHINESE,
  JAPANESE,
  KOREAN,
  ARABIC,
  PORTUGUESE
}

/** Enum representing craft skills */
enum class CraftSkills {
  KNITTING,
  SEWING,
  WOODWORKING,
  JEWELRY_MAKING,
  COOKING,
  BAKING,
  GARDENING,
  CARPENTRY,
  EMBROIDERY,
  ORIGAMI
}

/** Enum representing expertise levels */
enum class ExpertiseLevel {
  BEGINNER,
  INTERMEDIATE,
  ADVANCED,
  EXPERT,
  MASTER
}

/** Data class representing a skill */
data class Skill(
    val mainSubject: MainSubject = MainSubject.ACADEMICS,
    val skill: String = "", // Specific skill name (use enum.name when creating)
    val skillTime: Double = 0.0, // Time spent on this skill (in years)
    val expertise: ExpertiseLevel = ExpertiseLevel.BEGINNER
) {
  init {
    require(skillTime >= 0.0) { "Skill time must be non-negative" }
  }
}

/** Helper functions to get skills for each main subject */
object SkillsHelper {
  fun getSkillsForSubject(mainSubject: MainSubject): Array<out Enum<*>> {
    return when (mainSubject) {
      MainSubject.ACADEMICS -> AcademicSkills.values()
      MainSubject.SPORTS -> SportsSkills.values()
      MainSubject.MUSIC -> MusicSkills.values()
      MainSubject.ARTS -> ArtsSkills.values()
      MainSubject.TECHNOLOGY -> TechnologySkills.values()
      MainSubject.LANGUAGES -> LanguageSkills.values()
      MainSubject.CRAFTS -> CraftSkills.values()
    }
  }

  fun getSkillNames(mainSubject: MainSubject): List<String> {
    return getSkillsForSubject(mainSubject).map { it.name }
  }

  /**
   * Returns the color associated with a given main subject.
   *
   * This function maps each value of the [MainSubject] enum to a predefined color used in the
   * application's theme.
   *
   * @param subject The subject for which the corresponding color is requested.
   * @return The [Color] associated with the specified subject.
   */
  fun getColorForSubject(subject: MainSubject): Color {
    return when (subject) {
      MainSubject.ACADEMICS -> academicsColor
      MainSubject.SPORTS -> sportsColor
      MainSubject.MUSIC -> musicColor
      MainSubject.ARTS -> artsColor
      MainSubject.TECHNOLOGY -> technologyColor
      MainSubject.LANGUAGES -> languagesColor
      MainSubject.CRAFTS -> craftsColor
    }
  }
}
