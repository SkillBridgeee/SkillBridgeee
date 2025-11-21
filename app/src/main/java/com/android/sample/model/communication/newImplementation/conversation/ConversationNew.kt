package com.android.sample.model.communication.newImplementation.conversation

data class ConversationNew(
    val convId: String = "",
    val convCreatorId: String = "",
    val otherPersonId: String = "",
    val convName: String = "",
    val messages: List<MessageNew> = emptyList(),
)
