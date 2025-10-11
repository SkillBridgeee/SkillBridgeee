package com.android.sample.model.user

import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo

data class Profile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val location: Location = Location(),
    val description: String = "",
    val tutorRating: RatingInfo = RatingInfo(),
    val studentRating: RatingInfo = RatingInfo(),
)