package com.pesu.hotelmanagement.pattern;

import com.pesu.hotelmanagement.model.Room;
import java.time.LocalDate;

/**
 * STRATEGY PATTERN — Pricing Strategy
 *
 * Defines a family of pricing algorithms that can be swapped at runtime.
 * Instead of a single hardcoded formula, the BookingService delegates
 * to whichever strategy is most appropriate for the booking's dates.
 *
 * Concrete implementations:
 *   - StandardPricingStrategy  (baseline: price × nights)
 *   - WeekendPricingStrategy   (25% surcharge on Fri/Sat nights)
 *   - HolidayPricingStrategy   (50% surcharge on peak holiday dates)
 */
public interface PricingStrategy {
    double calculatePrice(Room room, LocalDate checkIn, LocalDate checkOut);
}
