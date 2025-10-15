package com.android.sample.model.tutor

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.user.Profile
import kotlin.collections.addAll

class FakeProfileRepository {

  private val _tutors: SnapshotStateList<Profile> = mutableStateListOf()

  val tutors: List<Profile>
    get() = _tutors

  private val _fakeUser: Profile =
      Profile(
          userId = "1",
          name = "Ava S.",
          email = "ava@gmail.com",
          levelOfEducation = "",
          location = Location(latitude = 0.0, longitude = 0.0),
          hourlyRate = "",
          description = "",
          tutorRating = RatingInfo(4.8, 25),
          studentRating = RatingInfo(5.0, 10))
  val fakeUser: Profile
    get() = _fakeUser

  init {
    loadMockData()
  }

  /** Loads fake tutor listings (mock data) */
  private fun loadMockData() {
    _tutors.addAll(
        listOf(
            Profile(
                userId = "12",
                name = "Liam P.",
                email = "none1@gmail.com",
                levelOfEducation = "",
                description = "",
                location = Location(latitude = 0.0, longitude = 0.0)),
            Profile(
                userId = "13",
                name = "Maria G.",
                email = "none2@gmail.com",
                levelOfEducation = "",
                description = "",
                location = Location(latitude = 0.0, longitude = 0.0)),
            Profile(
                userId = "14",
                name = "David C.",
                email = "none3@gmail.com",
                levelOfEducation = "",
                description = "",
                location = Location(latitude = 0.0, longitude = 0.0))))
  }
}
