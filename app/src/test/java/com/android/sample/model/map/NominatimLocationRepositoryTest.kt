package com.android.sample.model.map

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class NominatimLocationRepositoryTest {

  private lateinit var mockWebServer: MockWebServer
  private lateinit var repository: NominatimLocationRepository
  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    mockWebServer = MockWebServer()
    mockWebServer.start()

    val client = OkHttpClient.Builder().build()
    val baseUrl = mockWebServer.url("/").toString().removeSuffix("/")
    repository = NominatimLocationRepository(client, baseUrl, testDispatcher)
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun `search returns empty list when response is empty array`() = runTest {
    // Given
    val mockResponse = MockResponse().setResponseCode(200).setBody("[]")
    mockWebServer.enqueue(mockResponse)

    // When
    val result = repository.search("test")

    // Then
    assertTrue(result.isEmpty())
  }

  @Test
  fun `search returns list of locations when response contains data`() = runTest {
    // Given
    val jsonResponse =
        """
      [
        {
          "lat": "46.5196535",
          "lon": "6.6322734",
          "name": "Lausanne"
        },
        {
          "lat": "46.2043907",
          "lon": "6.1431577",
          "name": "Geneva"
        }
      ]
    """
            .trimIndent()

    val mockResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)
    mockWebServer.enqueue(mockResponse)

    // When
    val result = repository.search("Swiss cities")

    // Then
    assertEquals(2, result.size)
    assertEquals("Lausanne", result[0].name)
    assertEquals(46.5196535, result[0].latitude, 0.0001)
    assertEquals(6.6322734, result[0].longitude, 0.0001)
    assertEquals("Geneva", result[1].name)
    assertEquals(46.2043907, result[1].latitude, 0.0001)
    assertEquals(6.1431577, result[1].longitude, 0.0001)
  }

  @Test
  fun `search includes format json and query parameter in request`() = runTest {
    // Given
    val mockResponse = MockResponse().setResponseCode(200).setBody("[]")
    mockWebServer.enqueue(mockResponse)

    // When
    repository.search("EPFL")

    // Then
    val request = mockWebServer.takeRequest()
    assertTrue(request.path!!.contains("q=EPFL"))
    assertTrue(request.path!!.contains("format=json"))
  }

  @Test
  fun `search includes user agent header`() = runTest {
    // Given
    val mockResponse = MockResponse().setResponseCode(200).setBody("[]")
    mockWebServer.enqueue(mockResponse)

    // When
    repository.search("test")

    // Then
    val request = mockWebServer.takeRequest()
    assertEquals("SkillBridgeee", request.getHeader("User-Agent"))
  }

  @Test(expected = Exception::class)
  fun `search throws exception when response is not successful`() = runTest {
    // Given
    val mockResponse = MockResponse().setResponseCode(500).setBody("Internal Server Error")
    mockWebServer.enqueue(mockResponse)

    // When
    repository.search("test")

    // Then - exception is thrown
  }

  @Test
  fun `parseBody correctly parses valid JSON array`() {
    // Given
    val jsonBody =
        """
      [
        {
          "lat": "46.5196535",
          "lon": "6.6322734",
          "name": "Lausanne"
        }
      ]
    """
            .trimIndent()

    // When
    println("Testing parseBody with: $jsonBody")
    val result =
        try {
          repository.parseBody(jsonBody)
        } catch (e: Exception) {
          println("Exception in parseBody: ${e.message}")
          e.printStackTrace()
          throw e
        }
    println("Result size: ${result.size}")
    if (result.isNotEmpty()) {
      println("First result: ${result[0]}")
    }

    // Then
    assertEquals(1, result.size)
    assertEquals("Lausanne", result[0].name)
    assertEquals(46.5196535, result[0].latitude, 0.0001)
    assertEquals(6.6322734, result[0].longitude, 0.0001)
  }

  @Test
  fun `parseBody returns empty list for empty JSON array`() {
    // Given
    val jsonBody = "[]"

    // When
    val result = repository.parseBody(jsonBody)

    // Then
    assertTrue(result.isEmpty())
  }

  @Test
  fun `parseBody handles multiple locations correctly`() {
    // Given
    val jsonBody =
        """
      [
        {"lat": "1.0", "lon": "2.0", "name": "Location1"},
        {"lat": "3.0", "lon": "4.0", "name": "Location2"},
        {"lat": "5.0", "lon": "6.0", "name": "Location3"}
      ]
    """
            .trimIndent()

    // When
    val result = repository.parseBody(jsonBody)

    // Then
    assertEquals(3, result.size)
    assertEquals("Location1", result[0].name)
    assertEquals("Location2", result[1].name)
    assertEquals("Location3", result[2].name)
  }
}
