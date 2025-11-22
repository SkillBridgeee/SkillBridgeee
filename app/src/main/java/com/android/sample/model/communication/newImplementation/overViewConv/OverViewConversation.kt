package com.android.sample.model.communication.newImplementation.overViewConv

import com.android.sample.model.communication.newImplementation.conversation.MessageNew

data class OverViewConversation(
    val overViewId: String = "",
    val linkedConvId: String = "",
    val convName: String = "",
    val lastMsg: MessageNew = MessageNew(),
    val nonReadMsgNumber: Int = 0,
    val convCreatorId: String = "",
    val otherPersonId: String = ""
)
