package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
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

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  // Render the shared bars so their testTags exist during tests
  @Composable
  private fun TestHost(nav: NavHostController, content: @Composable () -> Unit) {
    Scaffold(
        topBar = { com.android.sample.ui.components.TopAppBar(nav) },
        bottomBar = { com.android.sample.ui.components.BottomNavBar(nav) }) { inner ->
          Box(Modifier.padding(inner)) { content() }
        }
  }

  private fun setContent(onOpen: (BookingCardUi) -> Unit = {}) {
    composeRule.setContent {
      SampleAppTheme {
        val nav = rememberNavController()
        TestHost(nav) {
          MyBookingsScreen(vm = MyBookingsViewModel(), navController = nav, onOpenDetails = onOpen)
        }
      }
    }
  }

  @Test
  fun renders_two_cards() {
    setContent()
    composeRule.onAllNodes(hasTestTag(MyBookingsPageTestTag.BOOKING_CARD)).assertCountEquals(2)
  }

  @Test
  fun shows_professor_and_course() {
    setContent()
    composeRule.onNodeWithText("Liam P.").assertIsDisplayed()
    composeRule.onNodeWithText("Piano Lessons").assertIsDisplayed()
    composeRule.onNodeWithText("Maria G.").assertIsDisplayed()
    composeRule.onNodeWithText("Calculus & Algebra").assertIsDisplayed()
  }

  @Test
  fun price_duration_and_date_visible() {
    val vm = MyBookingsViewModel()
    val items = vm.items.value
    setContent()
    composeRule
        .onNodeWithText("${items[0].pricePerHourLabel}-${items[0].durationLabel}")
        .assertIsDisplayed()
    composeRule
        .onNodeWithText("${items[1].pricePerHourLabel}-${items[1].durationLabel}")
        .assertIsDisplayed()
    composeRule.onNodeWithText(items[0].dateLabel).assertIsDisplayed()
    composeRule.onNodeWithText(items[1].dateLabel).assertIsDisplayed()
  }

  @Test
  fun details_button_click_passes_item() {
    val clicked = AtomicReference<BookingCardUi?>()
    setContent { clicked.set(it) }
    composeRule
        .onAllNodesWithTag(MyBookingsPageTestTag.BOOKING_DETAILS_BUTTON)
        .assertCountEquals(2)
        .onFirst()
        .assertIsDisplayed()
        .performClick()
    requireNotNull(clicked.get())
  }

  @Test
  fun avatar_initials_visible() {
    setContent()
    composeRule.onNodeWithText("L").assertIsDisplayed()
    composeRule.onNodeWithText("M").assertIsDisplayed()
  }

  @Test
  fun top_app_bar_title_wrapper_is_displayed() {
    setContent()
    composeRule.onNodeWithTag(MyBookingsPageTestTag.TOP_BAR_TITLE).assertIsDisplayed()
  }

  @Test
  fun back_button_not_present_on_root() {
    setContent()
    composeRule.onAllNodesWithTag(MyBookingsPageTestTag.GO_BACK).assertCountEquals(0)
  }

  @Test
  fun bottom_nav_bar_and_items_are_displayed() {
    setContent()
    composeRule.onNodeWithTag(MyBookingsPageTestTag.BOTTOM_NAV).assertIsDisplayed()
    composeRule.onNodeWithTag(MyBookingsPageTestTag.NAV_HOME).assertIsDisplayed()
    composeRule.onNodeWithTag(MyBookingsPageTestTag.NAV_BOOKINGS).assertIsDisplayed()
    composeRule.onNodeWithTag(MyBookingsPageTestTag.NAV_PROFILE).assertIsDisplayed()
  }

  @Test
  fun empty_state_renders_zero_cards() {
    val emptyVm =
        MyBookingsViewModel().also { vm ->
          val f = vm::class.java.getDeclaredField("_items")
          f.isAccessible = true
          @Suppress("UNCHECKED_CAST")
          (f.get(vm) as MutableStateFlow<List<BookingCardUi>>).value = emptyList()
        }

    composeRule.setContent {
      SampleAppTheme {
        val nav = rememberNavController()
        TestHost(nav) { MyBookingsScreen(vm = emptyVm, navController = nav) }
      }
    }

    composeRule.onAllNodes(hasTestTag(MyBookingsPageTestTag.BOOKING_CARD)).assertCountEquals(0)
  }

  @Test
  fun rating_row_shows_stars_and_counts() {
    setContent()
    composeRule.onNodeWithText("★★★★★").assertIsDisplayed()
    composeRule.onNodeWithText("(23)").assertIsDisplayed()
    composeRule.onNodeWithText("★★★★☆").assertIsDisplayed()
    composeRule.onNodeWithText("(41)").assertIsDisplayed()
  }
}
