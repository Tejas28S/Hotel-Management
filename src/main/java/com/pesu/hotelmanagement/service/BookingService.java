package com.pesu.hotelmanagement.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

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

    // ── All three strategies ────────────────────────────────
    @Autowired
    @Qualifier("standardPricing")
    private PricingStrategy standardPricingStrategy;

    @Autowired
    @Qualifier("weekendPricing")
    private PricingStrategy weekendPricingStrategy;

    @Autowired
    @Qualifier("holidayPricing")
    private PricingStrategy holidayPricingStrategy;

    // ── Holiday dates (same as HolidayPricingStrategy) ────────────────────────
    private static final Set<MonthDay> PEAK_DATES = Set.of(
        MonthDay.of(12, 24),
        MonthDay.of(12, 25),
        MonthDay.of(12, 31),
        MonthDay.of(1,  1),
        MonthDay.of(10, 31)
    );

    /**
     * STRATEGY PATTERN — Dynamically selects the correct pricing strategy
     * based on the booking dates at runtime.
     *
     * Priority:
     *   1. HolidayPricingStrategy  — if any night falls on a peak holiday date
     *   2. WeekendPricingStrategy  — if any night falls on Friday or Saturday
     *   3. StandardPricingStrategy — all other cases
     *
     * This is the core demonstration of the Strategy Pattern:
     * the algorithm is selected at runtime without changing BookingService logic.
     */
    public PricingStrategy selectStrategy(LocalDate checkIn, LocalDate checkOut) {
        LocalDate current = checkIn;
        boolean hasWeekend = false;

        while (current.isBefore(checkOut)) {
            // Holiday takes top priority
            if (PEAK_DATES.contains(MonthDay.from(current))) {
                return holidayPricingStrategy;
            }
            DayOfWeek day = current.getDayOfWeek();
            if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) {
                hasWeekend = true;
            }
            current = current.plusDays(1);
        }

        return hasWeekend ? weekendPricingStrategy : standardPricingStrategy;
    }

    /**
     * Returns a human-readable name for the active strategy given dates.
     * Used by the booking form and confirmation page to display which
     * pricing rule was applied.
     */
    public String getStrategyName(LocalDate checkIn, LocalDate checkOut) {
        PricingStrategy selected = selectStrategy(checkIn, checkOut);
        if (selected == holidayPricingStrategy) return "Holiday Pricing (+50% on peak dates)";
        if (selected == weekendPricingStrategy) return "Weekend Pricing (+25% on Fri/Sat nights)";
        return "Standard Pricing";
    }

    // ── BOOKING CREATION ───────────────────────────────────────────────────────

    @Transactional
    public Booking createBooking(Long guestId, Long roomId,
                                  LocalDate checkIn, LocalDate checkOut) {

        User guest = userRepository.findById(guestId)
                .orElseThrow(() -> new RuntimeException("Guest not found"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.isAvailable()) {
            throw new RuntimeException("Sorry, this room is not available for booking.");
        }

        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (days <= 0) {
            throw new IllegalArgumentException("Check-out must be at least one day after check-in.");
        }

        // STRATEGY PATTERN — strategy selected dynamically based on dates
        PricingStrategy strategy = selectStrategy(checkIn, checkOut);
        double totalPrice = strategy.calculatePrice(room, checkIn, checkOut);

        Booking booking = new Booking(guest, room, checkIn, checkOut, totalPrice);
        bookingRepository.save(booking);

        roomService.updateRoomStatus(roomId, RoomStatus.RESERVED);

        return booking;
    }

    // ── CHECK-IN ───────────────────────────────────────────────────────────────

    @Transactional
    public Booking checkIn(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Only CONFIRMED bookings can be checked in.");
        }
        booking.setStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);
        roomService.updateRoomStatus(booking.getRoom().getId(), RoomStatus.OCCUPIED);
        return booking;
    }

    // ── CHECK-OUT ──────────────────────────────────────────────────────────────

    @Transactional
    public Booking checkOut(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new RuntimeException("Only CHECKED_IN bookings can be checked out.");
        }
        booking.setStatus(BookingStatus.CHECKED_OUT);
        bookingRepository.save(booking);
        roomService.updateRoomStatus(booking.getRoom().getId(), RoomStatus.CLEANING);
        return booking;
    }

    // ── CANCEL ─────────────────────────────────────────────────────────────────

    @Transactional
    public Booking cancelBooking(Long bookingId, Long requestingUserId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isOwner = booking.getGuest().getId().equals(requestingUserId);
        boolean isAdmin  = "ADMIN".equals(requestingUser.getRole());

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("You are not authorised to cancel this booking.");
        }
        if (booking.getStatus() == BookingStatus.CHECKED_IN
                || booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new RuntimeException("Cannot cancel a booking that is already checked in or completed.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        roomService.updateRoomStatus(booking.getRoom().getId(), RoomStatus.AVAILABLE);
        return booking;
    }

    // ── QUERIES ────────────────────────────────────────────────────────────────

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