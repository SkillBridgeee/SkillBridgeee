package com.android.sample.ui.communication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.sample.model.communication.conversation.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    viewModel: MessageViewModel,
    convId: String,
) {

  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(convId) { viewModel.loadConversation(convId) }

  DisposableEffect(Unit) { onDispose { viewModel.onScreenLeft() } }

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = { TopAppBar(title = { Text(uiState.partnerName ?: "Messages") }) },
      bottomBar = {
        MessageInput(
            message = uiState.currentMessage,
            onMessageChanged = viewModel::onMessageChange,
            onSendClicked = viewModel::sendMessage)
      }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          // Show error if present
          uiState.error?.let { error ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer) {
                  Row(
                      modifier = Modifier.fillMaxWidth().padding(8.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f))
                        if (error.contains("not authenticated", ignoreCase = true)) {
                          TextButton(onClick = { viewModel.retry() }) {
                            Text(text = "Retry", color = MaterialTheme.colorScheme.onErrorContainer)
                          }
                        }
                      }
                }
          }

          if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator()
            }
          } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                reverseLayout = true // Shows latest messages at the bottom
                ) {
                  items(uiState.messages.reversed()) { message ->
                    MessageBubble(
                        message = message,
                        isCurrentUser = message.senderId == uiState.currentUserId)
                  }
                }
          }
        }
      }
}

@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
  val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
  val backgroundColor =
      if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer
      else MaterialTheme.colorScheme.secondaryContainer
  val bubbleShape =
      if (isCurrentUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
      } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
      }

  Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = alignment) {
    Column(
        modifier =
            Modifier.background(backgroundColor, bubbleShape)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = 300.dp)) {
          Text(text = message.content, style = MaterialTheme.typography.bodyLarge)
          // Optionally, add a timestamp here
        }
  }
}

@Composable
fun MessageInput(message: String, onMessageChanged: (String) -> Unit, onSendClicked: () -> Unit) {
  Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 3.dp) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp).imePadding(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
              value = message,
              onValueChange = { newValue -> onMessageChanged(newValue) },
              modifier = Modifier.weight(1f),
              placeholder = { Text("Type a message...") },
              shape = RoundedCornerShape(24.dp),
              maxLines = 4,
              singleLine = false)
          IconButton(
              onClick = onSendClicked,
              enabled = message.isNotBlank(),
              modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message",
                    tint =
                        if (message.isNotBlank()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
              }
        }
  }
}
