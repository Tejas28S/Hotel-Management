package com.pesu.hotelmanagement;

import com.pesu.hotelmanagement.model.Room;
import com.pesu.hotelmanagement.model.RoomStatus;
import com.pesu.hotelmanagement.repository.RoomRepository;
import com.pesu.hotelmanagement.repository.UserRepository;
import com.pesu.hotelmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * SINGLETON PATTERN (via Spring) — runs exactly once on application startup.
 *
 * Seeds the database with:
 *   1. A default ADMIN account  (admin@elitestays.com / admin123)
 *   2. A default STAFF account  (staff@elitestays.com / staff123)
 *   3. Three sample rooms if none exist yet
 *
 * Uses the FACTORY PATTERN (UserFactory via UserService) for all user creation.
 * Safe to re-run: checks for existing data before inserting.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private UserService    userService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoomRepository roomRepository;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedStaff();
        seedRooms();
    }

    private void seedAdmin() {
        if (userRepository.findByEmail("admin@elitestays.com").isEmpty()) {
            userService.registerAdmin("Hotel Manager", "admin@elitestays.com", "admin123");
            System.out.println("[SEED] Admin account created → admin@elitestays.com / admin123");
        }
    }

    private void seedStaff() {
        if (userRepository.findByEmail("staff@elitestays.com").isEmpty()) {
            userService.registerStaff("Front Desk Staff", "staff@elitestays.com", "staff123");
            System.out.println("[SEED] Staff account created → staff@elitestays.com / staff123");
        }
    }

    private void seedRooms() {
        if (roomRepository.count() == 0) {
            roomRepository.save(new Room("101", "Executive Suite", 8500,  RoomStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800&q=80"));
            roomRepository.save(new Room("PH-01", "Skyline Penthouse", 25000, RoomStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?w=800&q=80"));
            roomRepository.save(new Room("V-01", "Oceanfront Villa", 40000, RoomStatus.AVAILABLE,
                "https://images.unsplash.com/photo-1582268611958-ebfd161ef9cf?w=800&q=80"));
            System.out.println("[SEED] 3 sample rooms created.");
        }
    }
}
