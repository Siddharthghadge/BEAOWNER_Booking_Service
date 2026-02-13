
package com.carrental.bookingservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "bookings",
        indexes = {
                @Index(name = "idx_user", columnList = "user_id"),
                @Index(name = "idx_car", columnList = "car_id"),
                @Index(name = "idx_bike", columnList = "bike_id"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_customer_email", columnList = "customer_email") // ✅ useful for lookups
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---------------- USER DETAILS ----------------
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;

    /**
     * Email of the customer who created the booking.
     * Stored for internal use (notifications, audit).
     * Not exposed to frontend.
     */
    @Column(name = "customer_email", nullable = false, length = 150)
    private String customerEmail;

    // ---------------- VEHICLE DETAILS ----------------
    // Nullable because booking can be for CAR or BIKE

    @Column(name = "car_id")
    private Long carId;

    @Column(name = "bike_id")
    private Long bikeId;

    // ---------------- BOOKING TIMINGS ----------------
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endDate;

    // ---------------- STATUS & PAYMENT ----------------
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private Boolean paid = Boolean.FALSE;

    // SELF_DRIVE / WITH_DRIVER
    @Enumerated(EnumType.STRING)
    private ServiceType bookingType;

    // ---------------- OTP & VERSIONING ----------------
    private String pickupOtp;

    @Version
    private Long version;

    // ---------------- AUDIT FIELDS ----------------
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
