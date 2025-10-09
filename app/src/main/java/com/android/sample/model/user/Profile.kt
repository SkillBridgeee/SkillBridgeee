package com.android.sample.model.user

import com.android.sample.model.map.Location

/** Data class representing user profile information */
data class Profile(
    /**
     * I didn't change the userId request yet because according to my searches it would be better if
     * we implement it with authentication
     */
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val location: Location = Location(),
    val description: String = "",
    val isTutor: Boolean = false
)
