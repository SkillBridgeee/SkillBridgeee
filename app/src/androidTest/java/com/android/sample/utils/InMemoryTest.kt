// package com.android.sample.utils
//
// import com.android.sample.model.listing.ListingRepository
// import com.android.sample.model.map.Location
// import com.android.sample.model.rating.RatingInfo
// import com.android.sample.model.skill.Skill
// import com.android.sample.model.user.Profile
// import com.android.sample.model.user.ProfileRepository
// import java.util.UUID
//
/// **
// * Superclass for all local tests, which sets up a local repository before each test and restores
// * the original repository after each test.
// */
// open class InMemoryBootcampTest() : AppTest() {
//  override fun createInitializedProfileRepo(): ProfileRepository {
//    return ProfileFake()
//  }
//
//  override fun createInitializedListingRepo(): ListingRepository {
//    TODO("Not yet implemented")
//  }
//
//  val profile1 =
//      Profile(
//          userId = "creator_1",
//          name = "Alice",
//          email = "alice@example.com",
//          levelOfEducation = "Master",
//          location = Location(),
//          hourlyRate = "30",
//          description = "Experienced math tutor",
//          tutorRating = RatingInfo())
//
//  val profile2 =
//      Profile(
//          userId = "creator_2",
//          name = "Bob",
//          email = "bob@example.com",
//          levelOfEducation = "Bachelor",
//          location = Location(),
//          hourlyRate = "45",
//          description = "Student looking for physics help",
//          studentRating = RatingInfo())
//
//  class ProfileFake(val profileList: MutableList<Profile> = mutableListOf()) : ProfileRepository {
//
//    override fun getNewUid(): String = "profile_${UUID.randomUUID()}"
//
//    override suspend fun getProfile(userId: String): Profile? =
//        profileList.first { profile -> profile.userId == userId }
//
//    override suspend fun addProfile(profile: Profile) {
//      profileList.add(profile)
//    }
//
//    override suspend fun updateProfile(userId: String, profile: Profile) {
//      throw Error("Not yet implemented")
//    }
//
//    override suspend fun deleteProfile(userId: String) {
//      throw Error("Not yet implemented")
//    }
//
//    override suspend fun getAllProfiles(): List<Profile> = profileList
//
//    override suspend fun searchProfilesByLocation(
//        location: Location,
//        radiusKm: Double
//    ): List<Profile> = throw Error("Not yet implemented")
//
//    override suspend fun getProfileById(userId: String): Profile? =
//        throw Error("Not yet implemented")
//
//    override suspend fun getSkillsForUser(userId: String): List<Skill> =
//        throw Error("Not yet implemented")
//  }
// }
