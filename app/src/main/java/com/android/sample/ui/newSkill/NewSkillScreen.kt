package com.android.sample.ui.newSkill

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.material3.FabPosition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSkillScreen(
    profileId: String
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add a New Skill") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {}
            )
        },
        bottomBar = {
            // TODO implement bottom navigation Bar
            Text("BotBar")
        },
        floatingActionButton = {
            // TODO implement AppButton after pull request
        },
        floatingActionButtonPosition = FabPosition.Center,
        content = { pd ->
            SkillsContent(pd, profileId)
        }
    )
}




@Composable
fun SkillsContent(pd : PaddingValues, profileId: String) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(pd)

    ) {
        Spacer(modifier = Modifier.height(20.dp))


        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.9f)
                .background(
                    MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Gray, Color.LightGray)
                    ),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Create Your Lessons !",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))


                // Title Input
                OutlinedTextField(
                    value = "Course Title",
                    onValueChange = { },
                    label = { Text("Course Title") },
                    placeholder = { Text("Title") },
                    isError = false,
                    supportingText = {},
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Desc Input
                OutlinedTextField(
                    value = "Desc",
                    onValueChange = {  },
                    label = { Text("Description") },
                    placeholder = { Text("Description of the skill") },
                    isError = false,
                    supportingText = {},
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // category Input
                OutlinedTextField(
                    value = "Category",
                    onValueChange = {  },
                    label = { Text("Category") },
                    placeholder = { Text("What category") },
                    isError = false,
                    supportingText = {},
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Kind Input
                OutlinedTextField(
                    value = "Kind",
                    onValueChange = {  },
                    label = { Text("Kind") },
                    placeholder = { Text("What Kind") },
                    isError = false,
                    supportingText = {},
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Price Input
                OutlinedTextField(
                    value = "Price",
                    onValueChange = {},
                    label = { Text("Hourly Rate") },
                    placeholder = { Text("Price per Hours") },
                    isError = false,
                    supportingText = {},
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

            }
        }
    }
}



