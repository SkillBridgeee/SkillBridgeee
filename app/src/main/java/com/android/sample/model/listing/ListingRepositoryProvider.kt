package com.android.sample.model.listing

object ListingRepositoryProvider {
  private val _repository: ListingRepository by lazy { FakeListingRepository() }

  var repository: ListingRepository = _repository
}
