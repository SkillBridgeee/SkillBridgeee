package com.android.sample.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.android.sample.model.listing.FirestoreListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.skill.MainSubject
import com.android.sample.ui.screens.newSkill.NewSkillScreen
import com.android.sample.ui.screens.newSkill.NewSkillScreenTestTag
import com.android.sample.ui.screens.newSkill.NewSkillViewModel
import com.android.sample.utils.RepositoryTest
import com.github.se.bootcamp.utils.FirebaseEmulator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewSkillScreenTest : RepositoryTest() {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var viewModel: NewSkillViewModel

  @Before
  fun setup() {
    super.setUp()
    firestore = FirebaseEmulator.firestore

    // Mock FirebaseAuth to bypass authentication
    auth = mockk(relaxed = true)
    val mockUser = mockk<FirebaseUser>()
    every { auth.currentUser } returns mockUser
    every { mockUser.uid } returns testUserId // testUserId is from RepositoryTest

    listingRepository = FirestoreListingRepository(firestore, auth)
    ListingRepositoryProvider.setForTests(listingRepository)

    viewModel = NewSkillViewModel(listingRepository)
  }

  @Test
  fun saveButton_isDisplayed_andClickable() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.BUTTON_SAVE_SKILL).performClick()
  }

  @Test
  fun createLessonsTitle_isDisplayed() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.CREATE_LESSONS_TITLE).assertIsDisplayed()
  }

  @Test
  fun inputCourseTitle_isDisplayed() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).assertIsDisplayed()
  }

  @Test
  fun inputDescription_isDisplayed() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).assertIsDisplayed()
  }

  @Test
  fun inputPrice_isDisplayed() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).assertIsDisplayed()
  }

  @Test
  fun subjectField_isDisplayed() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD).assertIsDisplayed()
  }

  @Test
  fun subjectDropdown_showsItems_whenClicked() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_FIELD).performClick()
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN).assertIsDisplayed()
    val itemsDisplay =
        composeTestRule
            .onAllNodesWithTag(NewSkillScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX)
            .fetchSemanticsNodes()
    Assert.assertEquals(MainSubject.entries.size, itemsDisplay.size)
  }

  @Test
  fun titleField_acceptsInput_andNoError() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    val testTitle = "Cours Kotlin"
    composeTestRule
        .onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE)
        .performTextInput(testTitle)
    composeTestRule
        .onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE)
        .assertTextContains(testTitle)
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INVALID_TITLE_MSG).assertIsNotDisplayed()
  }

  @Test
  fun descriptionField_acceptsInput_andNoError() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    val testDesc = "Description du cours"
    composeTestRule
        .onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION)
        .performTextInput(testDesc)
    composeTestRule
        .onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION)
        .assertTextContains(testDesc)
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INVALID_DESC_MSG).assertIsNotDisplayed()
  }

  @Test
  fun priceField_acceptsInput_andNoError() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    val testPrice = "25"
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput(testPrice)
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).assertTextContains(testPrice)
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INVALID_PRICE_MSG).assertIsNotDisplayed()
  }

  @Test
  fun titleField_empty_showsError() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_COURSE_TITLE).performTextInput(" ")
    composeTestRule
        .onNodeWithTag(testTag = NewSkillScreenTestTag.INVALID_TITLE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun descriptionField_empty_showsError() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).performTextClearance()
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_DESCRIPTION).performTextInput(" ")
    composeTestRule
        .onNodeWithTag(testTag = NewSkillScreenTestTag.INVALID_DESC_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun priceField_invalid_showsError() {
    composeTestRule.setContent { NewSkillScreen(profileId = "test") }
    composeTestRule.onNodeWithTag(NewSkillScreenTestTag.INPUT_PRICE).performTextInput("abc")
    composeTestRule
        .onNodeWithTag(testTag = NewSkillScreenTestTag.INVALID_PRICE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun setError_showsAllFieldErrors() {
    val vm = NewSkillViewModel()
    composeTestRule.setContent { NewSkillScreen(skillViewModel = vm, profileId = "test") }

    composeTestRule.runOnIdle { vm.setError() }

    composeTestRule
        .onNodeWithTag(NewSkillScreenTestTag.INVALID_TITLE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NewSkillScreenTestTag.INVALID_DESC_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NewSkillScreenTestTag.INVALID_PRICE_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NewSkillScreenTestTag.INVALID_SUBJECT_MSG, useUnmergedTree = true)
        .assertIsDisplayed()
  }
}
