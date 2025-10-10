package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.android.sample.ui.bookings.BookingCardUi
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.bookings.MyBookingsScreen
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.theme.SampleAppTheme
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MyBookingsRobolectricTest {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  private fun setContent(onOpen: (BookingCardUi) -> Unit = {}) {
    compose.setContent {
      SampleAppTheme {
        MyBookingsScreen(
            vm = MyBookingsViewModel(),
            navController = rememberNavController(),
            onOpenDetails = onOpen)
      }
    }
  }

  @Test
  fun renders_two_cards() {
    setContent()
    compose.onAllNodes(hasTestTag(MyBookingsPageTestTag.BOOKING_CARD)).assertCountEquals(2)
  }

  @Test
  fun shows_professor_and_course() {
    setContent()
    compose.onNodeWithText("Liam P.").assertIsDisplayed()
    compose.onNodeWithText("Piano Lessons").assertIsDisplayed()
    compose.onNodeWithText("Maria G.").assertIsDisplayed()
    compose.onNodeWithText("Calculus & Algebra").assertIsDisplayed()
  }

  @Test
  fun price_duration_and_date_visible() {
    val vm = MyBookingsViewModel()
    val items = vm.items.value

    setContent()
    compose
        .onNodeWithText("${items[0].pricePerHourLabel}-${items[0].durationLabel}")
        .assertIsDisplayed()
    compose
        .onNodeWithText("${items[1].pricePerHourLabel}-${items[1].durationLabel}")
        .assertIsDisplayed()
    compose.onNodeWithText(items[0].dateLabel).assertIsDisplayed()
    compose.onNodeWithText(items[1].dateLabel).assertIsDisplayed()
  }

  @Test
  fun details_button_click_passes_item() {
    val clicked = AtomicReference<BookingCardUi?>()
    setContent { clicked.set(it) }

    compose
        .onAllNodesWithTag(MyBookingsPageTestTag.BOOKING_DETAILS_BUTTON)
        .assertCountEquals(2)
        .onFirst()
        .assertIsDisplayed()
        .performClick()

    // sanity check that the callback received a real UI item
    requireNotNull(clicked.get())
  }

  @Test
  fun avatar_initials_visible() {
    setContent()
    compose.onNodeWithText("L").assertIsDisplayed()
    compose.onNodeWithText("M").assertIsDisplayed()
  }

  @Test
  fun top_app_bar_title_wrapper_is_displayed() {
    setContent()
    compose.onNodeWithTag(MyBookingsPageTestTag.TOP_BAR_TITLE).assertIsDisplayed()
  }

  @Test
  fun back_button_not_present_on_root() {
    setContent()
    compose.onAllNodesWithTag(MyBookingsPageTestTag.GO_BACK).assertCountEquals(0)
  }

  @Test
  fun bottom_nav_bar_and_items_are_displayed() {
    setContent()
    compose.onNodeWithTag(MyBookingsPageTestTag.BOTTOM_NAV).assertIsDisplayed()
    compose.onNodeWithTag(MyBookingsPageTestTag.NAV_HOME).assertIsDisplayed()
    compose.onNodeWithTag(MyBookingsPageTestTag.NAV_BOOKINGS).assertIsDisplayed()
    compose.onNodeWithTag(MyBookingsPageTestTag.NAV_PROFILE).assertIsDisplayed()
  }

  @Test
  fun rating_row_shows_stars_and_counts() {
    setContent()
    compose.onNodeWithText("★★★★★").assertIsDisplayed()
    compose.onNodeWithText("(23)").assertIsDisplayed()
    compose.onNodeWithText("★★★★☆").assertIsDisplayed()
    compose.onNodeWithText("(41)").assertIsDisplayed()
  }

  @Test
  fun empty_state_renders_zero_cards() {
    // build a VM whose list is empty, without changing production code
    val emptyVm =
        MyBookingsViewModel().also { vm ->
          val f = vm::class.java.getDeclaredField("_items")
          f.isAccessible = true
          @Suppress("UNCHECKED_CAST")
          (f.get(vm) as MutableStateFlow<List<BookingCardUi>>).value = emptyList()
        }

    compose.setContent {
      SampleAppTheme { MyBookingsScreen(vm = emptyVm, navController = rememberNavController()) }
    }

    compose.onAllNodes(hasTestTag(MyBookingsPageTestTag.BOOKING_CARD)).assertCountEquals(0)
  }
}
