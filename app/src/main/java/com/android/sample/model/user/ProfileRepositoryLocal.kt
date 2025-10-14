package com.android.sample.model.user

import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import kotlin.String

class ProfileRepositoryLocal : ProfileRepository {

    val profileFake1 =
        Profile(
            userId = "test",
            name = "John Doe",
            email = "john.doe@epfl.ch",
            location = Location(latitude = 0.0, longitude = 0.0, name = "EPFL"),
            description = "Nice Guy")
    val profileFake2 =
        Profile(
            userId = "fake2",
            name = "GuiGui",
            email = "mimi@epfl.ch",
            location = Location(latitude = 0.0, longitude = 0.0, name = "Renens"),
            description = "Bad Guy")

    private val profileTutor1 =
        Profile(
            userId = "tutor-1",
            name = "Alice Martin",
            email = "alice@epfl.ch",
            location = Location(0.0, 0.0, "EPFL"),
            description = "Tutor 1")

    private val profileTutor2 =
        Profile(
            userId = "tutor-2",
            name = "Lucas Dupont",
            email = "lucas@epfl.ch",
            location = Location(0.0, 0.0, "Renens"),
            description = "Tutor 2")

    val profileList = listOf(profileFake1, profileFake2)

    override fun getNewUid(): String {
        TODO("Not yet implemented")
    }

    override suspend fun getProfile(userId: String): Profile =
        profileList.firstOrNull { it.userId == userId }
            ?: throw NoSuchElementException("Profile with id '$userId' not found")

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
        return profileList
    }

    override suspend fun searchProfilesByLocation(
        location: Location,
        radiusKm: Double
    ): List<Profile> {
        TODO("Not yet implemented")
    }

    override suspend fun getProfileById(userId: String): Profile {
        return profileList.firstOrNull { it.userId == userId }
            ?: throw NoSuchElementException("Profile with id '$userId' not found")
    }

    override suspend fun getSkillsForUser(userId: String): List<Skill> {
        TODO("Not yet implemented")
    }
}