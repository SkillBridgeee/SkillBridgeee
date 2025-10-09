package com.android.sample.model.communication

import java.util.Date

/** Data class representing a message between users */
data class Message(
    val sentFrom: String = "", // UID of the sender
    val sentTo: String = "", // UID of the receiver
    val sentTime: Date = Date(), // Date and time when message was sent
    val receiveTime: Date? = null, // Date and time when message was received
    val readTime: Date? = null, // Date and time when message was read for the first time
    val message: String = "" // The actual message content
) {
  init {
    require(sentFrom != sentTo) { "Sender and receiver cannot be the same user" }
    receiveTime?.let { require(!sentTime.after(it)) { "Receive time cannot be before sent time" } }
    readTime?.let { readTime ->
      require(!sentTime.after(readTime)) { "Read time cannot be before sent time" }
      receiveTime?.let { receiveTime ->
        require(!receiveTime.after(readTime)) { "Read time cannot be before receive time" }
      }
    }
  }
}
