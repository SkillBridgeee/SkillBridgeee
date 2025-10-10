package com.android.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sample.ui.theme.AccentBlue
import com.android.sample.ui.theme.AccentGreen
import com.android.sample.ui.theme.AccentPurple
import com.android.sample.ui.theme.PrimaryColor
import com.android.sample.ui.theme.SecondaryColor

object HomeScreenTestTags {
  const val WELCOME_SECTION = "welcomeSection"
  const val EXPLORE_SKILLS_SECTION = "exploreSkillsSection"
  const val SKILL_CARD = "skillCard"
  const val TOP_TUTOR_SECTION = "topTutorSection"
  const val TUTOR_CARD = "tutorCard"
  const val TUTOR_BOOK_BUTTON = "tutorBookButton"
  const val TUTOR_LIST = "tutorList"
  const val FAB_ADD = "fabAdd"
}

@Preview
@Composable
fun HomeScreen() {
  Scaffold(
      bottomBar = {},
      floatingActionButton = {
        FloatingActionButton(
            onClick = { /* TODO add new tutor */},
            containerColor = PrimaryColor,
            modifier = Modifier.testTag(HomeScreenTestTags.FAB_ADD)) {
              Icon(Icons.Default.Add, contentDescription = "Add")
            }
      }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize().background(Color.White)) {
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
  Column(
      modifier = Modifier.padding(horizontal = 10.dp).testTag(HomeScreenTestTags.WELCOME_SECTION)) {
        Text("Welcome back, Ava!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Ready to learn something new today?", color = Color.Gray, fontSize = 14.sp)
      }
}

@Composable
fun ExploreSkills() {
  Column(
      modifier =
          Modifier.padding(horizontal = 10.dp).testTag(HomeScreenTestTags.EXPLORE_SKILLS_SECTION)) {
        Text("Explore skills", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          // TODO: remove when we are able to have a list of the skills to dispaly
          SkillCard("Academics", AccentBlue)
          SkillCard("Music", AccentPurple)
          SkillCard("Sports", AccentGreen)
        }
      }
}

@Composable
fun SkillCard(title: String, bgColor: Color) {
  Column(
      modifier =
          Modifier.background(bgColor, RoundedCornerShape(12.dp))
              .padding(16.dp)
              .testTag(HomeScreenTestTags.SKILL_CARD),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontWeight = FontWeight.Bold, color = Color.Black)
      }
}

@Composable
fun TutorsSection() {
  Column(
      modifier =
          Modifier.padding(horizontal = 10.dp)
              .verticalScroll(rememberScrollState())
              .testTag(HomeScreenTestTags.TUTOR_LIST)) {
        Text(
            "Top-Rated Tutors",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.testTag(HomeScreenTestTags.TOP_TUTOR_SECTION))
        Spacer(modifier = Modifier.height(10.dp))

        // TODO: remove when we will have the database and connect to the list of the tutors
        TutorCard("Liam P.", "Piano Lessons", "$25/hr", 23)
        TutorCard("Maria G.", "Calculus & Algebra", "$30/hr", 41)
        TutorCard("David C.", "Acoustic Guitar", "$20/hr", 18)
      }
}

@Composable
fun TutorCard(name: String, subject: String, price: String, reviews: Int) {
  Card(
      modifier =
          Modifier.fillMaxWidth().padding(vertical = 5.dp).testTag(HomeScreenTestTags.TUTOR_CARD),
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(4.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Surface(shape = CircleShape, color = Color.LightGray, modifier = Modifier.size(40.dp)) {}

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold)
            Text(subject, color = SecondaryColor)
            Row {
              repeat(5) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp))
              }
              Text("($reviews)", fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
            }
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(price, color = SecondaryColor, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = { /* book tutor */},
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag(HomeScreenTestTags.TUTOR_BOOK_BUTTON)) {
                  Text("Book")
                }
          }
        }
      }
}
