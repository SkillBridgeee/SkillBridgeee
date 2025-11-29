package com.android.sample.communication

import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.communication.newImplementation.ConversationManager
import com.android.sample.model.communication.newImplementation.conversation.FirestoreConvRepository
import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import com.android.sample.model.communication.newImplementation.overViewConv.FirestoreOverViewConvRepository
import com.android.sample.ui.communication.MessageViewModel
import com.android.sample.utils.TestFirestore
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MessageViewModelEmulatorTest {

  private lateinit var convRepo: FirestoreConvRepository
  private lateinit var ovRepo: FirestoreOverViewConvRepository
  private lateinit var manager: ConversationManager

  private lateinit var convId: String
  private val currentUserId = "u1"
  private val otherUserId = "u2"

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() = runTest {
    Dispatchers.setMain(testDispatcher)

    convRepo = FirestoreConvRepository(TestFirestore.db)
    ovRepo = FirestoreOverViewConvRepository(TestFirestore.db)
    manager = ConversationManager(convRepo, ovRepo)

    UserSessionManager.setCurrentUserId(currentUserId)
  }

  @After
  fun tearDown() = runTest {
    if (::convId.isInitialized) {
      manager.deleteConvAndOverviews(convId)
    }
    Dispatchers.resetMain()
  }

  @Test
  fun loadConversation() = runTest {
    convId =
        manager.createConvAndOverviews(
            creatorId = currentUserId, otherUserId = otherUserId, convName = "testConv")

    val msg =
        MessageNew(
            msgId = "m1",
            senderId = otherUserId,
            receiverId = currentUserId,
            content = "Salut",
            createdAt = Date())
    manager.sendMessage(convId, msg)

    // Inject le dispatcher dans le ViewModel pour test
    val vm = MessageViewModel(manager)

    val conv = manager.getConv(convId)
    assertNotNull(convId)
    assertEquals(conv!!.convId, convId)

    // WHEN
    vm.loadConversation(convId)

    // Avance toutes les coroutines en attente
    testDispatcher.scheduler.advanceUntilIdle()

    // THEN
    val state = vm.uiState.value
    assertEquals(currentUserId, state.currentUserId)
    assertEquals(false, state.isLoading)
    assertEquals(1, state.messages.size)
    assertEquals("Salut", state.messages[0].content)
  }
}
