package com.android.sample.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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

// Add leadingIcon and enabled to the style to reduce top-level parameters
data class EllipsizingTextFieldStyle(
    val shape: RoundedCornerShape? = null,
    val colors: TextFieldColors? = null,
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    val leadingIcon: (@Composable (() -> Unit))? = null,
    val enabled: Boolean = true
)

@Suppress("LongParameterList")
@Composable
fun EllipsizingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    maxPreviewLength: Int = 40,
    style: EllipsizingTextFieldStyle = EllipsizingTextFieldStyle()
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

  // Choose defaults INSIDE @Composable
  val shape = style.shape ?: RoundedCornerShape(14.dp)
  val colors = style.colors ?: TextFieldDefaults.colors()

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
      enabled = style.enabled,
      shape = shape,
      visualTransformation = transform,
      leadingIcon = style.leadingIcon,
      keyboardOptions = style.keyboardOptions,
      colors =
          colors.copy(
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              disabledIndicatorColor = Color.Transparent))
}
