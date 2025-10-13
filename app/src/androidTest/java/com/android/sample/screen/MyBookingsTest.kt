package com.android.sample.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.android.sample.model.booking.FakeBookingRepository
import com.android.sample.ui.bookings.BookingCardUi
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.bookings.MyBookingsScreen
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.components.BottomNavBar
import com.android.sample.ui.components.TopAppBar
import com.android.sample.ui.theme.SampleAppTheme
import org.junit.Rule
import org.junit.Test

class MyBookingsTests {

  @get:Rule val composeRule = createComposeRule()

  @Composable
  private fun TestHost(nav: NavHostController, content: @Composable () -> Unit) {
    Scaffold(
        topBar = { Box(Modifier.testTag(MyBookingsPageTestTag.TOP_BAR_TITLE)) { TopAppBar(nav) } },
        bottomBar = {
          Box(Modifier.testTag(MyBookingsPageTestTag.BOTTOM_NAV)) { BottomNavBar(nav) }
        }) { inner ->
          Box(Modifier.padding(inner)) { content() }
        }
  }

  private fun setContent(onOpen: (BookingCardUi) -> Unit = {}) {
    composeRule.setContent {
      SampleAppTheme {
        val nav = rememberNavController()
        TestHost(nav) {
          MyBookingsScreen(
              viewModel = MyBookingsViewModel(FakeBookingRepository(), "s1"),
              navController = nav,
              onOpenDetails = onOpen)
        }
      }
    }
  }

  @Test
  fun shows_empty_state_when_no_bookings() {
    val emptyVm =
        MyBookingsViewModel(FakeBookingRepository(), "s1", initialLoadBlocking = true).apply {
          val f = this::class.java.getDeclaredField("_items")
          f.isAccessible = true
          @Suppress("UNCHECKED_CAST")
          (f.get(this) as kotlinx.coroutines.flow.MutableStateFlow<List<BookingCardUi>>).value =
              emptyList()
        }

    composeRule.setContent {
      SampleAppTheme {
        val nav = rememberNavController()
        TestHost(nav) { MyBookingsScreen(viewModel = emptyVm, navController = nav) }
      }
    }

    composeRule.onAllNodes(hasTestTag(MyBookingsPageTestTag.BOOKING_CARD)).assertCountEquals(0)
  }

  @Test
  fun screen_scaffold_path_renders_list() {
    // prepare a ViewModel with two UI items so the screen actually renders them
    val vm = MyBookingsViewModel(FakeBookingRepository(), "s1")
    val f = vm::class.java.getDeclaredField("_items")
    f.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    (f.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<List<BookingCardUi>>).value =
        listOf(
            BookingCardUi(
                id = "b1",
                tutorId = "t1",
                tutorName = "Alice",
                subject = "Math",
                pricePerHourLabel = "$50/hr",
                durationLabel = "2hrs",
                dateLabel = "01/01/2025",
                ratingStars = 5,
                ratingCount = 10),
            BookingCardUi(
                id = "b2",
                tutorId = "t2",
                tutorName = "Bob",
                subject = "Physics",
                pricePerHourLabel = "$40/hr",
                durationLabel = "1h 30m",
                dateLabel = "02/01/2025",
                ratingStars = 4,
                ratingCount = 5))

    composeRule.setContent {
      SampleAppTheme { MyBookingsScreen(viewModel = vm, navController = rememberNavController()) }
    }

    composeRule.onAllNodes(hasTestTag(MyBookingsPageTestTag.BOOKING_CARD)).assertCountEquals(2)
  }
}
