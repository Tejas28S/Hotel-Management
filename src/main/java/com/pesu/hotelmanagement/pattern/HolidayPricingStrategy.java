package com.pesu.hotelmanagement.pattern;

import com.pesu.hotelmanagement.model.Room;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Set;

/**
 * STRATEGY PATTERN — Concrete Strategy 3: Holiday / Peak Season Pricing
 *
 * Applies a 50% surcharge on any night that falls on a peak holiday date.
 * Peak dates can be extended easily by adding to PEAK_DATES below.
 */
@Component("holidayPricing")
public class HolidayPricingStrategy implements PricingStrategy {

    private static final double HOLIDAY_SURCHARGE = 1.50; // 50% extra

    // Add/remove peak dates here without touching any other class (Open-Closed Principle)
    private static final Set<MonthDay> PEAK_DATES = Set.of(
        MonthDay.of(12, 24), // Christmas Eve
        MonthDay.of(12, 25), // Christmas Day
        MonthDay.of(12, 31), // New Year's Eve
        MonthDay.of(1,  1),  // New Year's Day
        MonthDay.of(10, 31)  // Halloween
    );

    @Override
    public double calculatePrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        double total = 0;
        LocalDate current = checkIn;

        while (current.isBefore(checkOut)) {
            MonthDay today = MonthDay.from(current);
            if (PEAK_DATES.contains(today)) {
                total += room.getPrice() * HOLIDAY_SURCHARGE;
            } else {
                total += room.getPrice();
            }
            current = current.plusDays(1);
        }
        return total;
    }
}
