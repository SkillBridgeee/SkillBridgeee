package com.android.sample.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.sample.ui.theme.BlueApp
import com.android.sample.ui.theme.GreenApp


@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(300.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(BlueApp, GreenApp)
                ),
                shape = MaterialTheme.shapes.large
            )
    ) {
        ExtendedFloatingActionButton(
            modifier = Modifier.fillMaxWidth()
                .shadow(0.dp, MaterialTheme.shapes.large),
            containerColor = Color.Transparent,
            elevation = FloatingActionButtonDefaults.elevation(0.dp),
            onClick = onClick,
            content = {
                Text(
                    text = text,
                    color = Color.White
                )
            }
        )
    }

}