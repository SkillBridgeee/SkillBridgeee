package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.communication.newImplementation.ConversationManager
import com.android.sample.model.communication.newImplementation.conversation.ConvRepository
import com.android.sample.model.communication.newImplementation.conversation.ConversationNew
import com.android.sample.model.communication.newImplementation.conversation.MessageNew
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepository
import com.android.sample.ui.communication.MessageScreen
import com.android.sample.ui.communication.MessageViewModel
import com.android.sample.utils.fakeRepo.fakeConvManager.FakeConvRepo
import com.android.sample.utils.fakeRepo.fakeConvManager.FakeOverViewRepo
import java.util.Date
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var convRepo: ConvRepository
  private lateinit var overViewRepo: OverViewConvRepository
  private lateinit var manager: ConversationManager
  private lateinit var viewModel: MessageViewModel

  private val convId = "convTest"
  private val userA = "userA"
  private val userB = "userB"

  @Before
  fun setup() {
    convRepo = FakeConvRepo()
    overViewRepo = FakeOverViewRepo()
    manager = ConversationManager(convRepo, overViewRepo)

    viewModel = MessageViewModel(manager)

    UserSessionManager.setCurrentUserId(userA)

    runBlocking {
      convRepo.createConv(
          ConversationNew(convId = convId, convCreatorId = userA, otherPersonId = userB))
    }
  }

  // -----------------------------------------------------
  // TEST 1 — Affichage du message envoyé
  // -----------------------------------------------------
  @Test
  fun messageScreen_showsSentMessage() {
    composeTestRule.setContent { MessageScreen(viewModel = viewModel, convId = convId) }

    val textToSend = "Bonjour test"

    // Tape dans champ input
    composeTestRule.onNode(hasSetTextAction()).performTextInput(textToSend)

    // Clique sur envoyer
    composeTestRule.onNode(hasContentDescription("Send message")).performClick()

    // Vérifie que le message apparait à l'écran
    composeTestRule.onNodeWithText(textToSend).assertExists()
  }

  // -----------------------------------------------------
  // TEST 2 — Un message reçu apparaît dans l’UI
  // -----------------------------------------------------
  @Test
  fun messageScreen_showsIncomingMessage() = runTest {
    composeTestRule.setContent { MessageScreen(viewModel = viewModel, convId = convId) }

    // Simule réception d’un message
    manager.sendMessage(
        convId,
        MessageNew(
            msgId = "xyz",
            senderId = userB,
            receiverId = userA,
            content = "Salut utilisateur!",
            createdAt = Date()))

    composeTestRule.waitUntil(timeoutMillis = 1000) {
      composeTestRule.onAllNodesWithText("Salut utilisateur!").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithText("Salut utilisateur!").assertExists()
  }
}
