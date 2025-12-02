package com.android.sample.ui.tos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

object ToSTestTags {
  const val SCREEN = "tos_screen"
  const val ERROR_TEXT = "tos_error_text"
}

/**
 * ToS Screen - Mandatory Terms of Service acceptance screen
 *
 * This screen is displayed after user login/signup if they haven't accepted ToS. Users cannot
 * proceed to the app without accepting.
 *
 * @param onDecline callback when user declines ToS
 */
@Composable
fun ToSScreen(
    onDecline: () -> Unit,
) {
  Column(
      modifier =
          Modifier.fillMaxSize()
              .testTag(ToSTestTags.SCREEN)
              .background(MaterialTheme.colorScheme.background),
      horizontalAlignment = Alignment.CenterHorizontally) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
        ) {
          Column(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Terms of Service",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    text = "Please read and accept our Terms of Service to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary)
              }
        }

        // ToS Content
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)) {
              Text(
                  text = "1. User Responsibilities",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
              Text(
                  text =
                      "By using this application, you agree to abide by all applicable laws and regulations. You are responsible for maintaining the confidentiality of your account and password and for restricting access to your computer.",
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(bottom = 8.dp))

              Text(
                  text = "2. Intellectual Property Rights",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
              Text(
                  text =
                      "All content included in this application is the property of SkillBridge and is protected by international copyright laws. You agree not to reproduce, duplicate, copy, sell, or resell any portion of this application without express written permission.",
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(bottom = 8.dp))

              Text(
                  text = "3. Limitation of Liability",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
              Text(
                  text =
                      "In no case shall SkillBridge, its directors, officers, or representatives be liable for any indirect, incidental, or consequential damages arising out of or in connection with your use of the application.",
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(bottom = 8.dp))

              Text(
                  text = "4. Modification of Terms",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
              Text(
                  text =
                      "SkillBridge reserves the right to modify these Terms of Service at any time. Your continued use of the application following the posting of revised Terms means that you accept and agree to the changes.",
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(bottom = 8.dp))

              Text(
                  text = "5. Dispute Resolution",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
              Text(
                  text =
                      "Any disputes arising from these Terms of Service shall be governed by the laws of the jurisdiction in which SkillBridge is located. You agree to submit to the exclusive jurisdiction of the courts in that location.",
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(bottom = 16.dp))
            }
      }
}
