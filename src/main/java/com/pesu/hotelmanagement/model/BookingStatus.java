package com.pesu.hotelmanagement.model;

/**
 * Tracks the full lifecycle of a booking.
 *
 * State Transitions:
 *   CONFIRMED   → CHECKED_IN  (staff performs check-in on arrival date)
 *   CONFIRMED   → CANCELLED   (guest or admin cancels before check-in)
 *   CHECKED_IN  → CHECKED_OUT (staff performs check-out on departure date)
 */
public enum BookingStatus {
    CONFIRMED,
    CHECKED_IN,
    CHECKED_OUT,
    CANCELLED
}
