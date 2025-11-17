package com.android.sample.screens

import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.utils.AppTest
import org.junit.Before
import org.junit.Rule

class BookingDetailsScreenTestFUN : AppTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()
    composeTestRule.setContent { CreateEveryThing() }
    composeTestRule.navigateToMyBookings()
  }
}
