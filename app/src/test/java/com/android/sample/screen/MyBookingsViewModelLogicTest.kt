package com.android.sample.screen

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus
import com.android.sample.ui.bookings.BookingToUiMapper
import com.android.sample.ui.bookings.MyBookingsViewModel
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MyBookingsViewModelLogicTest {

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // helper domain booking that always satisfies end > start
  private fun booking(
      id: String = "b1",
      tutorId: String = "t1",
      start: Date = Date(),
      end: Date = Date(start.time + 60_000),
      price: Double = 50.0
  ) =
      Booking(
          bookingId = id,
          associatedListingId = "l1",
          listingCreatorId = tutorId,
          bookerId = "s1",
          sessionStart = start,
          sessionEnd = if (end.time <= start.time) Date(start.time + 1) else end,
          status = BookingStatus.CONFIRMED,
          price = price)

  // ---------- ViewModel init paths

  @Test
  fun init_async_success_populates_items() {
    val repo =
        object : BookingRepository {
          override fun getNewUid() = "x"

          override suspend fun getBookingsByUserId(userId: String) = listOf(booking())

          override suspend fun getAllBookings() = emptyList<Booking>()

          override suspend fun getBooking(bookingId: String) = booking()

          override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

          override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

          override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}
        }
    val vm = MyBookingsViewModel(repo, "s1")
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(1, vm.items.value.size)
  }

  @Test
  fun init_async_failure_sets_empty_items() {
    val repo =
        object : BookingRepository {
          override fun getNewUid() = "x"

          override suspend fun getBookingsByUserId(userId: String) = throw RuntimeException("boom")

          override suspend fun getAllBookings() = emptyList<Booking>()

          override suspend fun getBooking(bookingId: String) = booking()

          override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

          override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

          override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}
        }
    val vm = MyBookingsViewModel(repo, "s1")
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(vm.items.value.isEmpty())
  }

  @Test
  fun init_blocking_success_and_failure() {
    val okRepo =
        object : BookingRepository {
          override fun getNewUid() = "x"

          override suspend fun getBookingsByUserId(userId: String) = listOf(booking())

          override suspend fun getAllBookings() = emptyList<Booking>()

          override suspend fun getBooking(bookingId: String) = booking()

          override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

          override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

          override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}
        }
    val ok = MyBookingsViewModel(okRepo, "s1", initialLoadBlocking = true)
    assertEquals(1, ok.items.value.size)

    val badRepo =
        object : BookingRepository {
          override fun getNewUid() = "x"

          override suspend fun getBookingsByUserId(userId: String) = throw RuntimeException("boom")

          override suspend fun getAllBookings() = emptyList<Booking>()

          override suspend fun getBooking(bookingId: String) = booking()

          override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

          override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

          override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}
        }
    val bad = MyBookingsViewModel(badRepo, "s1", initialLoadBlocking = true)
    assertEquals(0, bad.items.value.size)
  }

  // ---------- refresh paths + mapping details

  @Test
  fun refresh_maps_single_booking_correctly() = runTest {
    val start = Date()
    val end = Date(start.time + 90 * 60 * 1000) // 1h30
    val bk = booking(id = "b123", tutorId = "tutor1", start = start, end = end, price = 100.0)
    val repo =
        object : BookingRepository {
          override fun getNewUid() = "x"

          override suspend fun getBookingsByUserId(userId: String) = listOf(bk)

          override suspend fun getAllBookings() = emptyList<Booking>()

          override suspend fun getBooking(bookingId: String) = bk

          override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

          override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

          override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}
        }

    val vm = MyBookingsViewModel(repo, "u1")
    vm.refresh()
    testDispatcher.scheduler.advanceUntilIdle()

    val ui = vm.items.value.single()
    assertEquals("b123", ui.id)
    assertEquals("tutor1", ui.tutorId)
    assertEquals("$100.0/hr", ui.pricePerHourLabel)
    assertEquals("1h 30m", ui.durationLabel)
    assertTrue(ui.dateLabel.matches(Regex("""\d{2}/\d{2}/\d{4}""")))
    assertEquals(0, ui.ratingStars)
    assertEquals(0, ui.ratingCount)
  }

  @Test
  fun mapper_duration_variants_and_fallbacks() {
    val now = Date()
    val oneHour = Date(now.time + 60 * 60 * 1000)
    val twoHours = Date(now.time + 2 * 60 * 60 * 1000)
    val twentyMin = Date(now.time + 20 * 60 * 1000)

    val m = BookingToUiMapper(Locale.US)
    assertEquals("1hr", m.map(booking(start = now, end = oneHour)).durationLabel)
    assertEquals("2hrs", m.map(booking(start = now, end = twoHours)).durationLabel)
    assertEquals("0h 20m", m.map(booking(start = now, end = twentyMin)).durationLabel)

    // tutorName fallback to listingCreatorId; subject fallback to "—"
    val ui = m.map(booking(tutorId = "teacher42"))
    assertEquals("teacher42", ui.tutorName)
    assertTrue(ui.subject.isNotEmpty())
  }

  @Test
  fun mapper_rating_is_clamped_and_date_format_ok() {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.set(2025, Calendar.JANUARY, 2, 0, 0, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val start = cal.time
    val end = Date(start.time + 1) // just > start
    val m = BookingToUiMapper(Locale.UK)
    val ui = m.map(booking(start = start, end = end))
    assertTrue(ui.ratingStars in 0..5)
    assertEquals("02/01/2025", ui.dateLabel)
  }

  @Test
  fun refresh_sets_empty_on_error() = runTest {
    val failing =
        object : BookingRepository {
          override fun getNewUid() = "x"

          override suspend fun getBookingsByUserId(userId: String) = throw RuntimeException("boom")

          override suspend fun getAllBookings() = emptyList<Booking>()

          override suspend fun getBooking(bookingId: String) = booking()

          override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

          override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

          override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}
        }
    val vm = MyBookingsViewModel(failing, "s1")
    vm.refresh()
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(vm.items.value.isEmpty())
  }

  // ---------- uiState: init -> Loading then Success
  @Test
  fun uiState_init_async_loading_then_success() {
    val repo =
        object : BookingRepository {
          override fun getNewUid() = "x"

          override suspend fun getBookingsByUserId(userId: String) = listOf(booking())

          override suspend fun getAllBookings() = emptyList<Booking>()

          override suspend fun getBooking(bookingId: String) = booking()

          override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

          override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

          override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}
        }

    val vm = MyBookingsViewModel(repo, "s1")
    // Immediately after construction, state should be Loading
    assertTrue(vm.uiState.value is com.android.sample.ui.bookings.MyBookingsUiState.Loading)

    // Let the init coroutine finish
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertTrue(state is com.android.sample.ui.bookings.MyBookingsUiState.Success)
    state as com.android.sample.ui.bookings.MyBookingsUiState.Success
    assertEquals(1, state.items.size)
  }

  // ---------- uiState: init -> Loading then Empty when repo returns empty
  @Test
  fun uiState_init_async_loading_then_empty() {
    val repo =
        object : BookingRepository {
          override fun getNewUid() = "x"

          override suspend fun getBookingsByUserId(userId: String) = emptyList<Booking>()

          override suspend fun getAllBookings() = emptyList<Booking>()

          override suspend fun getBooking(bookingId: String) = booking()

          override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

          override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

          override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}
        }

    val vm = MyBookingsViewModel(repo, "s1")
    assertTrue(vm.uiState.value is com.android.sample.ui.bookings.MyBookingsUiState.Loading)

    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(vm.uiState.value is com.android.sample.ui.bookings.MyBookingsUiState.Empty)
    assertTrue(vm.items.value.isEmpty())
  }

  // ---------- uiState: init -> Loading then Error on failure
  @Test
  fun uiState_init_async_loading_then_error() {
    val repo =
        object : BookingRepository {
          override fun getNewUid() = "x"

          override suspend fun getBookingsByUserId(userId: String) = throw RuntimeException("boom")

          override suspend fun getAllBookings() = emptyList<Booking>()

          override suspend fun getBooking(bookingId: String) = booking()

          override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

          override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

          override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}
        }

    val vm = MyBookingsViewModel(repo, "s1")
    assertTrue(vm.uiState.value is com.android.sample.ui.bookings.MyBookingsUiState.Loading)

    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertTrue(state is com.android.sample.ui.bookings.MyBookingsUiState.Error)
    assertTrue(vm.items.value.isEmpty())
  }

  // ---------- uiState: refresh transitions from Success -> Loading -> Empty
  @Test
  fun uiState_refresh_to_empty_updates_both_state_and_items() = runTest {
    // Mutable repo that delays to expose the Loading state
    class MutableRepo(var next: List<Booking>) : BookingRepository {
      override fun getNewUid() = "x"

      override suspend fun getBookingsByUserId(userId: String): List<Booking> {
        // ensure a suspension between setting Loading and producing the result
        kotlinx.coroutines.delay(1)
        return next
      }

      override suspend fun getAllBookings() = emptyList<Booking>()

      override suspend fun getBooking(bookingId: String) = next.firstOrNull() ?: booking()

      override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

      override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

      override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

      override suspend fun addBooking(booking: Booking) {}

      override suspend fun updateBooking(bookingId: String, booking: Booking) {}

      override suspend fun deleteBooking(bookingId: String) {}

      override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

      override suspend fun confirmBooking(bookingId: String) {}

      override suspend fun completeBooking(bookingId: String) {}

      override suspend fun cancelBooking(bookingId: String) {}
    }

    val repo = MutableRepo(next = listOf(booking(id = "b1")))
    val vm = MyBookingsViewModel(repo, "s1")

    // Finish initial load -> Success
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(vm.uiState.value is com.android.sample.ui.bookings.MyBookingsUiState.Success)
    assertEquals(1, vm.items.value.size)

    // Now repo goes empty; trigger refresh
    repo.next = emptyList()
    vm.refresh()

    // Run currently scheduled tasks; we should now be in Loading (suspended at delay)
    testDispatcher.scheduler.runCurrent()
    assertTrue(vm.uiState.value is com.android.sample.ui.bookings.MyBookingsUiState.Loading)

    // Finish the delayed fetch
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(vm.uiState.value is com.android.sample.ui.bookings.MyBookingsUiState.Empty)
    assertTrue(vm.items.value.isEmpty())
  }

  @Test
  fun mapper_price_label_uses_numeric_when_present_else_dash() {
    val now = Date()
    val m = BookingToUiMapper(Locale.US)

    val withNumber = m.map(booking(start = now, end = Date(now.time + 60_000), price = 42.0))
    assertEquals("$42.0/hr", withNumber.pricePerHourLabel)

    // With current Booking model, price is always a Double, so 0.0 formats as "$0.0/hr"
    val zeroPrice = m.map(booking(start = now, end = Date(now.time + 60_000), price = 0.0))
    assertEquals("$0.0/hr", zeroPrice.pricePerHourLabel)
  }

  @Test
  fun mapper_handles_reflection_edge_cases_gracefully() {
    val start = Date()
    val end = start // zero duration
    val m = BookingToUiMapper(Locale.US)
    val ui = m.map(booking(start = start, end = end, price = 10.0))

    // For zero minutes the mapper emits "${hours}hr", so "0hr"
    assertEquals("0hr", ui.durationLabel)
    assertTrue(ui.ratingStars in 0..5)
    assertTrue(ui.dateLabel.matches(Regex("""\d{2}/\d{2}/\d{4}""")))
  }
}
