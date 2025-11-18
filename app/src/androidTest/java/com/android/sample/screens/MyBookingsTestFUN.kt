package com.android.sample.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.components.BookingCardTestTag
import com.android.sample.utils.AppTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MyBookingsTestFUN : AppTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()
    composeTestRule.setContent { CreateEveryThing() }
    composeTestRule.navigateToMyBookings()
  }

  @Test
  fun testGoodScreen() {
    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.MY_BOOKINGS_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BookingCardTestTag.CARD).assertIsDisplayed()
  }
}
