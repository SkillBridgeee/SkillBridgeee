// import androidx.compose.ui.test.hasText
// import androidx.compose.ui.test.junit4.createComposeRule
// import androidx.compose.ui.test.onNodeWithText
// import androidx.compose.ui.test.onRoot
// import androidx.compose.ui.test.performClick
// import androidx.test.ext.junit.runners.AndroidJUnit4
// import androidx.test.platform.app.InstrumentationRegistry
// import com.android.sample.MainApp
// import com.android.sample.model.authentication.AuthenticationViewModel
// import org.junit.Rule
// import org.junit.Test
// import org.junit.runner.RunWith
//
// @RunWith(AndroidJUnit4::class)
// class MainActivityTest {
//
//  @get:Rule val composeTestRule = createComposeRule()
//
//  @Test
//  fun mainApp_composable_renders_without_crashing() {
//    composeTestRule.setContent {
//      MainApp(
//          authViewModel =
//              AuthenticationViewModel(InstrumentationRegistry.getInstrumentation().targetContext),
//          onGoogleSignIn = {})
//    }
//
//    // Verify that the main app structure is rendered
//    composeTestRule.onRoot().assertExists()
//  }
//
//  @Test
//  fun mainApp_contains_navigation_components() {
//    composeTestRule.setContent {
//      MainApp(
//          authViewModel =
//              AuthenticationViewModel(InstrumentationRegistry.getInstrumentation().targetContext),
//          onGoogleSignIn = {})
//    }
//
//    // First navigate from login to main app by clicking GitHub
//    composeTestRule.onNodeWithText("GitHub").performClick()
//    composeTestRule.waitForIdle()
//
//    // Now verify bottom navigation exists
//    composeTestRule.onNodeWithText("Skills").assertExists()
//    composeTestRule.onNodeWithText("Profile").assertExists()
//    composeTestRule.onNodeWithText("Bookings").assertExists()
//
//    // Test for Home in bottom nav specifically
//    composeTestRule.onAllNodes(hasText("Home")).fetchSemanticsNodes().let { nodes ->
//      assert(nodes.isNotEmpty()) // Verify at least one "Home" exists
//    }
//  }
// }
