package com.android.sample.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.components.ProposalCard
import com.android.sample.ui.components.RatingStars
import com.android.sample.ui.components.RequestCard

object ProfileScreenTestTags {
  const val SCREEN = "ProfileScreenTestTags.SCREEN"
  const val PROFILE_ICON = "ProfileScreenTestTags.PROFILE_ICON"
  const val NAME_TEXT = "ProfileScreenTestTags.NAME_TEXT"
  const val EMAIL_TEXT = "ProfileScreenTestTags.EMAIL_TEXT"
  const val LOCATION_TEXT = "ProfileScreenTestTags.LOCATION_TEXT"
  const val DESCRIPTION_TEXT = "ProfileScreenTestTags.DESCRIPTION_TEXT"
  const val TUTOR_RATING_SECTION = "ProfileScreenTestTags.TUTOR_RATING_SECTION"
  const val STUDENT_RATING_SECTION = "ProfileScreenTestTags.STUDENT_RATING_SECTION"
  const val TUTOR_RATING_VALUE = "ProfileScreenTestTags.TUTOR_RATING_VALUE"
  const val STUDENT_RATING_VALUE = "ProfileScreenTestTags.STUDENT_RATING_VALUE"
  const val PROPOSALS_SECTION = "ProfileScreenTestTags.PROPOSALS_SECTION"
  const val REQUESTS_SECTION = "ProfileScreenTestTags.REQUESTS_SECTION"
  const val LOADING_INDICATOR = "ProfileScreenTestTags.LOADING_INDICATOR"
  const val ERROR_TEXT = "ProfileScreenTestTags.ERROR_TEXT"
  const val BACK_BUTTON = "ProfileScreenTestTags.BACK_BUTTON"
  const val REFRESH_BUTTON = "ProfileScreenTestTags.REFRESH_BUTTON"
  const val EMPTY_PROPOSALS = "ProfileScreenTestTags.EMPTY_PROPOSALS"
  const val EMPTY_REQUESTS = "ProfileScreenTestTags.EMPTY_REQUESTS"
}

/**
 * ProfileScreen displays a user's profile including:
 * - Profile information (name, email, location, description)
 * - Tutor and Student ratings
 * - List of proposals (offerings to teach)
 * - List of requests (looking for tutors)
 *
 * @param profileId The ID of the profile to display.
 * @param onBackClick Callback when back button is clicked.
 * @param onProposalClick Callback when a proposal card is clicked.
 * @param onRequestClick Callback when a request card is clicked.
 * @param viewModel The ViewModel for managing profile data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileId: String,
    onProposalClick: (String) -> Unit = {},
    onRequestClick: (String) -> Unit = {},
    viewModel: ProfileScreenViewModel = viewModel {
      ProfileScreenViewModel(
          profileRepository = ProfileRepositoryProvider.repository,
          listingRepository = ListingRepositoryProvider.repository)
    }
) {
  // Properly observe StateFlow in Compose
  val uiState by viewModel.uiState.collectAsState()

  // Load profile data when profileId changes
  LaunchedEffect(profileId) { viewModel.loadProfile(profileId) }

  Scaffold(modifier = Modifier.testTag(ProfileScreenTestTags.SCREEN)) { paddingValues ->
    when {
      uiState.isLoading -> {
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center) {
              CircularProgressIndicator(
                  modifier = Modifier.testTag(ProfileScreenTestTags.LOADING_INDICATOR))
            }
      }
      uiState.errorMessage != null -> {
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center) {
              Text(
                  text = uiState.errorMessage ?: "Unknown error",
                  color = MaterialTheme.colorScheme.error,
                  modifier = Modifier.testTag(ProfileScreenTestTags.ERROR_TEXT))
            }
      }
      uiState.profile != null -> {
        ProfileContent(
            uiState = uiState,
            paddingValues = paddingValues,
            onProposalClick = onProposalClick,
            onRequestClick = onRequestClick)
      }
    }
  }
}

@Composable
private fun ProfileContent(
    uiState: ProfileScreenUiState,
    paddingValues: PaddingValues,
    onProposalClick: (String) -> Unit,
    onRequestClick: (String) -> Unit
) {
  val profile = uiState.profile ?: return

  LazyColumn(
      modifier = Modifier.fillMaxSize().padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Profile header
        item {
          Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally) {
                // Profile avatar
                Box(
                    modifier =
                        Modifier.size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .testTag(ProfileScreenTestTags.PROFILE_ICON),
                    contentAlignment = Alignment.Center) {
                      Text(
                          text = profile.name?.firstOrNull()?.uppercase() ?: "?",
                          style = MaterialTheme.typography.headlineLarge,
                          color = MaterialTheme.colorScheme.onPrimaryContainer,
                          fontWeight = FontWeight.Bold)
                    }

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                Text(
                    text = profile.name ?: "Unknown",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag(ProfileScreenTestTags.NAME_TEXT))

                Spacer(modifier = Modifier.height(4.dp))

                // Email
                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(ProfileScreenTestTags.EMAIL_TEXT))

                // Location
                if (profile.location.name.isNotBlank()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                      text = profile.location.name,
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.testTag(ProfileScreenTestTags.LOCATION_TEXT))
                }
              }
        }

        // Description
        if (profile.description.isNotBlank()) {
          item {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                  Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = profile.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag(ProfileScreenTestTags.DESCRIPTION_TEXT))
                  }
                }
          }
        }

        // Ratings section
        item {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Tutor Rating
                Card(
                    modifier =
                        Modifier.weight(1f).testTag(ProfileScreenTestTags.TUTOR_RATING_SECTION),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                      Column(
                          modifier = Modifier.fillMaxWidth().padding(12.dp),
                          horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "As Tutor",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(8.dp))
                            RatingStars(ratingOutOfFive = profile.tutorRating.averageRating)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text =
                                    String.format(
                                        "%.1f (${profile.tutorRating.totalRatings})",
                                        profile.tutorRating.averageRating),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier =
                                    Modifier.testTag(ProfileScreenTestTags.TUTOR_RATING_VALUE))
                          }
                    }

                // Student Rating
                Card(
                    modifier =
                        Modifier.weight(1f).testTag(ProfileScreenTestTags.STUDENT_RATING_SECTION),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                      Column(
                          modifier = Modifier.fillMaxWidth().padding(12.dp),
                          horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "As Student",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.height(8.dp))
                            RatingStars(ratingOutOfFive = profile.studentRating.averageRating)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text =
                                    String.format(
                                        "%.1f (${profile.studentRating.totalRatings})",
                                        profile.studentRating.averageRating),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier =
                                    Modifier.testTag(ProfileScreenTestTags.STUDENT_RATING_VALUE))
                          }
                    }
              }
        }

        // Proposals section
        item {
          Text(
              text = "Proposals (${uiState.proposals.size})",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag(ProfileScreenTestTags.PROPOSALS_SECTION))
        }

        if (uiState.proposals.isEmpty()) {
          item {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                  Text(
                      text = "No proposals yet",
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      textAlign = TextAlign.Center,
                      modifier =
                          Modifier.fillMaxWidth()
                              .padding(32.dp)
                              .testTag(ProfileScreenTestTags.EMPTY_PROPOSALS))
                }
          }
        } else {
          items(uiState.proposals) { proposal ->
            ProposalCard(proposal = proposal, onClick = onProposalClick)
          }
        }

        // Requests section
        item {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
              text = "Requests (${uiState.requests.size})",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag(ProfileScreenTestTags.REQUESTS_SECTION))
        }

        if (uiState.requests.isEmpty()) {
          item {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                  Text(
                      text = "No requests yet",
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      textAlign = TextAlign.Center,
                      modifier =
                          Modifier.fillMaxWidth()
                              .padding(32.dp)
                              .testTag(ProfileScreenTestTags.EMPTY_REQUESTS))
                }
          }
        } else {
          items(uiState.requests) { request ->
            RequestCard(request = request, onClick = onRequestClick)
          }
        }
      }
}
