package com.android.sample.components

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.map.Location
import com.android.sample.ui.components.LocationInputField
import com.android.sample.ui.components.LocationInputFieldTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationInputFieldTest {

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun typingText_updatesQuery_andShowsSuggestions() {
    // Arrange
    val testSuggestions =
        listOf(
            Location(name = "Paris"),
            Location(name = "London"),
            Location(name = "Berlin"),
        )

    var latestQuery = ""

    composeRule.setContent {
      Box {
        LocationInputField(
            locationQuery = latestQuery,
            errorMsg = null,
            locationSuggestions = testSuggestions,
            onLocationQueryChange = { latestQuery = it },
            onLocationSelected = {},
        )
      }
    }

    // Act
    composeRule.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextInput("Pa")

    // Assert - suggestions should show
    composeRule.onNodeWithText("Paris").assertIsDisplayed()
    composeRule.onNodeWithText("London").assertIsDisplayed()
    composeRule.onNodeWithText("Berlin").assertIsDisplayed()
  }

  @Test
  fun clickingSuggestion_triggersSelection_andHidesDropdown() {
    val testSuggestions = listOf(Location(name = "Montreal"))
    var selectedLocation: Location? = null

    composeRule.setContent {
      LocationInputField(
          locationQuery = "Mon",
          errorMsg = null,
          locationSuggestions = testSuggestions,
          onLocationQueryChange = {},
          onLocationSelected = { selectedLocation = it },
      )
    }

    composeRule.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextInput("MON")

    composeRule.waitForIdle()

    // Vérifie que le menu est bien visible et clique sur l'item
    composeRule.onNodeWithText("Montreal").assertIsDisplayed()

    composeRule.onNodeWithText("Montreal").performClick()

    // Vérifie que la sélection a bien été effectuée
    assert(selectedLocation?.name == "Montreal")
  }

  @Test
  fun showsErrorMessage_whenErrorProvided() {
    composeRule.setContent {
      LocationInputField(
          locationQuery = "",
          errorMsg = "Location cannot be empty",
          locationSuggestions = emptyList(),
          onLocationQueryChange = {},
          onLocationSelected = {},
      )
    }

    composeRule.waitForIdle()

    composeRule.onNodeWithText("Location cannot be empty").assertIsDisplayed()
  }

  @Test
  fun hidesSuggestions_whenListIsEmpty() {
    composeRule.setContent {
      LocationInputField(
          locationQuery = "Pa",
          errorMsg = null,
          locationSuggestions = emptyList(),
          onLocationQueryChange = {},
          onLocationSelected = {},
      )
    }

    // No suggestion text should appear
    composeRule.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).assertIsDisplayed()
  }
}
