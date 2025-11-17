package com.android.sample.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.bookings.BookingDetailsTestTag
import com.android.sample.utils.AppTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BookingDetailsScreenTestFUN : AppTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()
    composeTestRule.setContent { CreateEveryThing() }
    composeTestRule.navigateToBookingDetails()
  }

  @Test
  fun testGoodScreen() {
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.HEADER).assertIsDisplayed()
  }
}
