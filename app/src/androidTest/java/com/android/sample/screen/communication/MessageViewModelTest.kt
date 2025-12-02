package com.android.sample.screen.communication

import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.communication.newImplementation.ConversationManager
import com.android.sample.model.communication.newImplementation.conversation.ConvRepository
import com.android.sample.model.communication.newImplementation.conversation.ConversationNew
import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
import com.android.sample.ui.communication.MessageViewModel
import com.google.firebase.Timestamp
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MessageViewModelTest {

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
    // On force une erreur dans l’état
    viewModel.clearError() // juste pour reset
    viewModel.loadConversation("invalid_id")

    // Vérification initiale
    assertEquals("Conversation not found", viewModel.uiState.value.error)

    // Action
    viewModel.clearError()

    // L’erreur doit être supprimée
    assertEquals(null, viewModel.uiState.value.error)
  }
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
