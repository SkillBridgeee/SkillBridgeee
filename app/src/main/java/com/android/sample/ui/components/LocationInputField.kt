package com.android.sample.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.android.sample.model.map.Location

object LocationInputFieldTestTags {
  const val INPUT_LOCATION = "inputLocation"
  const val ERROR_MSG = "errorMsg"
}

/**
 * A composable input field for searching and selecting a location.
 *
 * Displays an [OutlinedTextField] that allows the user to enter a location name or address, along
 * with an optional dropdown list of location suggestions.
 *
 * When the user types into the text field, [onLocationQueryChange] is triggered to update the
 * search query, and the dropdown menu appears with matching [locationSuggestions]. Selecting an
 * item from the dropdown triggers [onLocationSelected] and closes the menu.
 *
 * @param locationQuery The current text value of the location input field.
 * @param errorMsg An optional error message to display below the text field.
 * @param locationSuggestions A list of suggested [Location] objects based on the current query.
 * @param onLocationQueryChange Callback invoked when the user updates the query text.
 * @param onLocationSelected Callback invoked when the user selects a suggested location.
 * @param modifier Optional [Modifier] for styling and layout customization.
 * @see OutlinedTextField
 * @see DropdownMenu
 */
@Composable
fun LocationInputField(
    locationQuery: String,
    errorMsg: String?,
    locationSuggestions: List<Location?>,
    onLocationQueryChange: (String) -> Unit,
    onLocationSelected: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
  var showDropdown by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = locationQuery,
        onValueChange = {
          onLocationQueryChange(it)
          showDropdown = true
        },
        label = { Text("Location / Campus") },
        placeholder = { Text("Enter an Address or Location") },
        isError = errorMsg != null,
        supportingText = {
          errorMsg?.let { Text(text = it, modifier.testTag(LocationInputFieldTestTags.ERROR_MSG)) }
        },
        modifier = Modifier.fillMaxWidth().testTag(LocationInputFieldTestTags.INPUT_LOCATION))

    DropdownMenu(
        expanded = showDropdown && locationSuggestions.isNotEmpty(),
        onDismissRequest = { showDropdown = false },
        properties = PopupProperties(focusable = false),
        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
          locationSuggestions.filterNotNull().take(3).forEach { location ->
            DropdownMenuItem(
                text = {
                  Text(
                      text = location.name.take(30) + if (location.name.length > 30) "..." else "",
                      maxLines = 1)
                },
                onClick = {
                  onLocationSelected(location)
                  showDropdown = false
                },
                modifier = Modifier.padding(8.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
          }
        }
  }
}
