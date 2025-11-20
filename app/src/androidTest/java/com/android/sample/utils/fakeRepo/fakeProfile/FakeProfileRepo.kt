package com.android.sample.utils.fakeRepo.fakeProfile

import com.android.sample.model.user.ProfileRepository

interface FakeProfileRepo : ProfileRepository {

  fun getCurrentUserId(): String

  fun getCurrentUserName(): String?
}
