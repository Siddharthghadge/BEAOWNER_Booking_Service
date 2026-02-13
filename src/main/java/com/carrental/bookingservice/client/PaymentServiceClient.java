package com.carrental.bookingservice.client;

import com.carrental.bookingservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;

//@FeignClient(name = "PAYMENT-SERVICE", url = "http://localhost:8084",path = "/api/payments/wallet")//
//public interface PaymentServiceClient {
//
//    @PostMapping("/internal/credit")
//    void creditOnOtp(
//            @RequestParam("ownerId") Long ownerId,
//            @RequestParam("ownerEmail") String ownerEmail, // 🔥 Added this
//            @RequestParam("amount") BigDecimal amount,
//            @RequestParam("bookingId") String bookingId
//    );
//
//    @PostMapping("/api/payments/refund")
//    void processRefund(
//            @RequestParam Long userId,
//            @RequestParam String email,
//            @RequestParam BigDecimal amount,
//            @RequestParam String bookingId
//    );
//}

@FeignClient(
        name = "PAYMENT-SERVICE",
        url = "${PAYMENT_SERVICE_URL}",
        configuration = FeignConfig.class // Ensure this is linked!
)
public interface PaymentServiceClient {

    @PostMapping("/api/payments/wallet/internal/credit")
    void creditOnOtp(
            @RequestParam("ownerId") Long ownerId,
            @RequestParam("ownerEmail") String ownerEmail,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("bookingId") String bookingId
    );

    @PostMapping("/api/payments/refund")
    void processRefund(
            @RequestParam("userId") Long userId,
            @RequestParam("email") String email,
            @RequestParam("amount") BigDecimal amount,  // ✅ BigDecimal
            @RequestParam("bookingId") Long bookingId
    );

}