package com.android.sample.ui.bookings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.android.sample.ui.bookings.MyBookingsPageTestTag.BOOKING_CARD
import com.android.sample.ui.bookings.MyBookingsPageTestTag.BOOKING_DETAILS_BUTTON
import com.android.sample.ui.bookings.MyBookingsPageTestTag.BOTTOM_NAV
import com.android.sample.ui.bookings.MyBookingsPageTestTag.TOP_BAR_TITLE
import com.android.sample.ui.theme.SampleAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class MyBookingsRobolectricExtraTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    // Helper: set VM items without subclassing (class is final)
    private fun MyBookingsViewModel.setItemsForTest(list: List<BookingCardUi>) {
        val f = MyBookingsViewModel::class.java.getDeclaredField("_items")
        f.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        (f.get(this) as MutableStateFlow<List<BookingCardUi>>).value = list
    }

    // Render screen with a provided list
    private fun setWithItems(items: List<BookingCardUi>) {
        val vm = MyBookingsViewModel().apply { setItemsForTest(items) }
        compose.setContent {
            SampleAppTheme {
                MyBookingsScreen(
                    vm = vm,
                    navController = rememberNavController()
                )
            }
        }
    }

    @Test
    fun topBar_title_text_is_visible() {
        compose.setContent {
            SampleAppTheme {
                MyBookingsScreen(
                    vm = MyBookingsViewModel(),
                    navController = rememberNavController()
                )
            }
        }
        compose.onAllNodesWithTag(TOP_BAR_TITLE).assertCountEquals(1)
        // Title string comes from shared TopAppBar; ensure some title is shown
        compose.onNodeWithText("SkillBridge").assertIsDisplayed()
    }

    @Test
    fun bottomBar_is_rendered() {
        compose.setContent {
            SampleAppTheme {
                MyBookingsScreen(
                    vm = MyBookingsViewModel(),
                    navController = rememberNavController()
                )
            }
        }
        compose.onAllNodesWithTag(BOTTOM_NAV).assertCountEquals(1)
        compose.onNodeWithText("Home").assertIsDisplayed()
        compose.onNodeWithText("Skills").assertIsDisplayed()
        compose.onNodeWithText("Profile").assertIsDisplayed()
        compose.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun details_buttons_count_matches_cards_and_click_works() {
        compose.setContent {
            SampleAppTheme {
                MyBookingsScreen(
                    vm = MyBookingsViewModel(),
                    navController = rememberNavController()
                )
            }
        }
        val expected = MyBookingsViewModel().items.value.size
        compose.onAllNodesWithTag(BOOKING_CARD).assertCountEquals(expected)
        compose.onAllNodesWithTag(BOOKING_DETAILS_BUTTON).assertCountEquals(expected).onFirst().performClick()
    }

    @Test
    fun rating_is_clamped_between_0_and_5() {
        val clampedItems = listOf(
            BookingCardUi("hi","alice","Piano","$10/hr","1hr","01/01/2026",-1,0), // -> 0 stars
            BookingCardUi("lo","bob","Guitar","$20/hr","2hrs","02/01/2026",7,99)  // -> 5 stars
        )
        setWithItems(clampedItems)
        compose.onNodeWithText("alice").assertIsDisplayed()
        compose.onNodeWithText("bob").assertIsDisplayed()
        compose.onNodeWithText("☆☆☆☆☆").assertIsDisplayed()
        compose.onNodeWithText("★★★★★").assertIsDisplayed()
    }

    @Test
    fun avatar_initial_is_uppercased_first_letter() {
        val single = listOf(
            BookingCardUi("x","zoe l.","Math","$15/hr","1hr","03/01/2026",3,10)
        )
        setWithItems(single)
        compose.onNodeWithText("Z").assertIsDisplayed()
    }
}
