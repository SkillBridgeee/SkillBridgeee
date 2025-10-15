package com.android.sample.model.booking

object BookingRepositoryProvider {
  private val _repository: BookingRepository by lazy { FakeBookingRepository() }

  var repository: BookingRepository = _repository
}
