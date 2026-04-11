package com.pesu.hotelmanagement.pattern;

/**
 * SINGLETON PATTERN — Application Configuration Manager
 *
 * Ensures there is exactly one configuration object shared across the entire
 * application. The instance is created lazily on first access and is
 * thread-safe via double-checked locking.
 *
 * In this project it acts as a central place for runtime settings such as
 * the hotel name, currency, and max booking window — values that are read
 * by services and controllers but never need more than one copy in memory.
 */
public class AppConfig {

    private static volatile AppConfig instance;

    // --- Configuration properties ---
    private final String hotelName   = "Elite Stays";
    private final String currency    = "INR";
    private final int    maxBookingWindowDays = 365; // How far ahead a guest can book
    private final int    minBookingNights     = 1;

    // Private constructor prevents direct instantiation
    private AppConfig() {}

    /**
     * Returns the single shared instance, creating it on first call.
     * Thread-safe via double-checked locking.
     */
    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) {
                    instance = new AppConfig();
                }
            }
        }
        return instance;
    }

    // --- Getters (read-only — no setters to keep config immutable) ---
    public String getHotelName()            { return hotelName; }
    public String getCurrency()             { return currency; }
    public int    getMaxBookingWindowDays() { return maxBookingWindowDays; }
    public int    getMinBookingNights()     { return minBookingNights; }
}
