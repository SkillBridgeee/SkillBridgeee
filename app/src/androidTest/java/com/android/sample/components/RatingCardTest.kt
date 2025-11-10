package com.android.sample.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.StarRating
import com.android.sample.model.user.Profile
import com.android.sample.ui.components.RatingCard
import org.junit.Rule
import org.junit.Test

class RatingCardTest {

    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    val rating = Rating(
        "1",
        "user-1",
        "listing-1",
        StarRating.FIVE,
        "Excellent service!",
    )



    val profile = Profile(
        userId = "user-1",
        name = "John Doe",
        email = "",
        levelOfEducation = "Bachelor's Degree",
        location = com.android.sample.model.map.Location(name = "New York"),
        hourlyRate = "30",
        description = "Experienced tutor"
    )

    fun setUpContent() {
        composeRule.setContent {
            RatingCard(
                rating = rating,
                creator = profile
            )
        }
    }

    @Test
    fun ratingCard_isDisplayed() {
        setUpContent()

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("RatingCardTestTags.CARD").assertExists()

    }

    @Test
    fun ratingCard_displaysCreatorName() {
        setUpContent()

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("RatingCardTestTags.CREATOR_NAME").assertExists()
    }

    @Test
    fun ratingCard_displaysCreatorImage() {
        setUpContent()

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("RatingCardTestTags.CREATOR_IMAGE").assertExists()
    }

    @Test
    fun ratingCard_displaysComment() {
        setUpContent()

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("RatingCardTestTags.COMMENT").assertExists()
    }

    @Test
    fun ratingCard_displaysStars() {
        setUpContent()

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("RatingCardTestTags.STARS").assertExists()
    }

    @Test
    fun ratingCard_displaysCreatorGrade() {
        setUpContent()

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("RatingCardTestTags.CREATOR_GRADE").assertExists()
    }

    @Test
    fun ratingCard_displaysInfoPart() {
        setUpContent()

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("RatingCardTestTags.INFO_PART").assertExists()
    }

    @Test
    fun ratingCard_displaysCorrectCommentWhenComment() {
        setUpContent()

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("RatingCardTestTags.COMMENT")
        composeRule.onNodeWithText("Excellent service!").assertExists()

    }

    @Test
    fun ratingCard_displaysCorrectCommentWhenNoComment() {
        composeRule.setContent {
            RatingCard(
                rating = Rating(
                    "1",
                    "user-1",
                    "listing-1",
                    StarRating.FIVE,
                ),
                creator = profile
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("RatingCardTestTags.COMMENT")
        composeRule.onNodeWithText("No comment provided").assertExists()

    }

    @Test
    fun ratingCard_displaysCorrectCreatorName() {
        Profile(
            userId = "user-1",
            name = "John Doe",
            email = "",
            levelOfEducation = "Bachelor's Degree",
            location = com.android.sample.model.map.Location(name = "New York"),
            hourlyRate = "30",
            description = "Experienced tutor"
        )
        composeRule.setContent {
            RatingCard(
                rating = rating,
                creator = profile
            )
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("RatingCardTestTags.CREATOR_NAME").assertIsDisplayed()
        composeRule.onNodeWithText("by John Doe").assertExists()

    }

    @Test
    fun ratingCard_displaysCorrectCreatorGrade() {
        setUpContent()

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("RatingCardTestTags.CREATOR_GRADE").assertIsDisplayed()
        composeRule.onNodeWithText("(5)").assertExists()

    }
}