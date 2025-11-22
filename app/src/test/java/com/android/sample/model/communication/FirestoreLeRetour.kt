package com.android.sample.model.communication

import app.cash.turbine.test
import com.android.sample.model.communication.newImplementation.conversation.*
import com.android.sample.utils.FirebaseEmulator
import com.android.sample.utils.RepositoryTest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

@Config(sdk = [28])
class FirestoreLeRetour : RepositoryTest() {

  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var convRepository: ConvRepository
  private val testUser1Id = "test-user-1"
  private val testUser2Id = "test-user-2"

  @Before
  override fun setUp() {
    super.setUp()
    firestore = FirebaseEmulator.firestore

    auth = FirebaseAuth.getInstance() // si tu n’as pas besoin de mock
    convRepository = FirestoreConvRepository(firestore)
  }

  @After
  override fun tearDown() = runTest {
    // Supprime toutes les conversations et messages
    val snapshot = firestore.collection(CONVERSATIONS_COLLECTION_PATH).get().await()
    for (doc in snapshot.documents) {
      doc.reference.delete().await()
    }

    super.tearDown()
  }

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
    assertEquals("conv123", result.convId)
    assertEquals("Chat A", result.convName)
    assertEquals(testUser1Id, result.convCreatorId)
    assertEquals(testUser2Id, result.otherPersonId)
  }

  @Test
  fun getConvReturnsNullWhenNotExists() = runTest {
    val result = convRepository.getConv("unknown")
    assertNull(result)
  }

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
    val conv =
        ConversationNew(convId = convId, convCreatorId = testUser1Id, otherPersonId = testUser2Id)
    convRepository.createConv(conv)

    val msg1 = MessageNew("1", "coucou", testUser1Id, testUser2Id)
    val msg2 = MessageNew("2", "World", testUser2Id, testUser1Id)

    // Ajoute les messages avant de collecter
    convRepository.sendMessage(convId, msg1)
    convRepository.sendMessage(convId, msg2)

    convRepository.listenMessages(convId).test {
      var current: List<MessageNew>
      do {
        current = awaitItem()
      } while (current.isEmpty())

      assertEquals(2, current.size)
      assertEquals("Hello", current[0].content)
      assertEquals("World", current[1].content)

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun emits_on_subsequent_sends_flow() = runTest {
    val convId = "conv-test"
    val conv = ConversationNew(convId = convId)
    convRepository.createConv(conv)

    val msg1 = MessageNew("1", "Hello", testUser1Id, testUser2Id)
    val msg2 = MessageNew("2", "World", testUser2Id, testUser1Id)

    convRepository.listenMessages(convId).test {
      val initialEmpty = awaitItem() // snapshot initial vide
      assertEquals(0, initialEmpty.size)

      convRepository.sendMessage(convId, msg1)

      val afterFirst = awaitItem()
      assertEquals(1, afterFirst.size)
      assertEquals("Hello", afterFirst[0].content)

      convRepository.sendMessage(convId, msg2)
      val afterSecond = awaitItem()
      assertEquals(2, afterSecond.size)
      assertEquals("Hello", afterSecond[0].content)
      assertEquals("World", afterSecond[1].content)

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun listenMessagesFailsOnBlankId() = runTest {
    assertFailsWith<IllegalArgumentException> { convRepository.listenMessages("").first() }
  }
}
