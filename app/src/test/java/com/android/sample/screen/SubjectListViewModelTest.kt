package com.android.sample.screen

import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.subject.SubjectListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SubjectListViewModelTest {

  private val dispatcher = StandardTestDispatcher()

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // ---------- Helpers -----------------------------------------------------

  private fun profile(id: String, name: String, desc: String, rating: Double, total: Int) =
      Profile(userId = id, name = name, description = desc, tutorRating = RatingInfo(rating, total))

  private fun skill(userId: String, s: String) =
      Skill(userId = userId, mainSubject = MainSubject.MUSIC, skill = s)

  private class FakeRepo(
      private val profiles: List<Profile> = emptyList(),
      private val skills: Map<String, List<Skill>> = emptyMap(),
      private val delayMs: Long = 0,
      private val throwOnGetAll: Boolean = false
  ) : ProfileRepository {
    override fun getNewUid(): String {
      TODO("Not yet implemented")
    }

    override suspend fun getProfile(userId: String): Profile {
      TODO("Not yet implemented")
    }

    override suspend fun addProfile(profile: Profile) {
      TODO("Not yet implemented")
    }

    override suspend fun updateProfile(userId: String, profile: Profile) {
      TODO("Not yet implemented")
    }

    override suspend fun deleteProfile(userId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun getAllProfiles(): List<Profile> {
      if (throwOnGetAll) error("boom")
      if (delayMs > 0) delay(delayMs)
      return profiles
    }

    override suspend fun searchProfilesByLocation(
        location: Location,
        radiusKm: Double
    ): List<Profile> {
      TODO("Not yet implemented")
    }

    override suspend fun getProfileById(userId: String): Profile {
      TODO("Not yet implemented")
    }

    override suspend fun getSkillsForUser(userId: String): List<Skill> = skills[userId].orEmpty()
  }

  // Seed used by most tests:
  // Top 2 should be A(4.9) then B(4.8,20), leaving C and D in the main list.
  private val A = profile("1", "Alpha", "Guitar lessons", 4.9, 10)
  private val B = profile("2", "Beta", "Piano lessons", 4.8, 20)
  private val C = profile("3", "Gamma", "Sing coach", 4.8, 15)
  private val D = profile("4", "Delta", "Piano tutor", 4.2, 5)

  private val defaultRepo =
      FakeRepo(
          profiles = listOf(A, B, C, D),
          skills =
              mapOf(
                  "1" to listOf(skill("1", "GUITAR")),
                  "2" to listOf(skill("2", "PIANO")),
                  "3" to listOf(skill("3", "SING")),
                  "4" to listOf(skill("4", "PIANO"))),
          delayMs = 1L)

  private fun newVm(repo: ProfileRepository = defaultRepo, topCount: Int = 2) =
      SubjectListViewModel(repository = repo, tutorsPerTopSection = topCount)

  // ---------- Tests -------------------------------------------------------

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun refresh_populatesTopTutors_andExcludesThemFromList() = runTest {
    val vm = newVm()
    vm.refresh()
    advanceUntilIdle()

    val ui = vm.ui.value
    assertFalse(ui.isLoading)
    assertNull(ui.error)

    // Top tutors are sorted by rating desc, then total ratings desc, then name
    assertEquals(listOf(A.userId, B.userId), ui.topTutors.map { it.userId })

    // Main list excludes top tutors
    assertTrue(ui.tutors.map { it.userId }.containsAll(listOf(C.userId, D.userId)))
    assertFalse(ui.tutors.any { it.userId in setOf(A.userId, B.userId) })
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun onQueryChanged_filtersByNameOrDescription_caseInsensitive() = runTest {
    val vm = newVm()
    vm.refresh()
    advanceUntilIdle()

    // "gamma" matches profile C by name
    vm.onQueryChanged("gAmMa")
    var ui = vm.ui.value
    assertEquals(listOf(C.userId), ui.tutors.map { it.userId })

    // "piano" matches D by description (B is top and excluded)
    vm.onQueryChanged("piano")
    ui = vm.ui.value
    assertEquals(listOf(D.userId), ui.tutors.map { it.userId })

    // nonsense query -> empty list
    vm.onQueryChanged("zzz")
    ui = vm.ui.value
    assertTrue(ui.tutors.isEmpty())
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun onSkillSelected_filtersByExactSkill_inCurrentMainSubject() = runTest {
    val vm = newVm()
    vm.refresh()
    advanceUntilIdle()

    // Only D (list) has PIANO; B also has PIANO but sits in top tutors, so excluded
    vm.onSkillSelected("PIANO")
    val ui = vm.ui.value
    assertEquals(listOf(D.userId), ui.tutors.map { it.userId })
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun combined_filters_are_ANDed() = runTest {
    val vm = newVm()
    vm.refresh()
    advanceUntilIdle()

    // D matches both query "delta" and skill "PIANO"
    vm.onQueryChanged("Del")
    vm.onSkillSelected("PIANO")
    var ui = vm.ui.value
    assertEquals(listOf(D.userId), ui.tutors.map { it.userId })

    // Change query to something that doesn't match D -> empty result
    vm.onQueryChanged("Gamma")
    ui = vm.ui.value
    assertTrue(ui.tutors.isEmpty())
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun topTutors_respects_tieBreakers_and_limit() = runTest {
    val X = profile("10", "Aaron", "Vocal coach", 4.8, 15)
    val Y = profile("11", "Zed", "Vocal coach", 4.8, 15)
    val repo =
        FakeRepo(
            profiles =
                listOf(A, X, Y), // A has 4.9; X and Y tie on rating & totals -> name tie-break
            skills = emptyMap())
    val vm = newVm(repo, topCount = 3)
    vm.refresh()
    advanceUntilIdle()

    val ui = vm.ui.value
    assertEquals(listOf(A.userId, X.userId, Y.userId), ui.topTutors.map { it.userId })
    assertTrue(ui.tutors.isEmpty()) // all promoted to top section
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun refresh_handlesErrors_and_setsErrorMessage() = runTest {
    val failingRepo = FakeRepo(throwOnGetAll = true)
    val vm = newVm(failingRepo)
    vm.refresh()
    advanceUntilIdle()

    val ui = vm.ui.value
    assertFalse(ui.isLoading)
    assertNotNull(ui.error)
    assertTrue(ui.topTutors.isEmpty())
    assertTrue(ui.tutors.isEmpty())
  }
}
