package com.android.sample.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import java.util.Date
import java.util.Locale

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

@Composable
private fun RowScope.ProposalCardContent(proposal: Proposal, rating: RatingInfo?) {
  Column(modifier = Modifier.weight(1f)) {
    StatusBadge(
        isActive = proposal.isActive,
        activeColor = MaterialTheme.colorScheme.primaryContainer,
        activeTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        testTag = ProposalCardTestTags.STATUS_BADGE)

    CardTitle(title = proposal.displayTitle(), testTag = ProposalCardTestTags.TITLE)
    Spacer(modifier = Modifier.height(4.dp))
    CardDescription(description = proposal.description, testTag = ProposalCardTestTags.DESCRIPTION)
    LocationAndDateRow(
        locationName = proposal.location.name,
        createdAt = proposal.createdAt,
        locationTestTag = ProposalCardTestTags.LOCATION,
        dateTestTag = ProposalCardTestTags.CREATED_DATE)

    // Compact rating row (optional)
    rating?.let {
      Spacer(modifier = Modifier.height(8.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        val avg = it.averageRating.coerceIn(0.0, 5.0)
        RatingStars(ratingOutOfFive = avg)
        Spacer(Modifier.width(8.dp))
        val ratingText =
            if (it.totalRatings == 0) {
              "No ratings yet"
            } else {
              String.format(Locale.getDefault(), "%.1f (%d)", avg, it.totalRatings)
            }
        Text(
            text = ratingText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontStyle = if (it.totalRatings == 0) FontStyle.Italic else FontStyle.Normal)
      }
    }
  }
}

@Composable
private fun ProposalCardPriceSection(hourlyRate: Double) {
  Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
    Text(
        text = String.format(Locale.getDefault(), "$%.2f/hr", hourlyRate),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.testTag(ProposalCardTestTags.HOURLY_RATE))

    Spacer(modifier = Modifier.height(8.dp))

    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = "View details",
        tint = MaterialTheme.colorScheme.onSurfaceVariant)
  }
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
