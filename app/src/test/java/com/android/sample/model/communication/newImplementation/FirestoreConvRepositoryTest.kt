package com.android.sample.model.communication.newImplementation

import com.android.sample.model.communication.CONVERSATIONS_COLLECTION_PATH
import com.android.sample.model.communication.newImplementation.conversation.*
import com.android.sample.utils.FirebaseEmulator
import com.android.sample.utils.RepositoryTest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertFailsWith
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
  fun listenMessagesEmitsMessagesInOrder() = runTest {
    val convId = "conv-listen"
    convRepository.createConv(ConversationNew(convId = convId))

    val msgRef =
        firestore.collection(CONVERSATIONS_COLLECTION_PATH).document(convId).collection("messages")

    val listenerMessages = mutableListOf<List<MessageNew>>()

    val job = launch {
      convRepository.listenMessages(convId).collect { messages -> listenerMessages.add(messages) }
    }

    delay(100) // laisse le listener s'attacher

    val msg1 =
        MessageNew(msgId = "1", content = "Hello", senderId = testUser1Id, receiverId = testUser2Id)
    val msg2 =
        MessageNew(msgId = "2", content = "World", senderId = testUser2Id, receiverId = testUser1Id)

    msgRef.document("1").set(msg1).await()
    delay(500) // temps pour que le listener reçoive le snapshot

    msgRef.document("2").set(msg2).await()
    delay(500) // idem

    job.cancel()

    assertTrue(listenerMessages.isNotEmpty())
    val lastEmission = listenerMessages.last()
    assertEquals(2, lastEmission.size)
    assertEquals("Hello", lastEmission[0].content)
    assertEquals("World", lastEmission[1].content)
  }

  // ------------------------------------------------------------
  //  TEST: listenMessages throws on blank ID
  // ------------------------------------------------------------
  @Test
  fun listenMessagesFailsOnBlankId() = runTest {
    assertFailsWith<IllegalArgumentException> { convRepository.listenMessages("").first() }
  }
}
