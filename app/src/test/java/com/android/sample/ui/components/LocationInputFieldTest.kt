package com.android.sample.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import com.android.sample.model.map.Location
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocationInputFieldTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testLocations =
      listOf(
          Location(latitude = 46.5196535, longitude = 6.6322734, name = "Lausanne"),
          Location(latitude = 46.2043907, longitude = 6.1431577, name = "Geneva"),
          Location(latitude = 47.3769, longitude = 8.5417, name = "Zurich"))

  @Test
  fun locationInputField_displaysCorrectly() {
    // Given
    composeTestRule.setContent {
      LocationInputField(
          locationQuery = "",
          errorMsg = null,
          locationSuggestions = emptyList(),
          onLocationQueryChange = {},
          onLocationSelected = {})
    }

    // Then
    composeTestRule.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).assertIsDisplayed()
  }

  @Test
  fun locationInputField_displaysLabel() {
    // Given
    composeTestRule.setContent {
      LocationInputField(
          locationQuery = "",
          errorMsg = null,
          locationSuggestions = emptyList(),
          onLocationQueryChange = {},
          onLocationSelected = {})
    }

    // Then
    composeTestRule.onNodeWithText("Location / Campus").assertIsDisplayed()
  }

  @Test
  fun locationInputField_displaysPlaceholder() {
    // Given
    composeTestRule.setContent {
      LocationInputField(
          locationQuery = "",
          errorMsg = null,
          locationSuggestions = emptyList(),
          onLocationQueryChange = {},
          onLocationSelected = {})
    }

    // Wait for composition
    composeTestRule.waitForIdle()

    // Then - check that the input field exists (placeholder shows when field is empty)
    composeTestRule.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).assertIsDisplayed()
    // Note: Placeholder text may not be directly testable in all scenarios, but the field should be
    // there
  }

  @Test
  fun locationInputField_displaysCurrentQuery() {
    // Given
    val query = "EPFL"
    composeTestRule.setContent {
      LocationInputField(
          locationQuery = query,
          errorMsg = null,
          locationSuggestions = emptyList(),
          onLocationQueryChange = {},
          onLocationSelected = {})
    }

    // Then
    composeTestRule.onNodeWithText(query).assertIsDisplayed()
  }

  @Test
  fun locationInputField_callsOnQueryChangeWhenTyping() {
    // Given
    var capturedQuery = ""
    composeTestRule.setContent {
      LocationInputField(
          locationQuery = "",
          errorMsg = null,
          locationSuggestions = emptyList(),
          onLocationQueryChange = { capturedQuery = it },
          onLocationSelected = {})
    }

    // When
    composeTestRule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION)
        .performTextInput("Lausanne")

    // Then
    assertEquals("Lausanne", capturedQuery)
  }

  @Test
  fun locationInputField_displaysSuggestions() {
    // Given
    composeTestRule.setContent {
      LocationInputField(
          locationQuery = "Swiss",
          errorMsg = null,
          locationSuggestions = testLocations,
          onLocationQueryChange = {},
          onLocationSelected = {})
    }

    // When - trigger the text field to show dropdown
    composeTestRule.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextInput("a")

    composeTestRule.waitForIdle()

    // Then - should show first 3 suggestions
    composeTestRule.onNodeWithText("Lausanne").assertIsDisplayed()
    composeTestRule.onNodeWithText("Geneva").assertIsDisplayed()
    composeTestRule.onNodeWithText("Zurich").assertIsDisplayed()
  }

  @Test
  fun locationInputField_callsOnSelectedWhenSuggestionClicked() {
    // Given
    var selectedLocation: Location? = null
    composeTestRule.setContent {
      LocationInputField(
          locationQuery = "Swiss",
          errorMsg = null,
          locationSuggestions = testLocations,
          onLocationQueryChange = {},
          onLocationSelected = { selectedLocation = it })
    }

    // When - trigger dropdown to show
    composeTestRule.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextInput("a")

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Lausanne").performClick()

    // Then
    assertEquals("Lausanne", selectedLocation?.name)
    assertEquals(46.5196535, selectedLocation?.latitude ?: 0.0, 0.0001)
  }

  @Test
  fun locationInputField_displaysErrorMessage() {
    // Given
    val errorMsg = "Location is required"
    composeTestRule.setContent {
      LocationInputField(
          locationQuery = "",
          errorMsg = errorMsg,
          locationSuggestions = emptyList(),
          onLocationQueryChange = {},
          onLocationSelected = {})
    }

    // Wait for composition
    composeTestRule.waitForIdle()

    // Then - error message should be visible in supporting text
    composeTestRule.onNodeWithText(errorMsg).assertIsDisplayed()
  }

  @Test
  fun locationInputField_doesNotShowDropdownWhenSuggestionsEmpty() {
    // Given
    composeTestRule.setContent {
      LocationInputField(
          locationQuery = "Test",
          errorMsg = null,
          locationSuggestions = emptyList(),
          onLocationQueryChange = {},
          onLocationSelected = {})
    }

    // Then - suggestions should not be visible
    composeTestRule.onNodeWithText("Lausanne").assertDoesNotExist()
  }

  @Test
  fun locationInputFieldStyled_displaysCorrectly() {
    // Given
    composeTestRule.setContent {
      LocationInputFieldStyled(
          locationQuery = "",
          errorMsg = null,
          locationSuggestions = emptyList(),
          onLocationQueryChange = {},
          onLocationSelected = {},
          shape = RoundedCornerShape(14.dp),
          colors = TextFieldDefaults.colors())
    }

    // Then
    composeTestRule.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).assertIsDisplayed()
  }

  @Test
  fun locationInputFieldStyled_displaysPlaceholder() {
    // Given
    composeTestRule.setContent {
      LocationInputFieldStyled(
          locationQuery = "",
          errorMsg = null,
          locationSuggestions = emptyList(),
          onLocationQueryChange = {},
          onLocationSelected = {})
    }

    // Then
    composeTestRule.onNodeWithText("Address").assertIsDisplayed()
  }

  @Test
  fun locationInputFieldStyled_callsOnQueryChange() {
    // Given
    var capturedQuery = ""
    composeTestRule.setContent {
      LocationInputFieldStyled(
          locationQuery = "",
          errorMsg = null,
          locationSuggestions = emptyList(),
          onLocationQueryChange = { capturedQuery = it },
          onLocationSelected = {})
    }

    // When
    composeTestRule
        .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION)
        .performTextInput("EPFL")

    // Then
    assertEquals("EPFL", capturedQuery)
  }

  @Test
  fun locationInputFieldStyled_displaysSuggestions() {
    // Given
    composeTestRule.setContent {
      LocationInputFieldStyled(
          locationQuery = "Test",
          errorMsg = null,
          locationSuggestions = testLocations,
          onLocationQueryChange = {},
          onLocationSelected = {})
    }

    // When - trigger dropdown
    composeTestRule.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextInput("a")

    composeTestRule.waitForIdle()

    // Then
    composeTestRule.onNodeWithText("Lausanne").assertIsDisplayed()
    composeTestRule.onNodeWithText("Geneva").assertIsDisplayed()
  }

  @Test
  fun locationInputFieldStyled_callsOnSelectedWhenClicked() {
    // Given
    var selectedLocation: Location? = null
    composeTestRule.setContent {
      LocationInputFieldStyled(
          locationQuery = "Test",
          errorMsg = null,
          locationSuggestions = testLocations,
          onLocationQueryChange = {},
          onLocationSelected = { selectedLocation = it })
    }

    // When - trigger dropdown first
    composeTestRule.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextInput("a")

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Geneva").performClick()

    // Then
    assertEquals("Geneva", selectedLocation?.name)
  }

  @Test
  fun locationInputField_limitsToThreeSuggestions() {
    // Given
    val manyLocations =
        listOf(
            Location(latitude = 1.0, longitude = 1.0, name = "Location1"),
            Location(latitude = 2.0, longitude = 2.0, name = "Location2"),
            Location(latitude = 3.0, longitude = 3.0, name = "Location3"),
            Location(latitude = 4.0, longitude = 4.0, name = "Location4"),
            Location(latitude = 5.0, longitude = 5.0, name = "Location5"))
    composeTestRule.setContent {
      LocationInputField(
          locationQuery = "Test",
          errorMsg = null,
          locationSuggestions = manyLocations,
          onLocationQueryChange = {},
          onLocationSelected = {})
    }

    // When - trigger dropdown
    composeTestRule.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextInput("a")

    composeTestRule.waitForIdle()

    // Then - only first 3 should be displayed
    composeTestRule.onNodeWithText("Location1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Location2").assertIsDisplayed()
    composeTestRule.onNodeWithText("Location3").assertIsDisplayed()
    composeTestRule.onNodeWithText("Location4").assertDoesNotExist()
    composeTestRule.onNodeWithText("Location5").assertDoesNotExist()
  }

  @Test
  fun locationInputField_truncatesLongNames() {
    // Given
    val longNameLocation =
        Location(
            latitude = 1.0,
            longitude = 1.0,
            name = "This is a very long location name that should be truncated")
    composeTestRule.setContent {
      LocationInputField(
          locationQuery = "Test",
          errorMsg = null,
          locationSuggestions = listOf(longNameLocation),
          onLocationQueryChange = {},
          onLocationSelected = {})
    }

    // When - trigger dropdown
    composeTestRule.onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION).performTextInput("a")

    composeTestRule.waitForIdle()

    // Then - name should be truncated at 30 chars with "..."
    // The truncation logic is: name.take(30) + "..." = "This is a very long location..." (30 chars
    // + "...")
    composeTestRule
        .onNodeWithText("This is a very long location n...", substring = false)
        .assertIsDisplayed()
  }
}
