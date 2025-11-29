package com.android.sample.communication

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.model.communication.newImplementation.ConversationManager
import com.android.sample.model.communication.newImplementation.conversation.FirestoreConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.FirestoreOverViewConvRepository
import com.android.sample.ui.communication.MessageScreen
import com.android.sample.utils.TestFirestore
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MessageScreenEmulatorTest {

    private lateinit var currentUserId: String

    private lateinit var otherId: String

    private lateinit var convRepo: FirestoreConvRepository
    private lateinit var ovRepo: FirestoreOverViewConvRepository
    private lateinit var manager: ConversationManager
    private lateinit var convId: String


    @Before
    fun setup() {
        currentUserId = "currentId"
        otherId = "otherId"
        convRepo = FirestoreConvRepository(TestFirestore.db)
        ovRepo = FirestoreOverViewConvRepository(TestFirestore.db)
        manager = ConversationManager(convRepo, ovRepo)

    }

    @After
    fun tearDown() = runTest {
        if (::convId.isInitialized) {
            manager.deleteConvAndOverviews(convId)
        }
    }



}