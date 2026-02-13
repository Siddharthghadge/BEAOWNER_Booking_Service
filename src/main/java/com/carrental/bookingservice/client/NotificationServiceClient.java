package com.carrental.bookingservice.client;

import com.carrental.bookingservice.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// src/main/java/com/carrental/bookingservice/client/NotificationServiceClient.java
@FeignClient(name = "notification-service", url = "${NOTIFICATION_SERVICE_URL}")
public interface NotificationServiceClient {
    @PostMapping("/api/notifications/booking-success")
    String sendNotification(@RequestBody NotificationRequest request);
}
