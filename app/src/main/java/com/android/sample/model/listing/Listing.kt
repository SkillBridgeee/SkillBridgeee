package com.android.sample.model.listing

import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import java.util.Date

/** Base class for proposals and requests */
sealed class Listing {
  abstract val listingId: String
  abstract val creatorUserId: String
  abstract val skill: Skill
  abstract val description: String
  abstract val location: Location
  abstract val createdAt: Date
  abstract val isActive: Boolean
}

/** Proposal - user offering to teach */
data class Proposal(
    override val listingId: String = "",
    override val creatorUserId: String = "",
    override val skill: Skill = Skill(),
    override val description: String = "",
    override val location: Location = Location(),
    override val createdAt: Date = Date(),
    override val isActive: Boolean = true,
    val hourlyRate: Double = 0.0
) : Listing() {
  init {
    require(hourlyRate >= 0) { "Hourly rate must be non-negative" }
  }
}

/** Request - user looking for a tutor */
data class Request(
    override val listingId: String = "",
    override val creatorUserId: String = "",
    override val skill: Skill = Skill(),
    override val description: String = "",
    override val location: Location = Location(),
    override val createdAt: Date = Date(),
    override val isActive: Boolean = true,
    val maxBudget: Double = 0.0
) : Listing() {
  init {
    require(maxBudget >= 0) { "Max budget must be non-negative" }
  }
}
