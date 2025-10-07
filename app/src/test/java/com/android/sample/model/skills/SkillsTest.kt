package com.android.sample.model.skills

import org.junit.Assert.*
import org.junit.Test

class SkillsTest {

  @Test
  fun `test Skills creation with default values`() {
    val skills = Skills()

    assertEquals("", skills.userId)
    assertEquals(MainSubject.ACADEMICS, skills.mainSubject)
    assertEquals("", skills.skill)
    assertEquals(0.0, skills.skillTime, 0.01)
    assertEquals(ExpertiseLevel.BEGINNER, skills.expertise)
  }

  @Test
  fun `test Skills creation with valid values`() {
    val skills =
        Skills(
            userId = "user123",
            mainSubject = MainSubject.SPORTS,
            skill = "FOOTBALL",
            skillTime = 5.5,
            expertise = ExpertiseLevel.INTERMEDIATE)

    assertEquals("user123", skills.userId)
    assertEquals(MainSubject.SPORTS, skills.mainSubject)
    assertEquals("FOOTBALL", skills.skill)
    assertEquals(5.5, skills.skillTime, 0.01)
    assertEquals(ExpertiseLevel.INTERMEDIATE, skills.expertise)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Skills validation - negative skill time`() {
    Skills(
        userId = "user123",
        mainSubject = MainSubject.ACADEMICS,
        skill = "MATHEMATICS",
        skillTime = -1.0,
        expertise = ExpertiseLevel.BEGINNER)
  }

  @Test
  fun `test Skills with zero skill time`() {
    val skills = Skills(userId = "user123", skillTime = 0.0)
    assertEquals(0.0, skills.skillTime, 0.01)
  }

  @Test
  fun `test Skills with various skill times`() {
    val skills1 = Skills(skillTime = 0.5)
    val skills2 = Skills(skillTime = 10.0)
    val skills3 = Skills(skillTime = 1000.25)

    assertEquals(0.5, skills1.skillTime, 0.01)
    assertEquals(10.0, skills2.skillTime, 0.01)
    assertEquals(1000.25, skills3.skillTime, 0.01)
  }

  @Test
  fun `test all MainSubject enum values`() {
    val academics = Skills(mainSubject = MainSubject.ACADEMICS)
    val sports = Skills(mainSubject = MainSubject.SPORTS)
    val music = Skills(mainSubject = MainSubject.MUSIC)
    val arts = Skills(mainSubject = MainSubject.ARTS)
    val technology = Skills(mainSubject = MainSubject.TECHNOLOGY)
    val languages = Skills(mainSubject = MainSubject.LANGUAGES)
    val crafts = Skills(mainSubject = MainSubject.CRAFTS)

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
    val beginner = Skills(expertise = ExpertiseLevel.BEGINNER)
    val intermediate = Skills(expertise = ExpertiseLevel.INTERMEDIATE)
    val advanced = Skills(expertise = ExpertiseLevel.ADVANCED)
    val expert = Skills(expertise = ExpertiseLevel.EXPERT)
    val master = Skills(expertise = ExpertiseLevel.MASTER)

    assertEquals(ExpertiseLevel.BEGINNER, beginner.expertise)
    assertEquals(ExpertiseLevel.INTERMEDIATE, intermediate.expertise)
    assertEquals(ExpertiseLevel.ADVANCED, advanced.expertise)
    assertEquals(ExpertiseLevel.EXPERT, expert.expertise)
    assertEquals(ExpertiseLevel.MASTER, master.expertise)
  }

  @Test
  fun `test Skills equality and hashCode`() {
    val skills1 =
        Skills(
            userId = "user123",
            mainSubject = MainSubject.TECHNOLOGY,
            skill = "PROGRAMMING",
            skillTime = 15.5,
            expertise = ExpertiseLevel.ADVANCED)

    val skills2 =
        Skills(
            userId = "user123",
            mainSubject = MainSubject.TECHNOLOGY,
            skill = "PROGRAMMING",
            skillTime = 15.5,
            expertise = ExpertiseLevel.ADVANCED)

    assertEquals(skills1, skills2)
    assertEquals(skills1.hashCode(), skills2.hashCode())
  }

  @Test
  fun `test Skills copy functionality`() {
    val originalSkills =
        Skills(
            userId = "user123",
            mainSubject = MainSubject.MUSIC,
            skill = "PIANO",
            skillTime = 8.0,
            expertise = ExpertiseLevel.INTERMEDIATE)

    val updatedSkills = originalSkills.copy(skillTime = 12.0, expertise = ExpertiseLevel.ADVANCED)

    assertEquals("user123", updatedSkills.userId)
    assertEquals(MainSubject.MUSIC, updatedSkills.mainSubject)
    assertEquals("PIANO", updatedSkills.skill)
    assertEquals(12.0, updatedSkills.skillTime, 0.01)
    assertEquals(ExpertiseLevel.ADVANCED, updatedSkills.expertise)

    assertNotEquals(originalSkills, updatedSkills)
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
}
