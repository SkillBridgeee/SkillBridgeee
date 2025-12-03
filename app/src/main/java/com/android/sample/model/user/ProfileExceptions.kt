package com.android.sample.model.user

/**
 * Exception thrown when a user is not authenticated but tries to perform an operation requiring
 * authentication
 */
class ProfileAuthenticationException(message: String = "User not authenticated") :
    Exception(message)

/** Exception thrown when a user doesn't have permission to perform an operation on a profile */
class ProfileAccessDeniedException(message: String = "Access denied to profile") :
    Exception(message)

/** Exception thrown when a profile is not found */
class ProfileNotFoundException(userId: String) : Exception("Profile with ID $userId not found")

/** Exception thrown when a profile operation fails due to validation errors */
class ProfileValidationException(message: String) : Exception(message)

/** Exception thrown when a profile operation fails due to a Firestore error */
class ProfileFirestoreException(message: String, cause: Throwable? = null) :
    Exception(message, cause)
