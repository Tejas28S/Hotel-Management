package com.pesu.hotelmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pesu.hotelmanagement.model.Booking;
import com.pesu.hotelmanagement.model.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // All bookings for a specific guest (My Bookings page)
    List<Booking> findByGuestId(Long guestId);

    // All bookings with a specific status (e.g., staff check-in queue)
    List<Booking> findByStatus(BookingStatus status);

    // Active bookings for a guest (excludes cancelled/checked-out)
    List<Booking> findByGuestIdAndStatus(Long guestId, BookingStatus status);
}