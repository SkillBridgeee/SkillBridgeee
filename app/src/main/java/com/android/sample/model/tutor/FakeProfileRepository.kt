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

    init {
        loadMockData()
    }

    /**
     * Loads fake tutor listings (mock data)
     */
    private fun loadMockData() {
        _tutors.addAll(
            listOf(
                Profile(
                    "12",
                    "Liam P.",
                    "Piano Lessons",
                    Location(0.0, 0.0),
                    "$25/hr",
                    "",
                    RatingInfo(4.8, 23)
                ),
                Profile(
                    "13",
                    "Maria G.",
                    "Calculus & Algebra",
                    Location(0.0, 0.0),
                    "$30/hr",
                    "",
                    RatingInfo(4.9, 41)
                ),
                Profile(
                    "14",
                    "David C.",
                    "Acoustic Guitar",
                    Location(0.0, 0.0),
                    "$20/hr",
                    "",
                    RatingInfo(4.7, 18)
                )
            )
        )
    }


}