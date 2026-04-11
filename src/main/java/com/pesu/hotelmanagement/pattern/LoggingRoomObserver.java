package com.pesu.hotelmanagement.pattern;

import com.pesu.hotelmanagement.model.Room;
import com.pesu.hotelmanagement.model.RoomStatus;
import org.springframework.stereotype.Component;

/**
 * OBSERVER PATTERN — Concrete Observer: Logging
 *
 * Automatically triggered whenever any room's status changes.
 * In production this would write to a file / monitoring system.
 * For now it prints a clear, timestamped audit log to the console.
 */
@Component
public class LoggingRoomObserver implements RoomStatusObserver {

    @Override
    public void onRoomStatusChanged(Room room, RoomStatus oldStatus, RoomStatus newStatus) {
        System.out.printf("[ROOM AUDIT] Room %s (%s) → %s ➜ %s%n",
            room.getRoomNumber(),
            room.getRoomType(),
            oldStatus,
            newStatus
        );
    }
}
