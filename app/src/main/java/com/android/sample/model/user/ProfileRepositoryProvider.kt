package com.android.sample.model.user

object ProfileRepositoryProvider {
  private val _repository: ProfileRepository by lazy { ProfileRepositoryLocal() }

  var repository: ProfileRepository = _repository
}
