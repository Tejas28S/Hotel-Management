package com.pesu.hotelmanagement.pattern;

import com.pesu.hotelmanagement.model.Room;
import com.pesu.hotelmanagement.model.RoomStatus;

/**
 * OBSERVER PATTERN — Observer Interface
 *
 * Any class that wants to react to a room status change implements this interface.
 * The RoomService (subject) will notify all registered observers whenever
 * a room's status transitions to a new state.
 *
 * Example use cases:
 *   - Log the change for auditing (LoggingObserver)
 *   - Alert housekeeping when a room enters CLEANING state
 *   - Alert maintenance when UNDER_MAINTENANCE is set
 */
public interface RoomStatusObserver {
    void onRoomStatusChanged(Room room, RoomStatus oldStatus, RoomStatus newStatus);
}
