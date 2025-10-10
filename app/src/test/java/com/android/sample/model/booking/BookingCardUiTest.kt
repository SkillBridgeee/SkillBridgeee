package com.android.sample.ui.bookings

import org.junit.Assert.assertEquals
import org.junit.Test

class BookingCardUiTest {
    @Test
    fun data_class_copy_and_equality() {
        val a = BookingCardUi("1","A","S","$1/hr","1hr","01/01/2026",5,10)
        val b = a.copy(durationLabel = "2hrs")
        assertEquals("2hrs", b.durationLabel)
        // not equal after change
        assert(a != b)
    }
}
