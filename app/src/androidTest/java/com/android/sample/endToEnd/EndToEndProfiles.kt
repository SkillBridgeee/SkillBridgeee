package com.android.sample.data

import java.util.UUID

data class TestUser(
    val name: String,
    val surname: String,
    val address: String,
    val levelOfEducation: String,
    val description: String,
    val email: String,
    val password: String = "testPassword123!"
)

object TestUsers {
  val TUTOR =
      TestUser(
          name = "John",
          surname = "Tutor",
          address = "123 Tutor Lane",
          levelOfEducation = "PhD in Physics",
          description = "Experienced Physics Tutor",
          email = "tutor_${UUID.randomUUID()}@test.com")

  val LEARNER =
      TestUser(
          name = "Jane",
          surname = "Learner",
          address = "456 Student Street",
          levelOfEducation = "High School",
          description = "Eager to learn",
          email = "learner_${UUID.randomUUID()}@test.com")
}
