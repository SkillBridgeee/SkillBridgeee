package com.android.sample.model.booking

import androidx.compose.ui.graphics.Color
import com.android.sample.ui.theme.bkgCompletedColor
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for PaymentStatus enum and its extension functions. Tests the color() and name()
 * extension functions.
 */
class PaymentStatusTest {

  @Test
  fun paymentStatus_PENDING_PAYMENT_returnsCorrectName() {
    val status = PaymentStatus.PENDING_PAYMENT
    assertEquals("Pending Payment", status.name())
  }

  @Test
  fun paymentStatus_PAYED_returnsCorrectName() {
    val status = PaymentStatus.PAYED
    assertEquals("Payment Sent", status.name())
  }

  @Test
  fun paymentStatus_CONFIRMED_returnsCorrectName() {
    val status = PaymentStatus.CONFIRMED
    assertEquals("Payment Confirmed", status.name())
  }

  @Test
  fun paymentStatus_PENDING_PAYMENT_returnsGrayColor() {
    val status = PaymentStatus.PENDING_PAYMENT
    assertEquals(Color.Gray, status.color())
  }

  @Test
  fun paymentStatus_PAYED_returnsGreenColor() {
    val status = PaymentStatus.PAYED
    assertEquals(Color.Green, status.color())
  }

  @Test
  fun paymentStatus_CONFIRMED_returnsCompletedColor() {
    val status = PaymentStatus.CONFIRMED
    assertEquals(bkgCompletedColor, status.color())
  }

  @Test
  fun paymentStatus_allValuesHaveUniqueNames() {
    val names = PaymentStatus.entries.map { it.name() }
    assertEquals(names.size, names.distinct().size)
  }

  @Test
  fun paymentStatus_allValuesHaveColors() {
    PaymentStatus.entries.forEach { status ->
      assertNotNull("PaymentStatus $status should have a color", status.color())
    }
  }
}
