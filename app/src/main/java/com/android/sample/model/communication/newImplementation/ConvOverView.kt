package com.android.sample.model.communication.newImplementation

data class ConvOverView(
    val previewId: String = "",
    val linkedConvId: String = "",
    val convName: String = "",
    val lastMsg: MessageNew = MessageNew(),
    val nonReadMsgNumber: Int = 0,
    val convCreatorId: String = "",
    val otherPersonId: String = ""
)
