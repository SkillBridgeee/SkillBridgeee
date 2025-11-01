package com.android.sample.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.sample.model.listing.Proposal
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
    modifier: Modifier = Modifier,
    testTag: String = ProposalCardTestTags.CARD
) {
  Card(
      shape = MaterialTheme.shapes.medium,
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      modifier = modifier.clickable { onClick(proposal.listingId) }.testTag(testTag)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              ProposalCardContent(proposal = proposal)
              Spacer(modifier = Modifier.width(16.dp))
              ProposalCardPriceSection(hourlyRate = proposal.hourlyRate)
            }
      }
}

@Composable
private fun RowScope.ProposalCardContent(proposal: Proposal) {
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
                location = com.android.sample.model.map.Location(name = "Campus Library"),
                isActive = true,
                skill =
                    com.android.sample.model.skill.Skill(
                        mainSubject = com.android.sample.model.skill.MainSubject.ACADEMICS,
                        skill = "Algebra",
                        skillTime = 5.0,
                        expertise = com.android.sample.model.skill.ExpertiseLevel.ADVANCED),
                createdAt = java.util.Date()),
        onClick = {})
  }
}
