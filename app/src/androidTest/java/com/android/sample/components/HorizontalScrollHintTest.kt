package com.android.sample.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.android.sample.ui.components.HorizontalScrollHint
import org.junit.Rule
import org.junit.Test

class HorizontalScrollHintTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val arrowContentDescription = "Scroll for more subjects"

    @Test
    fun horizontalScrollHint_visible_showsArrow() {
        composeTestRule.setContent {
            MaterialTheme {
                HorizontalScrollHint(visible = true)
            }
        }

        composeTestRule
            .onNodeWithContentDescription(arrowContentDescription)
            .assertIsDisplayed()
    }

    @Test
    fun horizontalScrollHint_notVisible_hidesArrow() {
        composeTestRule.setContent {
            MaterialTheme {
                HorizontalScrollHint(visible = false)
            }
        }

        composeTestRule
            .onNodeWithContentDescription(arrowContentDescription)
            .assertDoesNotExist()
    }
}
