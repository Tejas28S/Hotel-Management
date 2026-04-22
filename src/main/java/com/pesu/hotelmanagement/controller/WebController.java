package com.pesu.hotelmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.pesu.hotelmanagement.model.User;
import com.pesu.hotelmanagement.service.BookingService;
import com.pesu.hotelmanagement.service.RoomService;
import com.pesu.hotelmanagement.service.UserService;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;

@Controller
public class WebController {

    @Autowired private RoomService    roomService;
    @Autowired private BookingService bookingService;
    @Autowired private UserService    userService;

    // ── ROOMS ──────────────────────────────────────────────────────────────────

    @GetMapping("/rooms")
    public String viewRooms(
            @RequestParam(required = false) String checkIn,
            @RequestParam(required = false) String checkOut,
            Model model) {

        if (checkIn != null && checkOut != null) {
            LocalDate in  = LocalDate.parse(checkIn);
            LocalDate out = LocalDate.parse(checkOut);
            model.addAttribute("availableRooms", roomService.getAvailableRoomsByDates(in, out));
            model.addAttribute("searchCheckIn",  checkIn);
            model.addAttribute("searchCheckOut", checkOut);
        } else {
            model.addAttribute("availableRooms", roomService.getAvailableRooms());
        }
        return "rooms";
    }

    // ── BOOKING FORM ───────────────────────────────────────────────────────────

    @GetMapping("/book/{roomId}")
    public String showBookingForm(@PathVariable Long roomId,
                                   @RequestParam(required = false) String checkIn,
                                   @RequestParam(required = false) String checkOut,
                                   Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";

        model.addAttribute("room", roomService.getRoomById(roomId));

        // Pass strategy name if dates already selected (from rooms page search)
        if (checkIn != null && checkOut != null) {
            LocalDate in  = LocalDate.parse(checkIn);
            LocalDate out = LocalDate.parse(checkOut);
            model.addAttribute("pricingStrategy", bookingService.getStrategyName(in, out));
            model.addAttribute("preCheckIn",  checkIn);
            model.addAttribute("preCheckOut", checkOut);
        }
        return "booking-form";
    }

    // ── CONFIRM BOOKING ────────────────────────────────────────────────────────

    @PostMapping("/book/confirm")
    public String confirmBooking(
            @RequestParam Long roomId,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            HttpSession session, Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        try {
            LocalDate checkInDate  = LocalDate.parse(checkIn);
            LocalDate checkOutDate = LocalDate.parse(checkOut);

            // Capture strategy name BEFORE creating the booking
            String strategyName = bookingService.getStrategyName(checkInDate, checkOutDate);

            com.pesu.hotelmanagement.model.Booking booking =
                    bookingService.createBooking(loggedInUser.getId(), roomId, checkInDate, checkOutDate);

            model.addAttribute("booking", booking);
            model.addAttribute("pricingStrategy", strategyName); // shown on success page
            return "booking-success";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("room", roomService.getRoomById(roomId));
            return "booking-form";
        }
    }

    // ── MY BOOKINGS ────────────────────────────────────────────────────────────

    @GetMapping("/my-bookings")
    public String myBookings(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";
        model.addAttribute("myBookings", bookingService.getBookingsForGuest(loggedInUser.getId()));
        return "my-bookings";
    }

    @PostMapping("/bookings/cancel/{bookingId}")
    public String cancelBooking(@PathVariable Long bookingId,
                                 HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";
        try {
            bookingService.cancelBooking(bookingId, loggedInUser.getId());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/my-bookings";
    }

    // ── AUTH ───────────────────────────────────────────────────────────────────

    @GetMapping("/register")
    public String showRegister() { return "register"; }

    @PostMapping("/register")
    public String processRegister(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {
        if (userService.emailExists(email)) {
            model.addAttribute("error", "An account with this email already exists.");
            return "register";
        }
        userService.registerGuest(name, email, password);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLogin() { return "login"; }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session, Model model) {
        User user = userService.authenticate(email, password);
        if (user != null) {
            session.setAttribute("loggedInUser", user);
            if ("ADMIN".equals(user.getRole())) return "redirect:/admin/dashboard";
            if ("STAFF".equals(user.getRole())) return "redirect:/staff/dashboard";
            return "redirect:/rooms";
        }
        model.addAttribute("error", "Invalid email or password.");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/rooms";
    }
}