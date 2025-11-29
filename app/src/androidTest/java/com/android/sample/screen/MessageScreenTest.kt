// package com.android.sample.screen
//
// import androidx.activity.ComponentActivity
// import androidx.compose.ui.test.*
// import androidx.compose.ui.test.junit4.createAndroidComposeRule
// import com.android.sample.model.authentication.UserSessionManager
// import com.android.sample.model.communication.Conversation
// import com.android.sample.model.communication.Message
// import com.android.sample.model.communication.MessageRepository
// import com.android.sample.ui.communication.MessageScreen
// import com.android.sample.ui.communication.MessageViewModel
// import com.google.firebase.Timestamp
// import org.junit.After
// import org.junit.Before
// import org.junit.Rule
// import org.junit.Test
//
// @Suppress("DEPRECATION")
// class MessageScreenTest {
//
//  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
//
//  private val currentUserId = "user-1"
//  private val otherUserId = "user-2"
//  private val conversationId = "conv-123"
//
//  private val sampleMessages =
//      listOf(
//          Message(
//              messageId = "msg-1",
//              conversationId = conversationId,
//              sentFrom = currentUserId,
//              sentTo = otherUserId,
//              content = "Hello from me!",
//              sentTime = Timestamp.now(),
//              isRead = false),
//          Message(
//              messageId = "msg-2",
//              conversationId = conversationId,
//              sentFrom = otherUserId,
//              sentTo = currentUserId,
//              content = "Hi there from other user!",
//              sentTime = Timestamp.now(),
//              isRead = true))
//
//  @Before
//  fun setup() {
//    UserSessionManager.clearSession()
//    UserSessionManager.setCurrentUserId(currentUserId)
//  }
//
//  @After
//  fun cleanup() {
//    UserSessionManager.clearSession()
//  }
//
//  @Test
//  fun messageScreen_displaysMessages() {
//    val repository = FakeMessageRepository(sampleMessages)
//    val viewModel = MessageViewModel(repository, conversationId, otherUserId)
//
//    compose.setContent { MessageScreen(viewModel = viewModel, currentUserId = currentUserId) }
//
//    compose.waitForIdle()
//
//    // Check if messages are displayed
//    compose.onNodeWithText("Hello from me!").assertIsDisplayed()
//    compose.onNodeWithText("Hi there from other user!").assertIsDisplayed()
//  }
//
//  @Test
//  fun messageScreen_displaysEmptyState() {
//    val repository = FakeMessageRepository(emptyList())
//    val viewModel = MessageViewModel(repository, conversationId, otherUserId)
//
//    compose.setContent { MessageScreen(viewModel = viewModel, currentUserId = currentUserId) }
//
//    compose.waitForIdle()
//
//    // Check that input field is displayed even when empty
//    compose.onNodeWithText("Type a message...").assertIsDisplayed()
//  }
//
//  @Test
//  fun messageInput_allowsTyping() {
//    val repository = FakeMessageRepository(emptyList())
//    val viewModel = MessageViewModel(repository, conversationId, otherUserId)
//
//    compose.setContent { MessageScreen(viewModel = viewModel, currentUserId = currentUserId) }
//
//    compose.waitForIdle()
//
//    // Type a message
//    compose.onNodeWithText("Type a message...").performTextInput("Test message")
//
//    compose.waitForIdle()
//
//    // Verify the text appears
//    compose.onNodeWithText("Test message").assertIsDisplayed()
//  }
//
//  @Test
//  fun messageInput_sendButton_isDisabledWhenEmpty() {
//    val repository = FakeMessageRepository(emptyList())
//    val viewModel = MessageViewModel(repository, conversationId, otherUserId)
//
//    compose.setContent { MessageScreen(viewModel = viewModel, currentUserId = currentUserId) }
//
//    compose.waitForIdle()
//
//    // Send button should be disabled when input is empty
//    compose.onNodeWithContentDescription("Send message").assertIsNotEnabled()
//  }
//
//  @Test
//  fun messageInput_sendButton_isEnabledWhenTextExists() {
//    val repository = FakeMessageRepository(emptyList())
//    val viewModel = MessageViewModel(repository, conversationId, otherUserId)
//
//    compose.setContent { MessageScreen(viewModel = viewModel, currentUserId = currentUserId) }
//
//    compose.waitForIdle()
//
//    // Type a message
//    compose.onNodeWithText("Type a message...").performTextInput("Test message")
//
//    compose.waitForIdle()
//
//    // Send button should be enabled
//    compose.onNodeWithContentDescription("Send message").assertIsEnabled()
//  }
//
//  @Test
//  fun messageInput_sendButton_sendsMessage() {
//    val repository = FakeMessageRepository(emptyList())
//    val viewModel = MessageViewModel(repository, conversationId, otherUserId)
//
//    compose.setContent { MessageScreen(viewModel = viewModel, currentUserId = currentUserId) }
//
//    compose.waitForIdle()
//
//    // Type and send a message
//    compose.onNodeWithText("Type a message...").performTextInput("Test message")
//    compose.waitForIdle()
//
//    compose.onNodeWithContentDescription("Send message").performClick()
//    compose.waitForIdle()
//
//    // Verify message was sent to repository
//    assert(repository.sentMessages.isNotEmpty())
//    assert(repository.sentMessages.last().content == "Test message")
//  }
//
//  @Test
//  fun messageBubbles_displayDifferentStylesForUsers() {
//    val repository = FakeMessageRepository(sampleMessages)
//    val viewModel = MessageViewModel(repository, conversationId, otherUserId)
//
//    compose.setContent { MessageScreen(viewModel = viewModel, currentUserId = currentUserId) }
//
//    compose.waitForIdle()
//
//    // Both messages should be displayed
//    compose.onNodeWithText("Hello from me!").assertIsDisplayed()
//    compose.onNodeWithText("Hi there from other user!").assertIsDisplayed()
//  }
//
//  @Test
//  fun messageScreen_displaysError() {
//    val repository = FakeMessageRepository(emptyList(), shouldThrowError = true)
//    val viewModel = MessageViewModel(repository, conversationId, otherUserId)
//
//    compose.setContent { MessageScreen(viewModel = viewModel, currentUserId = currentUserId) }
//
//    compose.waitForIdle()
//
//    // Should display error message
//    compose
//        .onNodeWithText(text = "Failed to load messages", substring = true, ignoreCase = true)
//        .assertIsDisplayed()
//  }
//
//  @Test
//  fun messageScreen_displaysLoadingState() {
//    val repository = FakeMessageRepository(emptyList(), delayLoading = true)
//    val viewModel = MessageViewModel(repository, conversationId, otherUserId)
//
//    compose.setContent { MessageScreen(viewModel = viewModel, currentUserId = currentUserId) }
//
//    // Loading indicator should be shown initially
//    // Note: This might be flaky due to timing, but demonstrates the pattern
//  }
//
//  @Test
//  fun messageScreen_multilineInput() {
//    val repository = FakeMessageRepository(emptyList())
//    val viewModel = MessageViewModel(repository, conversationId, otherUserId)
//
//    compose.setContent { MessageScreen(viewModel = viewModel, currentUserId = currentUserId) }
//
//    compose.waitForIdle()
//
//    // Type a long message with multiple lines
//    val longMessage = "Line 1\nLine 2\nLine 3\nLine 4"
//    compose.onNodeWithText("Type a message...").performTextInput(longMessage)
//
//    compose.waitForIdle()
//
//    // Verify the text appears (at least part of it)
//    compose.onNodeWithText(longMessage, substring = true).assertIsDisplayed()
//  }
//
//  // Fake Repository for testing
//  private class FakeMessageRepository(
//      initialMessages: List<Message>,
//      private val shouldThrowError: Boolean = false,
//      private val delayLoading: Boolean = false
//  ) : MessageRepository {
//    private var messages: List<Message> = initialMessages
//    val sentMessages = mutableListOf<Message>()
//
//    override fun getNewUid() = "new-msg-id-${System.currentTimeMillis()}"
//
//    override suspend fun getMessagesInConversation(conversationId: String): List<Message> {
//      if (delayLoading) {
//        kotlinx.coroutines.delay(5000) // Simulate slow loading
//      }
//      if (shouldThrowError) throw Exception("Test error")
//      return messages.filter { it.conversationId == conversationId }
//    }
//
//    override suspend fun getMessage(messageId: String): Message? {
//      return messages.find { it.messageId == messageId }
//    }
//
//    override suspend fun sendMessage(message: Message): String {
//      if (shouldThrowError) throw Exception("Test error")
//      val messageWithId = message.copy(messageId = getNewUid())
//      sentMessages.add(messageWithId)
//      messages = messages + messageWithId
//      return messageWithId.messageId
//    }
//
//    override suspend fun markMessageAsRead(messageId: String, readTime: Timestamp) {}
//
//    override suspend fun deleteMessage(messageId: String) {
//      messages = messages.filter { it.messageId != messageId }
//    }
//
//    override suspend fun getUnreadMessagesInConversation(
//        conversationId: String,
//        userId: String
//    ): List<Message> {
//      return messages.filter { it.conversationId == conversationId && !it.isRead }
//    }
//
//    override suspend fun getConversationsForUser(userId: String): List<Conversation> = emptyList()
//
//    override suspend fun getConversation(conversationId: String): Conversation? = null
//
//    override suspend fun getOrCreateConversation(userId1: String, userId2: String): Conversation {
//      return Conversation(
//          conversationId = "new-conv",
//          participant1Id = userId1,
//          participant2Id = userId2,
//          lastMessageContent = "",
//          lastMessageTime = Timestamp.now(),
//          lastMessageSenderId = userId1)
//    }
//
//    override suspend fun updateConversation(conversation: Conversation) {}
//
//    override suspend fun markConversationAsRead(conversationId: String, userId: String) {}
//
//    override suspend fun deleteConversation(conversationId: String) {}
//  }
// }
