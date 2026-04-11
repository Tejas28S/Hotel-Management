package com.pesu.hotelmanagement.pattern;

import com.pesu.hotelmanagement.model.User;

/**
 * FACTORY PATTERN — UserFactory
 *
 * Centralises all user creation logic. Instead of manually setting the role
 * field in multiple places (controllers, seed data, tests), callers use this
 * factory and the role is always set correctly by one authoritative place.
 *
 * This also makes it easy to add per-role defaults in the future
 * (e.g., giving STAFF a default department, or GUEST a loyalty tier).
 */
public class UserFactory {

    /**
     * Creates a new GUEST user — the default role for public sign-ups.
     */
    public static User createGuest(String name, String email, String password) {
        return new User(name, email, password, "GUEST");
    }

    /**
     * Creates a new STAFF user — assigned by an administrator.
     * Staff can perform check-in / check-out and update room status.
     */
    public static User createStaff(String name, String email, String password) {
        return new User(name, email, password, "STAFF");
    }

    /**
     * Creates a new ADMIN user — highest privilege level.
     * Should only be called from admin-level controllers or seed scripts.
     */
    public static User createAdmin(String name, String email, String password) {
        return new User(name, email, password, "ADMIN");
    }
}
