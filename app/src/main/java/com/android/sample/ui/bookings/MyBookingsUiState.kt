package com.android.sample.ui.bookings

/**
 * Represents the UI state of the **My Bookings** screen.
 *
 * The screen should observe this state and render appropriate UI:
 * - [Loading] -> show a progress indicator.
 * - [Success] -> render the provided list of [BookingCardUi] (stable, formatted for display).
 * - [Empty] -> show an empty-state placeholder (no bookings available).
 * - [Error] -> show an error message and optionally a retry action.
 *
 * This sealed type keeps presentation concerns separate from the repository/domain layer: the
 * ViewModel is responsible for mapping domain models into [BookingCardUi] and emitting the correct
 * [MyBookingsUiState] variant.
 */
sealed class MyBookingsUiState {

  /**
   * Loading indicates an in-flight request to load bookings.
   *
   * UI: show a spinner or skeleton content. No list should be shown while this state is active.
   */
  object Loading : MyBookingsUiState()

  /**
   * Success contains the list of bookings ready to be displayed.
   * - `items` is a display-ready list of [BookingCardUi].
   * - The UI should render these items (for example via `LazyColumn` using each item's `id` as a
   *   stable key).
   */
  data class Success(val items: List<BookingCardUi>) : MyBookingsUiState()

  /**
   * Empty indicates the user has no bookings.
   *
   * UI: show an empty-state illustration/message and possible call-to-action (e.g., "Book a
   * tutor").
   */
  object Empty : MyBookingsUiState()

  /**
   * Error contains a human- or developer-facing message describing the failure.
   *
   * UI: display the message and provide a retry or support action. The message may be generic for
   * end-users or more detailed for logging depending on how the ViewModel formats it.
   */
  data class Error(val message: String) : MyBookingsUiState()
}
