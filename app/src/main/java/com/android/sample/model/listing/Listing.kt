package com.android.sample.model.listing

import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import java.util.Date

enum class ListingType {
  PROPOSAL,
  REQUEST
}

/** Base class for proposals and requests */
sealed class Listing {
  abstract val listingId: String
  abstract val creatorUserId: String
  abstract val skill: Skill
  abstract val description: String
  abstract val location: Location
  abstract val createdAt: Date
  abstract val active: Boolean
  abstract val hourlyRate: Double
  abstract val type: ListingType

  /** Display title: prefer description, then skill text, then main subject name */
  fun displayTitle(): String =
      description.ifBlank { skill.skill.ifBlank { skill.mainSubject.name } }
}

/** Proposal - user offering to teach */
data class Proposal(
    override val listingId: String = "",
    override val creatorUserId: String = "",
    override val skill: Skill = Skill(),
    override val description: String = "",
    override val location: Location = Location(),
    override val createdAt: Date = Date(),
    override val active: Boolean = true,
    override val hourlyRate: Double = 0.0,
    override val type: ListingType = ListingType.PROPOSAL
) : Listing() {}

/** Request - user looking for a tutor */
data class Request(
    override val listingId: String = "",
    override val creatorUserId: String = "",
    override val skill: Skill = Skill(),
    override val description: String = "",
    override val location: Location = Location(),
    override val createdAt: Date = Date(),
    override val active: Boolean = true,
    override val hourlyRate: Double = 0.0,
    override val type: ListingType = ListingType.REQUEST
) : Listing() {}
