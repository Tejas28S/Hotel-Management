package com.pesu.hotelmanagement.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pesu.hotelmanagement.model.Booking;
import com.pesu.hotelmanagement.model.BookingStatus;
import com.pesu.hotelmanagement.model.Room;
import com.pesu.hotelmanagement.model.RoomStatus;
import com.pesu.hotelmanagement.model.User;
import com.pesu.hotelmanagement.pattern.PricingStrategy;
import com.pesu.hotelmanagement.repository.BookingRepository;
import com.pesu.hotelmanagement.repository.RoomRepository;
import com.pesu.hotelmanagement.repository.UserRepository;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomService roomService;

    /**
     * STRATEGY PATTERN — the active pricing algorithm.
     * Swap the @Qualifier to "weekendPricing" or "holidayPricing"
     * to change pricing hotel-wide with zero other code changes.
     */
    @Autowired
    @Qualifier("weekendPricing")
    private PricingStrategy pricingStrategy;

    // ---------------------------------------------------------------
    // BOOKING CREATION
    // ---------------------------------------------------------------

    @Transactional
    public Booking createBooking(Long guestId, Long roomId, LocalDate checkIn, LocalDate checkOut) {

        User guest = userRepository.findById(guestId)
                .orElseThrow(() -> new RuntimeException("Guest not found"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.isAvailable()) {
            throw new RuntimeException("Sorry, this room is not available for booking.");
        }

        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (days <= 0) {
            throw new IllegalArgumentException("Check-out date must be at least one day after check-in.");
        }

        // STRATEGY PATTERN — price calculated by the injected strategy
        double totalPrice = pricingStrategy.calculatePrice(room, checkIn, checkOut);

        Booking booking = new Booking(guest, room, checkIn, checkOut, totalPrice);
        bookingRepository.save(booking);

        // Transition room: AVAILABLE → RESERVED
        roomService.updateRoomStatus(roomId, RoomStatus.RESERVED);

        return booking;
    }

    // ---------------------------------------------------------------
    // CHECK-IN  (STAFF action)
    // ---------------------------------------------------------------

    @Transactional
    public Booking checkIn(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Only CONFIRMED bookings can be checked in.");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);

        // Transition room: RESERVED → OCCUPIED
        roomService.updateRoomStatus(booking.getRoom().getId(), RoomStatus.OCCUPIED);

        return booking;
    }

    // ---------------------------------------------------------------
    // CHECK-OUT  (STAFF action)
    // ---------------------------------------------------------------

    @Transactional
    public Booking checkOut(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new RuntimeException("Only CHECKED_IN bookings can be checked out.");
        }

        booking.setStatus(BookingStatus.CHECKED_OUT);
        bookingRepository.save(booking);

        // Transition room: OCCUPIED → CLEANING  (triggers HousekeepingObserver)
        roomService.updateRoomStatus(booking.getRoom().getId(), RoomStatus.CLEANING);

        return booking;
    }

    // ---------------------------------------------------------------
    // CANCEL BOOKING  (GUEST or ADMIN action)
    // ---------------------------------------------------------------

    @Transactional
    public Booking cancelBooking(Long bookingId, Long requestingUserId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Security check: only the guest who owns the booking, or an ADMIN, can cancel
        boolean isOwner = booking.getGuest().getId().equals(requestingUserId);
        boolean isAdmin  = "ADMIN".equals(requestingUser.getRole());

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("You are not authorised to cancel this booking.");
        }

        if (booking.getStatus() == BookingStatus.CHECKED_IN || booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new RuntimeException("Cannot cancel a booking that is already checked in or completed.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Release the room back to AVAILABLE
        roomService.updateRoomStatus(booking.getRoom().getId(), RoomStatus.AVAILABLE);

        return booking;
    }

    // ---------------------------------------------------------------
    // QUERIES
    // ---------------------------------------------------------------

    public List<Booking> getBookingsForGuest(Long guestId) {
        return bookingRepository.findByGuestId(guestId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getConfirmedBookings() {
        return bookingRepository.findByStatus(BookingStatus.CONFIRMED);
    }
}