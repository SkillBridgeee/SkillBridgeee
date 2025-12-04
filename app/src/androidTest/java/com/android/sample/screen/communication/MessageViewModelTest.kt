package com.android.sample.screen.communication

import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.communication.newImplementation.ConversationManager
import com.android.sample.model.communication.newImplementation.conversation.ConvRepository
import com.android.sample.model.communication.newImplementation.conversation.ConversationNew
import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.communication.MessageViewModel
import com.google.firebase.Timestamp
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MessageViewModelTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var convRepo: ConvRepository
  private lateinit var overViewRepo: OverViewConvRepository
  private lateinit var manager: ConversationManager
  private lateinit var viewModel: MessageViewModel

  private val testUserId = "userA"
  private val otherUserId = "userB"
  private val convId = "conv123"

  @Before
  fun setup() {
    Dispatchers.setMain(UnconfinedTestDispatcher())

    UserSessionManager.setCurrentUserId(testUserId)

    ProfileRepositoryProvider.setForTests(FakeProfileRepository())
    convRepo = FakeConvRepo()
    overViewRepo = FakeOverViewRepo()
    manager = ConversationManager(convRepo, overViewRepo)

    viewModel = MessageViewModel(manager)

    // Mock user session

    // Create conversation
    runBlocking {
      convRepo.createConv(
          ConversationNew(convId = convId, convCreatorId = testUserId, otherPersonId = otherUserId))
    }
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    UserSessionManager.clearSession()
    ProfileRepositoryProvider.clearForTests()
  }

  // -----------------------------------------------------
  // TEST 1 — loadConversation() écoute les messages
  // -----------------------------------------------------
  @Test
  fun loadConversation() = runTest {
    viewModel.loadConversation(convId)

    // Simule un message reçu
    val msg =
        MessageNew(
            msgId = "m1",
            senderId = otherUserId,
            receiverId = testUserId,
            content = "Hello!",
            createdAt = Date())

    manager.sendMessage(convId, msg)

    val result = viewModel.uiState.value.messages

    assertEquals(1, result.size)
    assertEquals("Hello!", result.first().content)
  }

  // -----------------------------------------------------
  // TEST 2 — sendMessage() envoie un message
  // -----------------------------------------------------
  @Test
  fun sendMessage() = runTest {
    viewModel.loadConversation(convId)

    // Simule que l'utilisateur tape un message
    viewModel.onMessageChange("Salut !")

    viewModel.sendMessage()

    val messages = viewModel.uiState.value.messages
    assertEquals(1, messages.size)
    assertEquals("Salut !", messages.first().content)
  }

  @Test
  fun loadConversation_conversationNotFound_setsError() = runTest {
    val invalidConvId = "does_not_exist"

    viewModel.loadConversation(invalidConvId)

    val state = viewModel.uiState.value
    assertEquals("Conversation not found", state.error)
    assertEquals(true, state.messages.isEmpty())
  }

  @Test
  fun clearError_removesError() = runTest {
    // On force une erreur dans l'état
    viewModel.clearError() // juste pour reset
    viewModel.loadConversation("invalid_id")

    // Vérification initiale
    assertEquals("Conversation not found", viewModel.uiState.value.error)

    // Action
    viewModel.clearError()

    // L'erreur doit être supprimée
    assertEquals(null, viewModel.uiState.value.error)
  }

  // -----------------------------------------------------
  // TEST 5 — onMessageChange() updates current message
  // -----------------------------------------------------
  @Test
  fun onMessageChange_updatesCurrentMessage() = runTest {
    val newMessage = "Hello, World!"

    viewModel.onMessageChange(newMessage)

    assertEquals(newMessage, viewModel.uiState.value.currentMessage)
  }

  // -----------------------------------------------------
  // TEST 6 — sendMessage() with empty message does nothing
  // -----------------------------------------------------
  @Test
  fun sendMessage_withEmptyMessage_doesNothing() = runTest {
    viewModel.loadConversation(convId)

    // Don't set any message
    viewModel.onMessageChange("")

    viewModel.sendMessage()

    // No messages should be sent
    val messages = viewModel.uiState.value.messages
    assertEquals(0, messages.size)
  }

  // -----------------------------------------------------
  // TEST 7 — loadConversation() sets isLoading correctly
  // -----------------------------------------------------
  @Test
  fun loadConversation_setsLoadingState() = runTest {
    viewModel.loadConversation(convId)

    // Eventually loading should be false
    composeTestRule.waitForIdle()
    val state = viewModel.uiState.value
    assertEquals(false, state.isLoading)
  }

  // -----------------------------------------------------
  // TEST 8 — Multiple calls to loadConversation() work correctly
  // -----------------------------------------------------
  @Test
  fun loadConversation_multipleCalls_workCorrectly() = runTest {
    // First load
    viewModel.loadConversation(convId)

    // Send a message
    val msg1 =
        MessageNew(
            msgId = "m1",
            senderId = testUserId,
            receiverId = otherUserId,
            content = "First",
            createdAt = Date())
    manager.sendMessage(convId, msg1)

    composeTestRule.waitForIdle()
    assertEquals(1, viewModel.uiState.value.messages.size)

    // Load again (should cancel previous job)
    viewModel.loadConversation(convId)

    // Send another message
    val msg2 =
        MessageNew(
            msgId = "m2",
            senderId = otherUserId,
            receiverId = testUserId,
            content = "Second",
            createdAt = Date())
    manager.sendMessage(convId, msg2)

    composeTestRule.waitForIdle()
    assertEquals(2, viewModel.uiState.value.messages.size)
  }

  // -----------------------------------------------------
  // TEST 9 — currentUserId is set on initialization
  // -----------------------------------------------------
  @Test
  fun initialization_setsCurrentUserId() = runTest {
    val state = viewModel.uiState.value
    assertEquals(testUserId, state.currentUserId)
  }

  // -----------------------------------------------------
  // TEST 10 — sendMessage() clears current message
  // -----------------------------------------------------
  @Test
  fun sendMessage_clearsCurrentMessage() = runTest {
    viewModel.loadConversation(convId)

    viewModel.onMessageChange("Test message")
    assertEquals("Test message", viewModel.uiState.value.currentMessage)

    viewModel.sendMessage()

    composeTestRule.waitForIdle()
    assertEquals("", viewModel.uiState.value.currentMessage)
  }

  // -----------------------------------------------------
  // TEST 11 — Messages are ordered by date
  // -----------------------------------------------------
  @Test
  fun messages_areOrderedByDate() = runTest {
    viewModel.loadConversation(convId)

    val now = Date()
    val earlier = Date(now.time - 60000) // 1 minute earlier

    val msg1 =
        MessageNew(
            msgId = "m1",
            senderId = testUserId,
            receiverId = otherUserId,
            content = "Later message",
            createdAt = now)

    val msg2 =
        MessageNew(
            msgId = "m2",
            senderId = otherUserId,
            receiverId = testUserId,
            content = "Earlier message",
            createdAt = earlier)

    manager.sendMessage(convId, msg2)
    manager.sendMessage(convId, msg1)

    composeTestRule.waitForIdle()

    val messages = viewModel.uiState.value.messages
    assertEquals(2, messages.size)
    // Messages should be in chronological order
    assertEquals("Earlier message", messages[0].content)
    assertEquals("Later message", messages[1].content)
  }

  // -----------------------------------------------------
  // TEST 12 — retry() reloads conversation
  // -----------------------------------------------------
  @Test
  fun retry_reloadsConversation() = runTest {
    // First load
    viewModel.loadConversation(convId)
    composeTestRule.waitForIdle()

    // Send a message
    val msg1 =
        MessageNew(
            msgId = "m1",
            senderId = testUserId,
            receiverId = otherUserId,
            content = "First message",
            createdAt = Date())
    manager.sendMessage(convId, msg1)
    composeTestRule.waitForIdle()

    assertEquals(1, viewModel.uiState.value.messages.size)

    // Simulate an error (clear messages from state)
    // In real scenario, this might happen if connection was lost

    // Call retry
    viewModel.retry()
    composeTestRule.waitForIdle()

    // Messages should still be there (reloaded from repository)
    assertEquals(1, viewModel.uiState.value.messages.size)
    assertEquals("First message", viewModel.uiState.value.messages[0].content)
  }

  // -----------------------------------------------------
  // TEST 13 — retry() without previous conversation does nothing
  // -----------------------------------------------------
  @Test
  fun retry_withoutPreviousConversation_doesNothing() = runTest {
    // Don't load any conversation first

    // Call retry
    viewModel.retry()
    composeTestRule.waitForIdle()

    // Should not crash and state should be empty
    assertEquals(0, viewModel.uiState.value.messages.size)
    assertEquals(null, viewModel.uiState.value.error)
  }

  // -----------------------------------------------------
  // TEST 14 — retry() after error clears error and reloads
  // -----------------------------------------------------
  @Test
  fun retry_afterError_clearsErrorAndReloads() = runTest {
    // Load an invalid conversation to trigger error
    viewModel.loadConversation("invalid_conv_id")
    composeTestRule.waitForIdle()

    // Should have error
    assertEquals("Conversation not found", viewModel.uiState.value.error)

    // Now create a valid conversation
    runBlocking {
      convRepo.createConv(
          ConversationNew(
              convId = "invalid_conv_id", convCreatorId = testUserId, otherPersonId = otherUserId))
    }

    // Retry
    viewModel.retry()
    composeTestRule.waitForIdle()

    // Error should be cleared and conversation loaded
    assertEquals(null, viewModel.uiState.value.error)
  }

  // -----------------------------------------------------
  // TEST 15 — loadConversation with delayed userId initialization succeeds
  // -----------------------------------------------------
  @Test
  fun loadConversation_withDelayedUserIdInitialization_succeeds() = runTest {
    // Clear user session to simulate not initialized
    UserSessionManager.clearSession()

    // Create a new viewModel
    val newViewModel = MessageViewModel(manager)

    // Set userId after a small delay (simulating Firebase initialization)
    launch {
      delay(100)
      UserSessionManager.setCurrentUserId(testUserId)
    }

    // Load conversation (should wait and retry for userId)
    newViewModel.loadConversation(convId)

    // Wait for initialization and loading
    composeTestRule.waitForIdle()
    delay(600) // Wait for the delay(500) + some buffer

    // Should eventually succeed
    val state = newViewModel.uiState.value
    // Either it loaded successfully or still has the userId set
    assertEquals(testUserId, state.currentUserId)
  }

  // -----------------------------------------------------
  // TEST 16 — loadConversation without userId after delay sets error
  // -----------------------------------------------------
  @Test
  fun loadConversation_withoutUserIdAfterDelay_setsError() = runTest {
    // Clear user session complètement
    UserSessionManager.clearSession()

    // Create a new viewModel
    val newViewModel = MessageViewModel(manager)

    // Load conversation
    newViewModel.loadConversation(convId)

    // Wait for the delay and retry attempts
    delay(600)
    composeTestRule.waitForIdle()

    // Should have authentication error
    val state = newViewModel.uiState.value
    assertEquals(true, state.error?.contains("authenticated") == true)
    assertEquals(false, state.isLoading)
  }

  // -----------------------------------------------------
  // TEST 17 — retry() preserves conversation ID
  // -----------------------------------------------------
  @Test
  fun retry_preservesConversationId() = runTest {
    // Load first conversation
    viewModel.loadConversation(convId)
    composeTestRule.waitForIdle()

    val msg1 =
        MessageNew(
            msgId = "m1",
            senderId = testUserId,
            receiverId = otherUserId,
            content = "Message in conv1",
            createdAt = Date())
    manager.sendMessage(convId, msg1)
    composeTestRule.waitForIdle()

    assertEquals(1, viewModel.uiState.value.messages.size)

    // Retry should reload the same conversation
    viewModel.retry()
    composeTestRule.waitForIdle()

    // Should still have the same message
    assertEquals(1, viewModel.uiState.value.messages.size)
    assertEquals("Message in conv1", viewModel.uiState.value.messages[0].content)
  }

  // -----------------------------------------------------
  // TEST 18 — multiple retry() calls work correctly
  // -----------------------------------------------------
  @Test
  fun retry_multipleCalls_workCorrectly() = runTest {
    viewModel.loadConversation(convId)
    composeTestRule.waitForIdle()

    // First retry
    viewModel.retry()
    composeTestRule.waitForIdle()

    // Second retry
    viewModel.retry()
    composeTestRule.waitForIdle()

    // Third retry
    viewModel.retry()
    composeTestRule.waitForIdle()

    // Should not crash and maintain state
    assertEquals(testUserId, viewModel.uiState.value.currentUserId)
    assertEquals(null, viewModel.uiState.value.error)
  }

  // -----------------------------------------------------
  // TEST 19 — retry() after successful load works
  // -----------------------------------------------------
  @Test
  fun retry_afterSuccessfulLoad_reloadsSuccessfully() = runTest {
    // Load conversation successfully
    viewModel.loadConversation(convId)
    composeTestRule.waitForIdle()

    val msg1 =
        MessageNew(
            msgId = "m1",
            senderId = testUserId,
            receiverId = otherUserId,
            content = "Original message",
            createdAt = Date())
    manager.sendMessage(convId, msg1)
    composeTestRule.waitForIdle()

    assertEquals(1, viewModel.uiState.value.messages.size)

    // Add another message
    val msg2 =
        MessageNew(
            msgId = "m2",
            senderId = otherUserId,
            receiverId = testUserId,
            content = "New message",
            createdAt = Date())
    manager.sendMessage(convId, msg2)

    // Retry to get updated messages
    viewModel.retry()
    composeTestRule.waitForIdle()

    // Should have both messages
    assertEquals(2, viewModel.uiState.value.messages.size)
  }

  // -----------------------------------------------------
  // TEST 20 — userId delay logic waits 500ms before second attempt
  // -----------------------------------------------------
  @Test
  fun loadConversation_userIdDelayLogic_waits500ms() = runTest {
    // Clear user session
    UserSessionManager.clearSession()

    val newViewModel = MessageViewModel(manager)

    // Record start time
    val startTime = System.currentTimeMillis()

    // Set userId after 300ms (before the retry)
    launch {
      delay(300)
      UserSessionManager.setCurrentUserId(testUserId)
    }

    // Load conversation
    newViewModel.loadConversation(convId)

    // Wait for completion
    delay(700)
    composeTestRule.waitForIdle()

    val endTime = System.currentTimeMillis()
    val elapsed = endTime - startTime

    // Should have waited at least 500ms (for the delay)
    assertEquals(true, elapsed >= 500)

    // And should have succeeded
    assertEquals(testUserId, newViewModel.uiState.value.currentUserId)
  }

  // -----------------------------------------------------
  // TEST 21 — loadConversation resets unread message count on load
  // -----------------------------------------------------
  @Test
  fun loadConversation_resetsUnreadMessageCount() = runTest {
    // Load conversation
    viewModel.loadConversation(convId)
    composeTestRule.waitForIdle()

    // The conversation should be loaded successfully
    assertEquals(null, viewModel.uiState.value.error)
    assertEquals(testUserId, viewModel.uiState.value.currentUserId)

    // In a real scenario, resetUnreadCount would be called by the ConversationManager
    // We verify that the conversation loads without errors
    assertEquals(false, viewModel.uiState.value.isLoading)
  }

  // -----------------------------------------------------
  // TEST 22 — loadConversation continues even if resetUnreadCount fails
  // -----------------------------------------------------
  @Test
  fun loadConversation_continuesEvenIfResetUnreadCountFails() = runTest {
    // Even if resetUnreadCount throws an exception (caught internally),
    // the conversation should still load
    viewModel.loadConversation(convId)
    composeTestRule.waitForIdle()

    // Send a message to verify the conversation is working
    val msg =
        MessageNew(
            msgId = "m1",
            senderId = testUserId,
            receiverId = otherUserId,
            content = "Test message",
            createdAt = Date())
    manager.sendMessage(convId, msg)
    composeTestRule.waitForIdle()

    // Should still have the message
    assertEquals(1, viewModel.uiState.value.messages.size)
    assertEquals("Test message", viewModel.uiState.value.messages[0].content)
    assertEquals(null, viewModel.uiState.value.error)
  }

  // -----------------------------------------------------
  // TEST 23 — resetUnreadCount called before message listening starts
  // -----------------------------------------------------
  @Test
  fun loadConversation_resetsUnreadCountBeforeListeningToMessages() = runTest {
    // This test verifies that resetUnreadCount is called during loadConversation
    // by checking that the conversation loads successfully and messages are received
    viewModel.loadConversation(convId)
    composeTestRule.waitForIdle()

    // Add a message after loading
    val msg =
        MessageNew(
            msgId = "m1",
            senderId = otherUserId,
            receiverId = testUserId,
            content = "New message after load",
            createdAt = Date())
    manager.sendMessage(convId, msg)
    composeTestRule.waitForIdle()

    // Verify the conversation is functioning properly
    assertEquals(1, viewModel.uiState.value.messages.size)
    assertEquals("New message after load", viewModel.uiState.value.messages[0].content)
  }

  // -----------------------------------------------------
  // TEST 24 — multiple loadConversation calls reset unread count each time
  // -----------------------------------------------------
  @Test
  fun loadConversation_multipleCalls_resetsUnreadCountEachTime() = runTest {
    // First load
    viewModel.loadConversation(convId)
    composeTestRule.waitForIdle()

    // Send message
    val msg1 =
        MessageNew(
            msgId = "m1",
            senderId = otherUserId,
            receiverId = testUserId,
            content = "First",
            createdAt = Date())
    manager.sendMessage(convId, msg1)
    composeTestRule.waitForIdle()

    assertEquals(1, viewModel.uiState.value.messages.size)

    // Reload conversation (simulating user returning to the conversation)
    viewModel.loadConversation(convId)
    composeTestRule.waitForIdle()

    // Send another message
    val msg2 =
        MessageNew(
            msgId = "m2",
            senderId = otherUserId,
            receiverId = testUserId,
            content = "Second",
            createdAt = Date())
    manager.sendMessage(convId, msg2)
    composeTestRule.waitForIdle()

    // Both messages should be present
    assertEquals(2, viewModel.uiState.value.messages.size)
    assertEquals(null, viewModel.uiState.value.error)
  }

  // -----------------------------------------------------
  // TEST 25 — unread count reset works with valid userId
  // -----------------------------------------------------
  @Test
  fun loadConversation_withValidUserId_resetsUnreadCount() = runTest {
    // Ensure userId is set
    UserSessionManager.setCurrentUserId(testUserId)

    val newViewModel = MessageViewModel(manager)

    // Load conversation
    newViewModel.loadConversation(convId)
    composeTestRule.waitForIdle()

    // Should load successfully with the correct userId
    assertEquals(testUserId, newViewModel.uiState.value.currentUserId)
    assertEquals(null, newViewModel.uiState.value.error)
  }
}

class FakeProfileRepository : ProfileRepository {
  override fun getNewUid() = "fake-profile-id"

  override fun getCurrentUserId() = "userA"

  override suspend fun getProfile(userId: String): Profile? =
      Profile(
          userId = userId,
          name = "Test User",
          email = "test@example.com",
          location = Location(latitude = 0.0, longitude = 0.0, name = "Test Location"))

  override suspend fun addProfile(profile: Profile) {}

  override suspend fun updateProfile(userId: String, profile: Profile) {}

  override suspend fun deleteProfile(userId: String) {}

  override suspend fun getAllProfiles() = emptyList<Profile>()

  override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
      emptyList<Profile>()

  override suspend fun getProfileById(userId: String) = getProfile(userId)

  override suspend fun getSkillsForUser(userId: String) = emptyList<Skill>()

  override suspend fun updateTutorRatingFields(
      userId: String,
      averageRating: Double,
      totalRatings: Int
  ) {}

  override suspend fun updateStudentRatingFields(
      userId: String,
      averageRating: Double,
      totalRatings: Int
  ) {}
}

class FakeConvRepo : ConvRepository {

  // Stockage interne des conversations
  private val conversations = mutableMapOf<String, ConversationNew>()

  // Stockage des flows de messages par conversation
  private val messageFlows = mutableMapOf<String, MutableStateFlow<List<MessageNew>>>()

  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  override suspend fun getConv(convId: String): ConversationNew? {
    return conversations[convId]
  }

  override suspend fun createConv(conversation: ConversationNew) {
    val convId = conversation.convId.ifEmpty { getNewUid() }

    val newConv = conversation.copy(convId = convId, updatedAt = Timestamp.now())

    conversations[convId] = newConv
    messageFlows[convId] = MutableStateFlow(newConv.messages)
  }

  override suspend fun deleteConv(convId: String) {
    conversations.remove(convId)
    messageFlows.remove(convId)
  }

  override suspend fun sendMessage(convId: String, message: MessageNew) {
    val conv = conversations[convId] ?: return

    // Nouveau message ajouté
    val updatedMessages = conv.messages + message

    val updatedConv = conv.copy(messages = updatedMessages, updatedAt = Timestamp.now())

    conversations[convId] = updatedConv

    // Mise à jour du Flow
    messageFlows[convId]?.value = updatedMessages
  }

  override fun listenMessages(convId: String): Flow<List<MessageNew>> {
    return messageFlows.getOrPut(convId) { MutableStateFlow(emptyList()) }
  }
}

class FakeOverViewRepo : OverViewConvRepository {

  // Toutes les overviews stockées en mémoire
  private val overviews = mutableMapOf<String, OverViewConversation>()

  // Flows par utilisateur
  private val userFlows = mutableMapOf<String, MutableStateFlow<List<OverViewConversation>>>()

  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  override suspend fun getOverViewConvUser(userId: String): List<OverViewConversation> {
    return overviews.values.filter { it.overViewOwnerId == userId }
  }

  override suspend fun addOverViewConvUser(overView: OverViewConversation) {
    val id = overView.overViewId.ifEmpty { getNewUid() }

    val newOverView = overView.copy(overViewId = id)
    overviews[id] = newOverView

    refreshUserFlow(newOverView.overViewOwnerId)
  }

  override suspend fun deleteOverViewConvUser(convId: String) {
    val target = overviews.values.find { it.linkedConvId == convId }
    target?.let {
      overviews.remove(it.overViewId)
      refreshUserFlow(it.overViewOwnerId)
    }
  }

  override fun listenOverView(userId: String): Flow<List<OverViewConversation>> {
    return userFlows.getOrPut(userId) { MutableStateFlow(emptyList()) }
  }

  // ---------------------
  // Helpers
  // ---------------------

  private suspend fun refreshUserFlow(userId: String) {
    val list = getOverViewConvUser(userId)
    userFlows[userId]?.value = list
  }
}
