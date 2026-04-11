package com.pesu.hotelmanagement.pattern;

import com.pesu.hotelmanagement.model.Room;
import com.pesu.hotelmanagement.model.RoomStatus;
import org.springframework.stereotype.Component;

/**
 * OBSERVER PATTERN — Concrete Observer: Housekeeping Alert
 *
 * Triggered whenever a room transitions to CLEANING status.
 * In a real system this would send a push notification / task to housekeeping staff.
 */
@Component
public class HousekeepingObserver implements RoomStatusObserver {

    @Override
    public void onRoomStatusChanged(Room room, RoomStatus oldStatus, RoomStatus newStatus) {
        if (newStatus == RoomStatus.CLEANING) {
            System.out.printf("[HOUSEKEEPING] Room %s is ready to be cleaned. Please assign a staff member.%n",
                room.getRoomNumber());
        }
        if (newStatus == RoomStatus.UNDER_MAINTENANCE) {
            System.out.printf("[MAINTENANCE] Room %s has been flagged for maintenance. Work order created.%n",
                room.getRoomNumber());
        }
    }
}
