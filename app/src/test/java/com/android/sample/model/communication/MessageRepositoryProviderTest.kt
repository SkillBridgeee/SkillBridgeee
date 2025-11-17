package com.android.sample.model.communication

import com.android.sample.utils.RepositoryTest
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

@Config(sdk = [28])
class MessageRepositoryProviderTest : RepositoryTest() {

  @Before
  override fun setUp() {
    super.setUp()
    MessageRepositoryProvider.reset()
  }

  @After
  override fun tearDown() {
    MessageRepositoryProvider.reset()
    super.tearDown()
  }

  @Test
  fun getRepositoryReturnsFirestoreMessageRepositoryByDefault() {
    val repository = MessageRepositoryProvider.getRepository()
    assertNotNull(repository)
    assertTrue(repository is FirestoreMessageRepository)
  }

  @Test
  fun getRepositoryReturnsSameInstanceOnMultipleCalls() {
    val repository1 = MessageRepositoryProvider.getRepository()
    val repository2 = MessageRepositoryProvider.getRepository()

    assertSame(repository1, repository2)
  }

  @Test
  fun setRepositoryChangesTheRepository() {
    val mockRepository = mockk<MessageRepository>()
    MessageRepositoryProvider.setRepository(mockRepository)

    val repository = MessageRepositoryProvider.getRepository()
    assertSame(mockRepository, repository)
  }

  @Test
  fun resetClearsTheRepository() {
    val repository1 = MessageRepositoryProvider.getRepository()
    MessageRepositoryProvider.reset()
    val repository2 = MessageRepositoryProvider.getRepository()

    assertNotSame(repository1, repository2)
  }

  @Test
  fun setRepositoryThenResetRestoresDefaultBehavior() {
    val mockRepository = mockk<MessageRepository>()
    MessageRepositoryProvider.setRepository(mockRepository)

    MessageRepositoryProvider.reset()
    val repository = MessageRepositoryProvider.getRepository()

    assertTrue(repository is FirestoreMessageRepository)
    assertNotSame(mockRepository, repository)
  }

  @Test
  fun multipleResetsWork() {
    MessageRepositoryProvider.reset()
    MessageRepositoryProvider.reset()
    MessageRepositoryProvider.reset()

    val repository = MessageRepositoryProvider.getRepository()
    assertNotNull(repository)
  }
}
