package com.pesu.hotelmanagement.pattern;

import com.pesu.hotelmanagement.model.Room;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * STRATEGY PATTERN — Concrete Strategy 2: Weekend Pricing
 *
 * Applies a 25% surcharge for each night that falls on a Friday or Saturday.
 * All other nights are billed at the standard rate.
 */
@Component("weekendPricing")
public class WeekendPricingStrategy implements PricingStrategy {

    private static final double WEEKEND_SURCHARGE = 1.25; // 25% extra

    @Override
    public double calculatePrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        double total = 0;
        LocalDate current = checkIn;

        while (current.isBefore(checkOut)) {
            DayOfWeek day = current.getDayOfWeek();
            if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) {
                total += room.getPrice() * WEEKEND_SURCHARGE;
            } else {
                total += room.getPrice();
            }
            current = current.plusDays(1);
        }
        return total;
    }
}
