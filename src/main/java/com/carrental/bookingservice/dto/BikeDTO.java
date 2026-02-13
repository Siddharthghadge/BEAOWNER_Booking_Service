//package com.carrental.bookingservice.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.List;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class BikeDTO {
//    private Long id;
//    private String brand;
//    private String model;
//    private Integer engineCc;
//    private String bikeType;
//    private Double pricePerHour;
//    private String location;
//    private boolean isAvailable;
//    private List<String> imageUrls;
//}

package com.carrental.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BikeDTO {
    private Long id;
    private String brand;
    private String model;
    private Integer engineCc;
    private String bikeType;
    private Double pricePerHour;
    private String location; // Existing field
    private boolean isAvailable;
    private List<String> imageUrls;

    // --- Added for Notifications ---
    private String ownerMobile;
    private String pickupAddress;
    private String ownerEmail;
    private Long ownerId;
// Usually maps to location
}