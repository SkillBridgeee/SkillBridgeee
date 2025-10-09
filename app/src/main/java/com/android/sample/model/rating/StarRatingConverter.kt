package com.android.sample.model.rating

/** Converter for serializing/deserializing StarRating enum for Firestore */
/** Was recommended to facilitate the repository work so i am leaving it here */
/** If unnecessary after the repository work please delete */
object StarRatingConverter {
  @JvmStatic fun toInt(starRating: StarRating): Int = starRating.value

  @JvmStatic fun fromInt(value: Int): StarRating = StarRating.fromInt(value)
}
