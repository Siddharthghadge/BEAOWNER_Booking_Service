package com.carrental.bookingservice.client;

import com.carrental.bookingservice.dto.BikeDTO;
import com.carrental.bookingservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "bike-service",
        url = "${bike.service.url}",
        configuration = FeignConfig.class // ✅ token forwarding stays
)
public interface BikeClient {

    @GetMapping("/api/bikes/{id}")
    BikeDTO getBikeById(@PathVariable("id") Long id);

    @PutMapping("/api/bikes/{id}/availability")
    void updateAvailability(
            @PathVariable("id") Long id,
            @RequestParam("status") boolean status
    );
}
