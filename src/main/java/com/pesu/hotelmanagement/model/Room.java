package com.pesu.hotelmanagement.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomNumber;

    @Column(nullable = false)
    private String roomType;

    @Column(nullable = false)
    private double price;

    /**
     * Replaces the old boolean isAvailable.
     * Supports the full 5-state room lifecycle from the spec:
     * AVAILABLE → RESERVED → OCCUPIED → CLEANING → AVAILABLE
     *                      ↘ UNDER_MAINTENANCE ↗
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status = RoomStatus.AVAILABLE;

    @Column(length = 500)
    private String imageUrl;

    public Room() {}

    public Room(String roomNumber, String roomType, double price, RoomStatus status, String imageUrl) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.price = price;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /**
     * Convenience helper: a room is bookable only when AVAILABLE.
     * Keeps Thymeleaf templates and old queries working without changes.
     */
    public boolean isAvailable() {
        return this.status == RoomStatus.AVAILABLE;
    }
}