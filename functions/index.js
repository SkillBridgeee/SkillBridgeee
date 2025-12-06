const {onCall} = require('firebase-functions/v2/https');
const {onInit} = require('firebase-functions/v2/core');
const admin = require('firebase-admin');

// Use onInit() to defer initialization - avoids deployment timeouts
onInit(() => {
  admin.initializeApp();
  console.log('Firebase Admin SDK initialized');
});

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
 * @param {Object} request - The request object
 * @param {Object} request.data - The request data
 * @param {string} request.data.email - The email address of the user to verify
 * @returns {Promise<{success: boolean, message: string}>}
 */
exports.forceVerifyTestUser = onCall(async (request) => {
  const email = request.data.email;

  // Validate input
  if (!email) {
    throw new Error('Email is required');
  }

  // Security: Only allow test emails with @example.test domain
  if (!email.endsWith('@example.test')) {
    throw new Error('This function can only be used with @example.test email addresses');
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
      throw new Error('User not found with the provided email');
    }

    throw new Error('Failed to force email verification: ' + error.message);
  }
});

/**
 * Additional function to clean up test users (optional, for cleanup)
 */
exports.deleteTestUser = onCall(async (request) => {
  const email = request.data.email;

  if (!email) {
    throw new Error('Email is required');
  }

  // Security: Only allow test emails
  if (!email.endsWith('@example.test')) {
    throw new Error('This function can only be used with @example.test email addresses');
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
        message: 'User already deleted or does not exist'
      };
    }

    throw new Error('Failed to delete test user: ' + error.message);
  }
});

