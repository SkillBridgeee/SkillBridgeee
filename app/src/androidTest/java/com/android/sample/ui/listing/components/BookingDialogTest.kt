package com.android.sample.ui.listing.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.ui.listing.ListingScreenTestTags
import org.junit.Rule
import org.junit.Test

class BookingDialogTest {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun bookingDialog_displaysTitle() {
    compose.setContent { BookingDialog(onDismiss = {}, onConfirm = { _, _ -> }) }

    compose.onNodeWithText("Book Session").assertIsDisplayed()
    compose.onNodeWithText("Select session start and end times:").assertIsDisplayed()
  }

  @Test
  fun bookingDialog_hasSessionStartButton() {
    compose.setContent { BookingDialog(onDismiss = {}, onConfirm = { _, _ -> }) }

    compose.onNodeWithTag(ListingScreenTestTags.SESSION_START_BUTTON).assertIsDisplayed()
    compose
        .onNodeWithTag(ListingScreenTestTags.SESSION_START_BUTTON)
        .assertTextContains("Select Start Time")
  }

  @Test
  fun bookingDialog_hasSessionEndButton() {
    compose.setContent { BookingDialog(onDismiss = {}, onConfirm = { _, _ -> }) }

    compose.onNodeWithTag(ListingScreenTestTags.SESSION_END_BUTTON).assertIsDisplayed()
    compose
        .onNodeWithTag(ListingScreenTestTags.SESSION_END_BUTTON)
        .assertTextContains("Select End Time")
  }

  @Test
  fun bookingDialog_confirmButton_initiallyDisabled() {
    compose.setContent { BookingDialog(onDismiss = {}, onConfirm = { _, _ -> }) }

    compose.onNodeWithTag(ListingScreenTestTags.CONFIRM_BOOKING_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun bookingDialog_hasCancelButton() {
    compose.setContent { BookingDialog(onDismiss = {}, onConfirm = { _, _ -> }) }

    compose.onNodeWithTag(ListingScreenTestTags.CANCEL_BOOKING_BUTTON).assertIsDisplayed()
    compose.onNodeWithTag(ListingScreenTestTags.CANCEL_BOOKING_BUTTON).assertTextContains("Cancel")
  }

  @Test
  fun bookingDialog_cancelButton_callsDismiss() {
    var dismissed = false
    compose.setContent { BookingDialog(onDismiss = { dismissed = true }, onConfirm = { _, _ -> }) }

    compose.onNodeWithTag(ListingScreenTestTags.CANCEL_BOOKING_BUTTON).performClick()

    assert(dismissed)
  }

  @Test
  fun bookingDialog_startDatePicker_opensOnStartButtonClick() {
    compose.setContent { BookingDialog(onDismiss = {}, onConfirm = { _, _ -> }) }

    compose.onNodeWithTag(ListingScreenTestTags.SESSION_START_BUTTON).performClick()
    compose.waitForIdle()

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.START_DATE_PICKER_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    compose.onNodeWithTag(ListingScreenTestTags.START_DATE_PICKER_DIALOG).assertIsDisplayed()
  }

  @Test
  fun bookingDialog_startDatePicker_canBeCancelled() {
    compose.setContent { BookingDialog(onDismiss = {}, onConfirm = { _, _ -> }) }

    compose.onNodeWithTag(ListingScreenTestTags.SESSION_START_BUTTON).performClick()
    compose.waitForIdle()

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.START_DATE_PICKER_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    compose.waitForIdle()
    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithText("Cancel", useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    compose.onAllNodesWithText("Cancel", useUnmergedTree = true)[0].performClick()
    compose.waitForIdle()

    compose.onNodeWithTag(ListingScreenTestTags.START_DATE_PICKER_DIALOG).assertIsDisplayed()
  }

  @Test
  fun bookingDialog_startDatePicker_okButton_opensTimePicker() {
    compose.setContent { BookingDialog(onDismiss = {}, onConfirm = { _, _ -> }) }

    compose.onNodeWithTag(ListingScreenTestTags.SESSION_START_BUTTON).performClick()
    compose.waitForIdle()

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.START_DATE_PICKER_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    compose.waitForIdle()
    compose.waitUntil(5_000) {
      compose.onAllNodesWithText("OK", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    }
    compose.onAllNodesWithText("OK", useUnmergedTree = true)[0].performClick()
    compose.waitForIdle()

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.START_TIME_PICKER_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    compose.onNodeWithTag(ListingScreenTestTags.START_TIME_PICKER_DIALOG).assertIsDisplayed()
  }

  @Test
  fun bookingDialog_startTimePicker_canBeCancelled() {
    compose.setContent { BookingDialog(onDismiss = {}, onConfirm = { _, _ -> }) }

    compose.onNodeWithTag(ListingScreenTestTags.SESSION_START_BUTTON).performClick()
    compose.waitForIdle()

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.START_DATE_PICKER_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    compose.waitForIdle()
    compose.waitUntil(5_000) {
      compose.onAllNodesWithText("OK", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    }
    compose.onAllNodesWithText("OK", useUnmergedTree = true)[0].performClick()
    compose.waitForIdle()

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.START_TIME_PICKER_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    compose.waitForIdle()
    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithText("Cancel", useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    val cancelButtons = compose.onAllNodesWithText("Cancel", useUnmergedTree = true)
    cancelButtons[cancelButtons.fetchSemanticsNodes().size - 1].performClick()
    compose.waitForIdle()

    compose.onNodeWithTag(ListingScreenTestTags.START_TIME_PICKER_DIALOG).assertDoesNotExist()
  }

  @Test
  fun bookingDialog_endDatePicker_opensOnEndButtonClick() {
    compose.setContent { BookingDialog(onDismiss = {}, onConfirm = { _, _ -> }) }

    compose.onNodeWithTag(ListingScreenTestTags.SESSION_END_BUTTON).performClick()
    compose.waitForIdle()

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.END_DATE_PICKER_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    compose.onNodeWithTag(ListingScreenTestTags.END_DATE_PICKER_DIALOG).assertIsDisplayed()
  }

  @Test
  fun bookingDialog_endDatePicker_canBeCancelled() {
    compose.setContent { BookingDialog(onDismiss = {}, onConfirm = { _, _ -> }) }

    compose.onNodeWithTag(ListingScreenTestTags.SESSION_END_BUTTON).performClick()
    compose.waitForIdle()

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.END_DATE_PICKER_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    compose.waitForIdle()
    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithText("Cancel", useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    compose.onAllNodesWithText("Cancel", useUnmergedTree = true)[0].performClick()
    compose.waitForIdle()

    compose.onNodeWithTag(ListingScreenTestTags.END_DATE_PICKER_DIALOG).assertIsDisplayed()
  }

  @Test
  fun bookingDialog_afterSelectingBothTimes_confirmButtonEnabled() {
    compose.setContent { BookingDialog(onDismiss = {}, onConfirm = { _, _ -> }) }

    // Select start time
    compose.onNodeWithTag(ListingScreenTestTags.SESSION_START_BUTTON).performClick()
    compose.waitForIdle()

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.START_DATE_PICKER_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    compose.waitForIdle()
    compose.waitUntil(5_000) {
      compose.onAllNodesWithText("OK", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    }
    compose.onAllNodesWithText("OK", useUnmergedTree = true)[0].performClick()
    compose.waitForIdle()

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.START_TIME_PICKER_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    compose.waitForIdle()
    compose.waitUntil(5_000) {
      compose.onAllNodesWithText("OK", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    }
    val okButtons1 = compose.onAllNodesWithText("OK", useUnmergedTree = true)
    okButtons1[okButtons1.fetchSemanticsNodes().size - 1].performClick()
    compose.waitForIdle()

    // Select end time
    compose.onNodeWithTag(ListingScreenTestTags.SESSION_END_BUTTON).performClick()
    compose.waitForIdle()

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.END_DATE_PICKER_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    compose.waitForIdle()
    compose.waitUntil(5_000) {
      compose.onAllNodesWithText("OK", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    }
    compose.onAllNodesWithText("OK", useUnmergedTree = true)[0].performClick()
    compose.waitForIdle()

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.END_TIME_PICKER_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    compose.waitForIdle()
    compose.waitUntil(5_000) {
      compose.onAllNodesWithText("OK", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    }
    val okButtons2 = compose.onAllNodesWithText("OK", useUnmergedTree = true)
    okButtons2[okButtons2.fetchSemanticsNodes().size - 1].performClick()
    compose.waitForIdle()

    // Confirm button should now be enabled
    compose.onNodeWithTag(ListingScreenTestTags.CONFIRM_BOOKING_BUTTON).assertIsDisplayed()
  }

  @Test
  fun bookingDialog_hasCorrectTestTag() {
    compose.setContent { BookingDialog(onDismiss = {}, onConfirm = { _, _ -> }) }

    compose.onNodeWithTag(ListingScreenTestTags.BOOKING_DIALOG).assertExists()
  }
}
