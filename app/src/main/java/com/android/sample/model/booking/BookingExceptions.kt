package com.android.sample.model.booking

/**
 * Exception thrown when a user is not authenticated but tries to perform an operation requiring
 * authentication
 */
class BookingAuthenticationException(message: String = "User not authenticated") :
    Exception(message)

/** Exception thrown when a user doesn't have permission to perform an operation on a booking */
class BookingAccessDeniedException(message: String = "Access denied to booking") :
    Exception(message)

/** Exception thrown when a booking is not found */
class BookingNotFoundException(bookingId: String) :
    Exception("Booking with ID $bookingId not found")

/** Exception thrown when a booking operation fails due to validation errors */
class BookingValidationException(message: String) : Exception(message)

/** Exception thrown when a booking operation fails due to a Firestore error */
class BookingFirestoreException(message: String, cause: Throwable? = null) :
    Exception(message, cause)
