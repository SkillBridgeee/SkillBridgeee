package com.android.sample.model.communication.newImplementation.conversation

import com.google.firebase.Timestamp

data class ConversationNew(
    var convId: String = "",
    var convCreatorId: String = "",
    var otherPersonId: String = "",
    var convName: String = "",
    var updatedAt: Timestamp? = null,
    var messages: List<MessageNew> = emptyList(),
)
