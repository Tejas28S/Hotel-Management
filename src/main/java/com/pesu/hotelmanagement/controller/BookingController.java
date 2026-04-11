package com.pesu.hotelmanagement.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pesu.hotelmanagement.model.Booking;
import com.pesu.hotelmanagement.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // Endpoint: POST http://localhost:8080/api/bookings/create
    @PostMapping("/create")
    public Booking createBooking(
            @RequestParam Long guestId,
            @RequestParam Long roomId,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {

        // Convert the String dates from the URL into standard Java LocalDates
        LocalDate checkInDate = LocalDate.parse(checkIn);
        LocalDate checkOutDate = LocalDate.parse(checkOut);

        return bookingService.createBooking(guestId, roomId, checkInDate, checkOutDate);
    }

    // Endpoint: GET http://localhost:8080/api/bookings/user/{guestId}
    @GetMapping("/user/{guestId}")
    public List<Booking> getUserBookings(@PathVariable Long guestId) {
        return bookingService.getBookingsForGuest(guestId);
    }
}