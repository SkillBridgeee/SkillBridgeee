package com.android.sample.ui.bookings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.listing.ListingType
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    bkgViewModel: BookingDetailsViewModel = viewModel(),
    bookingId: String,
    onCreatorClick: (String) -> Unit,
) {

  val uiState by bkgViewModel.bookingUiState.collectAsState()

  LaunchedEffect(bookingId) { bkgViewModel.load(bookingId) }

  Scaffold { paddingValues ->
    if (uiState.loadError) {
      Box(
          modifier = Modifier.fillMaxSize().padding(paddingValues),
          contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
    } else {
      BookingDetailsContent(
          uiState = uiState,
          onCreatorClick = { profileId -> onCreatorClick(profileId) },
          modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp))
    }
  }
}

@Composable
fun BookingDetailsContent(
    uiState: BookingUIState,
    onCreatorClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {

    // Header
    BookingHeader(uiState)

    HorizontalDivider()

    // Info about the creator
    InfoCreator(uiState = uiState, onCreatorClick = onCreatorClick)

    HorizontalDivider()

    // Info about the courses
    InfoListing(uiState)

    HorizontalDivider()

    // Schedule
    InfoSchedule(uiState)

    HorizontalDivider()

    // Description
    InfoDesc(uiState)
  }
}

// --- Composable pour l'en-tête (utilise AnnotatedString pour le style) ---

@Composable
private fun BookingHeader(uiState: BookingUIState) {
  val prefixText =
      when (uiState.listing.type) {
        ListingType.REQUEST -> "Teacher for : "
        ListingType.PROPOSAL -> "Student for : "
      }

  val baseStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)
  val prefixSize = MaterialTheme.typography.bodyLarge.fontSize

  val styledText = buildAnnotatedString {
    withStyle(style = SpanStyle(fontSize = prefixSize)) { append(prefixText) }
    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
      append(uiState.listing.skill.skill)
    }
  }

  Column(horizontalAlignment = Alignment.Start) {
    Text(text = styledText, style = baseStyle, maxLines = 2, overflow = TextOverflow.Ellipsis)
    Spacer(modifier = Modifier.height(4.dp))
  }
}

@Composable
private fun InfoCreator(uiState: BookingUIState, onCreatorClick: (String) -> Unit) {
  val creatorRole =
      when (uiState.listing.type) {
        ListingType.REQUEST -> "Student"
        ListingType.PROPOSAL -> "Tutor"
      }

  //  Text(
  //      text = "Information about the $creatorRole",
  //      style = MaterialTheme.typography.titleMedium,
  //      fontWeight = FontWeight.Bold)

  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = "Information about the $creatorRole",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier.clip(RoundedCornerShape(8.dp))
                    .clickable { onCreatorClick(uiState.booking.listingCreatorId) }
                    .padding(horizontal = 6.dp, vertical = 2.dp)) {
              Text(
                  text = "More Info",
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.primary)
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = "View profile",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(start = 4.dp).size(18.dp))
            }
      }

  DetailRow(label = "$creatorRole Name", value = uiState.creatorProfile.name!!)
  DetailRow(label = "Email", value = uiState.creatorProfile.email)
}

@Composable
private fun InfoListing(uiState: BookingUIState) {
  Text(
      text = "Information about the course",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold)
  DetailRow(label = "Subject", value = uiState.listing.skill.mainSubject.name.replace("_", " "))
  DetailRow(label = "Location", value = uiState.listing.location.name)
  DetailRow(label = "Hourly Rate", value = uiState.booking.price.toString())
}

@Composable
private fun InfoSchedule(uiState: BookingUIState) {
  Text(
      text = "Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
  val dateFormatter = SimpleDateFormat("dd/MM/yyyy 'to' HH:mm", Locale.getDefault())

  DetailRow(
      label = "Start of the session", value = dateFormatter.format(uiState.booking.sessionStart))
  DetailRow(label = "End of the session", value = dateFormatter.format(uiState.booking.sessionEnd))
}

@Composable
private fun InfoDesc(uiState: BookingUIState) {
  Text(
      text = "Description of the listing",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold)
  Text(text = uiState.listing.description, style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun DetailRow(label: String, value: String) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.width(8.dp))
    Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
  }
}
