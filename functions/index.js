const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Initialize Firebase Admin SDK
admin.initializeApp();
console.log('Firebase Admin SDK initialized');

/**
 * Callable Cloud Function to force email verification for test users.
 *
 * This function is ONLY for E2E testing purposes and should be protected
 * to prevent abuse in production.
 *
 * Usage from Android test:
 * ```kotlin
 * val functions = Firebase.functions
 * val data = hashMapOf("email" to testEmail)
 * functions.getHttpsCallable("forceVerifyTestUser")
 *     .call(data)
 *     .await()
 * ```
 *
 * @param {Object} data - The request data
 * @param {string} data.email - The email address of the user to verify
 * @param {Object} context - The context object
 * @returns {Promise<{success: boolean, message: string}>}
 */
exports.forceVerifyTestUser = functions.https.onCall(async (data, context) => {
  const email = data.email;

  // Validate input
  if (!email) {
    throw new functions.https.HttpsError('invalid-argument', 'Email is required');
  }

  // Security: Only allow test emails with @example.test domain
  if (!email.endsWith('@example.test')) {
    throw new functions.https.HttpsError('permission-denied', 'This function can only be used with @example.test email addresses');
  }

  try {
    // Get the user by email
    const userRecord = await admin.auth().getUserByEmail(email);

    // Update the user to mark email as verified
    await admin.auth().updateUser(userRecord.uid, {
      emailVerified: true
    });

    console.log(`Email verified for test user: ${email} (UID: ${userRecord.uid})`);

    return {
      success: true,
      message: 'Email verification forced successfully',
      uid: userRecord.uid
    };
  } catch (error) {
    console.error('Error forcing email verification:', error);

    if (error.code === 'auth/user-not-found') {
      throw new functions.https.HttpsError('not-found', 'User not found with the provided email');
    }

    throw new functions.https.HttpsError('internal', 'Failed to force email verification: ' + error.message);
  }
});

/**
 * Additional function to clean up test users (optional, for cleanup)
 */
exports.deleteTestUser = functions.https.onCall(async (data, context) => {
  const email = data.email;

  if (!email) {
    throw new functions.https.HttpsError('invalid-argument', 'Email is required');
  }

  // Security: Only allow test emails
  if (!email.endsWith('@example.test')) {
    throw new functions.https.HttpsError('permission-denied', 'This function can only be used with @example.test email addresses');
  }

  try {
    const userRecord = await admin.auth().getUserByEmail(email);
    await admin.auth().deleteUser(userRecord.uid);

    console.log(`Deleted test user: ${email} (UID: ${userRecord.uid})`);

    return {
      success: true,
      message: 'Test user deleted successfully',
      uid: userRecord.uid
    };
  } catch (error) {
    console.error('Error deleting test user:', error);

    if (error.code === 'auth/user-not-found') {
      // User doesn't exist, that's fine
      return {
        success: true,
        message: 'User does not exist (already deleted or never created)'
      };
    }

    throw new functions.https.HttpsError('internal', 'Failed to delete test user: ' + error.message);
  }
});

