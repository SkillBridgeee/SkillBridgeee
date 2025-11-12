package com.android.sample.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun EllipsizingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    maxPreviewLength: Int = 40,
    shape: RoundedCornerShape = RoundedCornerShape(14.dp),
    colors: TextFieldColors = TextFieldDefaults.colors(),
    leadingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
  var focused by remember { mutableStateOf(false) }

  val transform = VisualTransformation { text ->
    if (!focused && text.text.length > maxPreviewLength) {
      val short = text.text.take(maxPreviewLength) + "..."
      TransformedText(AnnotatedString(short), OffsetMapping.Identity)
    } else {
      TransformedText(text, OffsetMapping.Identity)
    }
  }

  TextField(
      value = value,
      onValueChange = onValueChange,
      modifier =
          modifier
              .onFocusChanged { focused = it.isFocused }
              .semantics { if (!focused) contentDescription = value },
      placeholder = { Text(placeholder) },
      singleLine = true,
      maxLines = 1,
      shape = shape,
      visualTransformation = transform,
      leadingIcon = leadingIcon,
      keyboardOptions = keyboardOptions,
      colors =
          colors.copy(
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              disabledIndicatorColor = Color.Transparent))
}
