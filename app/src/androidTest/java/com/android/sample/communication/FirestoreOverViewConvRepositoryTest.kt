package com.android.sample.communication

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.communication.newImplementation.overViewConv.FirestoreOverViewConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
import com.android.sample.utils.TestFirestore
import junit.framework.TestCase.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirestoreOverViewConvRepositoryTest {

  private lateinit var repo: FirestoreOverViewConvRepository

  private val userA = "userA"
  private val userB = "userB"

  @Before
  fun setup() {
    repo = FirestoreOverViewConvRepository(TestFirestore.db)
  }

  // ----------------------------------------------------------
  // TEST 1 : CREATE + GET OVERVIEW
  // ----------------------------------------------------------
  @Test
  fun testAddAndGetOverview() = runTest {
    val overview =
        OverViewConversation(
            linkedConvId = "conv1",
            convName = "Conversation 1",
            convCreatorId = userA,
            otherPersonId = userB)

    repo.addOverViewConvUser(overview)

    val result = repo.getOverViewConvUser(userA)
    assertTrue(result.any { it.linkedConvId == "conv1" })

    val resultOther = repo.getOverViewConvUser(userB)
    assertTrue(resultOther.any { it.linkedConvId == "conv1" })
  }

  // ----------------------------------------------------------
  // TEST 2 : DELETE OVERVIEW
  // ----------------------------------------------------------
  @Test
  fun testDeleteOverview() = runTest {
    val convId = "conv5"
    val overview =
        OverViewConversation(
            linkedConvId = convId,
            convName = "Conversation 5",
            convCreatorId = userA,
            otherPersonId = userB)

    repo.addOverViewConvUser(overview)

    repo.deleteOverViewConvUser(convId)

    val result = repo.getOverViewConvUser(userA)
    assertTrue(result.none { it.linkedConvId == convId })
  }

  // ----------------------------------------------------------
  // TEST 3 : LISTEN OVERVIEW FLOW
  // ----------------------------------------------------------
  @Test
  fun testListenOverviewFlow() = runTest {
    val convId = "conv6"
    val overview =
        OverViewConversation(
            linkedConvId = convId,
            convName = "Conversation 6",
            convCreatorId = userA,
            otherPersonId = userB)

    repo.addOverViewConvUser(overview)

    val flow = repo.listenOverView(userA)

    val emitted = flow.first { it -> it.isNotEmpty() && it.any { it.linkedConvId == convId } }
    assertTrue(emitted.any { it.linkedConvId == convId })
  }
}
