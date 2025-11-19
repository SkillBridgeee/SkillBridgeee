package com.android.sample.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.ui.components.RatingStars
import com.android.sample.ui.components.RatingStarsInput
import com.android.sample.ui.components.RatingStarsInputTestTags
import com.android.sample.ui.components.RatingStarsTestTags
import org.junit.Rule
import org.junit.Test

class RatingStarsTest {

  @get:Rule val compose = createComposeRule()

  @Test
  fun renders_correct_number_of_stars() {
    compose.setContent { RatingStars(ratingOutOfFive = 3.0) }

    compose.onAllNodesWithTag(RatingStarsTestTags.FILLED_STAR).assertCountEquals(3)
    compose.onAllNodesWithTag(RatingStarsTestTags.OUTLINED_STAR).assertCountEquals(2)
  }

  @Test
  fun clamps_below_zero_to_zero() {
    compose.setContent { RatingStars(ratingOutOfFive = -2.0) }
    compose.onAllNodesWithTag(RatingStarsTestTags.FILLED_STAR).assertCountEquals(0)
    compose.onAllNodesWithTag(RatingStarsTestTags.OUTLINED_STAR).assertCountEquals(5)
  }

  @Test
  fun clamps_above_five_to_five() {
    compose.setContent { RatingStars(ratingOutOfFive = 10.0) }
    compose.onAllNodesWithTag(RatingStarsTestTags.FILLED_STAR).assertCountEquals(5)
    compose.onAllNodesWithTag(RatingStarsTestTags.OUTLINED_STAR).assertCountEquals(0)
  }

  @Test
  fun exposes_all_star_tags_and_click_calls_callback() {
    var received = -1
    compose.setContent {
      MaterialTheme { RatingStarsInput(selectedStars = 0, onSelected = { received = it }) }
    }

    // ensure all star tags exist
    for (i in 1..5) {
      compose.onNodeWithTag("${RatingStarsInputTestTags.STAR_PREFIX}$i").assertExists()
    }

    // click star 4 and verify callback
    compose.onNodeWithTag("${RatingStarsInputTestTags.STAR_PREFIX}4").performClick()
    compose.waitForIdle()
    assert(received == 4)
  }

  @Test
  fun clicking_star_updates_host_state_selected_stars() {
    val selected = mutableStateOf(0)
    compose.setContent {
      MaterialTheme {
        RatingStarsInput(selectedStars = selected.value, onSelected = { selected.value = it })
      }
    }

    // click star 5 and verify state was updated via callback (triggers recomposition)
    compose.onNodeWithTag("${RatingStarsInputTestTags.STAR_PREFIX}5").performClick()
    compose.waitForIdle()
    assert(selected.value == 5)
  }
}
