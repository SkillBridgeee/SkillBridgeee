package com.android.sample.ui.communication

import com.android.sample.model.authentication.FirebaseTestRule
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DiscussionViewModelTest {

  @get:Rule val firebaseRule = FirebaseTestRule()

  private val testDispatcher = StandardTestDispatcher()

  private val currentUserId = "user-1"

  private val sampleConversations =
      listOf(
          OverViewConversation(
              overViewId = "1",
              linkedConvId = "conv1",
              convName = "John Doe",
              lastMsg =
                  MessageNew(
                      content = "Hey, how are you?",
                      senderId = "user2",
                      receiverId = currentUserId,
                      createdAt = Date()),
              nonReadMsgNumber = 2,
              overViewOwnerId = currentUserId,
              otherPersonId = "user2"),
          OverViewConversation(
              overViewId = "2",
              linkedConvId = "conv2",
              convName = "Jane Smith",
              lastMsg =
                  MessageNew(
                      content = "See you tomorrow!",
                      senderId = currentUserId,
                      receiverId = "user3",
                      createdAt = Date()),
              nonReadMsgNumber = 0,
              overViewOwnerId = currentUserId,
              otherPersonId = "user3"))

  @Mock private lateinit var mockRepository: OverViewConvRepository

  private lateinit var viewModel: DiscussionViewModel

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    Dispatchers.setMain(testDispatcher)
    UserSessionManager.setCurrentUserId(currentUserId)
    `when`(mockRepository.listenOverView(currentUserId)).thenReturn(flowOf(sampleConversations))
    viewModel = DiscussionViewModel(overViewConvRepository = mockRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    UserSessionManager.clearSession()
  }

  @Test
  fun loadConversations_success_updatesState() = runTest {
    `when`(mockRepository.listenOverView(currentUserId)).thenReturn(flowOf(sampleConversations))

    // Create a new viewModel to trigger loadConversations in init
    val testViewModel = DiscussionViewModel(overViewConvRepository = mockRepository)
    advanceUntilIdle()

    val state = testViewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(2, state.conversations.size)
    assertEquals("John Doe", state.conversations[0].convName)
    assertNull(state.error)
  }

  @Test
  fun loadConversations_failure_setsError() = runTest {
    `when`(mockRepository.listenOverView(currentUserId))
        .thenReturn(flow { throw RuntimeException("Test error") })

    // Create a new viewModel to trigger loadConversations in init
    val testViewModel = DiscussionViewModel(overViewConvRepository = mockRepository)
    advanceUntilIdle()

    val state = testViewModel.uiState.value
    assertFalse(state.isLoading)
    assertNotNull(state.error)
    assertTrue(state.error!!.contains("An unexpected error occurred while loading conversations"))
  }

  @Test
  fun clearError_removesErrorMessage() = runTest {
    `when`(mockRepository.listenOverView(currentUserId))
        .thenReturn(flow { throw RuntimeException("Test error") })

    // Create a new viewModel to trigger loadConversations in init which will set error
    val testViewModel = DiscussionViewModel(overViewConvRepository = mockRepository)
    advanceUntilIdle()

    var state = testViewModel.uiState.value
    assertNotNull(state.error)

    testViewModel.clearError()
    advanceUntilIdle()

    state = testViewModel.uiState.value
    assertNull(state.error)
  }

  @Test
  fun discussionViewModel_handlesEmptyConversations() = runTest {
    `when`(mockRepository.listenOverView(currentUserId)).thenReturn(flowOf(emptyList()))

    // Create a new viewModel to trigger loadConversations in init
    val testViewModel = DiscussionViewModel(overViewConvRepository = mockRepository)
    advanceUntilIdle()

    val state = testViewModel.uiState.value
    assertFalse(state.isLoading)
    assertTrue(state.conversations.isEmpty())
    assertNull(state.error)
  }

  @Test
  fun retry_successfullyReloadsAfterFailure() = runTest {
    // Mock initial failure
    `when`(mockRepository.listenOverView(currentUserId))
        .thenReturn(flow { throw RuntimeException("Test error") })

    val testViewModel = DiscussionViewModel(overViewConvRepository = mockRepository)
    advanceUntilIdle()

    // Assert error is set after initial load
    var state = testViewModel.uiState.value
    assertNotNull(state.error)
    assertTrue(state.error!!.contains("An unexpected error occurred"))

    // Change mock to return success
    `when`(mockRepository.listenOverView(currentUserId)).thenReturn(flowOf(sampleConversations))

    // Call retry
    testViewModel.retry()
    advanceUntilIdle()

    // Assert conversations are loaded successfully
    state = testViewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(2, state.conversations.size)
    assertEquals("John Doe", state.conversations[0].convName)
    assertNull(state.error)
  }
}
