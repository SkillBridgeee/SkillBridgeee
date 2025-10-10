package com.android.sample.ui.tutor

import com.android.sample.model.user.Tutor

/**
 * Repository interface for fetching tutor data.
 */
interface TutorRepository {
    /**
     * Fetches a tutor by their ID.
     * @param id The ID of the tutor to fetch.
     * @return The tutor with the specified ID.
     */
  suspend fun getTutorById(id: String): Tutor
}
