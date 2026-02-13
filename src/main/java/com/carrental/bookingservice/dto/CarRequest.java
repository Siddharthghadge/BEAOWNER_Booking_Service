package com.carrental.bookingservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarRequest {

    private Long ownerId;
    private String brand;
    private String model;
    private String vehicleNumber;
    private Double pricePerHour;
    private boolean available;
}
