package com.android.sample.communication

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.communication.newImplementation.overViewConv.FirestoreOverViewConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
import com.android.sample.utils.TestFirestore
import junit.framework.TestCase.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirestoreOverViewConvRepositoryTest {

  private lateinit var repo: FirestoreOverViewConvRepository

  private val userA = "userA"
  private val userB = "userB"

  private lateinit var convId: String

  @Before
  fun setup() {
    repo = FirestoreOverViewConvRepository(TestFirestore.db)
  }

  @After
  fun tearDown() = runTest {
    if (::convId.isInitialized) {
      repo.deleteOverViewConvUser(convId)
    }
  }

  // ----------------------------------------------------------
  // TEST 1 : CREATE + GET OVERVIEW
  // ----------------------------------------------------------
  @Test
  fun testAddAndGetOverview() = runTest {
    convId = "conv1"
    val overview =
        OverViewConversation(
            overViewId = "id1",
            linkedConvId = "conv1",
            convName = "Conversation 1",
            overViewOwnerId = userA,
            otherPersonId = userB)

    repo.addOverViewConvUser(overview)

    val result = repo.getOverViewConvUser(userA)
    assertTrue(result.any { it.linkedConvId == "conv1" })
  }

  // ----------------------------------------------------------
  // TEST 2 : DELETE OVERVIEW
  // ----------------------------------------------------------
  @Test
  fun testDeleteOverview() = runTest {
    convId = "conv5"
    val overview =
        OverViewConversation(
            overViewId = "id5",
            linkedConvId = convId,
            convName = "Conversation 5",
            overViewOwnerId = userA,
            otherPersonId = userB)

    repo.addOverViewConvUser(overview)

    repo.deleteOverViewConvUser(convId)

    val result = repo.getOverViewConvUser(userA)
    assertTrue(result.none { it.linkedConvId == convId })
  }

  // ----------------------------------------------------------
  // TEST 3 : LISTEN OVERVIEW FLOW (Current User)
  // ----------------------------------------------------------
  @Test
  fun testListenOverviewFlowCurrentUser() = runTest {
    convId = "conv6"
    val overview =
        OverViewConversation(
            overViewId = "id6",
            linkedConvId = convId,
            convName = "Conversation 6",
            overViewOwnerId = userA,
            otherPersonId = userB)

    repo.addOverViewConvUser(overview)

    val flow = repo.listenOverView(userA)

    val emitted = flow.first { it -> it.isNotEmpty() && it.any { it.linkedConvId == convId } }
    assertTrue(emitted.any { it.linkedConvId == convId })
  }

  // ----------------------------------------------------------
  // TEST 4 : LISTEN OVERVIEW FLOW (Other User)
  // ----------------------------------------------------------
  @Test
  fun testListenOverviewFlowCurrentOtherUSer() = runTest {
    convId = "conv7"
    val overview =
        OverViewConversation(
            overViewId = "id7",
            linkedConvId = convId,
            convName = "Conversation 7",
            overViewOwnerId = userA,
            otherPersonId = userB)

    repo.addOverViewConvUser(overview)

    val flow = repo.listenOverView(userB)

    val emitted = flow.first { it -> it.isNotEmpty() && it.any { it.linkedConvId == convId } }
    assertTrue(emitted.any { it.linkedConvId == convId })
  }

  // ----------------------------------------------------------
  // TEST 5 : getNewUid() generates a valid and unique UUID
  // ----------------------------------------------------------
  @Test
  fun testGetNewUid() {
    val id1 = repo.getNewUid()
    val id2 = repo.getNewUid()

    assertTrue(id1.isNotBlank())
    assertTrue(id2.isNotBlank())
    assertNotSame("IDs should be unique", id1, id2)
  }

  // ----------------------------------------------------------
  // TEST 6 : addOverViewConvUser - require failure
  // ----------------------------------------------------------
  @Test(expected = IllegalArgumentException::class)
  fun testAddOverviewRequireFailsOnBlankId() = runTest {
    val overview =
        OverViewConversation(
            overViewId = "",
            linkedConvId = "convX",
            convName = "Invalid Overview",
            overViewOwnerId = userA,
            otherPersonId = userB)

    repo.addOverViewConvUser(overview)
  }

  // ----------------------------------------------------------
  // TEST 7 : deleteOverViewConvUser - require failure
  // ----------------------------------------------------------
  @Test(expected = IllegalArgumentException::class)
  fun testDeleteOverviewRequireFailsOnBlankConvId() = runTest { repo.deleteOverViewConvUser("") }

  // ----------------------------------------------------------
  // TEST 8 : getOverViewConvUser - require failure
  // ----------------------------------------------------------
  @Test(expected = IllegalArgumentException::class)
  fun testGetOverviewRequireFailsOnBlankUserId() = runTest { repo.getOverViewConvUser("") }
}
