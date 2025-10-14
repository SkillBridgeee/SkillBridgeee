package com.android.sample.model.skill

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class SkillsFakeRepository {

    private val _skills: SnapshotStateList<Skill> = mutableStateListOf()

    val skills: List<Skill>
        get() = _skills

    init {
        loadMockData()
    }

    private fun loadMockData() {
        _skills.addAll(
            listOf(
                Skill("1", MainSubject.ACADEMICS, "Math"),
                Skill("2", MainSubject.MUSIC, "Piano"),
                Skill("3", MainSubject.SPORTS, "Tennis"),
                Skill("4", MainSubject.ARTS, "Painting")
            )
        )
    }
}
