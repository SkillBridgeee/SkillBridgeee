package com.android.sample.ui.tutor

import com.android.sample.model.user.Tutor

interface TutorRepository {
  suspend fun getTutorById(id: String): Tutor
}
