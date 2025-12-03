package com.android.sample.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Shared composables for card components. These helper functions reduce cognitive complexity by
 * extracting reusable UI patterns.
 */
@Composable
internal fun StatusBadge(
    isActive: Boolean,
    activeColor: Color,
    activeTextColor: Color,
    testTag: String
) {
  val backgroundColor = if (isActive) activeColor else MaterialTheme.colorScheme.errorContainer
  val textColor = if (isActive) activeTextColor else MaterialTheme.colorScheme.onErrorContainer
  val statusText = if (isActive) "Active" else "Inactive"

  Surface(
      color = backgroundColor,
      shape = RoundedCornerShape(4.dp),
      modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).testTag(testTag))
      }
}

@Composable
internal fun CardTitle(title: String, testTag: String) {
  Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.testTag(testTag))
}

@Composable
internal fun CardDescription(description: String, testTag: String) {
  if (description.isNotBlank()) {
    Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.testTag(testTag))

    Spacer(modifier = Modifier.height(8.dp))
  }
}

@Composable
internal fun LocationAndDateRow(
    locationName: String,
    createdAt: java.util.Date,
    locationTestTag: String,
    dateTestTag: String
) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
      verticalAlignment = Alignment.CenterVertically) {
        LocationText(
            locationName = locationName, testTag = locationTestTag, modifier = Modifier.weight(1f))

        Spacer(modifier = Modifier.width(8.dp))

        CreatedDateText(createdAt = createdAt, testTag = dateTestTag)
      }
}

@Composable
internal fun LocationText(locationName: String, testTag: String, modifier: Modifier = Modifier) {
  val displayName = locationName.ifBlank { "No location" }
  Text(
      text = "📍 $displayName",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = modifier.testTag(testTag))
}

@Composable
internal fun CreatedDateText(createdAt: java.util.Date, testTag: String) {
  val formatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()) }
  val formattedDate =
      remember(createdAt, formatter) {
        createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
      }
  Text(
      text = "📅 $formattedDate",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.testTag(testTag))
}
