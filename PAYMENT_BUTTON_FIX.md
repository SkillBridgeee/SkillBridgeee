# Payment Button Visibility Fix

## Problem
When a tutor views their listing (from Profile > Listings tab), the booking cards showed payment action buttons to everyone, regardless of their role. Specifically:
- **"Payment Complete"** button was shown to both learner and tutor when `paymentStatus == PENDING_PAYMENT`
- **"Payment Received"** button was shown to both learner and tutor when `paymentStatus == PAYED`

This was incorrect because:
- Only the **learner** (bookerId) should see "Payment Complete" button
- Only the **tutor** (listingCreatorId) should see "Payment Received" button

## Root Cause
The `BookingCard` component in `ui.listing.components` didn't know who the current user was, so it couldn't determine which buttons to show based on the user's role in the booking.

## Solution

### 1. Added `currentUserId` to `ListingUiState`
```kotlin
data class ListingUiState(
    // ...existing fields...
    val currentUserId: String? = null
)
```

### 2. Updated `ListingViewModel.loadListing()`
Set the `currentUserId` when loading a listing:
```kotlin
_uiState.update {
    it.copy(
        // ...other fields...
        currentUserId = currentUserId,
        // ...
    )
}
```

### 3. Updated `BookingCard` signature
Added `currentUserId` parameter:
```kotlin
fun BookingCard(
    booking: Booking,
    bookerProfile: Profile?,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onPaymentComplete: () -> Unit = {},
    onPaymentReceived: () -> Unit = {},
    currentUserId: String? = null,  // NEW
    modifier: Modifier = Modifier
)
```

### 4. Updated payment button logic in `BookingCard`
Only show buttons to the appropriate user:
```kotlin
// Only show "Payment Complete" button to the learner (bookerId)
if (booking.paymentStatus == PaymentStatus.PENDING_PAYMENT && 
    currentUserId == booking.bookerId) {
    // Show Payment Complete button
}
// Only show "Payment Received" button to the tutor (listingCreatorId)
else if (booking.paymentStatus == PaymentStatus.PAYED && 
         currentUserId == booking.listingCreatorId) {
    // Show Payment Received button
}
```

### 5. Updated `bookingsSection` to pass `currentUserId`
```kotlin
BookingCard(
    booking = booking,
    bookerProfile = uiState.bookerProfiles[booking.bookerId],
    onApprove = { onApproveBooking(booking.bookingId) },
    onReject = { onRejectBooking(booking.bookingId) },
    currentUserId = uiState.currentUserId  // NEW
)
```

## Payment Flow
1. **PENDING_PAYMENT**: Learner needs to pay
   - Learner sees "Payment Complete" button
   - Tutor sees no button (just status)
   
2. **PAYED**: Learner has paid, tutor needs to confirm
   - Learner sees no button (just status)
   - Tutor sees "Payment Received" button
   
3. **CONFIRMED**: Tutor has confirmed receipt
   - No buttons shown to either party

## Files Modified
- `/app/src/main/java/com/android/sample/ui/listing/ListingViewModel.kt`
- `/app/src/main/java/com/android/sample/ui/listing/components/BookingCard.kt`
- `/app/src/main/java/com/android/sample/ui/listing/components/bookingsSection.kt`

## Testing
The fix ensures that:
- When a tutor views their listing from Profile > Listings, they only see buttons appropriate for their role
- When a learner views a booking, they only see buttons appropriate for their role
- Payment workflow is correctly enforced based on user roles

