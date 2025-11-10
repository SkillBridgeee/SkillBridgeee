package com.android.sample.ui.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.android.sample.model.listing.ListingType
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    bkgViewModel: BookingDetailsViewModel = BookingDetailsViewModel(),
    bookingId: String
) {

  val uiState by bkgViewModel.uiState.collectAsState()

  LaunchedEffect(bookingId) { bkgViewModel.load(bookingId) }

  Scaffold { paddingValues ->
    if (uiState.courseName.isEmpty() && uiState.creatorName.isEmpty()) {
      Box(
          modifier = Modifier.fillMaxSize().padding(paddingValues),
          contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
    } else {
      BookingDetailsContent(
          uiState = uiState,
          modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp))
    }
  }
}

@Composable
fun BookingDetailsContent(uiState: BkgDetailsUIState, modifier: Modifier = Modifier) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {

    // Header
    BookingHeader(uiState)

    HorizontalDivider()

    // Info about the creator
    InfoCreator(uiState)

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
private fun BookingHeader(uiState: BkgDetailsUIState) {
  val prefixText =
      when (uiState.type) {
        ListingType.REQUEST -> "Teacher for : "
        ListingType.PROPOSAL -> "Student for : "
      }

  val baseStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)
  val prefixSize = MaterialTheme.typography.bodyLarge.fontSize

  val styledText = buildAnnotatedString {
    withStyle(style = SpanStyle(fontSize = prefixSize)) { append(prefixText) }
    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(uiState.courseName) }
  }

  Column(horizontalAlignment = Alignment.Start) {
    Text(text = styledText, style = baseStyle, maxLines = 2, overflow = TextOverflow.Ellipsis)
    Spacer(modifier = Modifier.height(4.dp))
  }
}

@Composable
private fun InfoCreator(uiState: BkgDetailsUIState) {
  val creatorRole =
      when (uiState.type) {
        ListingType.REQUEST -> "Student"
        ListingType.PROPOSAL -> "Tutor"
      }

  Text(
      text = "Information about the $creatorRole",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold)

  DetailRow(label = "Creator Name", value = uiState.creatorName)
  DetailRow(label = "Email", value = uiState.creatorMail)
}

@Composable
private fun InfoListing(uiState: BkgDetailsUIState) {
  Text(
      text = "Information about the course",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold)
  DetailRow(label = "Subject", value = uiState.subject.name.replace("_", " "))
  DetailRow(label = "Location", value = uiState.location.name)
  DetailRow(label = "Hourly Rate", value = uiState.hourlyRate)
}

@Composable
private fun InfoSchedule(uiState: BkgDetailsUIState) {
  Text(
      text = "Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
  val dateFormatter = SimpleDateFormat("dd/MM/yyyy 'to' HH:mm", Locale.getDefault())

  DetailRow(label = "Start of the session", value = dateFormatter.format(uiState.start))
  DetailRow(label = "End of the session", value = dateFormatter.format(uiState.end))
}

@Composable
private fun InfoDesc(uiState: BkgDetailsUIState) {
  Text(
      text = "Description of the listing",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold)
  Text(
      text = uiState.description.ifEmpty { "No description about the lessons." },
      style = MaterialTheme.typography.bodyMedium)
}

// --- Composable réutilisable pour une ligne de détail ---

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
