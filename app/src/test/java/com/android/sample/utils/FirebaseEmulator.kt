package com.android.sample.utils

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase // Changed import
import io.mockk.InternalPlatformDsl.toArray
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object FirebaseEmulator {
  val auth by lazy { Firebase.auth }
  val firestore by lazy { Firebase.firestore }

  const val HOST = "localhost"
  const val EMULATORS_PORT = 4400
  const val FIRESTORE_PORT = 8080
  const val AUTH_PORT = 9099

  private val projectID by lazy { FirebaseApp.getInstance().options.projectId!! }

  private val httpClient =
      OkHttpClient.Builder()
          .connectTimeout(10, TimeUnit.SECONDS)
          .readTimeout(10, TimeUnit.SECONDS)
          .writeTimeout(10, TimeUnit.SECONDS)
          .build()

  private val firestoreEndpoint by lazy {
    "http://$HOST:$FIRESTORE_PORT/emulator/v1/projects/$projectID/databases/(default)/documents"
  }
  private val authEndpoint by lazy {
    "http://$HOST:$AUTH_PORT/emulator/v1/projects/$projectID/accounts"
  }
  private val emulatorsEndpoint = "http://$HOST:$EMULATORS_PORT/emulators"

  var isRunning = false
    private set

  fun connect() {
    if (isRunning) return

    isRunning = areEmulatorsRunning()
    if (isRunning) {
      // Configure Auth emulator
      auth.useEmulator(HOST, AUTH_PORT)

      // Configure Firestore emulator FIRST, before any other settings
      firestore.useEmulator(HOST, FIRESTORE_PORT)

      // Then configure Firestore settings for emulator
      try {
        val settings =
            FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false) // Disable persistence for tests
                .build()
        firestore.firestoreSettings = settings
        Log.i("FirebaseEmulator", "Firestore settings configured successfully")
      } catch (e: Exception) {
        Log.w("FirebaseEmulator", "Failed to set Firestore settings: ${e.message}")
        // Continue anyway, as this might not be critical for all tests
      }

      Log.i("FirebaseEmulator", "Successfully connected to Firebase emulators")
    } else {
      Log.e("FirebaseEmulator", "Firebase emulators are NOT running!")
      Log.e("FirebaseEmulator", "Please start emulators with: firebase emulators:start")
      throw IllegalStateException(
          "Firebase emulators are not running. Please start them with 'firebase emulators:start' " +
              "before running tests. Expected emulator at http://$HOST:$EMULATORS_PORT")
    }
  }

  private fun areEmulatorsRunning(): Boolean {
    // Try both localhost and 127.0.0.1 to handle different network configurations
    val hosts = listOf("localhost", "127.0.0.1")

    for (host in hosts) {
      val testEndpoint = "http://$host:$EMULATORS_PORT/emulators"
      val isRunning =
          runCatching {
                val request = Request.Builder().url(testEndpoint).build()
                val response = httpClient.newCall(request).execute()
                Log.d(
                    "FirebaseEmulator",
                    "Checking emulator at $testEndpoint: ${response.isSuccessful}")
                response.isSuccessful
              }
              .getOrElse { error ->
                Log.d("FirebaseEmulator", "Failed to connect to $testEndpoint: ${error.message}")
                false
              }

      if (isRunning) {
        Log.i("FirebaseEmulator", "Found running emulator at $testEndpoint")
        return true
      }
    }

    return false
  }

  private fun clearEmulator(endpoint: String) {
    if (!isRunning) return
    runCatching {
          val request = Request.Builder().url(endpoint).delete().build()
          httpClient.newCall(request).execute()
        }
        .onFailure {
          Log.w("FirebaseEmulator", "Failed to clear emulator at $endpoint: ${it.message}")
        }
  }

  fun clearAuthEmulator() {
    clearEmulator(authEndpoint)
  }

  fun clearFirestoreEmulator() {
    clearEmulator(firestoreEndpoint)
  }

  /**
   * Seeds a Google user in the Firebase Auth Emulator using a fake JWT id_token.
   *
   * @param fakeIdToken A JWT-shaped string, must contain at least "sub".
   * @param email The email address to associate with the account.
   */
  fun createGoogleUser(fakeIdToken: String) {
    val url =
        "http://$HOST:$AUTH_PORT/identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=fake-api-key"

    // postBody must be x-www-form-urlencoded style string, wrapped in JSON
    val postBody = "id_token=$fakeIdToken&providerId=google.com"

    val requestJson =
        JSONObject().apply {
          put("postBody", postBody)
          put("requestUri", "http://localhost")
          put("returnIdpCredential", true)
          put("returnSecureToken", true)
        }

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = requestJson.toString().toRequestBody(mediaType)

    val request =
        Request.Builder().url(url).post(body).addHeader("Content-Type", "application/json").build()

    val response = httpClient.newCall(request).execute()
    assert(response.isSuccessful) {
      "Failed to create user in Auth Emulator: ${response.code} ${response.message}"
    }
  }

  fun changeEmail(fakeIdToken: String, newEmail: String) {
    val response =
        httpClient
            .newCall(
                Request.Builder()
                    .url(
                        "http://$HOST:$AUTH_PORT/identitytoolkit.googleapis.com/v1/accounts:update?key=fake-api-key")
                    .post(
                        """
            {
                "idToken": "$fakeIdToken",
                "email": "$newEmail",
                "returnSecureToken": true
            }
        """
                            .trimIndent()
                            .toRequestBody())
                    .build())
            .execute()
    assert(response.isSuccessful) {
      "Failed to change email in Auth Emulator: ${response.code} ${response.message}"
    }
  }

  val users: String
    get() {
      val request =
          Request.Builder()
              .url(
                  "http://$HOST:$AUTH_PORT/identitytoolkit.googleapis.com/v1/accounts:query?key=fake-api-key")
              .build()

      Log.d("FirebaseEmulator", "Fetching users with request: ${request.url.toString()}")
      val response = httpClient.newCall(request).execute()
      Log.d("FirebaseEmulator", "Response received: ${response.toArray()}")
      return response.body.toString()
    }
}
