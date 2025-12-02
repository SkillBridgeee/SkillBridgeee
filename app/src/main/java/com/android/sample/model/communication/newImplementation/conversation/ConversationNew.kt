package com.android.sample.model.communication.newImplementation.conversation

import com.google.firebase.Timestamp

data class ConversationNew(
    val convId: String = "",
    val convCreatorId: String = "",
    val otherPersonId: String = "",
    val convName: String = "",
    val updatedAt: Timestamp? = null,
    val messages: List<MessageNew> = emptyList(),
)
