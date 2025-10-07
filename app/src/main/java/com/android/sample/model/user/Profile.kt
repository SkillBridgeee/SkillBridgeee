package com.android.sample.model.user

/** Data class representing user profile information */
data class Profile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val location: String = "",
    val description: String = ""
)
