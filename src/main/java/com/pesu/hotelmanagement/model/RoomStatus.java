package com.pesu.hotelmanagement.model;

/**
 * Represents the full lifecycle of a hotel room.
 * This replaces the old boolean isAvailable field with a proper state machine.
 *
 * State Transitions:
 *   AVAILABLE → RESERVED (guest books a room)
 *   RESERVED  → OCCUPIED (guest checks in)
 *   OCCUPIED  → CLEANING (guest checks out)
 *   CLEANING  → AVAILABLE (housekeeping completes)
 *   ANY STATE → UNDER_MAINTENANCE (admin flags for repairs)
 *   UNDER_MAINTENANCE → AVAILABLE (repairs complete)
 */
public enum RoomStatus {
    AVAILABLE,
    RESERVED,
    OCCUPIED,
    CLEANING,
    UNDER_MAINTENANCE
}
