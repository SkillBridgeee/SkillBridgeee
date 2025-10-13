package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
import com.android.sample.model.booking.FakeBookingRepository
import com.android.sample.ui.bookings.BookingCardUi
import com.android.sample.ui.bookings.MyBookingsContent
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

  @Composable
  private fun TestHost(nav: NavHostController, content: @Composable () -> Unit) {
    Scaffold(
        topBar = {
          Box(Modifier.testTag(MyBookingsPageTestTag.TOP_BAR_TITLE)) {
            com.android.sample.ui.components.TopAppBar(nav)
          }
        },
        bottomBar = { Box { com.android.sample.ui.components.BottomNavBar(nav) } }) { inner ->
          Box(Modifier.padding(inner)) { content() }
        }
  }

  private fun setContent(onOpen: (BookingCardUi) -> Unit = {}) {
    composeRule.setContent {
      SampleAppTheme {
        val nav = rememberNavController()
        TestHost(nav) {
          MyBookingsContent(
              viewModel =
                  MyBookingsViewModel(
                      com.android.sample.model.booking.FakeBookingRepository(), "s1"),
              navController = nav,
              onOpenDetails = onOpen)
        }
      }
    }
  }

  @Test
  fun booking_card_renders_and_details_click() {
    // create a single UI item and inject into VM
    val ui =
        BookingCardUi(
            id = "x1",
            tutorId = "t1",
            tutorName = "Test Tutor",
            subject = "Test Subject",
            pricePerHourLabel = "$40/hr",
            durationLabel = "2hrs",
            dateLabel = "01/01/2025",
            ratingStars = 3,
            ratingCount = 5)

    val vm = MyBookingsViewModel(com.android.sample.model.booking.FakeBookingRepository(), "s1")
    val field = vm::class.java.getDeclaredField("_items")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    (field.get(vm) as MutableStateFlow<List<BookingCardUi>>).value = listOf(ui)

    val clicked = AtomicReference<BookingCardUi?>()
    composeRule.setContent {
      SampleAppTheme {
        val nav = rememberNavController()
        TestHost(nav) {
          MyBookingsContent(
              viewModel = vm, navController = nav, onOpenDetails = { clicked.set(it) })
        }
      }
    }

    composeRule.onNodeWithText("Test Tutor").assertIsDisplayed()
    composeRule.onNodeWithText("Test Subject").assertIsDisplayed()
    composeRule.onNodeWithTag(MyBookingsPageTestTag.BOOKING_DETAILS_BUTTON).performClick()
    requireNotNull(clicked.get())
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
    val vm = MyBookingsViewModel(FakeBookingRepository(), "s1")
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
        MyBookingsViewModel(FakeBookingRepository(), "s1").also { vm ->
          val f = vm::class.java.getDeclaredField("_items")
          f.isAccessible = true
          @Suppress("UNCHECKED_CAST")
          (f.get(vm) as MutableStateFlow<List<BookingCardUi>>).value = emptyList()
        }

    composeRule.setContent {
      SampleAppTheme {
        val nav = rememberNavController()
        TestHost(nav) { MyBookingsContent(viewModel = emptyVm, navController = nav) }
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

  // kotlin
  @Test
  fun `tutor name click invokes onOpenTutor`() {
    val ui =
        BookingCardUi(
            id = "x-click",
            tutorId = "t1",
            tutorName = "Clickable Tutor",
            subject = "Subj",
            pricePerHourLabel = "$10/hr",
            durationLabel = "1hr",
            dateLabel = "01/01/2025",
            ratingStars = 2,
            ratingCount = 1)

    val vm = MyBookingsViewModel(FakeBookingRepository(), "s1")
    val field = vm::class.java.getDeclaredField("_items")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    (field.get(vm) as MutableStateFlow<List<BookingCardUi>>).value = listOf(ui)

    val clicked = java.util.concurrent.atomic.AtomicReference<BookingCardUi?>()
    composeRule.setContent {
      SampleAppTheme {
        val nav = rememberNavController()
        TestHost(nav) {
          MyBookingsContent(viewModel = vm, navController = nav, onOpenTutor = { clicked.set(it) })
        }
      }
    }

    composeRule.onNodeWithText("Clickable Tutor").performClick()
    requireNotNull(clicked.get())
  }

  @Test
  fun rating_row_clamps_negative_and_over_five_values() {
    val low =
        BookingCardUi(
            id = "low",
            tutorId = "tlow",
            tutorName = "Low",
            subject = "S",
            pricePerHourLabel = "$0/hr",
            durationLabel = "1hr",
            dateLabel = "01/01/2025",
            ratingStars = -3,
            ratingCount = 0)

    val high =
        BookingCardUi(
            id = "high",
            tutorId = "thigh",
            tutorName = "High",
            subject = "S",
            pricePerHourLabel = "$0/hr",
            durationLabel = "1hr",
            dateLabel = "01/01/2025",
            ratingStars = 10,
            ratingCount = 99)

    val vm = MyBookingsViewModel(FakeBookingRepository(), "s1")
    val field = vm::class.java.getDeclaredField("_items")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    (field.get(vm) as MutableStateFlow<List<BookingCardUi>>).value = listOf(low, high)

    composeRule.setContent {
      SampleAppTheme {
        val nav = rememberNavController()
        TestHost(nav) { MyBookingsContent(viewModel = vm, navController = nav) }
      }
    }

    // negative -> shows all empty stars "☆☆☆☆☆"
    composeRule.onNodeWithText("☆☆☆☆☆").assertIsDisplayed()
    // >5 -> clamped to 5 full stars "★★★★★"
    composeRule.onNodeWithText("★★★★★").assertIsDisplayed()
  }

  @Test
  fun my_bookings_screen_scaffold_renders() {
    composeRule.setContent {
      SampleAppTheme {
        MyBookingsScreen(
            viewModel = MyBookingsViewModel(FakeBookingRepository(), "s1"),
            navController = rememberNavController())
      }
    }
    // Just ensure list renders; bar assertions live in your existing bar tests
    composeRule.onAllNodes(hasTestTag(MyBookingsPageTestTag.BOOKING_CARD)).assertCountEquals(2)
  }
}
