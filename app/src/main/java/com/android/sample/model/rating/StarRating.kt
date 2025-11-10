package com.android.sample.model.rating

/** Enum representing possible star ratings (1-5) */
enum class StarRating(val value: Int) {
  ONE(1),
  TWO(2),
  THREE(3),
  FOUR(4),
  FIVE(5);

  companion object {
    fun fromInt(value: Int): StarRating =
        values().find { it.value == value }
            ?: throw IllegalArgumentException("Invalid star rating: $value")



  }
}
