package com.android.sample.model.listing

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.android.sample.model.map.Location
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill

/**
 * A local fake repository that simulates a list of tutor proposals.
 * Works perfectly with Jetpack Compose and ViewModels.
 */
class FakeListingRepository {

    private val _listings: SnapshotStateList<Proposal> = mutableStateListOf()

    val listings: List<Proposal>
        get() = _listings

    init {
        loadMockData()
    }

    private fun loadMockData() {
        _listings.addAll(
            listOf(
                Proposal(
                    "1",
                    "12",
                    Skill("1", MainSubject.MUSIC, "Piano"),
                    "Experienced piano teacher",
                    Location(37.7749, -122.4194),
                    hourlyRate = 25.0
                ),
                Proposal(
                    "2",
                    "13",
                    Skill("2", MainSubject.ACADEMICS, "Math"),
                    "Math tutor for high school students",
                    Location(34.0522, -118.2437),
                    hourlyRate = 30.0
                ),
                Proposal(
                    "3",
                    "14",
                    Skill("3", MainSubject.MUSIC, "Guitare"),
                    "Learn acoustic guitar basics",
                    Location(40.7128, -74.0060),
                    hourlyRate = 20.0
                )
            )
        )
    }

}
