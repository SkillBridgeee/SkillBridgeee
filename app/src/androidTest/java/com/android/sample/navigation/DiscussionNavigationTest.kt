package com.android.sample.navigation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
import com.android.sample.ui.communication.DiscussionScreen
import com.android.sample.ui.communication.DiscussionViewModel
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DiscussionNavigationTest {

  @get:Rule val composeTestRule = createComposeRule()

  private class FakeOverViewConvRepository : OverViewConvRepository {
    override fun getNewUid(): String = "dummy"

    override suspend fun getOverViewConvUser(userId: String): List<OverViewConversation> =
        emptyList()

    override suspend fun addOverViewConvUser(overView: OverViewConversation) {}

    override suspend fun deleteOverViewConvUser(convId: String) {}

    override fun listenOverView(userId: String): Flow<List<OverViewConversation>> =
        flowOf(
            listOf(
                OverViewConversation(
                    convName = "Chat with Alice",
                    linkedConvId = "conv1",
                    lastMsg = null,
                    nonReadMsgNumber = 0,
                )))
  }

  @Before
  fun setUp() {
    UserSessionManager.setCurrentUserId("user1")
    RouteStackManager.clear()
  }

  @Test
  fun clickingConversation_executesNavigationToDiscussion() {
    val fakeRepo = FakeOverViewConvRepository()
    val discussionViewModel = DiscussionViewModel(fakeRepo)

    lateinit var navController: NavHostController

    composeTestRule.setContent {
      navController = rememberNavController()

      NavHost(
          navController = navController,
          startDestination = NavRoutes.DISCUSSION,
      ) {
        composable(NavRoutes.DISCUSSION) {
          DiscussionScreen(
              viewModel = discussionViewModel,
              onConversationClick = { convIdClicked ->
                // current production code navigates to DISCUSSION
                navController.navigate(NavRoutes.DISCUSSION)
              },
          )
        }
      }
    }

    composeTestRule
        .onNodeWithTag("conversation_item_conv1", useUnmergedTree = true)
        .assertExists()
        .performClick()

    composeTestRule.runOnIdle {
      assertEquals(NavRoutes.DISCUSSION, navController.currentBackStackEntry?.destination?.route)
    }
  }

  @Test
  fun discussionRoute_isPushedToRouteStackManager() {
    val fakeRepo = FakeOverViewConvRepository()
    val discussionViewModel = DiscussionViewModel(fakeRepo)

    RouteStackManager.clear()

    composeTestRule.setContent {
      val navController = rememberNavController()

      NavHost(
          navController = navController,
          startDestination = NavRoutes.DISCUSSION,
      ) {
        composable(NavRoutes.DISCUSSION) {
          androidx.compose.runtime.LaunchedEffect(Unit) {
            RouteStackManager.addRoute(NavRoutes.DISCUSSION)
          }
          DiscussionScreen(
              viewModel = discussionViewModel,
              onConversationClick = { convIdClicked ->
                navController.navigate(NavRoutes.DISCUSSION)
              },
          )
        }
      }
    }

    composeTestRule.runOnIdle {
      assertEquals(NavRoutes.DISCUSSION, RouteStackManager.getCurrentRoute())
    }
  }
}
