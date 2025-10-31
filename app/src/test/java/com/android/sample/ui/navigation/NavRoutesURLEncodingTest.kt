package com.android.sample.ui.navigation

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import org.junit.Assert.*
import org.junit.Test

/**
 * Additional tests for URL encoding edge cases in NavRoutes. These tests ensure that emails are
 * properly encoded for navigation and can be decoded.
 */
class NavRoutesURLEncodingTest {

  @Test
  fun createSignUpRoute_encodingAndDecoding_roundTrip() {
    val originalEmail = "test@example.com"
    val route = NavRoutes.createSignUpRoute(originalEmail)

    // Extract the encoded email from the route
    val encodedEmail = route.substringAfter("signup?email=")

    // Decode it
    val decodedEmail = URLDecoder.decode(encodedEmail, StandardCharsets.UTF_8.toString())

    // Should match original
    assertEquals(originalEmail, decodedEmail)
  }

  @Test
  fun createSignUpRoute_withPercentSign_encodesCorrectly() {
    val route = NavRoutes.createSignUpRoute("test%user@example.com")

    // % should be encoded as %25
    assertTrue(route.contains("%25"))
  }

  @Test
  fun createSignUpRoute_withAmpersand_encodesCorrectly() {
    val route = NavRoutes.createSignUpRoute("test&user@example.com")

    // & should be encoded as %26
    assertTrue(route.contains("%26"))
  }

  @Test
  fun createSignUpRoute_withEquals_encodesCorrectly() {
    val route = NavRoutes.createSignUpRoute("test=user@example.com")

    // = should be encoded as %3D
    assertTrue(route.contains("%3D"))
  }

  @Test
  fun createSignUpRoute_withQuestionMark_encodesCorrectly() {
    val route = NavRoutes.createSignUpRoute("test?user@example.com")

    // ? should be encoded as %3F
    assertTrue(route.contains("%3F"))
  }

  @Test
  fun createSignUpRoute_withHash_encodesCorrectly() {
    val route = NavRoutes.createSignUpRoute("test#user@example.com")

    // # should be encoded as %23
    assertTrue(route.contains("%23"))
  }

  @Test
  fun createSignUpRoute_withSlash_encodesCorrectly() {
    val route = NavRoutes.createSignUpRoute("test/user@example.com")

    // / should be encoded as %2F
    assertTrue(route.contains("%2F"))
  }

  @Test
  fun createSignUpRoute_multipleSpecialChars_encodesAll() {
    val email = "user+tag@sub-domain.co.uk?param=value"
    val route = NavRoutes.createSignUpRoute(email)

    val encodedEmail = route.substringAfter("signup?email=")
    val decodedEmail = URLDecoder.decode(encodedEmail, StandardCharsets.UTF_8.toString())

    assertEquals(email, decodedEmail)
  }

  @Test
  fun createSignUpRoute_unicodeCharacters_handlesCorrectly() {
    val email = "tëst@éxample.com"
    val route = NavRoutes.createSignUpRoute(email)

    val encodedEmail = route.substringAfter("signup?email=")
    val decodedEmail = URLDecoder.decode(encodedEmail, StandardCharsets.UTF_8.toString())

    assertEquals(email, decodedEmail)
  }

  @Test
  fun createSignUpRoute_chineseCharacters_handlesCorrectly() {
    val email = "测试@example.com"
    val route = NavRoutes.createSignUpRoute(email)

    val encodedEmail = route.substringAfter("signup?email=")
    val decodedEmail = URLDecoder.decode(encodedEmail, StandardCharsets.UTF_8.toString())

    assertEquals(email, decodedEmail)
  }

  @Test
  fun createSignUpRoute_emojiInEmail_handlesCorrectly() {
    val email = "test😀@example.com"
    val route = NavRoutes.createSignUpRoute(email)

    val encodedEmail = route.substringAfter("signup?email=")
    val decodedEmail = URLDecoder.decode(encodedEmail, StandardCharsets.UTF_8.toString())

    assertEquals(email, decodedEmail)
  }

  @Test
  fun createSignUpRoute_longEmail_encodesCompletely() {
    val email = "very.long.email.address.with.many.dots.and.plus+tag@subdomain.example.co.uk"
    val route = NavRoutes.createSignUpRoute(email)

    // Should contain encoded @
    assertTrue(route.contains("%40"))

    // Decode and verify
    val encodedEmail = route.substringAfter("signup?email=")
    val decodedEmail = URLDecoder.decode(encodedEmail, StandardCharsets.UTF_8.toString())

    assertEquals(email, decodedEmail)
  }

  @Test
  fun createSignUpRoute_consecutiveSpecialChars_encodesCorrectly() {
    val email = "test++@@example..com"
    val route = NavRoutes.createSignUpRoute(email)

    val encodedEmail = route.substringAfter("signup?email=")
    val decodedEmail = URLDecoder.decode(encodedEmail, StandardCharsets.UTF_8.toString())

    assertEquals(email, decodedEmail)
  }
}
