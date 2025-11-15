package com.android.sample.ui.bookings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.booking.color
import com.android.sample.model.booking.name
import com.android.sample.model.listing.ListingType
import java.text.SimpleDateFormat
import java.util.Locale

object BookingDetailsTestTag {
  const val ERROR = "booking_details_error"
  const val HEADER = "booking_header"
  const val CREATOR_SECTION = "booking_creator_section"
  const val CREATOR_NAME = "booking_creator_name"
  const val CREATOR_EMAIL = "booking_creator_email"
  const val MORE_INFO_BUTTON = "booking_creator_more_info_button"
  const val LISTING_SECTION = "booking_listing_section"
  const val SCHEDULE_SECTION = "booking_schedule_section"
  const val DESCRIPTION_SECTION = "booking_description_section"

  const val STATUS = "booking_status"
  const val ROW = "booking_detail_row"
}

/**
 * Main composable function that displays the booking details screen.
 *
 * This function:
 * - Observes the UI state from [BookingDetailsViewModel].
 * - Loads the booking data based on the provided [bookingId].
 * - Displays either a loading/error indicator or the detailed booking content.
 *
 * @param bkgViewModel The [BookingDetailsViewModel] responsible for managing the booking data.
 * @param bookingId The unique identifier of the booking to display.
 * @param onCreatorClick Callback triggered when the user clicks the "More Info" button of the
 *   creator.
 */
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
            CircularProgressIndicator(modifier = Modifier.testTag(BookingDetailsTestTag.ERROR))
          }
    } else {
      BookingDetailsContent(
          uiState = uiState,
          onCreatorClick = { profileId -> onCreatorClick(profileId) },
          modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp))
    }
  }
}

/**
 * Composable function that displays the main content of the booking details screen.
 *
 * It includes:
 * - Header section
 * - Creator information
 * - Course/listing information
 * - Schedule details
 * - Listing description
 *
 * @param uiState The current [BookingUIState] holding booking, listing, and creator data.
 * @param onCreatorClick Callback invoked when the "More Info" button is clicked.
 * @param modifier Optional [Modifier] to apply to the container.
 */
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

/**
 * Composable function that displays the header section of a booking. The skill name is displayed in
 * bold, while the prefix uses a normal font weight.
 *
 * @param uiState The [BookingUIState] containing booking, listing, and creator information.
 */
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
      append(uiState.listing.displayTitle())
    }
  }

  Column(
      horizontalAlignment = Alignment.Start,
      modifier = Modifier.testTag(BookingDetailsTestTag.HEADER)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = styledText,
                  style = baseStyle,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis)
              BookingStatus(uiState.booking.status)
            }

        Spacer(modifier = Modifier.height(4.dp))
      }
}

/**
 * Composable function that displays the creator information section of a booking.
 *
 * The section includes:
 * - A header displaying "Information about the listing creator".
 * - A "More Info" button that triggers [onCreatorClick] with the creator's user ID.
 * - Detail rows for the creator's name and email.
 *
 * @param uiState The [BookingUIState] containing booking, listing, and creator information.
 * @param onCreatorClick Callback invoked when the "More Info" button is clicked; passes the
 *   creator's user ID.
 */
@Composable
private fun InfoCreator(uiState: BookingUIState, onCreatorClick: (String) -> Unit) {
  val creatorRole =
      when (uiState.listing.type) {
        ListingType.REQUEST -> "Student"
        ListingType.PROPOSAL -> "Tutor"
      }

  Column(modifier = Modifier.testTag(BookingDetailsTestTag.CREATOR_SECTION)) {
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
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                      .testTag(BookingDetailsTestTag.MORE_INFO_BUTTON)) {
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
    DetailRow(
        label = "$creatorRole Name",
        value = uiState.creatorProfile.name ?: "Unknown",
        modifier = Modifier.testTag(BookingDetailsTestTag.CREATOR_NAME))
    DetailRow(
        label = "Email",
        value = uiState.creatorProfile.email,
        modifier = Modifier.testTag(BookingDetailsTestTag.CREATOR_EMAIL))
  }
}

/**
 * Composable function that displays the listing/course information section of a booking.
 *
 * The section includes:
 * - A header titled "Information about the course".
 * - A detail row for the subject of the listing.
 * - A detail row for the location of the listing.
 * - A detail row for the hourly rate of the booking.
 *
 * @param uiState The [BookingUIState] containing the booking and listing information.
 */
@Composable
private fun InfoListing(uiState: BookingUIState) {
  Column(modifier = Modifier.testTag(BookingDetailsTestTag.LISTING_SECTION)) {
    Text(
        text = "Information about the course",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold)
    DetailRow(label = "Subject", value = uiState.listing.skill.mainSubject.name.replace("_", " "))
    DetailRow(label = "Location", value = uiState.listing.location.name)
    DetailRow(label = "Hourly Rate", value = uiState.booking.price.toString())
  }
}

/**
 * Composable function that displays the schedule section of a booking.
 *
 * The section includes:
 * - A header titled "Schedule".
 * - A detail row showing the start time of the session.
 * - A detail row showing the end time of the session.
 *
 * Dates are formatted using the pattern "dd/MM/yyyy 'to' HH:mm" based on the default locale.
 *
 * @param uiState The [BookingUIState] containing the booking details, including session start and
 *   end times.
 */
@Composable
private fun InfoSchedule(uiState: BookingUIState) {
  Column(modifier = Modifier.testTag(BookingDetailsTestTag.SCHEDULE_SECTION)) {
    Text(
        text = "Schedule",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold)
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy 'to' HH:mm", Locale.getDefault()) }

    DetailRow(
        label = "Start of the session",
        value = dateFormatter.format(uiState.booking.sessionStart),
    )
    DetailRow(
        label = "End of the session", value = dateFormatter.format(uiState.booking.sessionEnd))
  }
}

/**
 * Composable function that displays the description section of a booking's listing.
 *
 * The section includes:
 * - A header titled "Description of the listing".
 * - The actual description text of the listing from [BookingUIState].
 *
 * @param uiState The [BookingUIState] containing the listing details, including the description.
 */
@Composable
private fun InfoDesc(uiState: BookingUIState) {
  Column(modifier = Modifier.testTag(BookingDetailsTestTag.DESCRIPTION_SECTION)) {
    Text(
        text = "Description of the listing",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold)
    Text(text = uiState.listing.description, style = MaterialTheme.typography.bodyMedium)
  }
}

/**
 * Composable function that displays a single detail row with a label and its corresponding value.
 *
 * The row layout includes:
 * - A label on the left, styled with bodyLarge and a variant surface color.
 * - A value on the right, styled with bodyLarge and semi-bold font weight.
 * - A spacer of 8.dp between the label and value to ensure proper spacing.
 *
 * @param label The text label to display on the left side of the row.
 * @param value The text value to display on the right side of the row.
 * @param modifier Optional [Modifier] for styling or testing, e.g., attaching a test tag.
 */
@Composable
fun DetailRow(label: String, value: String, modifier: Modifier = Modifier) {
  Row(
      modifier = modifier.fillMaxWidth().testTag(BookingDetailsTestTag.ROW),
      horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis)
      }
}

@Composable
private fun BookingStatus(status: BookingStatus) {
  Text(
      text = status.name(),
      color = status.color(),
      fontSize = 8.sp,
      fontWeight = FontWeight.SemiBold,
      modifier =
          Modifier.border(width = 1.dp, color = status.color(), shape = RoundedCornerShape(12.dp))
              .padding(horizontal = 12.dp, vertical = 6.dp)
              .testTag(BookingDetailsTestTag.STATUS))
}
