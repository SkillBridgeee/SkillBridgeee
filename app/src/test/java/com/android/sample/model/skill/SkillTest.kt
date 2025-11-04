package com.android.sample.model.skill

import com.android.sample.model.skill.SkillsHelper.getColorForSubject
import com.android.sample.ui.theme.subjectColor1
import com.android.sample.ui.theme.subjectColor2
import com.android.sample.ui.theme.subjectColor3
import com.android.sample.ui.theme.subjectColor4
import com.android.sample.ui.theme.subjectColor5
import com.android.sample.ui.theme.subjectColor6
import com.android.sample.ui.theme.subjectColor7
import org.junit.Assert.*
import org.junit.Test

class SkillTest {

  @Test
  fun `test Skill creation with default values`() {
    val skill = Skill()

    assertEquals(MainSubject.ACADEMICS, skill.mainSubject)
    assertEquals("", skill.skill)
    assertEquals(0.0, skill.skillTime, 0.01)
    assertEquals(ExpertiseLevel.BEGINNER, skill.expertise)
  }

  @Test
  fun `test Skill creation with valid values`() {
    val skill =
        Skill(
            mainSubject = MainSubject.SPORTS,
            skill = "FOOTBALL",
            skillTime = 5.5,
            expertise = ExpertiseLevel.INTERMEDIATE)

    assertEquals(MainSubject.SPORTS, skill.mainSubject)
    assertEquals("FOOTBALL", skill.skill)
    assertEquals(5.5, skill.skillTime, 0.01)
    assertEquals(ExpertiseLevel.INTERMEDIATE, skill.expertise)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Skill validation - negative skill time`() {
    Skill(
        mainSubject = MainSubject.ACADEMICS,
        skill = "MATHEMATICS",
        skillTime = -1.0,
        expertise = ExpertiseLevel.BEGINNER)
  }

  @Test
  fun `test Skill with zero skill time`() {
    val skill = Skill(skillTime = 0.0)
    assertEquals(0.0, skill.skillTime, 0.01)
  }

  @Test
  fun `test Skill with various skill times`() {
    val skill1 = Skill(skillTime = 0.5)
    val skill2 = Skill(skillTime = 10.0)
    val skill3 = Skill(skillTime = 1000.25)

    assertEquals(0.5, skill1.skillTime, 0.01)
    assertEquals(10.0, skill2.skillTime, 0.01)
    assertEquals(1000.25, skill3.skillTime, 0.01)
  }

  @Test
  fun `test all MainSubject enum values`() {
    val academics = Skill(mainSubject = MainSubject.ACADEMICS)
    val sports = Skill(mainSubject = MainSubject.SPORTS)
    val music = Skill(mainSubject = MainSubject.MUSIC)
    val arts = Skill(mainSubject = MainSubject.ARTS)
    val technology = Skill(mainSubject = MainSubject.TECHNOLOGY)
    val languages = Skill(mainSubject = MainSubject.LANGUAGES)
    val crafts = Skill(mainSubject = MainSubject.CRAFTS)

    assertEquals(MainSubject.ACADEMICS, academics.mainSubject)
    assertEquals(MainSubject.SPORTS, sports.mainSubject)
    assertEquals(MainSubject.MUSIC, music.mainSubject)
    assertEquals(MainSubject.ARTS, arts.mainSubject)
    assertEquals(MainSubject.TECHNOLOGY, technology.mainSubject)
    assertEquals(MainSubject.LANGUAGES, languages.mainSubject)
    assertEquals(MainSubject.CRAFTS, crafts.mainSubject)
  }

  @Test
  fun `test all ExpertiseLevel enum values`() {
    val beginner = Skill(expertise = ExpertiseLevel.BEGINNER)
    val intermediate = Skill(expertise = ExpertiseLevel.INTERMEDIATE)
    val advanced = Skill(expertise = ExpertiseLevel.ADVANCED)
    val expert = Skill(expertise = ExpertiseLevel.EXPERT)
    val master = Skill(expertise = ExpertiseLevel.MASTER)

    assertEquals(ExpertiseLevel.BEGINNER, beginner.expertise)
    assertEquals(ExpertiseLevel.INTERMEDIATE, intermediate.expertise)
    assertEquals(ExpertiseLevel.ADVANCED, advanced.expertise)
    assertEquals(ExpertiseLevel.EXPERT, expert.expertise)
    assertEquals(ExpertiseLevel.MASTER, master.expertise)
  }

  @Test
  fun `test Skill equality and hashCode`() {
    val skill1 =
        Skill(
            mainSubject = MainSubject.TECHNOLOGY,
            skill = "PROGRAMMING",
            skillTime = 15.5,
            expertise = ExpertiseLevel.ADVANCED)

    val skill2 =
        Skill(
            mainSubject = MainSubject.TECHNOLOGY,
            skill = "PROGRAMMING",
            skillTime = 15.5,
            expertise = ExpertiseLevel.ADVANCED)

    assertEquals(skill1, skill2)
    assertEquals(skill1.hashCode(), skill2.hashCode())
  }

  @Test
  fun `test Skill copy functionality`() {
    val originalSkill =
        Skill(
            mainSubject = MainSubject.MUSIC,
            skill = "PIANO",
            skillTime = 8.0,
            expertise = ExpertiseLevel.INTERMEDIATE)

    val updatedSkill = originalSkill.copy(skillTime = 12.0, expertise = ExpertiseLevel.ADVANCED)

    assertEquals(MainSubject.MUSIC, updatedSkill.mainSubject)
    assertEquals("PIANO", updatedSkill.skill)
    assertEquals(12.0, updatedSkill.skillTime, 0.01)
    assertEquals(ExpertiseLevel.ADVANCED, updatedSkill.expertise)

    assertNotEquals(originalSkill, updatedSkill)
  }
}

class SkillsHelperTest {

  @Test
  fun `test getSkillsForSubject - ACADEMICS`() {
    val academicSkills = SkillsHelper.getSkillsForSubject(MainSubject.ACADEMICS)

    assertEquals(AcademicSkills.values().size, academicSkills.size)
    assertTrue(academicSkills.contains(AcademicSkills.MATHEMATICS))
    assertTrue(academicSkills.contains(AcademicSkills.PHYSICS))
    assertTrue(academicSkills.contains(AcademicSkills.CHEMISTRY))
  }

  @Test
  fun `test getSkillsForSubject - SPORTS`() {
    val sportsSkills = SkillsHelper.getSkillsForSubject(MainSubject.SPORTS)

    assertEquals(SportsSkills.values().size, sportsSkills.size)
    assertTrue(sportsSkills.contains(SportsSkills.FOOTBALL))
    assertTrue(sportsSkills.contains(SportsSkills.BASKETBALL))
    assertTrue(sportsSkills.contains(SportsSkills.TENNIS))
  }

  @Test
  fun `test getSkillsForSubject - MUSIC`() {
    val musicSkills = SkillsHelper.getSkillsForSubject(MainSubject.MUSIC)

    assertEquals(MusicSkills.values().size, musicSkills.size)
    assertTrue(musicSkills.contains(MusicSkills.PIANO))
    assertTrue(musicSkills.contains(MusicSkills.GUITAR))
    assertTrue(musicSkills.contains(MusicSkills.VIOLIN))
  }

  @Test
  fun `test getSkillsForSubject - ARTS`() {
    val artsSkills = SkillsHelper.getSkillsForSubject(MainSubject.ARTS)

    assertEquals(ArtsSkills.values().size, artsSkills.size)
    assertTrue(artsSkills.contains(ArtsSkills.PAINTING))
    assertTrue(artsSkills.contains(ArtsSkills.DRAWING))
    assertTrue(artsSkills.contains(ArtsSkills.PHOTOGRAPHY))
  }

  @Test
  fun `test getSkillsForSubject - TECHNOLOGY`() {
    val techSkills = SkillsHelper.getSkillsForSubject(MainSubject.TECHNOLOGY)

    assertEquals(TechnologySkills.values().size, techSkills.size)
    assertTrue(techSkills.contains(TechnologySkills.PROGRAMMING))
    assertTrue(techSkills.contains(TechnologySkills.WEB_DEVELOPMENT))
    assertTrue(techSkills.contains(TechnologySkills.DATA_SCIENCE))
  }

  @Test
  fun `test getSkillsForSubject - LANGUAGES`() {
    val languageSkills = SkillsHelper.getSkillsForSubject(MainSubject.LANGUAGES)

    assertEquals(LanguageSkills.values().size, languageSkills.size)
    assertTrue(languageSkills.contains(LanguageSkills.ENGLISH))
    assertTrue(languageSkills.contains(LanguageSkills.SPANISH))
    assertTrue(languageSkills.contains(LanguageSkills.FRENCH))
  }

  @Test
  fun `test getSkillsForSubject - CRAFTS`() {
    val craftSkills = SkillsHelper.getSkillsForSubject(MainSubject.CRAFTS)

    assertEquals(CraftSkills.values().size, craftSkills.size)
    assertTrue(craftSkills.contains(CraftSkills.COOKING))
    assertTrue(craftSkills.contains(CraftSkills.WOODWORKING))
    assertTrue(craftSkills.contains(CraftSkills.SEWING))
  }

  @Test
  fun `test getSkillNames - ACADEMICS`() {
    val academicSkillNames = SkillsHelper.getSkillNames(MainSubject.ACADEMICS)

    assertEquals(AcademicSkills.values().size, academicSkillNames.size)
    assertTrue(academicSkillNames.contains("MATHEMATICS"))
    assertTrue(academicSkillNames.contains("PHYSICS"))
    assertTrue(academicSkillNames.contains("CHEMISTRY"))
    assertTrue(academicSkillNames.contains("BIOLOGY"))
    assertTrue(academicSkillNames.contains("HISTORY"))
  }

  @Test
  fun `test getSkillNames - SPORTS`() {
    val sportsSkillNames = SkillsHelper.getSkillNames(MainSubject.SPORTS)

    assertEquals(SportsSkills.values().size, sportsSkillNames.size)
    assertTrue(sportsSkillNames.contains("FOOTBALL"))
    assertTrue(sportsSkillNames.contains("BASKETBALL"))
    assertTrue(sportsSkillNames.contains("TENNIS"))
    assertTrue(sportsSkillNames.contains("SWIMMING"))
  }

  @Test
  fun `test getSkillNames returns strings`() {
    val skillNames = SkillsHelper.getSkillNames(MainSubject.MUSIC)

    // Verify all returned values are strings
    skillNames.forEach { skillName ->
      assertTrue(skillName is String)
      assertTrue(skillName.isNotEmpty())
    }
  }

  @Test
  fun `test all MainSubject enums have corresponding skills`() {
    MainSubject.values().forEach { mainSubject ->
      val skills = SkillsHelper.getSkillsForSubject(mainSubject)
      val skillNames = SkillsHelper.getSkillNames(mainSubject)

      assertTrue("${mainSubject.name} should have skills", skills.isNotEmpty())
      assertTrue("${mainSubject.name} should have skill names", skillNames.isNotEmpty())
      assertEquals(
          "Skills array and names list should have same size for ${mainSubject.name}",
          skills.size,
          skillNames.size)
    }
  }
}

class EnumTest {

  @Test
  fun `test AcademicSkills enum values`() {
    val academicSkills = AcademicSkills.values()
    assertEquals(10, academicSkills.size)

    assertTrue(academicSkills.contains(AcademicSkills.MATHEMATICS))
    assertTrue(academicSkills.contains(AcademicSkills.PHYSICS))
    assertTrue(academicSkills.contains(AcademicSkills.CHEMISTRY))
    assertTrue(academicSkills.contains(AcademicSkills.BIOLOGY))
    assertTrue(academicSkills.contains(AcademicSkills.HISTORY))
    assertTrue(academicSkills.contains(AcademicSkills.GEOGRAPHY))
    assertTrue(academicSkills.contains(AcademicSkills.LITERATURE))
    assertTrue(academicSkills.contains(AcademicSkills.ECONOMICS))
    assertTrue(academicSkills.contains(AcademicSkills.PSYCHOLOGY))
    assertTrue(academicSkills.contains(AcademicSkills.PHILOSOPHY))
  }

  @Test
  fun `test SportsSkills enum values`() {
    val sportsSkills = SportsSkills.values()
    assertEquals(10, sportsSkills.size)

    assertTrue(sportsSkills.contains(SportsSkills.FOOTBALL))
    assertTrue(sportsSkills.contains(SportsSkills.BASKETBALL))
    assertTrue(sportsSkills.contains(SportsSkills.TENNIS))
    assertTrue(sportsSkills.contains(SportsSkills.SWIMMING))
    assertTrue(sportsSkills.contains(SportsSkills.RUNNING))
    assertTrue(sportsSkills.contains(SportsSkills.SOCCER))
    assertTrue(sportsSkills.contains(SportsSkills.VOLLEYBALL))
    assertTrue(sportsSkills.contains(SportsSkills.BASEBALL))
    assertTrue(sportsSkills.contains(SportsSkills.GOLF))
    assertTrue(sportsSkills.contains(SportsSkills.CYCLING))
  }

  @Test
  fun `test MusicSkills enum values`() {
    val musicSkills = MusicSkills.values()
    assertEquals(10, musicSkills.size)

    assertTrue(musicSkills.contains(MusicSkills.PIANO))
    assertTrue(musicSkills.contains(MusicSkills.GUITAR))
    assertTrue(musicSkills.contains(MusicSkills.VIOLIN))
    assertTrue(musicSkills.contains(MusicSkills.DRUMS))
    assertTrue(musicSkills.contains(MusicSkills.SINGING))
  }

  @Test
  fun `test TechnologySkills enum values`() {
    val techSkills = TechnologySkills.values()
    assertEquals(10, techSkills.size)

    assertTrue(techSkills.contains(TechnologySkills.PROGRAMMING))
    assertTrue(techSkills.contains(TechnologySkills.WEB_DEVELOPMENT))
    assertTrue(techSkills.contains(TechnologySkills.MOBILE_DEVELOPMENT))
    assertTrue(techSkills.contains(TechnologySkills.DATA_SCIENCE))
    assertTrue(techSkills.contains(TechnologySkills.AI_MACHINE_LEARNING))
  }

  @Test
  fun `test enum name properties`() {
    assertEquals("MATHEMATICS", AcademicSkills.MATHEMATICS.name)
    assertEquals("FOOTBALL", SportsSkills.FOOTBALL.name)
    assertEquals("PIANO", MusicSkills.PIANO.name)
    assertEquals("PAINTING", ArtsSkills.PAINTING.name)
    assertEquals("PROGRAMMING", TechnologySkills.PROGRAMMING.name)
    assertEquals("ENGLISH", LanguageSkills.ENGLISH.name)
    assertEquals("COOKING", CraftSkills.COOKING.name)

    assertEquals("BEGINNER", ExpertiseLevel.BEGINNER.name)
    assertEquals("MASTER", ExpertiseLevel.MASTER.name)

    assertEquals("ACADEMICS", MainSubject.ACADEMICS.name)
    assertEquals("SPORTS", MainSubject.SPORTS.name)
  }

  @Test
  fun `test getColorForSubject mapping for all MainSubject values`() {
    val expectedColors =
        mapOf(
            MainSubject.ACADEMICS to subjectColor1,
            MainSubject.SPORTS to subjectColor2,
            MainSubject.MUSIC to subjectColor3,
            MainSubject.ARTS to subjectColor4,
            MainSubject.TECHNOLOGY to subjectColor5,
            MainSubject.LANGUAGES to subjectColor6,
            MainSubject.CRAFTS to subjectColor7)

    MainSubject.values().forEach { subject ->
      val expected = expectedColors[subject]
      val actual = getColorForSubject(subject)

      assertEquals("Color mismatch for subject $subject", expected, actual)
      assertNotNull("Color should not be null for $subject", actual)
    }
  }
}
