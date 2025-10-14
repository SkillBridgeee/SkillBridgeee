package com.android.sample.model.tutor

import com.android.sample.model.user.ProfileRepository

/** Provides a single instance of the TutorRepository (swap for a remote impl in prod/tests). */
object TutorRepositoryProvider {
  val repository: ProfileRepository by lazy { TutorProfileRepositoryLocal() }
}
