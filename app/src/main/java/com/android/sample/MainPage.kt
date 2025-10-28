package com.android.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.MainPageViewModel.SubjectColors.getSubjectColor
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.ui.theme.PrimaryColor
import com.android.sample.ui.theme.SecondaryColor

/**
 * Provides test tag identifiers for the HomeScreen and its child composables.
 *
 * These tags are used to locate UI components during automated testing.
 */
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

/**
 * The main HomeScreen composable for the SkillBridge app.
 *
 * Displays a scaffolded layout containing:
 * - A Floating Action Button (FAB)
 * - Greeting section
 * - Skills exploration carousel
 * - List of top-rated tutors
 *
 * Data is provided by the [MainPageViewModel].
 *
 * @param mainPageViewModel The ViewModel providing UI state and event handlers.
 */
@Preview
@Composable
fun HomeScreen(
    mainPageViewModel: MainPageViewModel = viewModel(),
    onNavigateToNewSkill: (String) -> Unit = {},
    onNavigateToSubjectList: (MainSubject) -> Unit = {}
) {
  val uiState by mainPageViewModel.uiState.collectAsState()
  val navigationEvent by mainPageViewModel.navigationEvent.collectAsState()

  LaunchedEffect(navigationEvent) {
    navigationEvent?.let { profileId ->
      onNavigateToNewSkill(profileId)
      mainPageViewModel.onNavigationHandled()
    }
  }

  Scaffold(
      floatingActionButton = {
        FloatingActionButton(
            onClick = { mainPageViewModel.onAddTutorClicked("test") }, // Hardcoded user ID for now
            containerColor = PrimaryColor,
            modifier = Modifier.testTag(HomeScreenTestTags.FAB_ADD)) {
              Icon(Icons.Default.Add, contentDescription = "Add")
            }
      }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize().background(Color.White)) {
          Spacer(modifier = Modifier.height(10.dp))
          GreetingSection(uiState.welcomeMessage)
          Spacer(modifier = Modifier.height(20.dp))
          ExploreSubjects(uiState.subjects, onNavigateToSubjectList)
          Spacer(modifier = Modifier.height(20.dp))
          TutorsSection(uiState.tutors, onBookClick = mainPageViewModel::onBookTutorClicked)
        }
      }
}

/**
 * Displays a greeting message and a short subtitle encouraging user engagement.
 *
 * @param welcomeMessage The personalized greeting text shown to the user.
 */
@Composable
fun GreetingSection(welcomeMessage: String) {
  Column(
      modifier = Modifier.padding(horizontal = 10.dp).testTag(HomeScreenTestTags.WELCOME_SECTION)) {
        Text(welcomeMessage, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Ready to learn something new today?", color = Color.Gray, fontSize = 14.sp)
      }
}

/**
 * Displays a horizontally scrollable row of skill cards.
 *
 * Each card represents a skill available for learning.
 *
 * @param subjects The list of [MainSubject] items to display.
 * @param onSubjectCardClicked Callback invoked when a subject card is clicked for navigation.
 */
@Composable
fun ExploreSubjects(subjects: List<MainSubject>, onSubjectCardClicked: (MainSubject) -> Unit = {}) {
  Column(
      modifier =
          Modifier.padding(horizontal = 10.dp).testTag(HomeScreenTestTags.EXPLORE_SKILLS_SECTION)) {
        Text(text = "Explore Subjects", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()) {
              items(subjects) {
                val subjectColor = getSubjectColor(it)
                SubjectCard(subject = it, color = subjectColor, onSubjectCardClicked)
              }
            }
      }
}

/** Displays a single subject card with its color. */
@Composable
fun SubjectCard(
    subject: MainSubject,
    color: Color,
    onSubjectCardClicked: (MainSubject) -> Unit = {}
) {
  Column(
      modifier =
          Modifier.width(120.dp)
              .height(80.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(color)
              .clickable { onSubjectCardClicked(subject) }
              .padding(vertical = 16.dp, horizontal = 12.dp)
              .testTag(HomeScreenTestTags.SKILL_CARD)
              .wrapContentSize(Alignment.Center)
              .clickable { onSubjectCardClicked(subject) },
  ) {
    val textColor = if (color.luminance() > 0.5f) Color.Black else Color.White

    Text(text = subject.name, color = textColor)
  }
}

/**
 * Displays a vertical list of top-rated tutors using a [LazyColumn].
 *
 * Each item in the list is rendered using [TutorCard].
 *
 * @param tutors The list of [TutorCardUi] objects to display.
 * @param onBookClick The callback invoked when the "Book" button is clicked.
 */
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

/**
 * Displays a tutor’s information card, including name, subject, hourly rate, and rating stars.
 *
 * The card includes a "Book" button that triggers [onBookClick].
 *
 * @param tutor The [TutorCardUi] object containing tutor data.
 * @param onBookClick The callback executed when the "Book" button is clicked.
 */
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
            Text(
                "$${"%.2f".format(tutor.hourlyRate)} / hr",
                color = SecondaryColor,
                fontWeight = FontWeight.Bold)
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
