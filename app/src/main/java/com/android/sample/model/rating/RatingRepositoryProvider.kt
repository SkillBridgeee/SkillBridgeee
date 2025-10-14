package com.android.sample.model.rating

object RatingRepositoryProvider {
  private val _repository: RatingRepository by lazy { FakeRatingRepository() }

  var repository: RatingRepository = _repository
}
