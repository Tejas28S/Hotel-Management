package com.pesu.hotelmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.pesu.hotelmanagement.model.Room;
import com.pesu.hotelmanagement.model.RoomStatus;
import com.pesu.hotelmanagement.model.User;
import com.pesu.hotelmanagement.service.BookingService;
import com.pesu.hotelmanagement.service.RoomService;
import com.pesu.hotelmanagement.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private RoomService    roomService;
    @Autowired private UserService    userService;
    @Autowired private BookingService bookingService;

    private boolean isAdmin(HttpSession session) {
        User u = (User) session.getAttribute("loggedInUser");
        return u != null && "ADMIN".equals(u.getRole());
    }

    // ---------------------------------------------------------------
    // DASHBOARD
    // ---------------------------------------------------------------

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("totalRooms",    roomService.getAllRooms().size());
        model.addAttribute("totalBookings", bookingService.getAllBookings().size());
        model.addAttribute("totalGuests",   userService.getAllUsers().size());
        model.addAttribute("staffCount",    userService.getStaffMembers().size());
        return "admin-dashboard";
    }

    // ---------------------------------------------------------------
    // ROOM MANAGEMENT
    // ---------------------------------------------------------------

    @GetMapping("/suites")
    public String manageSuites(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("allRooms", roomService.getAllRooms());
        model.addAttribute("statuses", RoomStatus.values());
        return "admin-suites";
    }

    @PostMapping("/suites/add")
    public String addNewSuite(
            @RequestParam String roomNumber,
            @RequestParam String roomType,
            @RequestParam double price,
            @RequestParam String imageUrl,
            HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        // FACTORY PATTERN — room always starts AVAILABLE
        roomService.addRoom(new Room(roomNumber, roomType, price, RoomStatus.AVAILABLE, imageUrl));
        return "redirect:/admin/suites";
    }

    @PostMapping("/suites/delete/{id}")
    public String deleteRoom(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        roomService.deleteRoom(id);
        return "redirect:/admin/suites";
    }

    @PostMapping("/suites/status/{id}")
    public String updateRoomStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        roomService.updateRoomStatus(id, RoomStatus.valueOf(status));
        return "redirect:/admin/suites";
    }

    // ---------------------------------------------------------------
    // BOOKING MANAGEMENT
    // ---------------------------------------------------------------

    @GetMapping("/bookings")
    public String globalBookings(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("allBookings", bookingService.getAllBookings());
        return "admin-bookings";
    }

    @PostMapping("/bookings/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        User admin = (User) session.getAttribute("loggedInUser");
        try {
            bookingService.cancelBooking(id, admin.getId());
        } catch (Exception ignored) {}
        return "redirect:/admin/bookings";
    }

    // ---------------------------------------------------------------
    // GUEST DIRECTORY
    // ---------------------------------------------------------------

    @GetMapping("/guests")
    public String guestDirectory(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("allGuests",  userService.getAllUsers());
        model.addAttribute("staffList",  userService.getStaffMembers());
        return "admin-guests";
    }

    @PostMapping("/staff/add")
    public String addStaff(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        // FACTORY PATTERN — creates a STAFF user
        userService.registerStaff(name, email, password);
        return "redirect:/admin/guests";
    }
}