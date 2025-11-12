package com.android.sample.model

object ValidationUtils {

  fun requireNonBlank(value: String?, fieldName: String) {
    val v = value?.trim()
    require(!v.isNullOrEmpty()) { "$fieldName must not be blank." }
  }

  fun requireMaxLength(value: String?, fieldName: String, max: Int) {
    val v = value?.trim()
    require(v == null || v.length <= max) { "$fieldName is too long (max $max characters)." }
  }

  fun requireMinLength(value: String?, fieldName: String, min: Int) {
    val v = value?.trim()
    require(v == null || v.length >= min) { "$fieldName is too short (min $min characters)." }
  }

  fun requireId(value: String?, fieldName: String = "id") {
    requireNonBlank(value?.trim(), fieldName)
  }
}
