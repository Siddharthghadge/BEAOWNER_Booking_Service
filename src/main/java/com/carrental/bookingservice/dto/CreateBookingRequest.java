package com.carrental.bookingservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    private Long carId;
    private Long bikeId;

    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    @NotBlank(message = "Owner email is required")
    private String ownerEmail;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @NotBlank(message = "Booking type is required")
    private String bookingType;

    // 🔥 ADD THIS FIELD
    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;

    public boolean isValid() {
        return (carId != null || bikeId != null);
    }
}


