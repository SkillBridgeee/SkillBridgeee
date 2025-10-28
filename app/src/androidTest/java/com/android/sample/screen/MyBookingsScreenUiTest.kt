package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.rating.RatingType
import com.android.sample.model.rating.StarRating
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.bookings.MyBookingsScreen
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.theme.SampleAppTheme
import java.util.*
import org.junit.Rule
import org.junit.Test

class MyBookingsScreenUiTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  /** VM wired to use demo=true so the screen shows 2 cards deterministically. */
  private fun vmWithDemo(): MyBookingsViewModel =
      MyBookingsViewModel(
          // 2 deterministic bookings (L1/t1 = 1h @ $30; L2/t2 = 1h30 @ $25)
          bookingRepo =
              object : BookingRepository {
                override fun getNewUid() = "X"

                override suspend fun getAllBookings() = emptyList<Booking>()

                override suspend fun getBooking(bookingId: String) = error("not used")

                override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

                override suspend fun getBookingsByUserId(userId: String) =
                    listOf(
                        Booking(
                            bookingId = "b-1",
                            associatedListingId = "L1",
                            listingCreatorId = "t1",
                            bookerId = userId,
                            sessionStart = Date(),
                            sessionEnd = Date(System.currentTimeMillis() + 60 * 60 * 1000), // 1h
                            price = 30.0),
                        Booking(
                            bookingId = "b-2",
                            associatedListingId = "L2",
                            listingCreatorId = "t2",
                            bookerId = userId,
                            sessionStart = Date(),
                            sessionEnd = Date(System.currentTimeMillis() + 90 * 60 * 1000), // 1h30
                            price = 25.0))

                override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

                override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

                override suspend fun addBooking(booking: Booking) {}

                override suspend fun updateBooking(bookingId: String, booking: Booking) {}

                override suspend fun deleteBooking(bookingId: String) {}

                override suspend fun updateBookingStatus(
                    bookingId: String,
                    status: BookingStatus
                ) {}

                override suspend fun confirmBooking(bookingId: String) {}

                override suspend fun completeBooking(bookingId: String) {}

                override suspend fun cancelBooking(bookingId: String) {}
              },
          userId = "s1",

          // Listing echoes the requested id and maps creator to t1/t2
          listingRepo =
              object : ListingRepository {
                override fun getNewUid() = "L"

                override suspend fun getAllListings() = emptyList<Listing>()

                override suspend fun getProposals() = emptyList<Proposal>()

                override suspend fun getRequests() =
                    emptyList<com.android.sample.model.listing.Request>()

                override suspend fun getListing(listingId: String): Listing =
                    Proposal(
                        listingId = listingId,
                        creatorUserId =
                            when (listingId) {
                              "L1" -> "t1"
                              "L2" -> "t2"
                              else -> "t1"
                            },
                        // Let defaults for Skill() be used to keep subject stable
                        description = "demo $listingId",
                        location = Location(),
                        hourlyRate = if (listingId == "L1") 30.0 else 25.0)

                override suspend fun getListingsByUser(userId: String) = emptyList<Listing>()

                override suspend fun addProposal(proposal: Proposal) {}

                override suspend fun addRequest(
                    request: com.android.sample.model.listing.Request
                ) {}

                override suspend fun updateListing(listingId: String, listing: Listing) {}

                override suspend fun deleteListing(listingId: String) {}

                override suspend fun deactivateListing(listingId: String) {}

                override suspend fun searchBySkill(skill: com.android.sample.model.skill.Skill) =
                    emptyList<Listing>()

                override suspend fun searchByLocation(location: Location, radiusKm: Double) =
                    emptyList<Listing>()
              },

          // Profiles for both tutors
          profileRepo =
              object : ProfileRepository {
                override fun getNewUid() = "P"

                override suspend fun getProfile(userId: String) =
                    when (userId) {
                      "t1" -> Profile(userId = "t1", name = "Alice Martin", email = "a@a.com")
                      "t2" -> Profile(userId = "t2", name = "Lucas Dupont", email = "l@a.com")
                      else -> Profile(userId = userId, name = "Unknown", email = "u@a.com")
                    }

                override suspend fun addProfile(profile: Profile) {}

                override suspend fun updateProfile(userId: String, profile: Profile) {}

                override suspend fun deleteProfile(userId: String) {}

                override suspend fun getAllProfiles() = emptyList<Profile>()

                override suspend fun searchProfilesByLocation(
                    location: Location,
                    radiusKm: Double
                ) = emptyList<Profile>()

                override suspend fun getProfileById(userId: String) = getProfile(userId)

                override suspend fun getSkillsForUser(userId: String) =
                    emptyList<com.android.sample.model.skill.Skill>()
              },

          // Ratings: L1 averages to 5★, L2 to 4★
          ratingRepo =
              object : RatingRepository {
                override fun getNewUid() = "R"

                override suspend fun getAllRatings() = emptyList<Rating>()

                override suspend fun getRating(ratingId: String) = error("not used")

                override suspend fun getRatingsByFromUser(fromUserId: String) = emptyList<Rating>()

                override suspend fun getRatingsByToUser(toUserId: String) = emptyList<Rating>()

                override suspend fun getRatingsOfListing(listingId: String): List<Rating> =
                    when (listingId) {
                      "L1" ->
                          listOf(
                              Rating("r1", "s1", "t1", StarRating.FIVE, "", RatingType.TUTOR),
                              Rating("r2", "s2", "t1", StarRating.FIVE, "", RatingType.TUTOR),
                              Rating("r3", "s3", "t1", StarRating.FIVE, "", RatingType.TUTOR))
                      "L2" ->
                          listOf(
                              Rating("r4", "s4", "t2", StarRating.FOUR, "", RatingType.TUTOR),
                              Rating("r5", "s5", "t2", StarRating.FOUR, "", RatingType.TUTOR))
                      else -> emptyList()
                    }

                override suspend fun addRating(rating: Rating) {}

                override suspend fun updateRating(ratingId: String, rating: Rating) {}

                override suspend fun deleteRating(ratingId: String) {}

                override suspend fun getTutorRatingsOfUser(userId: String) = emptyList<Rating>()

                override suspend fun getStudentRatingsOfUser(userId: String) = emptyList<Rating>()
              },
          locale = Locale.US)

  @Test
  fun full_screen_demo_renders_two_cards() {
    val vm = vmWithDemo()
    composeRule.setContent {
      SampleAppTheme {
        val nav = rememberNavController()
        MyBookingsScreen(viewModel = vm, navController = nav)
      }
    }
    // wait for composition to settle enough to find nodes
    composeRule.waitUntil(5_000) {
      composeRule
          .onAllNodesWithTag(MyBookingsPageTestTag.BOOKING_CARD)
          .fetchSemanticsNodes()
          .size == 2
    }
    composeRule.onAllNodesWithTag(MyBookingsPageTestTag.BOOKING_CARD).assertCountEquals(2)
    composeRule.onAllNodesWithTag(MyBookingsPageTestTag.BOOKING_DETAILS_BUTTON).assertCountEquals(2)
  }

  @Test
  fun bookings_list_empty_renders_zero_cards() {
    // Render BookingsList directly with an empty list
    composeRule.setContent {
      SampleAppTheme {
        val nav = rememberNavController()
        com.android.sample.ui.bookings.BookingsList(bookings = emptyList(), navController = nav)
      }
    }
    composeRule.onAllNodesWithTag(MyBookingsPageTestTag.BOOKING_CARD).assertCountEquals(0)
  }

  @Test
  fun rating_rows_visible_from_demo_cards() {
    val vm = vmWithDemo()
    composeRule.setContent {
      SampleAppTheme {
        val nav = rememberNavController()
        MyBookingsScreen(viewModel = vm, navController = nav)
      }
    }
    // First demo card is 5★; second demo card is 4★ in your VM demo content.
    composeRule.onNodeWithText("★★★★★").assertIsDisplayed()
    composeRule.onNodeWithText("★★★★☆").assertIsDisplayed()
  }

  @Test
  fun price_duration_line_uses_space_dash_space_format() {
    val vm = vmWithDemo()
    composeRule.setContent {
      SampleAppTheme {
        val nav = rememberNavController()
        MyBookingsScreen(viewModel = vm, navController = nav)
      }
    }

    // From demo card 1: "$30.0/hr - 1hr"
    composeRule.onNodeWithText("$30.0/hr - 1hr").assertIsDisplayed()
  }

  @Test
  fun empty_state_shows_message_and_tag() {
    composeRule.setContent {
      SampleAppTheme {
        val nav = rememberNavController()
        // Render the list with no items to trigger the empty state
        com.android.sample.ui.bookings.BookingsList(bookings = emptyList(), navController = nav)
      }
    }

    // No cards are rendered
    composeRule.onAllNodesWithTag(MyBookingsPageTestTag.BOOKING_CARD).assertCountEquals(0)

    // The empty-state container is visible
    composeRule.onNodeWithTag(MyBookingsPageTestTag.EMPTY_BOOKINGS).assertIsDisplayed()

    // The helper text is visible
    composeRule.onNodeWithText("No bookings available").assertIsDisplayed()
  }
}
