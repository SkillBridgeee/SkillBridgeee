package com.android.sample.ui.tutor

import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile

/** Repository interface for fetching tutor data. */
interface TutorRepository {

  /**
   * Fetch the tutor's profile by user id
   *
   * @param id The user id of the tutor
   */
  suspend fun getProfileById(id: String): Profile

  /**
   * Fetch the skills owned by this user
   *
   * @param userId The user id of the tutor
   */
  suspend fun getSkillsForUser(userId: String): List<Skill>
}
