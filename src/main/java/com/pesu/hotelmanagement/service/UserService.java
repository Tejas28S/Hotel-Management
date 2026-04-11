package com.pesu.hotelmanagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.pesu.hotelmanagement.model.User;
import com.pesu.hotelmanagement.pattern.UserFactory;
import com.pesu.hotelmanagement.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * BCrypt password encoder — SINGLETON-like (Spring manages one instance).
     * Passwords are hashed before storage; plaintext is never saved to the DB.
     */
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // ---------------------------------------------------------------
    // REGISTRATION (uses FACTORY PATTERN)
    // ---------------------------------------------------------------

    /**
     * FACTORY PATTERN — public sign-up always creates a GUEST.
     * Role cannot be injected by the caller; the factory enforces it.
     */
    public User registerGuest(String name, String email, String password) {
        User user = UserFactory.createGuest(name, email, encoder.encode(password));
        return userRepository.save(user);
    }

    /**
     * FACTORY PATTERN — admin creates a STAFF account.
     */
    public User registerStaff(String name, String email, String password) {
        User user = UserFactory.createStaff(name, email, encoder.encode(password));
        return userRepository.save(user);
    }

    /**
     * FACTORY PATTERN — seeding / bootstrap creates an ADMIN account.
     */
    public User registerAdmin(String name, String email, String password) {
        User user = UserFactory.createAdmin(name, email, encoder.encode(password));
        return userRepository.save(user);
    }

    // ---------------------------------------------------------------
    // AUTHENTICATION
    // ---------------------------------------------------------------

    /**
     * Authenticates a user by comparing the supplied plaintext password
     * against the stored BCrypt hash. Returns the User on success, null on failure.
     */
    public User authenticate(String email, String password) {
        Optional<User> found = userRepository.findByEmail(email);
        if (found.isPresent() && encoder.matches(password, found.get().getPassword())) {
            return found.get();
        }
        return null;
    }

    // ---------------------------------------------------------------
    // QUERIES
    // ---------------------------------------------------------------

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getStaffMembers() {
        return userRepository.findByRole("STAFF");
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}