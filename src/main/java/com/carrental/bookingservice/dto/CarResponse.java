
package com.carrental.bookingservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarResponse {
    @JsonProperty("carId")
    private Long id;
    private Long ownerId;
    private String brand;
    private String model;
    private String vehicleNumber;
    private BigDecimal pricePerHour;
    private BigDecimal driverChargesPerDay;
    private boolean available;

    // --- Added for Notifications ---
    private String ownerMobile;
    private String location;
    private String ownerEmail;
// This acts as the Pickup Location
}