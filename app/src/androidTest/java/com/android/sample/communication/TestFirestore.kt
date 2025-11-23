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

  private var initialized = false
  private lateinit var _db: FirebaseFirestore

  val db: FirebaseFirestore
    get() {
      if (!initialized) connect()
      return _db
    }

  fun connect() {
    if (initialized) return

    if (!isEmulatorRunning()) {
      throw IllegalStateException(
          "Firestore emulator is not running. Start it with:\n\nfirebase emulators:start")
    }

    Log.i("TestFirestore", "Connecting Firestore to emulator...")

    val instance = FirebaseFirestore.getInstance()
    instance.useEmulator(HOST, FIRESTORE_PORT)

    // Disable persistence for deterministic tests
    val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()

    instance.firestoreSettings = settings

    _db = instance
    initialized = true

    Log.i("TestFirestore", "Firestore emulator connected successfully.")
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
