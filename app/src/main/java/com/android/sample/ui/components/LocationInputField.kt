package com.android.sample.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.android.sample.model.map.Location

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

  val locationMsgError = "Location cannot be empty"
  Box(modifier = modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = locationQuery,
        onValueChange = {
          onLocationQueryChange(it)
          showDropdown = true // afficher la liste dès qu’on tape
        },
        label = { Text("Location") },
        placeholder = { Text("Enter an Address or Location") },
        isError = errorMsg != null,
        supportingText = { errorMsg?.let { Text(text = it) } },
        modifier = Modifier.fillMaxWidth())

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
            Divider()
          }

          if (locationSuggestions.size > 3) {
            DropdownMenuItem(
                text = { Text("More...") },
                onClick = {
                  // Optionnel : afficher plus de résultats
                },
                modifier = Modifier.padding(8.dp))
          }
        }
  }
}
