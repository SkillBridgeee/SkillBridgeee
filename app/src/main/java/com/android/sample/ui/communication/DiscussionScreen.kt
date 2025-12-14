package com.android.sample.ui.communication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.sample.model.communication.overViewConv.OverViewConversation

private const val DISMISS_ERROR_DESCRIPTION = "Dismiss error"

@Composable
fun DiscussionScreen(
    viewModel: DiscussionViewModel,
    onConversationClick: (conversationId: String) -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()
  val currentUserId = com.android.sample.model.authentication.UserSessionManager.getCurrentUserId()

  Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
      // Show error if present
      uiState.error?.let { error ->
        Surface(
            modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.errorContainer) {
              Row(
                  modifier = Modifier.padding(8.dp),
                  verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.clearError() }) {
                      Icon(
                          imageVector = Icons.Default.Close,
                          contentDescription = DISMISS_ERROR_DESCRIPTION,
                          tint = MaterialTheme.colorScheme.onErrorContainer)
                    }
                  }
            }
      }

      if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().testTag("discussion_loading"),
            contentAlignment = Alignment.Center) {
              CircularProgressIndicator(modifier = Modifier.testTag("discussion_loading_indicator"))
            }
      } else {
        LazyColumn(modifier = Modifier.fillMaxSize().testTag("discussion_list")) {
          itemsIndexed(uiState.conversations) { index, conversation ->
            val participantName =
                uiState.participantNames[conversation.otherPersonId] ?: "Unknown User"
            ConversationItem(
                conversation = conversation,
                participantName = participantName,
                currentUserId = currentUserId,
                onClick = { onConversationClick(conversation.linkedConvId) },
                index = index)
          }
        }
      }
    }
  }
}

@Composable
fun ConversationItem(
    conversation: OverViewConversation,
    participantName: String,
    currentUserId: String?,
    onClick: () -> Unit,
    index: Int
) {
  val backgroundColor =
      if (index % 2 == 0) {
        MaterialTheme.colorScheme.surface
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
      }

  // Determine if the last message was sent by the current user
  val lastMessageText =
      conversation.lastMsg?.let { lastMsg ->
        val isMyMessage = currentUserId != null && lastMsg.senderId == currentUserId
        if (isMyMessage) {
          "You: ${lastMsg.content}"
        } else {
          lastMsg.content
        }
      } ?: ""

  Row(
      modifier =
          Modifier.fillMaxWidth()
              .background(backgroundColor)
              .clickable(onClick = onClick)
              .testTag("conversation_item_${conversation.linkedConvId}")
              .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically) {
        // Avatar placeholder
        Box(
            modifier =
                Modifier.size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center) {
              Text(
                  text = participantName.take(1).uppercase(),
                  style = MaterialTheme.typography.headlineSmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer)
            }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = participantName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false))
                conversation.lastMsg?.createdAt?.let { timestamp ->
                  Text(
                      text = TimeFormatUtils.formatDiscussionTimestamp(timestamp),
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.padding(start = 8.dp))
                }
              }
          Text(
              text = lastMessageText,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)
        }

        if (conversation.nonReadMsgNumber > 0) {
          Badge(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.onError) {
                Text(text = conversation.nonReadMsgNumber.toString())
              }
        }
      }
}
