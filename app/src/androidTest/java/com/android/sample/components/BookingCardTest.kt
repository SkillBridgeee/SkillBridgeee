package com.android.sample.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingType
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.ui.components.BookingCard
import com.android.sample.ui.components.BookingCardTestTag
import java.util.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookingCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  // --- MOCKS ------------------------------------------------------

  private fun mockBooking(
      id: String = "booking123",
      status: BookingStatus = BookingStatus.CONFIRMED,
      start: Date = Date(),
      end: Date = Date(),
      price: Double = 25.0
  ): Booking =
      Booking(
          bookingId = id,
          associatedListingId = "listing123",
          listingCreatorId = "creator123",
          bookerId = "booker123",
          sessionStart = start,
          sessionEnd = end,
          status = status,
          price = price)

  private fun mockListing(
      title: String = "Math Tutoring",
      type: ListingType = ListingType.REQUEST,
      rate: Double = 25.0
  ): Listing =
      when (type) {
        ListingType.REQUEST ->
            Request(
                listingId = "listing123",
                creatorUserId = "creator123",
                title = title,
                skill = Skill(skill = "Math"),
                description = "Looking for a math tutor",
                hourlyRate = rate,
                isActive = true)
        ListingType.PROPOSAL ->
            Proposal(
                listingId = "listing123",
                creatorUserId = "creator123",
                title = title,
                skill = Skill(skill = "Math"),
                description = "Offering math tutoring",
                hourlyRate = rate,
                isActive = true)
      }

  private fun mockProfile(name: String = "Alice Tutor") =
      Profile(userId = "creator123", name = name)

  // --- TESTS ------------------------------------------------------

  @Test
  fun bookingCard_displaysTutorTitle_whenListingTypeIsRequest() {
    val booking = mockBooking()
    val listing = mockListing(type = ListingType.REQUEST)
    val profile = mockProfile()

    composeTestRule.setContent {
      BookingCard(booking = booking, listing = listing, creator = profile)
    }

    composeTestRule
        .onNodeWithTag(testTag = BookingCardTestTag.LISTING_TITLE, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Tutor for Math Tutoring").assertIsDisplayed()
  }

  @Test
  fun bookingCard_displaysStudentTitle_whenListingTypeIsProposal() {
    val booking = mockBooking()
    val listing = mockListing(type = ListingType.PROPOSAL)
    val profile = mockProfile()

    composeTestRule.setContent {
      BookingCard(booking = booking, listing = listing, creator = profile)
    }

    composeTestRule.onNodeWithText("Student for Math Tutoring").assertIsDisplayed()
  }

  @Test
  fun bookingCard_displaysCreatorName() {
    val booking = mockBooking()
    val listing = mockListing()
    val profile = mockProfile(name = "Bob Teacher")

    composeTestRule.setContent {
      BookingCard(booking = booking, listing = listing, creator = profile)
    }

    composeTestRule.onNodeWithText("by Bob Teacher").assertIsDisplayed()
  }

  @Test
  fun bookingCard_displaysPriceAndDate() {
    val booking = mockBooking(price = 40.0)
    val listing = mockListing(rate = 40.0)
    val profile = mockProfile()

    composeTestRule.setContent {
      BookingCard(booking = booking, listing = listing, creator = profile)
    }

    composeTestRule
        .onNodeWithTag(testTag = BookingCardTestTag.PRICE, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("$40.00 / hr").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(testTag = BookingCardTestTag.DATE, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun bookingCard_clickTriggersCallback() {
    val booking = mockBooking()
    val listing = mockListing()
    val profile = mockProfile()
    var clickedId: String? = null

    composeTestRule.setContent {
      BookingCard(
          booking = booking,
          listing = listing,
          creator = profile,
          onClickBookingCard = { clickedId = it })
    }

    composeTestRule.onNodeWithTag(BookingCardTestTag.CARD).performClick()

    assert(clickedId == booking.bookingId)
  }
}
