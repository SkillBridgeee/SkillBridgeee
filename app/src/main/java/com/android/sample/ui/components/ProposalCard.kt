package com.android.sample.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import java.util.Date

object ProposalCardTestTags {
  const val CARD = "ProposalCardTestTags.CARD"
  const val TITLE = "ProposalCardTestTags.TITLE"
  const val DESCRIPTION = "ProposalCardTestTags.DESCRIPTION"
  const val HOURLY_RATE = "ProposalCardTestTags.HOURLY_RATE"
  const val LOCATION = "ProposalCardTestTags.LOCATION"
  const val CREATED_DATE = "ProposalCardTestTags.CREATED_DATE"
  const val STATUS_BADGE = "ProposalCardTestTags.STATUS_BADGE"
}

/**
 * A card component displaying a proposal (tutor offering to teach).
 *
 * @param proposal The proposal data to display.
 * @param onClick Callback when the card is clicked, receives the proposal ID.
 * @param modifier Modifier for styling.
 * @param testTag Optional test tag for the card.
 */
@Composable
fun ProposalCard(
    proposal: Proposal,
    onClick: (String) -> Unit,
    rating: RatingInfo? = null,
    modifier: Modifier = Modifier,
    testTag: String = ProposalCardTestTags.CARD
) {
  ListingCardBase(
      id = proposal.listingId,
      title = proposal.displayTitle(),
      description = proposal.description,
      locationName = proposal.location.name,
      createdAt = proposal.createdAt,
      hourlyRate = proposal.hourlyRate,
      isActive = proposal.isActive,
      rating = rating,
      cardContainerColor = MaterialTheme.colorScheme.surfaceVariant,
      badgeActiveColor = MaterialTheme.colorScheme.primaryContainer,
      badgeActiveTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
      priceColor = MaterialTheme.colorScheme.primary,
      testTagCard = testTag,
      testTagTitle = ProposalCardTestTags.TITLE,
      testTagDescription = ProposalCardTestTags.DESCRIPTION,
      testTagHourlyRate = ProposalCardTestTags.HOURLY_RATE,
      testTagLocation = ProposalCardTestTags.LOCATION,
      testTagCreatedDate = ProposalCardTestTags.CREATED_DATE,
      testTagStatusBadge = ProposalCardTestTags.STATUS_BADGE,
      onClick = onClick,
      modifier = modifier)
}

@Preview
@Composable
private fun ProposalCardPreview() {
  MaterialTheme {
    ProposalCard(
        proposal =
            Proposal(
                listingId = "proposal-123",
                creatorUserId = "user-42",
                description = "Math tutoring for high school students",
                hourlyRate = 25.0,
                location = Location(name = "Campus Library"),
                isActive = true,
                skill =
                    Skill(
                        mainSubject = MainSubject.ACADEMICS,
                        skill = "Algebra",
                        skillTime = 5.0,
                        expertise = ExpertiseLevel.ADVANCED),
                createdAt = Date()),
        onClick = {},
        rating = RatingInfo(averageRating = 4.5, totalRatings = 10))
  }
}
