package com.android.sample.ui.HomePage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.SkillsHelper
import com.android.sample.ui.components.HorizontalScrollHint
import com.android.sample.ui.components.ProposalCard
import com.android.sample.ui.components.RequestCard
import com.android.sample.ui.theme.PrimaryColor

/**
 * Provides test tag identifiers for the HomeScreen and its child composables.
 *
 * These tags are used to locate UI components during automated testing.
 */
object HomeScreenTestTags {
  const val WELCOME_SECTION = "welcomeSection"
  const val EXPLORE_SKILLS_SECTION = "exploreSkillsSection"
  const val ALL_SUBJECT_LIST = "allSubjectList"
  const val SKILL_CARD = "skillCard"
  const val PROPOSAL_SECTION = "proposalSection"
  const val PROPOSAL_CARD = "proposalCard"
  const val PROPOSAL_LIST = "proposalList"

  const val REFRESH_BUTTON = "refreshButton"

  const val REQUEST_SECTION = "requestSection"
  const val REQUEST_CARD = "requestCard"

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
@Composable
fun HomeScreen(
    mainPageViewModel: MainPageViewModel = MainPageViewModel(),
    onNavigateToSubjectList: (MainSubject) -> Unit = {},
    onNavigateToAddNewListing: () -> Unit,
    onNavigateToListingDetails: (String) -> Unit,
) {
  val uiState by mainPageViewModel.uiState.collectAsState()

  LaunchedEffect(Unit) { mainPageViewModel.load() }

  Scaffold(
      floatingActionButton = {
        FloatingActionButton(
            onClick = { onNavigateToAddNewListing() },
            containerColor = PrimaryColor,
            modifier = Modifier.testTag(HomeScreenTestTags.FAB_ADD)) {
              Icon(Icons.Default.Add, contentDescription = "Add")
            }
      }) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize().background(Color.White),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
              item {
                Spacer(modifier = Modifier.height(10.dp))
                GreetingSection(
                    welcomeMessage = uiState.welcomeMessage,
                    refresh = { mainPageViewModel.refreshListing() })
              }

              // Explore subjects
              item { ExploreSubjects(uiState.subjects, onNavigateToSubjectList) }

              when (val error = uiState.errorMsg) {
                null -> {
                  item {
                    ProposalsSection(
                        proposals = uiState.proposals, onProposalClick = onNavigateToListingDetails)
                  }
                  item {
                    RequestsSection(
                        requests = uiState.requests, onRequestClick = onNavigateToListingDetails)
                  }
                }
                else -> {
                  item { ErrorSection(errorMsg = error, reload = { mainPageViewModel.load() }) }
                }
              }

              // Bottom padding
              item { Spacer(modifier = Modifier.height(16.dp)) }
            }
      }
}

/**
 * Displays a greeting message and a short subtitle encouraging user engagement.
 *
 * @param welcomeMessage The personalized greeting text shown to the user.
 */
@Composable
fun GreetingSection(welcomeMessage: String, refresh: () -> Unit) {
  Row(modifier = Modifier.fillMaxWidth()) {
    // Greeting Section
    Column(
        modifier =
            Modifier.padding(horizontal = 10.dp).testTag(HomeScreenTestTags.WELCOME_SECTION)) {
          Text(welcomeMessage, fontWeight = FontWeight.Bold, fontSize = 18.sp)
          Text("Ready to learn something new today?", color = Color.Gray, fontSize = 14.sp)
        }

    Spacer(modifier = Modifier.weight(1f))

    // Refresh button
    IconButton(
        modifier = Modifier.padding(end = 16.dp).testTag(HomeScreenTestTags.REFRESH_BUTTON),
        onClick = refresh) {
          Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Refresh HomePage")
        }
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
  val listState = rememberLazyListState()
  val showHint by remember { derivedStateOf { listState.canScrollForward } }

  Column(
      modifier =
          Modifier.padding(horizontal = 10.dp).testTag(HomeScreenTestTags.EXPLORE_SKILLS_SECTION)) {
        Text(text = "Explore Subjects", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
          LazyRow(
              state = listState,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.fillMaxWidth().testTag(HomeScreenTestTags.ALL_SUBJECT_LIST)) {
                items(subjects) { subject ->
                  val subjectColor = SkillsHelper.getColorForSubject(subject)
                  SubjectCard(
                      subject = subject,
                      color = subjectColor,
                      onSubjectCardClicked = onSubjectCardClicked)
                }
              }

          HorizontalScrollHint(
              visible = showHint,
              modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp))
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
              .wrapContentSize(Alignment.Center)) {
        val textColor = if (color.luminance() > 0.5f) Color.Black else Color.White

        Text(text = subject.name, color = textColor)
      }
}

/**
 * Displays a list of top proposal listings.
 *
 * Shows a section title and a scrollable list of proposal cards.
 */
@Composable
fun ProposalsSection(proposals: List<Proposal>, onProposalClick: (String) -> Unit) {
  Column(modifier = Modifier.padding(horizontal = 10.dp)) {
    Text(
        text = "Latest Proposals",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.testTag(HomeScreenTestTags.PROPOSAL_SECTION))

    Spacer(modifier = Modifier.height(10.dp))

    if (proposals.isEmpty()) {
      Text(
          text = "No proposals available yet.",
          color = Color.Gray,
          fontSize = 14.sp,
          modifier = Modifier.padding(8.dp))
    } else {
      Column(
          modifier = Modifier.fillMaxWidth().testTag(HomeScreenTestTags.PROPOSAL_LIST),
          verticalArrangement = Arrangement.spacedBy(8.dp)) {
            proposals.forEach { proposal ->
              ProposalCard(
                  proposal = proposal,
                  onClick = onProposalClick,
                  modifier = Modifier.fillMaxWidth().testTag(HomeScreenTestTags.PROPOSAL_CARD))
            }
          }
    }
  }
}

/**
 * Displays a list of recent request listings.
 *
 * Shows a section title and a scrollable list of request cards.
 */
@Composable
fun RequestsSection(requests: List<Request>, onRequestClick: (String) -> Unit) {
  Column(modifier = Modifier.padding(horizontal = 10.dp)) {
    Text(
        text = "Recent Requests",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.testTag(HomeScreenTestTags.REQUEST_SECTION))

    Spacer(modifier = Modifier.height(10.dp))

    if (requests.isEmpty()) {
      Text(
          text = "No requests available yet.",
          color = Color.Gray,
          fontSize = 14.sp,
          modifier = Modifier.padding(8.dp))
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        requests.forEach { request ->
          RequestCard(
              request = request,
              onClick = onRequestClick,
              modifier = Modifier.fillMaxWidth().testTag(HomeScreenTestTags.REQUEST_CARD))
        }
      }
    }
  }
}

@Composable
fun ErrorSection(errorMsg: String, reload: () -> Unit) {
  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Something went wrong",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = errorMsg, fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = reload) { Text("Retry") }
      }
}
