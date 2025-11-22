package com.android.sample.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.android.sample.ui.components.VerticalScrollHint
import org.junit.Rule
import org.junit.Test

class VerticalScrollHintTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val arrowContentDescription = "Scroll down"

    @Test
    fun verticalScrollHint_visible_showsArrow() {
        composeTestRule.setContent {
            MaterialTheme {
                VerticalScrollHint(visible = true)
            }
        }

        composeTestRule
            .onNodeWithContentDescription(arrowContentDescription)
            .assertIsDisplayed()
    }

    @Test
    fun verticalScrollHint_notVisible_hidesArrow() {
        composeTestRule.setContent {
            MaterialTheme {
                VerticalScrollHint(visible = false)
            }
        }

        composeTestRule
            .onNodeWithContentDescription(arrowContentDescription)
            .assertDoesNotExist()
    }
}
