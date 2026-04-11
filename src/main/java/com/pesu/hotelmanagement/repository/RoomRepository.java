package com.pesu.hotelmanagement.repository;

import com.pesu.hotelmanagement.model.Room;
import com.pesu.hotelmanagement.model.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Find all rooms with a specific status (replaces old findByIsAvailableTrue)
    List<Room> findByStatus(RoomStatus status);

    /**
     * Date-aware availability search:
     * Returns rooms that are AVAILABLE and have no overlapping confirmed/checked-in bookings.
     */
    @Query("SELECT r FROM Room r WHERE r.status = 'AVAILABLE' AND r.id NOT IN " +
           "(SELECT b.room.id FROM Booking b WHERE " +
           "b.status IN ('CONFIRMED', 'CHECKED_IN') AND " +
           "(b.checkInDate < :checkOut AND b.checkOutDate > :checkIn))")
    List<Room> findAvailableRoomsByDates(@Param("checkIn") LocalDate checkIn,
                                         @Param("checkOut") LocalDate checkOut);
}