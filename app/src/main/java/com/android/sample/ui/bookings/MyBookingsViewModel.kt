package com.android.sample.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.booking.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * ViewModel that loads bookings via the injected \`BookingRepository\`, maps domain -> UI using
 * \`BookingToUiMapper\`, and exposes both an items flow and a UI state flow.
 */
class MyBookingsViewModel(
    private val repo: BookingRepository,
    private val userId: String,
    private val mapper: BookingToUiMapper = BookingToUiMapper(),
    private val initialLoadBlocking: Boolean =
        false // set true in tests to synchronously populate items
) : ViewModel() {

  private val _items = MutableStateFlow<List<BookingCardUi>>(emptyList())
  val items: StateFlow<List<BookingCardUi>> = _items

  init {
    if (initialLoadBlocking) {
      // blocking load (use from tests to observe items immediately)
      runBlocking {
        try {
          val bookings = repo.getBookingsByUserId(userId)
          _items.value = if (bookings.isEmpty()) emptyList() else bookings.map { mapper.map(it) }
        } catch (_: Throwable) {
          _items.value = emptyList()
        }
      }
    } else {
      // normal async load
      viewModelScope.launch {
        try {
          val bookings = repo.getBookingsByUserId(userId)
          _items.value = if (bookings.isEmpty()) emptyList() else bookings.map { mapper.map(it) }
        } catch (_: Throwable) {
          _items.value = emptyList()
        }
      }
    }
  }

  fun refresh() {
    viewModelScope.launch {
      try {
        val bookings = repo.getBookingsByUserId(userId)
        _items.value = if (bookings.isEmpty()) emptyList() else bookings.map { mapper.map(it) }
      } catch (_: Throwable) {
        _items.value = emptyList()
      }
    }
  }
}
