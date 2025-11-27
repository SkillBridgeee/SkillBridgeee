package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
import com.android.sample.ui.communication.DiscussionScreen
import com.android.sample.ui.communication.DiscussionViewModel
import java.util.Date
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@Suppress("DEPRECATION")
class DiscussionScreenTest {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

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

  @Before
  fun setup() {
    UserSessionManager.clearSession()
    UserSessionManager.setCurrentUserId(currentUserId)
  }

  @After
  fun cleanup() {
    UserSessionManager.clearSession()
  }

  @Test
  fun discussionScreen_displaysConversations() {
    val viewModel = createViewModelWithConversations(sampleConversations)

    compose.setContent { DiscussionScreen(viewModel = viewModel, onConversationClick = {}) }

    compose.waitForIdle()

    // Check if conversations are displayed
    compose.onNodeWithText("John Doe").assertIsDisplayed()
    compose.onNodeWithText("Jane Smith").assertIsDisplayed()
    compose.onNodeWithText("Hey, how are you?").assertIsDisplayed()
    compose.onNodeWithText("See you tomorrow!").assertIsDisplayed()
  }

  @Test
  fun discussionScreen_displaysEmptyState() {
    val viewModel = createViewModelWithConversations(emptyList())

    compose.setContent { DiscussionScreen(viewModel = viewModel, onConversationClick = {}) }

    compose.waitForIdle()

    // Should not display any conversation names
    compose.onNodeWithText("John Doe").assertDoesNotExist()
  }

  @Test
  fun discussionScreen_clickConversation_callsCallback() {
    val viewModel = createViewModelWithConversations(sampleConversations)
    var clickedConversationId: String? = null

    compose.setContent {
      DiscussionScreen(viewModel = viewModel, onConversationClick = { clickedConversationId = it })
    }

    compose.waitForIdle()

    // Click on the first conversation using testTag
    compose.onNodeWithTag("conversation_item_conv1").performClick()

    compose.waitForIdle()

    // Verify callback was called with correct ID
    assert(clickedConversationId == "conv1")
  }

  @Test
  fun discussionScreen_displaysUnreadBadge() {
    val viewModel = createViewModelWithConversations(sampleConversations)

    compose.setContent { DiscussionScreen(viewModel = viewModel, onConversationClick = {}) }

    compose.waitForIdle()

    // Should display the unread count for John Doe
    compose.onNodeWithText("2").assertIsDisplayed()
    // Should not display badge for Jane Smith (0 unread)
  }

  @Test
  fun discussionScreen_displaysError() {
    val viewModel = createViewModelWithError()

    compose.setContent { DiscussionScreen(viewModel = viewModel, onConversationClick = {}) }

    compose.waitForIdle()

    // Should display error message
    compose
        .onNodeWithText(
            text = "An unexpected error occurred while loading conversations",
            substring = true,
            ignoreCase = true)
        .assertIsDisplayed()
  }

  @Test
  fun discussionScreen_dismissError_hidesError() {
    val viewModel = createViewModelWithError()

    compose.setContent { DiscussionScreen(viewModel = viewModel, onConversationClick = {}) }

    compose.waitForIdle()

    // Error should be displayed
    compose
        .onNodeWithText(
            text = "An unexpected error occurred while loading conversations", substring = true)
        .assertIsDisplayed()

    // Click the dismiss button
    compose.onNodeWithContentDescription("Dismiss error").performClick()

    compose.waitForIdle()

    // Error should be hidden
    compose
        .onNodeWithText(
            text = "An unexpected error occurred while loading conversations", substring = true)
        .assertDoesNotExist()
  }

  @Test
  fun discussionScreen_displaysLoadingState() {
    val viewModel = createViewModelWithDelay()

    compose.setContent { DiscussionScreen(viewModel = viewModel, onConversationClick = {}) }

    // Loading indicator should be shown initially
    compose.onNodeWithTag("discussion_loading_indicator").assertIsDisplayed()

    // Wait for the loading to finish
    compose.waitUntil {
      compose.onAllNodesWithTag("discussion_loading_indicator").fetchSemanticsNodes().isEmpty()
    }
  }

  private fun createViewModelWithConversations(
      conversations: List<OverViewConversation>
  ): DiscussionViewModel {
    val repository = FakeOverViewConvRepository()
    repository.setConversations(conversations)
    return DiscussionViewModel(repository)
  }

  private fun createViewModelWithError(): DiscussionViewModel {
    val repository = FakeOverViewConvRepository()
    repository.setShouldThrowError(true)
    return DiscussionViewModel(repository)
  }

  private fun createViewModelWithDelay(): DiscussionViewModel {
    val repository = FakeOverViewConvRepository()
    repository.setDelayLoading(true)
    return DiscussionViewModel(repository)
  }

  /** Simple in-memory fake repository for tests. */
  private class FakeOverViewConvRepository(
      private val conversations: MutableList<OverViewConversation> =
          mutableListOf(
              OverViewConversation(
                  overViewId = "1",
                  linkedConvId = "conv1",
                  convName = "John Doe",
                  lastMsg =
                      MessageNew(
                          content = "Hey, how are you?",
                          senderId = "user2",
                          receiverId = "user1",
                          createdAt = Date()),
                  nonReadMsgNumber = 2,
                  overViewOwnerId = "user1",
                  otherPersonId = "user2")),
      private var shouldThrowError: Boolean = false,
      private var delayLoading: Boolean = false
  ) : OverViewConvRepository {

    fun setConversations(newConversations: List<OverViewConversation>) {
      conversations.clear()
      conversations.addAll(newConversations)
    }

    fun setShouldThrowError(throwError: Boolean) {
      shouldThrowError = throwError
    }

    fun setDelayLoading(delay: Boolean) {
      delayLoading = delay
    }

    override fun getNewUid(): String = "fake-uid-${System.currentTimeMillis()}"

    override suspend fun getOverViewConvUser(userId: String): List<OverViewConversation> {
      if (shouldThrowError) throw RuntimeException("Test error")
      if (delayLoading) kotlinx.coroutines.delay(100)
      return conversations.filter { it.overViewOwnerId == userId || it.otherPersonId == userId }
    }

    override suspend fun addOverViewConvUser(overView: OverViewConversation) {
      if (shouldThrowError) throw RuntimeException("Test error")
      conversations.add(overView)
    }

    override suspend fun deleteOverViewConvUser(convId: String) {
      if (shouldThrowError) throw RuntimeException("Test error")
      conversations.removeIf { it.linkedConvId == convId }
    }

    override fun listenOverView(
        userId: String
    ): kotlinx.coroutines.flow.Flow<List<OverViewConversation>> {
      if (shouldThrowError) {
        return kotlinx.coroutines.flow.callbackFlow { throw RuntimeException("Test error") }
      }
      if (delayLoading) {
        return kotlinx.coroutines.flow.callbackFlow {
          kotlinx.coroutines.delay(1000)
          trySend(
              conversations.filter { it.overViewOwnerId == userId || it.otherPersonId == userId })
          close()
        }
      }
      return kotlinx.coroutines.flow.flowOf(
          conversations.filter { it.overViewOwnerId == userId || it.otherPersonId == userId })
    }
  }
}
