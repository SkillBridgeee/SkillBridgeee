# Cloud Functions for SkillBridge

This directory contains Cloud Functions used for E2E testing purposes.

## Setup

### Prerequisites
- Node.js 20+ (matches Firebase CLI bundled version)
- Firebase CLI installed globally

### Installation
```bash
cd functions
npm install
```

## Available Functions

### forceVerifyTestUser
Callable function to mark a test user's email as verified.

**Security**: Only works with `@example.test` email addresses.

**Usage from Android tests**:
```kotlin
val functions = Firebase.functions
val data = hashMapOf("email" to testEmail)
functions.getHttpsCallable("forceVerifyTestUser")
    .call(data)
    .await()
```

### deleteTestUser
Callable function to delete a test user.

**Security**: Only works with `@example.test` email addresses.

## Running the Emulators

### Start all emulators:
```bash
# From project root
firebase emulators:start
```

### Start only functions emulator:
```bash
# From project root
firebase emulators:start --only functions
```

### Connect Android tests to emulator:
The functions will automatically connect to the emulator when running E2E tests locally.

## Recent Updates (December 2025)

### Fixed Issues:
1. ✅ **Upgraded firebase-functions to v5.1.1** - Now supports latest Firebase Extensions features
2. ✅ **Updated Node version to 20** - Matches bundled Firebase CLI version (no more warnings)
3. ✅ **Added onInit() hook** - Defers initialization to avoid deployment timeouts
4. ✅ **Migrated to v2 callable functions** - Better performance and error handling
5. ✅ **Added Functions Framework dependency** - Pinned version for consistent deployments
6. ✅ **Updated firebase.json** - Better configuration with codebase settings and runtime specification

### Key Changes:

**Before (v4 syntax)**:
```javascript
const functions = require('firebase-functions');
admin.initializeApp(); // Runs during deployment

exports.myFunction = functions.https.onCall(async (data, context) => {
  // function code
});
```

**After (v5 syntax)**:
```javascript
const {onCall} = require('firebase-functions/v2/https');
const {onInit} = require('firebase-functions/v2/core');

onInit(() => {
  admin.initializeApp(); // Only runs when deployed, not during discovery
});

exports.myFunction = onCall(async (request) => {
  const data = request.data;
  // function code
});
```

### Benefits:
- **No more timeout errors** during emulator startup
- **Faster deployment** and emulator initialization
- **Better error handling** with modern v2 API
- **Future-proof** with latest Firebase features support

## Troubleshooting

### Emulator won't start / timeout errors
- Make sure you've run `npm install` in the functions directory
- Check that your Node version matches (run `node --version`)
- Clear node_modules and reinstall: `rm -rf node_modules && npm install`

### Function not found in emulator
- Make sure the emulator is running
- Check the emulator logs for any errors
- Verify the function is exported in index.js

### Android test can't connect to function
- Ensure emulators are running before starting tests
- Check that the Firebase emulator configuration is correct in your test setup
- Verify the function name matches exactly

## Emulator Ports

When running emulators, these ports will be used:
- **Auth**: 9099
- **Firestore**: 8080
- **Functions**: 5001
- **Emulator UI**: 4000
- **Hub**: 4400

## Deployment

**Note**: These functions are designed for testing only. They should NOT be deployed to production.

If you need to deploy:
```bash
firebase deploy --only functions
```

However, consider:
- Adding environment-based security checks
- Disabling these functions in production
- Using separate Firebase projects for test and production

