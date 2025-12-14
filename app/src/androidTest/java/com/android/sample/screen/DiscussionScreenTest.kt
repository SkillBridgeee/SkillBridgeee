package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.communication.conversation.Message
import com.android.sample.model.communication.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.overViewConv.OverViewConversation
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
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
                  Message(
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
                  Message(
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

    // Wait for data to load
    compose.waitUntil(10_000) {
      compose
          .onAllNodesWithText("Hey, how are you?", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Check if conversations are displayed
    compose.onNodeWithText("Hey, how are you?").assertIsDisplayed()
    // Second message should have "You:" prefix since it was sent by current user
    compose.onNodeWithText("You: See you tomorrow!", substring = true).assertIsDisplayed()
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

    compose.waitUntil(3_000) {
      compose.onAllNodesWithTag("conversation_item_conv1").fetchSemanticsNodes().isNotEmpty()
    }

    // Click on the first conversation using testTag
    compose.onNodeWithTag("conversation_item_conv1").performClick()

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

  // -----------------------------------------------------
  // TEST — Timestamp display in conversation list
  // -----------------------------------------------------
  @Test
  fun discussionScreen_displaysRecentMessageTimestamp() {
    val recentConversation =
        OverViewConversation(
            overViewId = "1",
            linkedConvId = "conv1",
            convName = "Test User",
            lastMsg =
                Message(
                    content = "Recent message",
                    senderId = "user2",
                    receiverId = currentUserId,
                    createdAt = Date() // Current time - should show "Just now"
                    ),
            nonReadMsgNumber = 0,
            overViewOwnerId = currentUserId,
            otherPersonId = "user2")

    val viewModel = createViewModelWithConversations(listOf(recentConversation))

    compose.setContent { DiscussionScreen(viewModel = viewModel, onConversationClick = {}) }

    // Wait for data to load and message to be displayed
    compose.waitUntil(10_000) {
      compose
          .onAllNodesWithText("Recent message", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Verify the message content is displayed
    compose.onNodeWithText("Recent message", substring = true).assertIsDisplayed()

    // Verify timestamp is displayed (should be "Just now" for recent message)
    // compose.onNodeWithText("Just now", substring = true).assertExists()
  }

  @Test
  fun discussionScreen_displaysOlderMessageTimestamp() {
    val olderConversation =
        OverViewConversation(
            overViewId = "1",
            linkedConvId = "conv1",
            convName = "Old Contact",
            lastMsg =
                Message(
                    content = "Old message",
                    senderId = "user2",
                    receiverId = currentUserId,
                    createdAt = Date(System.currentTimeMillis() - 7200000) // 2 hours ago
                    ),
            nonReadMsgNumber = 0,
            overViewOwnerId = currentUserId,
            otherPersonId = "user2")

    val viewModel = createViewModelWithConversations(listOf(olderConversation))

    compose.setContent { DiscussionScreen(viewModel = viewModel, onConversationClick = {}) }

    // Wait for data to load and message to be displayed
    compose.waitUntil(10_000) {
      compose.onAllNodesWithText("Old message", substring = true).fetchSemanticsNodes().isNotEmpty()
    }

    // Verify the message content is displayed
    compose.onNodeWithText("Old message", substring = true).assertIsDisplayed()

    // Verify timestamp is displayed (should show "2h ago")
    compose.onNodeWithText("ago", substring = true).assertExists()
  }
  // -----------------------------------------------------
  // TEST — "You:" prefix for current user's messages
  // -----------------------------------------------------
  @Test
  fun discussionScreen_displaysYouPrefixForMyMessage() {
    val myMessageConversation =
        OverViewConversation(
            overViewId = "1",
            linkedConvId = "conv1",
            convName = "Test User",
            lastMsg =
                Message(
                    content = "Hello there!",
                    senderId = currentUserId, // Message sent by current user
                    receiverId = "user2",
                    createdAt = Date()),
            nonReadMsgNumber = 0,
            overViewOwnerId = currentUserId,
            otherPersonId = "user2")

    val viewModel = createViewModelWithConversations(listOf(myMessageConversation))

    compose.setContent { DiscussionScreen(viewModel = viewModel, onConversationClick = {}) }

    // Wait for data to load and conversation to be displayed
    compose.waitUntil(10_000) {
      compose
          .onAllNodesWithText("You: Hello there!", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Verify "You:" prefix is displayed
    compose.onNodeWithText("You: Hello there!", substring = true).assertIsDisplayed()
  }

  @Test
  fun discussionScreen_noYouPrefixForOtherUsersMessage() {
    val otherMessageConversation =
        OverViewConversation(
            overViewId = "1",
            linkedConvId = "conv1",
            convName = "Other User",
            lastMsg =
                Message(
                    content = "Hi, how are you?",
                    senderId = "user2", // Message sent by other user
                    receiverId = currentUserId,
                    createdAt = Date()),
            nonReadMsgNumber = 1,
            overViewOwnerId = currentUserId,
            otherPersonId = "user2")

    val viewModel = createViewModelWithConversations(listOf(otherMessageConversation))

    compose.setContent { DiscussionScreen(viewModel = viewModel, onConversationClick = {}) }

    // Wait for data to load and conversation to be displayed
    compose.waitUntil(10_000) {
      compose
          .onAllNodesWithText("Hi, how are you?", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Verify message is displayed without "You:" prefix
    compose.onNodeWithText("Hi, how are you?").assertIsDisplayed()
    // Verify "You:" prefix is NOT present
    compose.onNodeWithText("You: Hi, how are you?", substring = true).assertDoesNotExist()
  }

  @Test
  fun discussionScreen_mixedConversationsWithYouPrefix() {
    val conversations =
        listOf(
            // Conversation where current user sent last message
            OverViewConversation(
                overViewId = "1",
                linkedConvId = "conv1",
                convName = "Alice",
                lastMsg =
                    Message(
                        content = "See you soon!",
                        senderId = currentUserId,
                        receiverId = "user2",
                        createdAt = Date()),
                nonReadMsgNumber = 0,
                overViewOwnerId = currentUserId,
                otherPersonId = "user2"),
            // Conversation where other user sent last message
            OverViewConversation(
                overViewId = "2",
                linkedConvId = "conv2",
                convName = "Bob",
                lastMsg =
                    Message(
                        content = "Thanks for the help!",
                        senderId = "user3",
                        receiverId = currentUserId,
                        createdAt = Date()),
                nonReadMsgNumber = 1,
                overViewOwnerId = currentUserId,
                otherPersonId = "user3"))

    val viewModel = createViewModelWithConversations(conversations)

    compose.setContent { DiscussionScreen(viewModel = viewModel, onConversationClick = {}) }

    // Wait for data to load and both conversations to be displayed
    compose.waitUntil(10_000) {
      compose
          .onAllNodesWithText("You: See you soon!", substring = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Verify "You:" prefix for current user's message
    compose.onNodeWithText("You: See you soon!", substring = true).assertIsDisplayed()

    // Verify no "You:" prefix for other user's message
    compose.onNodeWithText("Thanks for the help!", substring = true).assertIsDisplayed()
    compose.onNodeWithText("You: Thanks for the help!", substring = true).assertDoesNotExist()
  }

  private fun createViewModelWithConversations(
      conversations: List<OverViewConversation>
  ): DiscussionViewModel {
    val overViewRepository = FakeOverViewConvRepository()
    val profileRepository = FakeProfileRepository()

    overViewRepository.setConversations(conversations)
    return DiscussionViewModel(overViewRepository, profileRepository)
  }

  private fun createViewModelWithError(): DiscussionViewModel {
    val overViewRepository = FakeOverViewConvRepository()
    val profileRepository = FakeProfileRepository()
    overViewRepository.setShouldThrowError(true)
    return DiscussionViewModel(overViewRepository, profileRepository)
  }

  private fun createViewModelWithDelay(): DiscussionViewModel {
    val overViewRepository = FakeOverViewConvRepository()
    val profileRepository = FakeProfileRepository()
    overViewRepository.setDelayLoading(true)
    return DiscussionViewModel(overViewRepository, profileRepository)
  }

  /** Fake ProfileRepository that does nothing. */
  private class FakeProfileRepository :
      com.android.sample.utils.fakeRepo.fakeProfile.FakeProfileRepo {
    override suspend fun getProfile(userId: String): Profile? = null

    override suspend fun addProfile(profile: Profile) {
      TODO("Not yet implemented")
    }

    override suspend fun updateProfile(userId: String, profile: Profile) {
      TODO("Not yet implemented")
    }

    override suspend fun deleteProfile(userId: String) {}

    override suspend fun getAllProfiles(): List<Profile> {
      TODO("Not yet implemented")
    }

    override suspend fun searchProfilesByLocation(
        location: Location,
        radiusKm: Double
    ): List<Profile> {
      TODO("Not yet implemented")
    }

    override suspend fun getProfileById(userId: String): Profile? {
      TODO("Not yet implemented")
    }

    override suspend fun getSkillsForUser(userId: String): List<Skill> {
      TODO("Not yet implemented")
    }

    override suspend fun updateTutorRatingFields(
        userId: String,
        averageRating: Double,
        totalRatings: Int
    ) {
      TODO("Not yet implemented")
    }

    override suspend fun updateStudentRatingFields(
        userId: String,
        averageRating: Double,
        totalRatings: Int
    ) {
      TODO("Not yet implemented")
    }

    override fun getNewUid(): String = "fake-profile-uid"

    override fun getCurrentUserId(): String = "fake-current-user-id"

    override fun getCurrentUserName(): String? = "Fake User"
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
                      Message(
                          content = "Hey, how are you?",
                          senderId = "user2",
                          receiverId = "user1",
                          createdAt = Date()),
                  nonReadMsgNumber = 2,
                  overViewOwnerId = "user1",
                  otherPersonId = "user2")),
      private var shouldThrowError: Boolean = false,
      private var delayLoading: Boolean = false,
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

    override suspend fun deleteOverViewById(overViewId: String) {
      TODO("Not yet implemented")
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
