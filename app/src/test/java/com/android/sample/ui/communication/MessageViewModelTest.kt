package com.android.sample.ui.communication

import com.android.sample.model.authentication.FirebaseTestRule
import com.android.sample.model.communication.Conversation
import com.android.sample.model.communication.Message
import com.android.sample.model.communication.MessageRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class MessageViewModelTest {

  @get:Rule val firebaseRule = FirebaseTestRule()

  private val testDispatcher = StandardTestDispatcher()

  private val currentUserId = "user-1"
  private val otherUserId = "user-2"
  private val conversationId = "conv-123"

  private val sampleMessages =
      listOf(
          Message(
              messageId = "msg-1",
              conversationId = conversationId,
              sentFrom = currentUserId,
              sentTo = otherUserId,
              content = "Hello!",
              sentTime = Timestamp.now(),
              isRead = false),
          Message(
              messageId = "msg-2",
              conversationId = conversationId,
              sentFrom = otherUserId,
              sentTo = currentUserId,
              content = "Hi there!",
              sentTime = Timestamp.now(),
              isRead = true),
          Message(
              messageId = "msg-3",
              conversationId = conversationId,
              sentFrom = currentUserId,
              sentTo = otherUserId,
              content = "How are you?",
              sentTime = Timestamp.now(),
              isRead = false))

  private lateinit var fakeRepository: FakeMessageRepository
  private lateinit var viewModel: MessageViewModel

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    com.android.sample.model.authentication.UserSessionManager.setCurrentUserId(currentUserId)
    fakeRepository = FakeMessageRepository()
    viewModel =
        MessageViewModel(
            messageRepository = fakeRepository,
            conversationId = conversationId,
            otherUserId = otherUserId)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    com.android.sample.model.authentication.UserSessionManager.clearSession()
  }

  @Test
  fun initialState_isCorrect() = runTest {
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state)
    assertTrue(state.messages.isEmpty())
    assertEquals("", state.currentMessage)
    assertFalse(state.isLoading)
    assertNull(state.error)
  }

  @Test
  fun loadMessages_success_updatesState() = runTest {
    fakeRepository.setMessages(sampleMessages)

    // Create a new viewModel to trigger loadMessages in init
    val testViewModel =
        MessageViewModel(
            messageRepository = fakeRepository,
            conversationId = conversationId,
            otherUserId = otherUserId)
    advanceUntilIdle()

    val state = testViewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(3, state.messages.size)
    assertEquals("Hello!", state.messages[0].content)
    assertNull(state.error)
  }

  @Test
  fun loadMessages_failure_setsError() = runTest {
    fakeRepository.setShouldThrowError(true)

    // Create a new viewModel to trigger loadMessages in init
    val testViewModel =
        MessageViewModel(
            messageRepository = fakeRepository,
            conversationId = conversationId,
            otherUserId = otherUserId)
    advanceUntilIdle()

    val state = testViewModel.uiState.value
    assertFalse(state.isLoading)
    assertNotNull(state.error)
    assertTrue(state.error!!.contains("Failed to load messages"))
  }

  @Test
  fun onMessageChange_updatesCurrentMessage() = runTest {
    val newMessage = "Test message"

    viewModel.onMessageChange(newMessage)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(newMessage, state.currentMessage)
  }

  @Test
  fun sendMessage_success_clearsCurrentMessageAndRefreshes() = runTest {
    fakeRepository.setMessages(sampleMessages)
    viewModel.onMessageChange("New message")
    advanceUntilIdle()

    viewModel.sendMessage()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("", state.currentMessage)
    assertTrue(fakeRepository.sentMessages.isNotEmpty())
    assertEquals("New message", fakeRepository.sentMessages.last().content)
  }

  @Test
  fun sendMessage_emptyMessage_doesNotSend() = runTest {
    viewModel.onMessageChange("")
    advanceUntilIdle()

    viewModel.sendMessage()
    advanceUntilIdle()

    assertTrue(fakeRepository.sentMessages.isEmpty())
  }

  @Test
  fun sendMessage_whitespaceOnly_doesNotSend() = runTest {
    viewModel.onMessageChange("   ")
    advanceUntilIdle()

    viewModel.sendMessage()
    advanceUntilIdle()

    assertTrue(fakeRepository.sentMessages.isEmpty())
  }

  @Test
  fun sendMessage_failure_setsError() = runTest {
    fakeRepository.setShouldThrowError(true)
    viewModel.onMessageChange("Test message")
    advanceUntilIdle()

    viewModel.sendMessage()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.error)
    assertTrue(state.error!!.contains("Failed to send message"))
  }

  @Test
  fun sendMessage_createsCorrectMessageObject() = runTest {
    val messageContent = "Test message content"
    viewModel.onMessageChange(messageContent)
    advanceUntilIdle()

    viewModel.sendMessage()
    advanceUntilIdle()

    val sentMessage = fakeRepository.sentMessages.last()
    assertEquals(conversationId, sentMessage.conversationId)
    assertEquals(currentUserId, sentMessage.sentFrom)
    assertEquals(otherUserId, sentMessage.sentTo)
    assertEquals(messageContent, sentMessage.content)
  }

  @Test
  fun clearError_removesErrorMessage() = runTest {
    fakeRepository.setShouldThrowError(true)

    // Create a new viewModel to trigger loadMessages in init which will set error
    val testViewModel =
        MessageViewModel(
            messageRepository = fakeRepository,
            conversationId = conversationId,
            otherUserId = otherUserId)
    advanceUntilIdle()

    var state = testViewModel.uiState.value
    assertNotNull(state.error)

    testViewModel.clearError()
    advanceUntilIdle()

    state = testViewModel.uiState.value
    assertNull(state.error)
  }

  @Test
  fun messageViewModel_handlesEmptyConversation() = runTest {
    fakeRepository.setMessages(emptyList())

    // Create a new viewModel to trigger loadMessages in init
    val testViewModel =
        MessageViewModel(
            messageRepository = fakeRepository,
            conversationId = conversationId,
            otherUserId = otherUserId)
    advanceUntilIdle()

    val state = testViewModel.uiState.value
    assertTrue(state.messages.isEmpty())
    assertFalse(state.isLoading)
    assertNull(state.error)
  }

  // Fake Repository for testing
  private class FakeMessageRepository : MessageRepository {
    private var messages: List<Message> = emptyList()
    private var shouldThrowError = false
    val sentMessages = mutableListOf<Message>()

    fun setMessages(newMessages: List<Message>) {
      messages = newMessages
    }

    fun setShouldThrowError(value: Boolean) {
      shouldThrowError = value
    }

    override fun getNewUid() = "new-msg-id"

    override suspend fun getMessagesInConversation(conversationId: String): List<Message> {
      if (shouldThrowError) throw Exception("Test error")
      return messages.filter { it.conversationId == conversationId }
    }

    override suspend fun getMessage(messageId: String): Message? {
      return messages.find { it.messageId == messageId }
    }

    override suspend fun sendMessage(message: Message): String {
      if (shouldThrowError) throw Exception("Test error")
      sentMessages.add(message)
      return message.messageId
    }

    override suspend fun markMessageAsRead(messageId: String, readTime: Timestamp) {}

    override suspend fun deleteMessage(messageId: String) {}

    override suspend fun getUnreadMessagesInConversation(
        conversationId: String,
        userId: String
    ): List<Message> {
      return messages.filter { it.conversationId == conversationId && !it.isRead }
    }

    override suspend fun getConversationsForUser(userId: String): List<Conversation> = emptyList()

    override suspend fun getConversation(conversationId: String): Conversation? = null

    override suspend fun getOrCreateConversation(userId1: String, userId2: String): Conversation {
      return Conversation(
          conversationId = "new-conv",
          participant1Id = userId1,
          participant2Id = userId2,
          lastMessageContent = "",
          lastMessageTime = Timestamp.now(),
          lastMessageSenderId = userId1)
    }

    override suspend fun updateConversation(conversation: Conversation) {}

    override suspend fun markConversationAsRead(conversationId: String, userId: String) {}

    override suspend fun deleteConversation(conversationId: String) {}
  }
}
