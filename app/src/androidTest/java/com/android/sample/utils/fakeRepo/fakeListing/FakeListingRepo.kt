package com.android.sample.utils.fakeRepo.fakeListing

import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository

interface FakeListingRepo : ListingRepository {

  fun getLastListingCreated(): Listing?
}
