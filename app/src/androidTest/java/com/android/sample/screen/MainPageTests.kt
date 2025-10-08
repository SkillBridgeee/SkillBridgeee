package com.android.sample.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.android.sample.HomeScreen
import com.android.sample.HomeScreenTestTags
import org.junit.Rule
import org.junit.Test

class MainPageTests {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun allSectionsAreDisplayed() {
        composeRule.setContent {
            HomeScreen()
        }

        composeRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeScreenTestTags.EXPLORE_SKILLS_SECTION).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeScreenTestTags.TUTOR_LIST).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertExists()
    }
    @Test
    fun skillCardsAreClickable(){
        composeRule.setContent {
            HomeScreen()
        }

        composeRule.onAllNodesWithTag(HomeScreenTestTags.SKILL_CARD).onFirst().assertIsDisplayed().performClick()
    }

    @Test
    fun skillCardsAreWellDisplayed(){
        composeRule.setContent {
            HomeScreen()
        }

        composeRule.onAllNodesWithTag(HomeScreenTestTags.SKILL_CARD).onFirst().assertIsDisplayed()
    }

    //@Test
    /*fun tutorListIsScrollable(){
        composeRule.setContent {
            HomeScreen()
        }

        composeRule.onNodeWithTag(HomeScreenTestTags.TUTOR_LIST).performScrollToIndex(2)
    }*/

    @Test
    fun tutorListIsWellDisplayed(){
        composeRule.setContent {
            HomeScreen()
        }

        composeRule.onAllNodesWithTag(HomeScreenTestTags.TUTOR_CARD).onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithTag(HomeScreenTestTags.TUTOR_BOOK_BUTTON).onFirst().assertIsDisplayed()
    }

    @Test
    fun fabAddIsClickable(){
        composeRule.setContent {
            HomeScreen()
        }

        composeRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertIsDisplayed().performClick()
    }

    @Test
    fun fabAddIsWellDisplayed(){
        composeRule.setContent {
            HomeScreen()
        }

        composeRule.onNodeWithTag(HomeScreenTestTags.FAB_ADD).assertIsDisplayed()
    }

    @Test
    fun tutorBookButtonIsClickable(){
        composeRule.setContent {
            HomeScreen()
        }

        composeRule.onAllNodesWithTag(HomeScreenTestTags.TUTOR_BOOK_BUTTON).onFirst().assertIsDisplayed().performClick()
    }

    @Test
    fun tutorBookButtonIsWellDisplayed(){
        composeRule.setContent {
            HomeScreen()
        }

        composeRule.onAllNodesWithTag(HomeScreenTestTags.TUTOR_BOOK_BUTTON).onFirst().assertIsDisplayed()
    }

    @Test
    fun welcomeSectionIsWellDisplayed(){
        composeRule.setContent {
            HomeScreen()
        }

        composeRule.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION).assertIsDisplayed()
    }
}