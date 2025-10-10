package com.android.sample.components

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import com.android.sample.ui.components.RatingStars
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
}
