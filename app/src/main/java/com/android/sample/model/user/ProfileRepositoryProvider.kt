package com.android.sample.model.user

import com.android.sample.model.tutor.ProfileRepositoryLocal

/** Provides a single instance of the TutorRepository (swap for a remote impl in prod/tests). */
object ProfileRepositoryProvider {
  var repository: ProfileRepository = ProfileRepositoryLocal()
}
