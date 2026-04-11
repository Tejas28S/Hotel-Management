package com.pesu.hotelmanagement.pattern;

import com.pesu.hotelmanagement.model.Room;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * STRATEGY PATTERN — Concrete Strategy 1: Standard Pricing
 *
 * The default, no-frills formula:  totalPrice = pricePerNight × numberOfNights
 */
@Component("standardPricing")
public class StandardPricingStrategy implements PricingStrategy {

    @Override
    public double calculatePrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) throw new IllegalArgumentException("Check-out must be after check-in.");
        return room.getPrice() * nights;
    }
}
