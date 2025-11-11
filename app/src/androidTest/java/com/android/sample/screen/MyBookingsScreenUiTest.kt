package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import com.android.sample.model.booking.*
import com.android.sample.model.listing.*
import com.android.sample.model.map.Location
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.bookings.MyBookingsScreen
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.components.BookingCardTestTag
import com.android.sample.ui.theme.SampleAppTheme
import java.util.*
import org.junit.Rule
import org.junit.Test

class MyBookingsScreenUiTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  /** ViewModel standard avec données valides */
  private fun demoViewModel(): MyBookingsViewModel =
      MyBookingsViewModel(
          bookingRepo =
              object : BookingRepository {
                override fun getNewUid() = "demoB"

                override suspend fun getBookingsByUserId(userId: String): List<Booking> =
                    listOf(
                        Booking(
                            bookingId = "b1",
                            associatedListingId = "L1",
                            listingCreatorId = "t1",
                            bookerId = userId,
                            sessionStart = Date(),
                            sessionEnd = Date(System.currentTimeMillis() + 60 * 60 * 1000),
                            price = 30.0),
                        Booking(
                            bookingId = "b2",
                            associatedListingId = "L2",
                            listingCreatorId = "t2",
                            bookerId = userId,
                            sessionStart = Date(),
                            sessionEnd = Date(System.currentTimeMillis() + 90 * 60 * 1000),
                            price = 25.0))

                // les autres fonctions non utilisées dans les tests
                override suspend fun getAllBookings() = emptyList<Booking>()

                override suspend fun getBooking(bookingId: String) = error("unused")

                override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

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
          listingRepo =
              object : ListingRepository {
                override fun getNewUid() = "demoL"

                override suspend fun getListing(listingId: String): Listing =
                    Proposal(
                        listingId = listingId,
                        creatorUserId = if (listingId == "L1") "t1" else "t2",
                        description = "Demo Listing $listingId",
                        location = Location(),
                        hourlyRate = if (listingId == "L1") 30.0 else 25.0)

                override suspend fun getAllListings() = emptyList<Listing>()

                override suspend fun getProposals() = emptyList<Proposal>()

                override suspend fun getRequests() = emptyList<Request>()

                override suspend fun getListingsByUser(userId: String) = emptyList<Listing>()

                override suspend fun addProposal(proposal: Proposal) {}

                override suspend fun addRequest(request: Request) {}

                override suspend fun updateListing(listingId: String, listing: Listing) {}

                override suspend fun deleteListing(listingId: String) {}

                override suspend fun deactivateListing(listingId: String) {}

                override suspend fun searchBySkill(skill: com.android.sample.model.skill.Skill) =
                    emptyList<Listing>()

                override suspend fun searchByLocation(location: Location, radiusKm: Double) =
                    emptyList<Listing>()
              },
          profileRepo =
              object : ProfileRepository {
                override fun getNewUid() = "demoP"

                override suspend fun getProfile(userId: String): Profile =
                    when (userId) {
                      "t1" ->
                          Profile(userId = "t1", name = "Alice Martin", email = "alice@test.com")
                      "t2" ->
                          Profile(userId = "t2", name = "Lucas Dupont", email = "lucas@test.com")
                      else -> Profile(userId = userId, name = "Unknown", email = "unknown@test.com")
                    }

                override suspend fun getProfileById(userId: String) = getProfile(userId)

                override suspend fun addProfile(profile: Profile) {}

                override suspend fun updateProfile(userId: String, profile: Profile) {}

                override suspend fun deleteProfile(userId: String) {}

                override suspend fun getAllProfiles() = emptyList<Profile>()

                override suspend fun searchProfilesByLocation(
                    location: Location,
                    radiusKm: Double
                ) = emptyList<Profile>()

                override suspend fun getSkillsForUser(userId: String) =
                    emptyList<com.android.sample.model.skill.Skill>()
              })

  @Test
  fun demo_shows_two_booking_cards() {
    val vm = demoViewModel()
    composeRule.setContent {
      SampleAppTheme {
        val nav = rememberNavController()
        MyBookingsScreen(viewModel = vm, onBookingClick = {})
      }
    }

    composeRule.waitUntil(2_000) {
      composeRule
          .onAllNodesWithTag(BookingCardTestTag.CARD, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .size == 2
    }
  }

  @Test
  fun error_state_displays_message() {
    val listingRepo =
        object : ListingRepository {
          override fun getNewUid() = "demoL"

          override suspend fun getListing(listingId: String): Listing =
              Proposal(
                  listingId = listingId,
                  creatorUserId = if (listingId == "L1") "t1" else "t2",
                  description = "Demo Listing $listingId",
                  location = Location(),
                  hourlyRate = if (listingId == "L1") 30.0 else 25.0)

          override suspend fun getAllListings() = emptyList<Listing>()

          override suspend fun getProposals() = emptyList<Proposal>()

          override suspend fun getRequests() = emptyList<Request>()

          override suspend fun getListingsByUser(userId: String) = emptyList<Listing>()

          override suspend fun addProposal(proposal: Proposal) {}

          override suspend fun addRequest(request: Request) {}

          override suspend fun updateListing(listingId: String, listing: Listing) {}

          override suspend fun deleteListing(listingId: String) {}

          override suspend fun deactivateListing(listingId: String) {}

          override suspend fun searchBySkill(skill: com.android.sample.model.skill.Skill) =
              emptyList<Listing>()

          override suspend fun searchByLocation(location: Location, radiusKm: Double) =
              emptyList<Listing>()
        }

    val profileRepo =
        object : ProfileRepository {
          override fun getNewUid() = "demoP"

          override suspend fun getProfile(userId: String): Profile =
              when (userId) {
                "t1" -> Profile("t1", "Alice Martin", "alice@test.com")
                "t2" -> Profile("t2", "Lucas Dupont", "lucas@test.com")
                else -> Profile(userId, "Unknown", "unknown@test.com")
              }

          override suspend fun getProfileById(userId: String) = getProfile(userId)

          override suspend fun addProfile(profile: Profile) {}

          override suspend fun updateProfile(userId: String, profile: Profile) {}

          override suspend fun deleteProfile(userId: String) {}

          override suspend fun getAllProfiles() = emptyList<Profile>()

          override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
              emptyList<Profile>()

          override suspend fun getSkillsForUser(userId: String) =
              emptyList<com.android.sample.model.skill.Skill>()
        }

    val vm =
        MyBookingsViewModel(
            bookingRepo =
                object : BookingRepository {
                  override fun getNewUid() = "demoError"

                  override suspend fun getBookingsByUserId(userId: String): List<Booking> {
                    throw RuntimeException("Simulated failure")
                  }

                  override suspend fun getAllBookings() = emptyList<Booking>()

                  override suspend fun getBooking(bookingId: String) = error("unused")

                  override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

                  override suspend fun getBookingsByStudent(studentId: String) =
                      emptyList<Booking>()

                  override suspend fun getBookingsByListing(listingId: String) =
                      emptyList<Booking>()

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
            listingRepo = listingRepo,
            profileRepo = profileRepo)

    composeRule.setContent {
      SampleAppTheme { MyBookingsScreen(viewModel = vm, onBookingClick = {}) }
    }

    composeRule.waitUntil(2_000) {
      composeRule
          .onAllNodesWithTag(MyBookingsPageTestTag.ERROR, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }
}
