package com.android.sample.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingType
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import java.util.Date
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ListingCardTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private fun fakeTutor(
      name: String = "Alice Johnson",
      locationName: String = "Campus East",
      avgRating: Double = 4.5,
      totalRatings: Int = 32,
      userId: String = "tutor-42"
  ): Profile {
    return Profile(
        userId = userId,
        name = name,
        email = "alice@example.com",
        levelOfEducation = "BSc Music",
        location = Location(name = locationName),
        hourlyRate = "25",
        description = "Piano teacher, 6+ yrs experience",
        tutorRating = RatingInfo(averageRating = avgRating, totalRatings = totalRatings),
        studentRating = RatingInfo())
  }

  private fun fakeListing(
      listingId: String = "listing-123",
      creatorUserId: String = "tutor-42",
      description: String = "Beginner piano coaching",
      hourlyRate: Double = 25.0,
      locationName: String = "Campus East",
      skill: Skill =
          Skill(
              mainSubject = MainSubject.MUSIC,
              skill = "PIANO",
              skillTime = 6.0,
              expertise = ExpertiseLevel.ADVANCED)
  ): Listing {
    return Proposal(
        listingId = listingId,
        creatorUserId = creatorUserId,
        skill = skill,
        description = description,
        location = Location(name = locationName),
        createdAt = Date(),
        active = true,
        hourlyRate = hourlyRate,
        type = ListingType.PROPOSAL)
  }

  @Test
  fun listingCard_displaysCoreInfo() {
    val tutor =
        fakeTutor(
            name = "Alice Johnson",
            locationName = "Campus East",
            avgRating = 4.5,
            totalRatings = 32,
            userId = "tutor-42")

    val listing =
        fakeListing(
            listingId = "listing-123",
            creatorUserId = tutor.userId,
            description = "Beginner piano coaching",
            hourlyRate = 25.0,
            locationName = "Campus East")

    composeRule.setContent {
      MaterialTheme {
        ListingCard(
            listing = listing,
            creator = tutor,
            creatorRating = tutor.tutorRating,
            onOpenListing = {},
            onBook = {})
      }
    }

    // Card renders (by tag)
    composeRule.onNodeWithTag(ListingCardTestTags.CARD).assertIsDisplayed()

    // Title / name of the listing
    composeRule.onNodeWithText("Beginner piano coaching").assertIsDisplayed()

    // Tutor line: "by Alice Johnson"
    composeRule.onNodeWithText("by Alice Johnson").assertIsDisplayed()

    // Price "$25.00 / hr"
    val expectedPrice = String.format(Locale.getDefault(), "$%.2f / hr", 25.0)
    composeRule.onNodeWithText(expectedPrice).assertIsDisplayed()

    // Rating count "(32)"
    composeRule.onNodeWithText("(32)").assertIsDisplayed()

    // Location "Campus East"
    composeRule.onNodeWithText("Campus East").assertIsDisplayed()

    // Book button visible
    composeRule.onNodeWithTag(ListingCardTestTags.BOOK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun listingCard_callsOnBookWhenButtonClicked() {
    val tutor = fakeTutor(userId = "tutor-42")
    val listing =
        fakeListing(
            listingId = "listing-abc",
            creatorUserId = "tutor-42",
            description = "Beginner piano coaching",
            hourlyRate = 25.0)

    var bookedListingId: String? = null

    composeRule.setContent {
      MaterialTheme {
        ListingCard(
            listing = listing,
            creator = tutor,
            creatorRating = tutor.tutorRating,
            onOpenListing = {},
            onBook = { listingId -> bookedListingId = listingId })
      }
    }

    // Click the Book button
    composeRule.onNodeWithTag(ListingCardTestTags.BOOK_BUTTON).performClick()

    // Verify callback got correct ID
    assertEquals("listing-abc", bookedListingId)
  }

  @Test
  fun listingCard_callsOnOpenListingWhenCardClicked() {
    val tutor = fakeTutor(userId = "tutor-99")
    val listing =
        fakeListing(
            listingId = "listing-xyz",
            creatorUserId = "tutor-99",
            description = "Advanced violin mentoring",
            hourlyRate = 40.0,
        )

    var openedListingId: String? = null

    composeRule.setContent {
      MaterialTheme {
        ListingCard(
            listing = listing,
            creator = tutor,
            creatorRating = tutor.tutorRating,
            onOpenListing = { id -> openedListingId = id },
            onBook = {})
      }
    }

    // Click the card container (not the button)
    composeRule.onNodeWithTag(ListingCardTestTags.CARD).performClick()

    assertEquals("listing-xyz", openedListingId)
  }

  @Test
  fun listingCard_fallbacksWorkWhenCreatorMissing() {
    // No Profile passed in (creator = null), so we fall back to creatorUserId.
    val listing =
        fakeListing(
            listingId = "listing-no-creator",
            creatorUserId = "tutor-anon",
            description = "Math tutoring for IB exams",
            hourlyRate = 30.0,
            locationName = "Library Hall")

    composeRule.setContent {
      MaterialTheme {
        ListingCard(
            listing = listing,
            creator = null,
            creatorRating = RatingInfo(averageRating = 5.0, totalRatings = 1),
            onOpenListing = {},
            onBook = {})
      }
    }

    // Title from listing.description
    composeRule.onNodeWithText("Math tutoring for IB exams").assertIsDisplayed()

    // Tutor line falls back to creatorUserId ("by tutor-anon")
    composeRule.onNodeWithText("by tutor-anon").assertIsDisplayed()

    // Location displays normally
    composeRule.onNodeWithText("Library Hall").assertIsDisplayed()
  }

  @Test
  fun listingCard_titleFallsBackToSkillWhenDescriptionBlank() {
    val tutor = fakeTutor(name = "Bob Smith", userId = "tutor-77")
    // description is blank on purpose, skill.skill is "PIANO"
    val listing =
        fakeListing(
            listingId = "listing-skill-fallback",
            creatorUserId = tutor.userId,
            description = "",
            hourlyRate = 20.0,
            locationName = "Music Hall",
            skill =
                Skill(
                    mainSubject = MainSubject.MUSIC,
                    skill = "PIANO",
                    skillTime = 2.0,
                    expertise = ExpertiseLevel.INTERMEDIATE))

    composeRule.setContent {
      MaterialTheme {
        ListingCard(
            listing = listing,
            creator = tutor,
            creatorRating = tutor.tutorRating,
            onOpenListing = {},
            onBook = {})
      }
    }

    // Since description = "", we expect the title to fall back to skill = "PIANO"
    composeRule.onNodeWithText("PIANO").assertIsDisplayed()
    // We still expect correct tutor fallback text
    composeRule.onNodeWithText("by Bob Smith").assertIsDisplayed()
  }

  @Test
  fun listingCard_titleFallsBackToMainSubjectWhenDescriptionAndSkillBlank() {
    val tutor = fakeTutor(name = "Charlie", userId = "tutor-88")

    // Here: description = "", skill.skill = "".
    // That should make the card fall back to mainSubject.name ("MUSIC").
    val listing =
        fakeListing(
            listingId = "listing-subject-fallback",
            creatorUserId = tutor.userId,
            description = "",
            hourlyRate = 18.0,
            locationName = "Studio 2",
            skill =
                Skill(
                    mainSubject = MainSubject.MUSIC,
                    skill = "", // <- blank this time
                    skillTime = 1.0,
                    expertise = ExpertiseLevel.BEGINNER))

    composeRule.setContent {
      MaterialTheme {
        ListingCard(
            listing = listing,
            creator = tutor,
            creatorRating = tutor.tutorRating,
            onOpenListing = {},
            onBook = {})
      }
    }

    // Expect fallback to mainSubject.name, i.e. "MUSIC"
    composeRule.onNodeWithText("MUSIC").assertIsDisplayed()
    composeRule.onNodeWithText("by Charlie").assertIsDisplayed()
  }

  @Test
  fun listingCard_showsUnknownWhenLocationNameBlank() {
    val tutor =
        fakeTutor(
            name = "Dana",
            locationName = "", // tutor location doesn't really matter here
            userId = "tutor-55")

    // listing.location.name is "", so UI should display "Unknown"
    val listing =
        fakeListing(
            listingId = "listing-unknown-loc",
            creatorUserId = tutor.userId,
            description = "Chemistry help",
            hourlyRate = 35.0,
            locationName = "" // <- blank on purpose
            )

    composeRule.setContent {
      MaterialTheme {
        ListingCard(
            listing = listing,
            creator = tutor,
            creatorRating = tutor.tutorRating,
            onOpenListing = {},
            onBook = {})
      }
    }

    // Fallback for location should be "Unknown"
    composeRule.onNodeWithText("Unknown").assertIsDisplayed()
  }
}
