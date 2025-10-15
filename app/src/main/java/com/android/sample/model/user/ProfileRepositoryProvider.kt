package com.android.sample.model.user
/** Provides a single instance of the TutorRepository (swap for a remote impl in prod/tests). */
object ProfileRepositoryProvider {
  var repository: ProfileRepository = ProfileRepositoryLocal()
}
