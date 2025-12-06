package com.android.sample.model.communication.overViewConv

import com.android.sample.model.communication.conversation.Message

/**
 * Represents a summary or overview of a conversation for a user.
 *
 * This class is used to display a list of conversations in a user interface, showing the most
 * important information without loading the full message history.
 *
 * @property overViewId Unique identifier for this overview record.
 * @property linkedConvId The ID of the actual conversation this overview is linked to.
 * @property convName The display name of the conversation (e.g., contact or group name).
 * @property lastMsg The last message sent or received in this conversation.
 * @property nonReadMsgNumber The number of unread messages for the current user.
 * @property overViewOwnerId The ID of the user who created the conversation.
 * @property otherPersonId The ID of the other participant in the conversation (for 1:1 chats).
 */
data class OverViewConversation(
    val overViewId: String = "",
    val linkedConvId: String = "",
    val convName: String = "",
    val nonReadMsgNumber: Int = 0,
    val lastMsg: Message? = null,
    val overViewOwnerId: String = "",
    val otherPersonId: String = "",
)
