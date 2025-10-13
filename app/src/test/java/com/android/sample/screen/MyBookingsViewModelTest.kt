package com.android.sample.ui.bookings

import com.android.sample.model.booking.FakeBookingRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class MyBookingsViewModelTest {

  @Test
  fun demo_items_are_mapped_correctly() {
    val vm = MyBookingsViewModel(FakeBookingRepository(), "s1")
    val items = vm.items.value
    assertEquals(2, items.size)

    val first = items[0]
    assertEquals("Liam P.", first.tutorName)
    assertEquals("Piano Lessons", first.subject)
    assertEquals("$50/hr", first.pricePerHourLabel)
    assertEquals("2hrs", first.durationLabel)
    assertEquals(5, first.ratingStars)
    assertEquals(23, first.ratingCount)

    val second = items[1]
    assertEquals("Maria G.", second.tutorName)
    assertEquals("Calculus & Algebra", second.subject)
    assertEquals("$30/hr", second.pricePerHourLabel)
    assertEquals("1hr", second.durationLabel)
    assertEquals(4, second.ratingStars)
    assertEquals(41, second.ratingCount)
  }

  @Test
  fun dates_are_ddMMyyyy() {
    val pattern = Regex("""\d{2}/\d{2}/\d{4}""")
    val items = MyBookingsViewModel(FakeBookingRepository(), "s1").items.value
    assert(pattern.matches(items[0].dateLabel))
    assert(pattern.matches(items[1].dateLabel))
  }
}
