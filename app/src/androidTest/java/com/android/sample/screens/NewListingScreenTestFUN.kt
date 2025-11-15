package com.android.sample.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.rating.RatingRepository
import com.android.sample.ui.newListing.NewListingScreenTestTag
import com.android.sample.utils.AppTest
import com.android.sample.utils.fakeRepo.BookingFake
import com.android.sample.utils.fakeRepo.ListingFake
import com.android.sample.utils.fakeRepo.RatingFake
import com.android.sample.utils.fakeRepo.fakeProfile.FakeProfileRepo
import com.android.sample.utils.fakeRepo.fakeProfile.ProfileFakeWorking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewListingScreenTestFUN : AppTest() {

  @get:Rule val composeTestRule = createComposeRule()

  override fun createInitializedProfileRepo(): FakeProfileRepo {
    return ProfileFakeWorking()
  }

  override fun createInitializedListingRepo(): ListingRepository {
    return ListingFake()
  }

  override fun createInitializedBookingRepo(): BookingRepository {
    return BookingFake()
  }

  override fun createInitializedRatingRepo(): RatingRepository {
    return RatingFake()
  }

  @Before
  override fun setUp() {
    super.setUp()
    composeTestRule.setContent { CreateEveryThing() }
    composeTestRule.navigateToNewListing()
  }

  @Test
  fun testGoodScreen() {
    composeTestRule.onNodeWithTag(NewListingScreenTestTag.CREATE_LESSONS_TITLE).assertIsDisplayed()
  }
}
