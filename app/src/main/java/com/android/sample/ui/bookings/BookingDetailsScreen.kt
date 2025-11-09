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

// --- Composable Principal ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    bkgViewModel: BookingDetailsViewModel = BookingDetailsViewModel(),
    bookingId: String
) {

  val uiState by bkgViewModel.uiState.collectAsState()

  LaunchedEffect(bookingId) { bkgViewModel.load(bookingId) }

  Scaffold() { paddingValues ->
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
    BookingHeader(uiState)

    HorizontalDivider()

    // 2. Section Informations de Session
    Text(
        text = "Informations de la Session",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold)
    DetailRow(
        label = "Type d'offre",
        value =
            when (uiState.type) {
              ListingType.PROPOSAL -> "Tutorat (Proposition)"
              ListingType.REQUEST -> "Demande (Recherche de Tuteur)"
            })
    DetailRow(label = "Matière", value = uiState.subject.name.replace("_", " "))
    DetailRow(label = "Localisation", value = uiState.location.name)

    HorizontalDivider()

    // 3. Section Horaires
    Text(
        text = "Horaires",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold)
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.getDefault())
    DetailRow(label = "Début de session", value = dateFormatter.format(uiState.start))
    DetailRow(label = "Fin de session", value = dateFormatter.format(uiState.end))

    HorizontalDivider()

    // 4. Description
    Text(
        text = "Description du besoin ou de l'offre",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold)
    Text(
        text = uiState.description.ifEmpty { "Aucune description fournie." },
        style = MaterialTheme.typography.bodyMedium)
  }
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

// --- Composable pour l'en-tête (utilise AnnotatedString pour le style) ---

@Composable
fun BookingHeader(uiState: BkgDetailsUIState) {
  val prefixText =
      when (uiState.type) {
        ListingType.REQUEST -> "Tuteur pour : "
        ListingType.PROPOSAL -> "Étudiant pour : "
      }

  // Définir les styles pour le préfixe (petit) et le corps (grand, gras)
  val baseStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)
  val prefixSize = MaterialTheme.typography.bodyLarge.fontSize // Taille légèrement plus petite

  val styledText = buildAnnotatedString {
    // Appliquer la taille plus petite au préfixe
    withStyle(style = SpanStyle(fontSize = prefixSize)) { append(prefixText) }

    // Appliquer le gras au titre du cours
    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(uiState.courseName) }
  }

  Column(horizontalAlignment = Alignment.Start) {
    Text(
        text = styledText,
        style = baseStyle, // Appliquer le style de base
        maxLines = 2,
        overflow = TextOverflow.Ellipsis)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Partenaire : ${uiState.creatorName}",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary)
  }
}
