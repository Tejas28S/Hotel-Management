package com.pesu.hotelmanagement.controller;

/**
 * This REST controller has been superseded by the MVC auth routes in WebController
 * (/register, /login, /logout) and is kept as a placeholder only.
 *
 * All user registration now goes through UserService.registerGuest() which:
 *   - Uses the FACTORY PATTERN to enforce role assignment
 *   - Hashes passwords with BCrypt before storage
 */
public class UserController {
    // Intentionally empty — see WebController for auth endpoints.
}
