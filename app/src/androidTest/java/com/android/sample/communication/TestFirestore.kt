package com.android.sample.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request

object TestFirestore {

  private const val HOST = "10.0.2.2" // localhost for Android emulator
  private const val FIRESTORE_PORT = 8080
  private const val EMULATOR_UI_PORT = 4400

  private val httpClient =
      OkHttpClient.Builder()
          .connectTimeout(3, TimeUnit.SECONDS)
          .readTimeout(3, TimeUnit.SECONDS)
          .writeTimeout(3, TimeUnit.SECONDS)
          .build()

  // Singleton Firestore instance, lazy-initialized
  val db: FirebaseFirestore by lazy {
    if (!isEmulatorRunning()) {
      throw IllegalStateException(
          "Firestore emulator is not running. Start it with:\n\nfirebase emulators:start")
    }

    Log.i("TestFirestore", "Connecting Firestore to emulator...")

    val instance = FirebaseFirestore.getInstance()

    // ⚠ Must be called BEFORE any Firestore usage
    instance.useEmulator(HOST, FIRESTORE_PORT)

    // Disable persistence for deterministic tests
    val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()

    instance.firestoreSettings = settings

    Log.i("TestFirestore", "Firestore emulator connected successfully.")

    instance
  }

  private fun isEmulatorRunning(): Boolean {
    val url = "http://$HOST:$EMULATOR_UI_PORT/emulators"

    return try {
      val request = Request.Builder().url(url).build()
      val response = httpClient.newCall(request).execute()
      response.isSuccessful
    } catch (e: Exception) {
      Log.w("TestFirestore", "Emulator not reachable at $url")
      false
    }
  }
}
