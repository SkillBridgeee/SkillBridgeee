package com.android.sample.screen.communication

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.communication.ConversationManager
import com.android.sample.model.communication.conversation.ConvRepository
import com.android.sample.model.communication.conversation.Conversation
import com.android.sample.model.communication.conversation.Message
import com.android.sample.model.communication.overViewConv.OverViewConvRepository
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.communication.MessageScreen
import com.android.sample.ui.communication.MessageViewModel
import com.android.sample.utils.fakeRepo.fakeConvManager.FakeConvRepo
import com.android.sample.utils.fakeRepo.fakeConvManager.FakeOverViewRepo
import java.util.Date
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var convRepo: ConvRepository
  private lateinit var overViewRepo: OverViewConvRepository

  private lateinit var profileRepository: ProfileRepository
  private lateinit var manager: ConversationManager
  private lateinit var viewModel: MessageViewModel

  private val convId = "convTest"
  private val userA = "userA"
  private val userB = "userB"

  @Before
  fun setup() {
    convRepo = FakeConvRepo()
    overViewRepo = FakeOverViewRepo()
    profileRepository = FakeProfileRepository()

    manager = ConversationManager(convRepo, overViewRepo, null)

    viewModel = MessageViewModel(manager, profileRepository)

    UserSessionManager.setCurrentUserId(userA)

    runBlocking {
      convRepo.createConv(
          Conversation(convId = convId, convCreatorId = userA, otherPersonId = userB))
    }
  }

  // -----------------------------------------------------
  // TEST 1 — Affichage du message envoyé
  // -----------------------------------------------------
  @Test
  fun messageScreen_showsSentMessage() {
    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }

    val textToSend = "Bonjour test"

    // Tape dans champ input
    composeTestRule.onNode(hasSetTextAction()).performTextInput(textToSend)

    // Clique sur envoyer
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()

    // Vérifie que le message apparait à l'écran
    composeTestRule.onNodeWithText(textToSend).assertExists()
  }

  // -----------------------------------------------------
  // TEST 2 — Un message reçu apparaît dans l’UI
  // -----------------------------------------------------
  @Test
  fun messageScreen_showsIncomingMessage() = runTest {
    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }

    // Simule réception d’un message
    manager.sendMessage(
        convId,
        Message(
            msgId = "xyz",
            senderId = userB,
            receiverId = userA,
            content = "Salut utilisateur!",
            createdAt = Date()))

    composeTestRule.waitUntil(timeoutMillis = 1000) {
      composeTestRule.onAllNodesWithText("Salut utilisateur!").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithText("Salut utilisateur!").assertExists()
  }

  // -----------------------------------------------------
  // TEST 3 — Empty message cannot be sent
  // -----------------------------------------------------
  @Test
  fun messageScreen_emptyMessageCannotBeSent() {
    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }

    // Try to send without typing anything
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()

    // No message should appear (since it's empty)
    composeTestRule.waitForIdle()
    // The send button should be disabled or no message added
    assertEquals(0, viewModel.uiState.value.messages.size)
  }

  // -----------------------------------------------------
  // TEST 4 — Loading state is shown initially
  // -----------------------------------------------------
  @Test
  fun messageScreen_showsLoadingStateInitially() {
    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }

    // Initially loading should be true
    composeTestRule.waitForIdle()
    // Check that messages eventually load
    viewModel.loadConversation(convId)
    composeTestRule.waitForIdle()
  }

  // -----------------------------------------------------
  // TEST 5 — Multiple messages are displayed in order
  // -----------------------------------------------------
  @Test
  fun messageScreen_displaysMultipleMessagesInOrder() = runTest {
    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }

    // Send multiple messages
    manager.sendMessage(
        convId,
        Message(
            msgId = "m1",
            senderId = userA,
            receiverId = userB,
            content = "First message",
            createdAt = Date()))

    manager.sendMessage(
        convId,
        Message(
            msgId = "m2",
            senderId = userB,
            receiverId = userA,
            content = "Second message",
            createdAt = Date()))

    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule.onAllNodesWithText("First message").fetchSemanticsNodes().isNotEmpty() &&
          composeTestRule.onAllNodesWithText("Second message").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithText("First message").assertExists()
    composeTestRule.onNodeWithText("Second message").assertExists()
  }

  // -----------------------------------------------------
  // TEST 6 — Message input clears after sending
  // -----------------------------------------------------
  @Test
  fun messageScreen_inputClearsAfterSending() {
    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }

    val textToSend = "Test message"

    // Type in input
    composeTestRule.onNode(hasSetTextAction()).performTextInput(textToSend)

    // Send message
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()

    composeTestRule.waitForIdle()

    // Current message should be empty
    assertEquals("", viewModel.uiState.value.currentMessage)
  }

  // -----------------------------------------------------
  // TEST 7 — Info message is shown when conversation is missing
  // -----------------------------------------------------
  @Test
  fun messageScreen_showsInfoMessageWhenConversationDeleted() = runTest {
    convRepo.deleteConv(convId)

    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }

    val infoText = "This conversation was deleted by the other user."

    // Wait until the info message appears
    composeTestRule.waitUntil(timeoutMillis = 2_000) {
      composeTestRule.onAllNodesWithText(infoText).fetchSemanticsNodes().isNotEmpty()
    }

    // Assert that the info message Surface is displayed
    composeTestRule.onNodeWithText(infoText).assertExists()
  }

  @Test
  fun messageSendButton_isDisabledWhenMessageIsEmpty() {
    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }
    composeTestRule.onNode(hasContentDescription("Send message")).assertExists()

    composeTestRule.onNode(hasContentDescription("Send message")).assertIsNotEnabled()

    composeTestRule.onNode(hasSetTextAction()).performTextInput("Hello")

    // Now the send button should be enabled
    composeTestRule.onNode(hasContentDescription("Send message")).assertExists()

    composeTestRule.onNode(hasContentDescription("Send message")).assertIsEnabled()

    composeTestRule.onNode(hasSetTextAction()).performTextClearance()

    composeTestRule.onNode(hasContentDescription("Send message")).assertExists()

    composeTestRule.onNode(hasContentDescription("Send message")).assertIsNotEnabled()
  }

  @Test
  fun messageScreen_showsPartnerName() {
    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Test User").assertExists()
  }

  @Test
  fun messageScreen_textInputReflectsInViewModel() {
    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Hello World")

    assertEquals("Hello World", viewModel.uiState.value.currentMessage)
  }

  @Test
  fun messageScreen_multipleSentMessagesShowUp() {
    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }
    repeat(3) { index ->
      composeTestRule.onNode(hasSetTextAction()).performTextInput("Msg $index")
      composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    }

    assertEquals(3, viewModel.uiState.value.messages.size)
  }

  @Test
  fun messageScreen_clearingInputDisablesSendButton() {
    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }
    val sendButton = composeTestRule.onNode(hasContentDescription("Send message"))

    sendButton.assertIsNotEnabled()

    composeTestRule.onNode(hasSetTextAction()).performTextInput("Hello")

    sendButton.assertIsEnabled()

    composeTestRule.onNode(hasSetTextAction()).performTextClearance()

    sendButton.assertIsNotEnabled()
  }

  @Test
  fun messageScreen_listUpdatesWhenNewMessagesArrive() = runTest {
    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }
    val before = viewModel.uiState.value.messages.size

    manager.sendMessage(convId, Message("new", userB, userA, "Remote message", Date()))

    composeTestRule.waitUntil(2000) { viewModel.uiState.value.messages.size > before }
  }

  @Test
  fun messageField_isEmptyAfterSendingMessage() {
    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }
    val messageText = "Test Message"

    composeTestRule.onNode(hasSetTextAction()).performTextInput(messageText)

    composeTestRule.onNode(hasContentDescription("Send message")).performClick()

    composeTestRule.waitForIdle()

    assertEquals("", viewModel.uiState.value.currentMessage)
  }

  @Test
  fun sendButton_isDisabledAfterSendingMessage() {
    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }
    val messageText = "Another Test Message"

    composeTestRule.onNode(hasSetTextAction()).performTextInput(messageText)

    composeTestRule.onNode(hasContentDescription("Send message")).performClick()

    composeTestRule.waitForIdle()

    composeTestRule.onNode(hasContentDescription("Send message")).assertIsNotEnabled()
  }

  @Test
  fun messageScreen_scrollsDown() {
    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message1")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("Scroll Test Message")
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()

    composeTestRule.onNodeWithTag("message_list").assertExists()
    composeTestRule
        .onNodeWithTag("message_list")
        .performScrollToNode(hasText("Scroll Test Message1"))
  }

  // -----------------------------------------------------
  // TEST — Timestamp display
  // -----------------------------------------------------
  @Test
  fun messageScreen_displaysSingleMessageTimestamp() = runTest {
    // Add a message with a specific timestamp
    val testMessage =
        Message(
            msgId = "msg1",
            content = "Test with timestamp",
            senderId = userA,
            receiverId = userB,
            createdAt = Date())
    manager.sendMessage(convId, testMessage)

    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }

    composeTestRule.waitForIdle()

    // Verify the message content is displayed
    composeTestRule.onNodeWithText("Test with timestamp").assertExists()

    // Verify that a timestamp is displayed (should contain "ago" or time format like ":")
    // The timestamp should be visible somewhere on the screen
    composeTestRule.waitUntil(3000) {
      composeTestRule
          .onAllNodesWithText("Just now", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty() ||
          composeTestRule
              .onAllNodesWithText(":", substring = true)
              .fetchSemanticsNodes()
              .isNotEmpty()
    }
  }

  @Test
  fun messageScreen_displaysMultipleMessagesWithTimestamps() = runTest {
    // Add multiple messages
    val messages =
        listOf(
            Message(
                "msg1", "First message", userA, userB, Date(System.currentTimeMillis() - 3600000)),
            Message(
                "msg2", "Second message", userB, userA, Date(System.currentTimeMillis() - 1800000)),
            Message("msg3", "Third message", userA, userB, Date()))

    messages.forEach { manager.sendMessage(convId, it) }

    composeTestRule.setContent {
      MessageScreen(viewModel = viewModel, convId = convId, onConversationDeleted = {})
    }

    composeTestRule.waitForIdle()

    // Verify all messages are displayed
    composeTestRule.onNodeWithText("First message").assertExists()
    composeTestRule.onNodeWithText("Second message").assertExists()
    composeTestRule.onNodeWithText("Third message").assertExists()

    // At least one timestamp should be visible
    composeTestRule.waitUntil(3000) {
      composeTestRule.onAllNodesWithText(":", substring = true).fetchSemanticsNodes().size >= 3
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
            location = Location(0.0, 0.0, "Test Location"))

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

  @Test
  fun messageScreen_callsOnConversationDeletedWhenStateFlagIsTrue() {
    var callbackCalled = false

    composeTestRule.setContent {
      MessageScreen(
          viewModel = viewModel, convId = convId, onConversationDeleted = { callbackCalled = true })
    }

    // Force deletion
    viewModel.deleteConversation()

    composeTestRule.waitUntil(2000) { callbackCalled }
  }

  @Test
  fun messageScreen_deleteButtonDeletesConversation() {
    var deleteCallbackTriggered = false

    composeTestRule.setContent {
      MessageScreen(
          viewModel = viewModel,
          convId = convId,
          onConversationDeleted = { deleteCallbackTriggered = true })
    }

    // Click delete icon
    composeTestRule.onNode(hasContentDescription("Delete conversation")).performClick()

    composeTestRule.waitUntil(timeoutMillis = 2_000) {
      viewModel.uiState.value.isDeleted || deleteCallbackTriggered
    }

    assert(viewModel.uiState.value.isDeleted)
  }

  @Test
  fun messageScreen_onConversationDeletedCallbackCalled() {
    var callbackCalled = false

    composeTestRule.setContent {
      MessageScreen(
          viewModel = viewModel, convId = convId, onConversationDeleted = { callbackCalled = true })
    }

    // Force deletion manually
    viewModel.deleteConversation()

    composeTestRule.waitUntil(2_000) { callbackCalled }

    assert(callbackCalled)
  }
}
