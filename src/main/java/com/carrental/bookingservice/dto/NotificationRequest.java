package com.carrental.bookingservice.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    private String customerEmail;
    private String customerName;
    private String customerPhone;
    private String ownerName;
    private String ownerPhone;
    private String carModel; // Change from vehicleModel to carModel
    private String pickupLocation;
    private String pickupOtp;
    private String bookingType;
    private String termsAndConditions;
    private String customerAddress;
    private String ownerEmail;

}