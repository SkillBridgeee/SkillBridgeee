package com.android.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.skill.Skill
import com.android.sample.ui.theme.PrimaryColor
import com.android.sample.ui.theme.SecondaryColor
import kotlin.random.Random

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
fun HomeScreen(mainPageViewModel: MainPageViewModel = viewModel()) {
  val uiState by mainPageViewModel.uiState.collectAsState()

  Scaffold(
      floatingActionButton = {
        FloatingActionButton(
            onClick = { mainPageViewModel.onAddTutorClicked() },
            containerColor = PrimaryColor,
            modifier = Modifier.testTag(HomeScreenTestTags.FAB_ADD)) {
              Icon(Icons.Default.Add, contentDescription = "Add")
            }
      }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize().background(Color.White)) {
          Spacer(modifier = Modifier.height(10.dp))
          GreetingSection(uiState.welcomeMessage)
          Spacer(modifier = Modifier.height(20.dp))
          ExploreSkills(uiState.skills)
          Spacer(modifier = Modifier.height(20.dp))
          TutorsSection(uiState.tutors, onBookClick = mainPageViewModel::onBookTutorClicked)
        }
      }
}

@Composable
fun GreetingSection(welcomeMessage: String) {
  Column(
      modifier = Modifier.padding(horizontal = 10.dp).testTag(HomeScreenTestTags.WELCOME_SECTION)) {
        Text(welcomeMessage, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Ready to learn something new today?", color = Color.Gray, fontSize = 14.sp)
      }
}

@Composable
fun ExploreSkills(skills: List<Skill>) {
  Column(
      modifier =
          Modifier.padding(horizontal = 10.dp).testTag(HomeScreenTestTags.EXPLORE_SKILLS_SECTION)) {
        Text("Explore skills", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          items(skills) { SkillCard(skill = it) }
        }
      }
}

@Composable
fun SkillCard(skill: Skill) {
  val randomColor = remember {
    Color(
        red = Random.nextFloat(), green = Random.nextFloat(), blue = Random.nextFloat(), alpha = 1f)
  }
  Column(
      modifier =
          Modifier.background(randomColor, RoundedCornerShape(12.dp))
              .padding(16.dp)
              .testTag(HomeScreenTestTags.SKILL_CARD),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(skill.skill, fontWeight = FontWeight.Bold, color = Color.Black)
      }
}

@Composable
fun TutorsSection(tutors: List<TutorCardUi>, onBookClick: (String) -> Unit) {
  Column(modifier = Modifier.padding(horizontal = 10.dp)) {
    Text(
        text = "Top-Rated Tutors",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.testTag(HomeScreenTestTags.TOP_TUTOR_SECTION))

    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(
        modifier = Modifier.testTag(HomeScreenTestTags.TUTOR_LIST).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
          items(tutors) { TutorCard(it, onBookClick) }
        }
  }
}

@Composable
fun TutorCard(tutor: TutorCardUi, onBookClick: (String) -> Unit) {
  Card(
      modifier =
          Modifier.fillMaxWidth().padding(vertical = 5.dp).testTag(HomeScreenTestTags.TUTOR_CARD),
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(4.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Surface(shape = CircleShape, color = Color.LightGray, modifier = Modifier.size(40.dp)) {}

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(tutor.name, fontWeight = FontWeight.Bold)
            Text(tutor.subject, color = SecondaryColor)
            Row {
              repeat(5) { i ->
                val tint = if (i < tutor.ratingStars) Color.Black else Color.Gray
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp))
              }
              Text(
                  "(${tutor.ratingCount})",
                  fontSize = 12.sp,
                  modifier = Modifier.padding(start = 4.dp))
            }
          }

          Column(horizontalAlignment = Alignment.End) {
            Text("$${tutor.hourlyRate} / hr", color = SecondaryColor, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = { onBookClick(tutor.name) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag(HomeScreenTestTags.TUTOR_BOOK_BUTTON)) {
                  Text("Book")
                }
          }
        }
      }
}
