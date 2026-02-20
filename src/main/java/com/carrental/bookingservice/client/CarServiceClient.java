package com.carrental.bookingservice.client;

import com.carrental.bookingservice.dto.CarRequest;
import com.carrental.bookingservice.dto.CarResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "car-service",
        url = "${car.service.url}"
)
public interface CarServiceClient {

    @GetMapping("/api/cars/available")
    List<CarResponse> getAvailableCars();

    @PutMapping("/api/cars/{id}/status")
    void updateCarStatus(@PathVariable("id") Long id, @RequestParam("status") String status);

    @GetMapping("/api/cars/{id}")
    CarResponse getCarById(@PathVariable("id") Long id);
}
