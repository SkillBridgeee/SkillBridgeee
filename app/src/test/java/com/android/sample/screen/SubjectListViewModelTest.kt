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

// Ai generated tests for the SubjectListViewModel
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

  private fun skill(userId: String, s: String) = Skill(mainSubject = MainSubject.MUSIC, skill = s)

  private class FakeRepo(
      private val profiles: List<Profile> = emptyList(),
      private val skills: Map<String, List<Skill>> = emptyMap(),
      private val delayMs: Long = 0,
      private val throwOnGetAll: Boolean = false
  ) : ProfileRepository {
    override fun getNewUid(): String = "unused"

    override suspend fun getProfile(userId: String): Profile = error("unused")

    override suspend fun addProfile(profile: Profile) {}

    override suspend fun updateProfile(userId: String, profile: Profile) {}

    override suspend fun deleteProfile(userId: String) {}

    override suspend fun getAllProfiles(): List<Profile> {
      if (throwOnGetAll) error("boom")
      if (delayMs > 0) delay(delayMs)
      return profiles
    }

    override suspend fun searchProfilesByLocation(
        location: Location,
        radiusKm: Double
    ): List<Profile> = emptyList()

    override suspend fun getProfileById(userId: String): Profile = error("unused")

    override suspend fun getSkillsForUser(userId: String): List<Skill> = skills[userId].orEmpty()
  }

  // Seed used by most tests:
  // Sorted (best first) should be: A(4.9,10), B(4.8,20), C(4.8,15), D(4.2,5)
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

  private fun newVm(repo: ProfileRepository = defaultRepo) = SubjectListViewModel(repository = repo)

  // ---------- Tests -------------------------------------------------------

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun refresh_populatesSingleSortedList() = runTest {
    val vm = newVm()
    vm.refresh()
    advanceUntilIdle()

    val ui = vm.ui.value
    assertFalse(ui.isLoading)
    assertNull(ui.error)

    // Single list contains everyone, sorted by rating desc, total ratings desc, then name
    assertEquals(listOf(A.userId, B.userId, C.userId, D.userId), ui.tutors.map { it.userId })
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

    // "piano" matches B (desc) and D (desc/name) -> both shown, sorted best-first
    vm.onQueryChanged("piano")
    ui = vm.ui.value
    assertEquals(listOf(B.userId, D.userId), ui.tutors.map { it.userId })

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

    // PIANO should return B and D (no separate top section anymore), best-first
    vm.onSkillSelected("PIANO")
    val ui = vm.ui.value
    assertEquals(listOf(B.userId, D.userId), ui.tutors.map { it.userId })
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun combined_filters_are_ANDed() = runTest {
    val vm = newVm()
    vm.refresh()
    advanceUntilIdle()

    // D matches both query "del" and skill "PIANO"
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
  fun sorting_respects_tieBreakers() = runTest {
    // X and Y tie on rating & totals -> name tie-breaker (Aaron before Zed)
    val X = profile("10", "Aaron", "Vocal coach", 4.8, 15)
    val Y = profile("11", "Zed", "Vocal coach", 4.8, 15)
    val repo = FakeRepo(profiles = listOf(A, X, Y), skills = emptyMap())
    val vm = newVm(repo)
    vm.refresh()
    advanceUntilIdle()

    val ui = vm.ui.value
    assertEquals(listOf(A.userId, X.userId, Y.userId), ui.tutors.map { it.userId })
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
    assertTrue(ui.tutors.isEmpty())
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun refresh_setsErrorState_whenRepositoryFails() = runTest {
    val failingRepo =
        object : ProfileRepository {
          override fun getNewUid(): String = "unused"

          override suspend fun getProfile(userId: String): Profile = error("unused")

          override suspend fun addProfile(profile: Profile) {}

          override suspend fun updateProfile(userId: String, profile: Profile) {}

          override suspend fun deleteProfile(userId: String) {}

          override suspend fun getAllProfiles(): List<Profile> = error("Boom failure")

          override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
              emptyList<Profile>()

          override suspend fun getProfileById(userId: String): Profile = error("unused")

          override suspend fun getSkillsForUser(userId: String): List<Skill> = emptyList()
        }

    val vm = SubjectListViewModel(repository = failingRepo)
    vm.refresh()
    advanceUntilIdle()

    val ui = vm.ui.value
    assertFalse(ui.isLoading)
    assertTrue(ui.error?.contains("Boom failure") == true)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun onSkillSelected_filtersTutorsBySkill() = runTest {
    val p1 = profile("1", "Alice", "Guitar Lessons", 4.9, 23)
    val p2 = profile("2", "Bob", "Piano Lessons", 4.8, 15)
    val repo =
        object : ProfileRepository {
          override fun getNewUid(): String = "unused"

          override suspend fun getProfile(userId: String): Profile = error("unused")

          override suspend fun addProfile(profile: Profile) {}

          override suspend fun updateProfile(userId: String, profile: Profile) {}

          override suspend fun deleteProfile(userId: String) {}

          override suspend fun getAllProfiles(): List<Profile> = listOf(p1, p2)

          override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
              emptyList<Profile>()

          override suspend fun getProfileById(userId: String): Profile = error("unused")

          override suspend fun getSkillsForUser(userId: String): List<Skill> =
              if (userId == "1") listOf(Skill(MainSubject.MUSIC, "GUITAR"))
              else listOf(Skill(MainSubject.MUSIC, "PIANO"))
        }

    val vm = SubjectListViewModel(repo)
    vm.refresh()
    advanceUntilIdle()

    vm.onSkillSelected("PIANO")
    val ui = vm.ui.value
    assertEquals(listOf("2"), ui.tutors.map { it.userId })
  }
}
