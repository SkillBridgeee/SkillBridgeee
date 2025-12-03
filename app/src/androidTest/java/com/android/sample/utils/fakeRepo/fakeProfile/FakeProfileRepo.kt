package com.android.sample.utils.fakeRepo.fakeProfile

import com.android.sample.model.user.ProfileRepository

interface FakeProfileRepo : ProfileRepository {

  override fun getCurrentUserId(): String

  fun getCurrentUserName(): String?
}
