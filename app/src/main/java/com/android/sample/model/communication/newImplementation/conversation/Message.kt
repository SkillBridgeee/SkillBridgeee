package com.android.sample.model.communication.newImplementation.conversation

import java.util.Date

data class Message(
    val msgId: String = "",
    val content: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val createdAt: Date = Date()
)
