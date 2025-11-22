package com.android.sample.model.communication

import app.cash.turbine.test
import com.android.sample.model.communication.newImplementation.conversation.*
import com.android.sample.utils.FirebaseEmulator
import com.android.sample.utils.RepositoryTest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

@Config(sdk = [28])
class FirestoreConvRepositoryTest : RepositoryTest() {

  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var convRepository: ConvRepository
  private val testUser1Id = "test-user-1"
  private val testUser2Id = "test-user-2"

  @Before
  override fun setUp() {
    super.setUp()
    firestore = FirebaseEmulator.firestore

    auth = mockk()
    val mockUser = mockk<FirebaseUser>()
    every { auth.currentUser } returns mockUser
    every { mockUser.uid } returns testUser1Id

    convRepository = FirestoreConvRepository(firestore)
  }

  @After
  override fun tearDown() = runBlocking {
    // Clean up messages
    val messagesSnapshot = firestore.collection(CONVERSATIONS_COLLECTION_PATH).get().await()
    for (document in messagesSnapshot.documents) {
      document.reference.delete().await()
    }

    // Clean up conversations
    val conversationsSnapshot = firestore.collection(CONVERSATIONS_COLLECTION_PATH).get().await()
    for (document in conversationsSnapshot.documents) {
      document.reference.delete().await()
    }

    super.tearDown()
  }

  // ------------------------------------------------------------
  //  TEST: create + get conversation
  // ------------------------------------------------------------
  @Test
  fun createAndGetConversationWorks() = runTest {
    val conv =
        ConversationNew(
            convId = "conv123",
            convCreatorId = testUser1Id,
            otherPersonId = testUser2Id,
            convName = "Chat A")

    convRepository.createConv(conv)
    val result = convRepository.getConv("conv123")

    assertNotNull(result)
    assertEquals("conv123", result!!.convId)
    assertEquals("Chat A", result.convName)
    assertEquals(testUser1Id, result.convCreatorId)
    assertEquals(testUser2Id, result.otherPersonId)
  }

  // ------------------------------------------------------------
  //  TEST: getConv returns null if conversation missing
  // ------------------------------------------------------------
  @Test
  fun getConvReturnsNullWhenNotExists() = runTest {
    val result = convRepository.getConv("unknown")
    assertNull(result)
  }

  // ------------------------------------------------------------
  //  TEST: delete conversation
  // ------------------------------------------------------------
  @Test
  fun deleteConvDeletesConversation() = runTest {
    val convId = "convToDelete"
    val conv = ConversationNew(convId = convId)

    convRepository.createConv(conv)
    assertNotNull(convRepository.getConv(convId))

    convRepository.deleteConv(convId)
    assertNull(convRepository.getConv(convId))
  }

  @Test
  fun emits_when_messages_already_present() = runTest {
    val convId = "conv-test"
    val repo = FakeConvRepository()
    repo.createConv(ConversationNew(convId = convId))

    val msg1 = MessageNew("1", "Hello", "u1", "u2")
    val msg2 = MessageNew("2", "World", "u2", "u1")

    // send messages before collecting
    repo.sendMessage(convId, msg1)
    repo.sendMessage(convId, msg2)

    // Now start collecting: first awaitItem() should contain both messages
    repo.listenMessages(convId).test {
      val current = awaitItem()
      assertEquals(2, current.size)
      assertEquals("Hello", current[0].content)
      assertEquals("World", current[1].content)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun emits_on_subsequent_sends() = runTest {
    val convId = "conv-test"
    val repo = FakeConvRepository()
    repo.createConv(ConversationNew(convId = convId))

    val msg1 = MessageNew("1", "Hello", "u1", "u2")
    val msg2 = MessageNew("2", "World", "u2", "u1")

    repo.listenMessages(convId).test {
      // StateFlow émet immédiatement sa valeur courante (probablement emptyList())
      val initial = awaitItem()
      assertTrue(initial.isEmpty(), "expected initial snapshot to be empty")

      // maintenant envoie un message -> nouvelle émission
      repo.sendMessage(convId, msg1)
      val afterFirst = awaitItem()
      assertEquals(1, afterFirst.size)
      assertEquals("Hello", afterFirst[0].content)

      // envoie second -> encore une émission
      repo.sendMessage(convId, msg2)
      val afterSecond = awaitItem()
      assertEquals(2, afterSecond.size)
      assertEquals("Hello", afterSecond[0].content)
      assertEquals("World", afterSecond[1].content)

      cancelAndIgnoreRemainingEvents()
    }
  }

  // ------------------------------------------------------------
  //  TEST: listenMessages throws on blank ID
  // ------------------------------------------------------------
  @Test
  fun listenMessagesFailsOnBlankId() = runTest {
    assertFailsWith<IllegalArgumentException> { convRepository.listenMessages("").first() }
  }
}

class FakeConvRepository : ConvRepository {

  private val conversations = mutableMapOf<String, MutableStateFlow<List<MessageNew>>>()

  override fun getNewUid(): String {
    return ""
  }

  override suspend fun getConv(convId: String): ConversationNew? {
    return null
  }

  override suspend fun createConv(conversation: ConversationNew) {
    conversations[conversation.convId] = MutableStateFlow(emptyList())
  }

  override suspend fun deleteConv(convId: String) {}

  override fun listenMessages(convId: String): Flow<List<MessageNew>> {
    return conversations[convId] ?: MutableStateFlow(emptyList()) // fallback si non créé
  }

  override suspend fun sendMessage(convId: String, message: MessageNew) {
    val flow = conversations[convId] ?: error("Conversation $convId not initialized")

    flow.value = flow.value + message
  }
}
