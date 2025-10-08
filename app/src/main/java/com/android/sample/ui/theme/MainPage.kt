package com.android.sample.ui.theme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun HomeScreen() {
    Scaffold(
        bottomBar = {  },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO add new tutor */ },
                containerColor = Color(0xFF00ACC1)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFEFEFEF))
        ) {

            Spacer(modifier = Modifier.height(10.dp))
            GreetingSection()
            Spacer(modifier = Modifier.height(20.dp))
            ExploreSkills()
            Spacer(modifier = Modifier.height(20.dp))
            TutorsSection()
        }
    }
}

@Composable
fun GreetingSection() {
    Column(modifier = Modifier.padding(horizontal = 10.dp)) {
        Text("Welcome back, Ava!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Ready to learn something new today?", color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun ExploreSkills() {
    Column(modifier = Modifier.padding(horizontal = 10.dp)) {
        Text("Explore skills", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SkillCard("Academics", Color(0xFF4FC3F7))
            SkillCard("Music", Color(0xFFBA68C8))
            SkillCard("Sports", Color(0xFF81C784))
        }
    }
}

@Composable
fun SkillCard(title: String, bgColor: Color) {
    Column(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
fun TutorsSection() {
    Column(modifier = Modifier.padding(horizontal = 10.dp)) {
        Text("Top-Rated Tutors", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(10.dp))

        TutorCard("Liam P.", "Piano Lessons", "$25/hr", 23)
        TutorCard("Maria G.", "Calculus & Algebra", "$30/hr", 41)
        TutorCard("David C.", "Acoustic Guitar", "$20/hr", 18)
    }
}

@Composable
fun TutorCard(name: String, subject: String, price: String, reviews: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.LightGray,
                modifier = Modifier.size(40.dp)
            ) {}

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold)
                Text(subject, color = Color(0xFF1E88E5))
                Row {
                    repeat(5) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text("($reviews)", fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(price, color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = { /* book tutor */ },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Book")
                }
            }
        }
    }
}

