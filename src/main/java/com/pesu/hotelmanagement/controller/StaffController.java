package com.pesu.hotelmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.pesu.hotelmanagement.model.BookingStatus;
import com.pesu.hotelmanagement.model.RoomStatus;
import com.pesu.hotelmanagement.model.User;
import com.pesu.hotelmanagement.service.BookingService;
import com.pesu.hotelmanagement.service.RoomService;

import jakarta.servlet.http.HttpSession;

/**
 * STAFF Controller
 *
 * Handles all operations that hotel staff are permitted to perform:
 *   - View the check-in queue (confirmed bookings for today)
 *   - Perform guest check-in  (CONFIRMED → CHECKED_IN)
 *   - Perform guest check-out (CHECKED_IN → CHECKED_OUT + room → CLEANING)
 *   - Mark a room as cleaned  (CLEANING → AVAILABLE)
 *   - Flag a room for maintenance (→ UNDER_MAINTENANCE)
 */
@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired private BookingService bookingService;
    @Autowired private RoomService    roomService;

    private boolean isStaff(HttpSession session) {
        User u = (User) session.getAttribute("loggedInUser");
        return u != null && ("STAFF".equals(u.getRole()) || "ADMIN".equals(u.getRole()));
    }

    // ---------------------------------------------------------------
    // STAFF DASHBOARD
    // ---------------------------------------------------------------

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isStaff(session)) return "redirect:/login";

        model.addAttribute("confirmedBookings", bookingService.getConfirmedBookings());
        model.addAttribute("checkedInBookings", bookingService.getAllBookings()
                .stream()
                .filter(b -> b.getStatus() == BookingStatus.CHECKED_IN)
                .toList());
        model.addAttribute("cleaningRooms", roomService.getAllRooms()
                .stream()
                .filter(r -> r.getStatus() == RoomStatus.CLEANING || r.getStatus() == RoomStatus.UNDER_MAINTENANCE)
                .toList());
        return "staff-dashboard";
    }

    // ---------------------------------------------------------------
    // CHECK-IN
    // ---------------------------------------------------------------

    @PostMapping("/checkin/{bookingId}")
    public String checkIn(@PathVariable Long bookingId, HttpSession session, Model model) {
        if (!isStaff(session)) return "redirect:/login";
        try {
            bookingService.checkIn(bookingId);
        } catch (Exception e) {
            // Surface the error back on the dashboard
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/staff/dashboard";
    }

    // ---------------------------------------------------------------
    // CHECK-OUT
    // ---------------------------------------------------------------

    @PostMapping("/checkout/{bookingId}")
    public String checkOut(@PathVariable Long bookingId, HttpSession session) {
        if (!isStaff(session)) return "redirect:/login";
        try {
            bookingService.checkOut(bookingId);
        } catch (Exception ignored) {}
        return "redirect:/staff/dashboard";
    }

    // ---------------------------------------------------------------
    // ROOM STATUS MANAGEMENT (cleaning / maintenance)
    // ---------------------------------------------------------------

    @PostMapping("/rooms/status/{roomId}")
    public String updateRoomStatus(
            @PathVariable Long roomId,
            @RequestParam String status,
            HttpSession session) {
        if (!isStaff(session)) return "redirect:/login";
        roomService.updateRoomStatus(roomId, RoomStatus.valueOf(status));
        return "redirect:/staff/dashboard";
    }
}
