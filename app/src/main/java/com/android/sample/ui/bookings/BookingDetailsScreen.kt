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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.booking.PaymentStatus
import com.android.sample.model.booking.color
import com.android.sample.model.booking.name
import com.android.sample.model.listing.ListingType
import com.android.sample.ui.bookings.BookingDetailsStrings.BOOKING_REQUEST_FROM
import com.android.sample.ui.bookings.BookingDetailsStrings.TAP_TO_VIEW_STUDENT_PROFILE
import com.android.sample.ui.bookings.BookingDetailsStrings.TAP_TO_VIEW_TUTOR_PROFILE
import com.android.sample.ui.components.RatingStarsInput
import com.android.sample.ui.components.VerticalScrollHint
import com.android.sample.ui.listing.ListingScreenTestTags
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.compareTo

object BookingDetailsTestTag {
  const val ERROR = "booking_details_error"
  const val HEADER = "booking_header"
  const val CREATOR_SECTION = "booking_creator_section"
  const val CREATOR_NAME = "booking_creator_name"
  const val CREATOR_EMAIL = "booking_creator_email"
  const val MORE_INFO_BUTTON = "booking_creator_more_info_button"
  const val BOOKER_SECTION = "booking_booker_section"
  const val BOOKER_NAME_ROW = "booking_booker_name_row"
  const val BOOKER_NAME = "booking_booker_name"
  const val LISTING_SECTION = "booking_listing_section"
  const val SCHEDULE_SECTION = "booking_schedule_section"
  const val DESCRIPTION_SECTION = "booking_description_section"

  const val STATUS = "booking_status"
  const val ROW = "booking_detail_row"
  const val COMPLETE_BUTTON = "booking_complete_button"

  const val RATING_SECTION = "booking_rating_section"

  const val RATING_SUBMIT_BUTTON = "booking_rating_submit"

  const val BOOKER_USER_COMMENT = "booking_booker_user_comment"
  const val BOOKER_LISTING_COMMENT = "booking_booker_listing_comment"
  const val CREATOR_COMMENT = "booking_creator_comment"
  const val PAYMENT_WARNING_DIALOG = "booking_payment_warning_dialog"
  const val PAYMENT_WARNING_CONFIRM = "booking_payment_warning_confirm"
  const val PAYMENT_WARNING_CANCEL = "booking_payment_warning_cancel"
  const val PAYMENT_REQUIRED_MESSAGE = "booking_payment_required_message"
  const val TOTAL_PRICE_SECTION = "booking_total_price_section"
  const val CONTENT = "booking_details_content"
}

object BookingDetailsStrings {
  const val BOOKING_REQUEST_FROM = "Booking Request From:"

  const val TAP_TO_VIEW_STUDENT_PROFILE = "Tap name to view student profile"
  const val TAP_TO_VIEW_TUTOR_PROFILE = "Tap name to view tutor profile"
  const val ACCEPT = "Accept"
  const val DENY = "Deny"
  const val BOOKING_HEADER_TEACHER = "Teacher for : "
  const val BOOKING_HEADER_STUDENT = "Student for : "
  const val CREATOR_STUDENT = "Student"
  const val CREATOR_TUTOR = "Tutor"
  const val INFO_ABOUT = "Information about the"
  const val MORE_INFO = "More Info"
  const val VIEW_PROFILE = "View Profile"
  const val NAME = "Name"
  const val UNKNOWN = "Unknown"
  const val EMAIL = "Email"
  const val STUDENT_PROFILE = "Student profile"
  const val COURSE_INFO = "Information about the course"
  const val DOMAIN = "Domain"
  const val SUBJECT = "Subject"
  const val LOCATION = "Location"
  const val HOURLY_RATE = "Hourly Rate"
  const val SCHEDULE = "Schedule"
  const val START_OF_SESSION = "Start of the session"
  const val END_OF_SESSION = "End of the session"
  const val DATE_FORMAT = "dd/MM/yyyy 'to' HH:mm"
  const val DESCRIPTION_OF_LISTING = "Description of the listing"
  const val HAS_SESSION_TAKEN_PLACE = "Has the session taken place?"
  const val MARK_COMPLETED = "Mark as completed"

  const val LISTING = "Listing"
  const val SUBMIT_RATINGS = "Submit ratings"
  const val PAYMENT_STATUS = "Payment Status:"
  const val NOTIFY_TUTOR =
      "Once you've paid for the session, click the button below to notify the tutor."
  const val PAYMENT_COMPLETE = "Payment Complete"
  const val WAITING_PAYMENT_STUDENT = "Waiting for the student to complete the payment."
  const val STUDENT_PAID =
      "The student has marked the payment as complete. Confirm once you've received it."
  const val PAYMENT_RECEIVED = "Payment Received"
  const val WAITING_PAYMENT_TUTOR = "Waiting for the tutor to confirm receipt of payment."
  const val PAYMENT_CONFIRMED = "Payment has been successfully completed and confirmed!"
  const val TOTAL_PRICE = "Total Price"
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
 * @param onBookerClick Callback triggered when the user clicks the booker's name (student who made
 *   the booking).
 */
@Composable
fun BookingDetailsScreen(
    bkgViewModel: BookingDetailsViewModel = viewModel(),
    bookingId: String,
    onCreatorClick: (String) -> Unit = {},
    onBookerClick: (String) -> Unit = {}
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
          onBookerClick = { profileId -> onBookerClick(profileId) },
          onMarkCompleted = { bkgViewModel.markBookingAsCompleted() },
          onSubmitBookerRatings = { userStars, listingStars, userComment, listingComment ->
            bkgViewModel.submitBookerRatings(userStars, listingStars, userComment, listingComment)
          },
          onSubmitCreatorRating = { stars, comment ->
            bkgViewModel.submitCreatorRating(stars, comment)
          },
          onPaymentComplete = { bkgViewModel.markPaymentComplete() },
          onPaymentReceived = { bkgViewModel.confirmPaymentReceived() },
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
 * @param onBookerClick Callback invoked when the booker's name is clicked.
 * @param modifier Optional [Modifier] to apply to the container.
 */
@Composable
fun BookingDetailsContent(
    uiState: BookingUIState,
    onCreatorClick: (String) -> Unit,
    onBookerClick: (String) -> Unit,
    onMarkCompleted: () -> Unit,
    onSubmitBookerRatings: (Int, Int, String, String) -> Unit,
    onSubmitCreatorRating: (Int, String) -> Unit,
    onPaymentComplete: () -> Unit,
    onPaymentReceived: () -> Unit,
    modifier: Modifier = Modifier
) {

  val scrollState = rememberScrollState()
  val showHint by remember { derivedStateOf { scrollState.value < scrollState.maxValue } }

  Box(modifier = modifier.fillMaxSize()) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .testTag(BookingDetailsTestTag.CONTENT),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {

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

          HorizontalDivider()

          // Total Price
          TotalPriceLabel(uiState)

          HorizontalDivider()

          // Let the student mark the session as completed once it is confirmed
          if (uiState.booking.status == BookingStatus.CONFIRMED) {
            // Determine if current user is the tutor based on listing type
            // PROPOSAL: creator is tutor, REQUEST: booker is tutor
            val isTutor =
                when (uiState.listing.type) {
                  ListingType.PROPOSAL -> uiState.isCreator
                  ListingType.REQUEST -> uiState.isBooker
                }
            ConfirmCompletionSection(
                paymentStatus = uiState.booking.paymentStatus,
                isTutor = isTutor,
                onMarkCompleted = onMarkCompleted)
          }

          if (uiState.booking.status == BookingStatus.COMPLETED) {
            RatingSections(
                uiState = uiState,
                onSubmitBookerRatings = onSubmitBookerRatings,
                onSubmitCreatorRating = onSubmitCreatorRating)
          }

          // Accept/Deny buttons for tutors when a listing is booked
          if (uiState.booking.status == BookingStatus.PENDING && uiState.isCreator) {
            HorizontalDivider()

            // Show booker information
            InfoBooker(uiState = uiState, onBookerClick = onBookerClick)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                  Button(onClick = { uiState.onAcceptBooking() }) {
                    Text(BookingDetailsStrings.ACCEPT)
                  }
                  Button(onClick = { uiState.onDenyBooking() }) { Text(BookingDetailsStrings.DENY) }
                }
          }

          // Payment actions based on the payment status - only for CONFIRMED bookings
          if (uiState.booking.status == BookingStatus.CONFIRMED) {
            // Determine if current user is the tutor based on listing type
            // PROPOSAL: creator is tutor, REQUEST: booker is tutor
            val isTutorForPayment =
                when (uiState.listing.type) {
                  ListingType.PROPOSAL -> uiState.isCreator
                  ListingType.REQUEST -> uiState.isBooker
                }
            PaymentActionSection(
                booking = uiState.booking,
                isCreator = isTutorForPayment,
                onPaymentComplete = onPaymentComplete,
                onPaymentReceived = onPaymentReceived)
          }
        }
    VerticalScrollHint(
        visible = showHint,
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp))
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
        ListingType.REQUEST -> {
          if (uiState.isCreator) {
            BookingDetailsStrings.BOOKING_HEADER_STUDENT
          } else {
            BookingDetailsStrings.BOOKING_HEADER_TEACHER
          }
        }
        ListingType.PROPOSAL -> {
          if (uiState.isCreator) {
            BookingDetailsStrings.BOOKING_HEADER_TEACHER
          } else {
            BookingDetailsStrings.BOOKING_HEADER_STUDENT
          }
        }
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
        ListingType.REQUEST -> BookingDetailsStrings.CREATOR_STUDENT
        ListingType.PROPOSAL -> BookingDetailsStrings.CREATOR_TUTOR
      }

  Column(modifier = Modifier.testTag(BookingDetailsTestTag.CREATOR_SECTION)) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
          Text(
              text = "${BookingDetailsStrings.INFO_ABOUT} $creatorRole",
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
                    text = BookingDetailsStrings.MORE_INFO,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = BookingDetailsStrings.VIEW_PROFILE,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp).size(18.dp))
              }
        }
    DetailRow(
        label = "$creatorRole ${BookingDetailsStrings.NAME}",
        value = uiState.creatorProfile.name ?: BookingDetailsStrings.UNKNOWN,
        modifier = Modifier.testTag(BookingDetailsTestTag.CREATOR_NAME))
    DetailRow(
        label = BookingDetailsStrings.EMAIL,
        value = uiState.creatorProfile.email,
        modifier = Modifier.testTag(BookingDetailsTestTag.CREATOR_EMAIL))
  }
}

/**
 * Composable function that displays the booker (student) information section.
 *
 * This section is shown to tutors when they need to review a booking request. It includes:
 * - The booker's name (clickable to view their profile)
 * - A helper text indicating the name is clickable
 *
 * @param uiState The [BookingUIState] containing booking and booker profile information.
 * @param onBookerClick Callback invoked when the booker's name is clicked; passes the booker's user
 *   ID.
 */
@Composable
private fun InfoBooker(uiState: BookingUIState, onBookerClick: (String) -> Unit) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 8.dp)
              .testTag(BookingDetailsTestTag.BOOKER_SECTION),
      verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = BOOKING_REQUEST_FROM,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier.fillMaxWidth()
                    .clickable { onBookerClick(uiState.booking.bookerId) }
                    .padding(vertical = 8.dp)
                    .testTag(BookingDetailsTestTag.BOOKER_NAME_ROW)) {
              Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = BookingDetailsStrings.STUDENT_PROFILE,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                  text = uiState.bookerProfile.name ?: BookingDetailsStrings.UNKNOWN,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.testTag(BookingDetailsTestTag.BOOKER_NAME))
            }

        val text =
            when (uiState.listing.type) {
              ListingType.REQUEST -> TAP_TO_VIEW_TUTOR_PROFILE
              ListingType.PROPOSAL -> TAP_TO_VIEW_STUDENT_PROFILE
            }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        text = BookingDetailsStrings.COURSE_INFO,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold)
    DetailRow(
        label = BookingDetailsStrings.DOMAIN,
        value = uiState.listing.skill.mainSubject.name.replace("_", " "))
    DetailRow(
        label = BookingDetailsStrings.SUBJECT,
        value = uiState.listing.skill.skill.replace("_", " "))
    DetailRow(label = BookingDetailsStrings.LOCATION, value = uiState.listing.location.name)
    DetailRow(label = BookingDetailsStrings.HOURLY_RATE, value = "$${uiState.hourlyRate}")
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
        text = BookingDetailsStrings.SCHEDULE,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold)
    val dateFormatter = remember {
      SimpleDateFormat(BookingDetailsStrings.DATE_FORMAT, Locale.getDefault())
    }

    DetailRow(
        label = BookingDetailsStrings.START_OF_SESSION,
        value = dateFormatter.format(uiState.booking.sessionStart))
    DetailRow(
        label = BookingDetailsStrings.END_OF_SESSION,
        value = dateFormatter.format(uiState.booking.sessionEnd))
  }
}

@Composable
private fun TotalPriceLabel(uiState: BookingUIState) {
  Row(modifier = Modifier.testTag(BookingDetailsTestTag.TOTAL_PRICE_SECTION)) {
    DetailRow(label = BookingDetailsStrings.TOTAL_PRICE, value = "$${uiState.booking.price}")
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
        text = BookingDetailsStrings.DESCRIPTION_OF_LISTING,
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

/**
 * UI section allowing a user to confirm that a booked learning session has been completed.
 *
 * This component displays a prompt text and a button. When the user taps the **"Mark as
 * completed"** button, the `onMarkCompleted` callback is invoked.
 *
 * If the payment has not been confirmed yet and the current user is the one who should receive the
 * payment (tutor for PROPOSAL, booker/tutor for REQUEST), a warning dialog is shown asking if they
 * want to proceed without payment confirmation.
 *
 * It is typically shown when a booking has the status `CONFIRMED` and the user can now validate
 * that the session actually took place.
 *
 * @param paymentStatus The current payment status of the booking.
 * @param isTutor Whether the current user is the listing creator.
 * @param onMarkCompleted Callback triggered when the user confirms marking the booking as
 *   completed.
 */
@Composable
private fun ConfirmCompletionSection(
    paymentStatus: PaymentStatus,
    isTutor: Boolean,
    onMarkCompleted: () -> Unit
) {
  var showWarningDialog by remember { mutableStateOf(false) }

  // Determine if the current user is the one who receives payment (the tutor)
  // isTutor is already computed correctly based on listing type:
  // - For PROPOSAL: isTutor=true if user is creator (tutor)
  // - For REQUEST: isTutor=true if user is booker (tutor)
  // The tutor is always the payment receiver
  val isPaymentReceiver = isTutor

  // The payer (student) is the opposite of the payment receiver
  val isPayer = !isPaymentReceiver

  // Check if payment is not yet confirmed
  val paymentNotConfirmed = paymentStatus != PaymentStatus.CONFIRMED

  // Payer cannot complete until payment is confirmed
  val isButtonDisabledForPayer = isPayer && paymentNotConfirmed

  // Show warning dialog if needed
  if (showWarningDialog) {
    AlertDialog(
        onDismissRequest = { showWarningDialog = false },
        title = { Text("Payment Not Confirmed") },
        text = {
          Text(
              "The payment has not been confirmed yet. Are you sure you want to mark this " +
                  "booking as completed before the payment has been accepted?")
        },
        confirmButton = {
          Button(
              onClick = {
                showWarningDialog = false
                onMarkCompleted()
              },
              modifier = Modifier.testTag(BookingDetailsTestTag.PAYMENT_WARNING_CONFIRM)) {
                Text("Yes, Complete Anyway")
              }
        },
        dismissButton = {
          Button(
              onClick = { showWarningDialog = false },
              modifier = Modifier.testTag(BookingDetailsTestTag.PAYMENT_WARNING_CANCEL)) {
                Text("Cancel")
              }
        },
        modifier = Modifier.testTag(BookingDetailsTestTag.PAYMENT_WARNING_DIALOG))
  }

  Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = BookingDetailsStrings.HAS_SESSION_TAKEN_PLACE,
            style = MaterialTheme.typography.bodyMedium,
        )

        // Show message to payer if they can't complete yet
        if (isButtonDisabledForPayer) {
          Text(
              text =
                  "You cannot mark the booking as completed until the payment has been confirmed.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error,
              modifier = Modifier.testTag(BookingDetailsTestTag.PAYMENT_REQUIRED_MESSAGE))
        }

        Button(
            onClick = {
              // Show warning if payment not confirmed and user is the payment receiver
              if (paymentNotConfirmed && isPaymentReceiver) {
                showWarningDialog = true
              } else {
                onMarkCompleted()
              }
            },
            enabled = !isButtonDisabledForPayer,
            modifier = Modifier.testTag(BookingDetailsTestTag.COMPLETE_BUTTON)) {
              Text(text = BookingDetailsStrings.MARK_COMPLETED)
            }
      }
}

@Composable
private fun RatingSections(
    uiState: BookingUIState,
    onSubmitBookerRatings: (Int, Int, String, String) -> Unit,
    onSubmitCreatorRating: (Int, String) -> Unit
) {
  val listingType = uiState.listing.type
  val progress = uiState.ratingProgress

  // ----- Booker section -----
  if (uiState.isBooker) {
    val (userLabel, alreadySubmitted) =
        when (listingType) {
          ListingType.REQUEST ->
              "Student" to (progress.bookerRatedStudent && progress.bookerRatedListing)
          ListingType.PROPOSAL ->
              "Tutor" to (progress.bookerRatedTutor && progress.bookerRatedListing)
        }

    if (!alreadySubmitted) {
      BookerRatingSection(userLabel = userLabel, onSubmit = onSubmitBookerRatings)
    }
  }

  // ----- Creator section -----
  if (uiState.isCreator) {
    val (userLabel, alreadySubmitted) =
        when (listingType) {
          ListingType.REQUEST -> "Tutor" to progress.creatorRatedTutor
          ListingType.PROPOSAL -> "Student" to progress.creatorRatedStudent
        }

    if (!alreadySubmitted) {
      CreatorRatingSection(userLabel = userLabel, onSubmit = onSubmitCreatorRating)
    }
  }
}

@Composable
private fun BookerRatingSection(userLabel: String, onSubmit: (Int, Int, String, String) -> Unit) {
  var userStars by remember { mutableIntStateOf(0) }
  var listingStars by remember { mutableIntStateOf(0) }
  var userComment by remember { mutableStateOf("") }
  var listingComment by remember { mutableStateOf("") }

  val enabled = userStars in 1..5 && listingStars in 1..5

  Column(
      modifier = Modifier.fillMaxWidth().testTag(BookingDetailsTestTag.RATING_SECTION),
      verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Your ratings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold)

        RatingRow(label = userLabel, selected = userStars, onSelected = { userStars = it })
        // In BookerRatingSection - user comment
        OutlinedTextField(
            value = userComment,
            onValueChange = { if (it.length <= 500) userComment = it },
            label = { Text("Comment about the $userLabel (optional)") },
            supportingText = { Text("${userComment.length}/500") },
            modifier = Modifier.fillMaxWidth().testTag(BookingDetailsTestTag.BOOKER_USER_COMMENT),
            singleLine = false,
            maxLines = 5)

        RatingRow(label = "Listing", selected = listingStars, onSelected = { listingStars = it })
        // In BookerRatingSection - listing comment
        OutlinedTextField(
            value = listingComment,
            onValueChange = { if (it.length <= 500) listingComment = it },
            label = { Text("Comment about the listing (optional)") },
            supportingText = { Text("${listingComment.length}/500") },
            modifier =
                Modifier.fillMaxWidth().testTag(BookingDetailsTestTag.BOOKER_LISTING_COMMENT),
            singleLine = false,
            maxLines = 5)

        if (!enabled) {
          Text(
              text = "Please select ratings for both categories",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error,
              modifier = Modifier.padding(top = 4.dp))
        }

        Button(
            enabled = enabled,
            onClick = { onSubmit(userStars, listingStars, userComment, listingComment) },
            modifier = Modifier.testTag(BookingDetailsTestTag.RATING_SUBMIT_BUTTON)) {
              Text("Submit")
            }
      }
}

@Composable
private fun CreatorRatingSection(userLabel: String, onSubmit: (Int, String) -> Unit) {
  var stars by remember { mutableIntStateOf(0) }
  val enabled = stars in 1..5
  var comment by remember { mutableStateOf("") }

  // Add test tag so tests can find/scroll the creator rating section
  Column(
      modifier = Modifier.fillMaxWidth().testTag(BookingDetailsTestTag.RATING_SECTION),
      verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Your rating",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold)

        RatingRow(label = userLabel, selected = stars, onSelected = { stars = it })
        // In CreatorRatingSection - creator comment
        OutlinedTextField(
            value = comment,
            onValueChange = { if (it.length <= 500) comment = it },
            label = { Text("Comment (optional)") },
            supportingText = { Text("${comment.length}/500") },
            modifier = Modifier.fillMaxWidth().testTag(BookingDetailsTestTag.CREATOR_COMMENT),
            singleLine = false,
            maxLines = 5)

        // Add test tag to the submit button so tests can scroll to/click it reliably
        Button(
            enabled = enabled,
            onClick = { onSubmit(stars, comment) },
            modifier = Modifier.testTag(BookingDetailsTestTag.RATING_SUBMIT_BUTTON)) {
              Text("Submit")
            }
      }
}

/**
 * A reusable UI component that displays a rating input row consisting of:
 * - A label (e.g., "Tutor", "Listing")
 * - A star-based rating selector using [RatingStarsInput]
 *
 * This composable eliminates duplicated logic between the tutor and listing rating UI.
 *
 * @param label The descriptive label shown above the star rating (e.g., "Tutor").
 * @param selected The currently selected star value (0–5).
 * @param onSelected Callback invoked when the user selects a different number of stars.
 * @param modifier Optional [Modifier] applied to the container.
 */
@Composable
private fun RatingRow(
    label: String,
    selected: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier) {
    Text(text = label, style = MaterialTheme.typography.bodyMedium)
    RatingStarsInput(selectedStars = selected, onSelected = onSelected)
  }
}

/**
 * Composable function that displays payment action buttons based on the payment status of the
 * booking.
 *
 * The payment flow depends on the listing type:
 * - PROPOSAL: The booker is the student who needs to pay, the listing creator is the tutor who
 *   receives
 * - REQUEST: The listing creator is the student who needs to pay, the booker is the tutor who
 *   receives
 *
 * @param booking The booking object containing payment status information.
 * @param isTutor Whether the current user is the tutor (listing creator).
 * @param listingType The type of listing (PROPOSAL or REQUEST) to determine payment roles.
 * @param onPaymentComplete Callback invoked when the "Payment Complete" button is clicked.
 * @param onPaymentReceived Callback invoked when the "Payment Received" button is clicked.
 */
@Composable
private fun PaymentActionSection(
    booking: Booking,
    isCreator: Boolean,
    onPaymentComplete: () -> Unit,
    onPaymentReceived: () -> Unit
) {
  // Determine if the current user is the one who should pay (the student)
  // - For PROPOSAL: isTutor=false means booker (student), who pays
  // - For REQUEST: isTutor=false means creator (student), who pays
  // In both cases, the student (non-tutor) is the one who pays
  val isStudentPaying = !isCreator

  // Always display the current payment status
  Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        // Display current payment status
        Text(
            text = "${BookingDetailsStrings.PAYMENT_STATUS} ${booking.paymentStatus.name()}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)

        // Show appropriate action based on payment status and user role
        when (booking.paymentStatus) {
          PaymentStatus.PENDING_PAYMENT -> {
            // Student (payer) sees the payment complete button
            if (isStudentPaying) {
              Text(
                  text = BookingDetailsStrings.NOTIFY_TUTOR,
                  style = MaterialTheme.typography.bodyMedium,
              )
              Button(
                  onClick = onPaymentComplete,
                  modifier = Modifier.testTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON)) {
                    Text(BookingDetailsStrings.PAYMENT_COMPLETE)
                  }
            } else {
              // Tutor sees waiting message
              Text(
                  text = BookingDetailsStrings.WAITING_PAYMENT_STUDENT,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          PaymentStatus.PAID -> {
            // Tutor (payment receiver) sees the payment received button
            if (!isStudentPaying) {
              Text(
                  text = BookingDetailsStrings.STUDENT_PAID,
                  style = MaterialTheme.typography.bodyMedium,
              )
              Button(
                  onClick = onPaymentReceived,
                  modifier = Modifier.testTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON)) {
                    Text(BookingDetailsStrings.PAYMENT_RECEIVED)
                  }
            } else {
              // Student sees waiting message
              Text(
                  text = BookingDetailsStrings.WAITING_PAYMENT_TUTOR,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          PaymentStatus.CONFIRMED -> {
            // Both users see confirmation message
            Text(
                text = BookingDetailsStrings.PAYMENT_CONFIRMED,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.SemiBold)
          }
        }
      }
}
