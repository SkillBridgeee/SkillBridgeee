package com.android.sample.model.rating

/** Data class representing a rating given to a tutor */
data class Ratings(
    val rating: StarRating = StarRating.ONE, // Rating between 1-5 as enum
    val fromUserId: String = "", // UID of the user giving the rating
    val fromUserName: String = "", // Name of the user giving the rating
    val ratingUID: String = "" // UID of the person who got the rating (tutor)
)
