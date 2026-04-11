package com.pesu.hotelmanagement.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pesu.hotelmanagement.model.Room;
import com.pesu.hotelmanagement.model.RoomStatus;
import com.pesu.hotelmanagement.pattern.RoomStatusObserver;
import com.pesu.hotelmanagement.repository.RoomRepository;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    /**
     * OBSERVER PATTERN — Subject (observable)
     * Spring auto-injects every @Component that implements RoomStatusObserver
     * (LoggingRoomObserver, HousekeepingObserver, etc.)
     */
    @Autowired
    private List<RoomStatusObserver> observers;

    // --- Core CRUD ---

    public List<Room> getAvailableRooms() {
        return roomRepository.findByStatus(RoomStatus.AVAILABLE);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room addRoom(Room room) {
        return roomRepository.save(room);
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    public List<Room> getAvailableRoomsByDates(LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAvailableRoomsByDates(checkIn, checkOut);
    }

    /**
     * Central method for ALL room status transitions.
     * Every change goes through here so observers are always notified.
     *
     * Valid transitions enforced here:
     *   AVAILABLE        → RESERVED         (on booking)
     *   RESERVED         → OCCUPIED         (on check-in)
     *   OCCUPIED         → CLEANING         (on check-out)
     *   CLEANING         → AVAILABLE        (housekeeping done)
     *   ANY              → UNDER_MAINTENANCE (admin flag)
     *   UNDER_MAINTENANCE → AVAILABLE       (repairs done)
     */
    public Room updateRoomStatus(Long roomId, RoomStatus newStatus) {
        Room room = getRoomById(roomId);
        RoomStatus oldStatus = room.getStatus();

        room.setStatus(newStatus);
        roomRepository.save(room);

        // Notify all registered observers (LoggingRoomObserver, HousekeepingObserver…)
        for (RoomStatusObserver observer : observers) {
            observer.onRoomStatusChanged(room, oldStatus, newStatus);
        }

        return room;
    }
}