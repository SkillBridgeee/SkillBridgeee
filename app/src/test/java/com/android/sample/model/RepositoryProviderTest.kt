package com.android.sample.model

import android.content.Context
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RepositoryProviderTest {

  private lateinit var provider: TestRepositoryProvider

  @Before
  fun setup() {
    provider = TestRepositoryProvider()
  }

  @Test
  fun `repository throws when not initialized`() {
    val exception = assertThrows(IllegalStateException::class.java) { provider.repository }
    assertTrue(exception.message?.contains("not initialized") == true)
    assertTrue(exception.message?.contains("init") == true)
  }

  @Test
  fun `init sets repository`() {
    val context = mockk<Context>(relaxed = true)
    provider.init(context, useEmulator = false)

    assertNotNull(provider.repository)
    assertTrue(provider.repository is String)
  }

  @Test
  fun `init with emulator flag sets repository`() {
    val context = mockk<Context>(relaxed = true)
    provider.init(context, useEmulator = true)

    assertNotNull(provider.repository)
    assertEquals("initialized_with_emulator", provider.repository)
  }

  @Test
  fun `setForTests sets repository for testing`() {
    val testRepo = "test_repository"
    provider.setForTests(testRepo)

    assertEquals(testRepo, provider.repository)
  }

  @Test
  fun `setForTests allows accessing repository without init`() {
    provider.setForTests("mock_repo")

    val repo = provider.repository
    assertEquals("mock_repo", repo)
  }

  @Test
  fun `init can be called multiple times`() {
    val context = mockk<Context>(relaxed = true)
    provider.init(context, useEmulator = false)
    val firstRepo = provider.repository

    provider.init(context, useEmulator = true)
    val secondRepo = provider.repository

    assertNotEquals(firstRepo, secondRepo)
  }

  @Test
  fun `setForTests overrides initialized repository`() {
    val context = mockk<Context>(relaxed = true)
    provider.init(context, useEmulator = false)

    provider.setForTests("overridden")
    assertEquals("overridden", provider.repository)
  }

  // Concrete test implementation of RepositoryProvider
  private class TestRepositoryProvider : RepositoryProvider<String>() {
    override fun init(context: Context, useEmulator: Boolean) {
      _repository = if (useEmulator) "initialized_with_emulator" else "initialized"
    }
  }
}
