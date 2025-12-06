package com.android.sample.utils.fakeRepo.fakeConvManager

import com.android.sample.model.communication.conversation.ConvRepository
import com.android.sample.model.communication.conversation.Conversation
import com.android.sample.model.communication.conversation.Message
import com.google.firebase.Timestamp
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeConvRepo : ConvRepository {

  // Stockage interne des conversations
  private val conversations = mutableMapOf<String, Conversation>()

  // Stockage des flows de messages par conversation
  private val messageFlows = mutableMapOf<String, MutableStateFlow<List<Message>>>()

  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  override suspend fun getConv(convId: String): Conversation? {
    return conversations[convId]
  }

  override suspend fun createConv(conversation: Conversation) {
    val convId = conversation.convId.ifEmpty { getNewUid() }

    val newConv = conversation.copy(convId = convId, updatedAt = Timestamp.now())

    conversations[convId] = newConv
    messageFlows[convId] = MutableStateFlow(newConv.messages)
  }

  override suspend fun deleteConv(convId: String) {
    conversations.remove(convId)
    messageFlows.remove(convId)
  }

  override suspend fun sendMessage(convId: String, message: Message) {
    val conv = conversations[convId] ?: return

    // Nouveau message ajouté
    val updatedMessages = conv.messages + message

    val updatedConv = conv.copy(messages = updatedMessages, updatedAt = Timestamp.now())

    conversations[convId] = updatedConv

    // Mise à jour du Flow
    messageFlows[convId]?.value = updatedMessages
  }

  override fun listenMessages(convId: String): Flow<List<Message>> {
    return messageFlows.getOrPut(convId) { MutableStateFlow(emptyList()) }
  }
}
