package com.android.sample.model.tutor

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.user.Profile

class FakeProfileRepository {

  private val _tutors: SnapshotStateList<Profile> = mutableStateListOf()

  val tutors: List<Profile>
    get() = _tutors

  private val _fakeUser: Profile =
      Profile("1", "Ava S.", "ava@gmail.com", Location(0.0, 0.0), "$0/hr", "", RatingInfo(4.5, 10))
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
                "12",
                "Liam P.",
                "none1@gmail.com",
                Location(0.0, 0.0),
                "$25/hr",
                "",
                RatingInfo(2.1, 23)),
            Profile(
                "13",
                "Maria G.",
                "none2@gmail.com",
                Location(0.0, 0.0),
                "$30/hr",
                "",
                RatingInfo(4.9, 41)),
            Profile(
                "14",
                "David C.",
                "none3@gmail.com",
                Location(0.0, 0.0),
                "$20/hr",
                "",
                RatingInfo(4.7, 18))))
  }
}
