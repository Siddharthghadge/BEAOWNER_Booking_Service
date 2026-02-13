package com.carrental.bookingservice.controller;

import com.carrental.bookingservice.dto.BookingMapper;
import com.carrental.bookingservice.dto.BookingResponse;
import com.carrental.bookingservice.dto.CreateBookingRequest;
import com.carrental.bookingservice.dto.UserResponse;
import com.carrental.bookingservice.entity.Booking;
import com.carrental.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    // ---------------- CREATE BOOKING ----------------
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication
    ) {
        // ✅ Log the request to find exactly why it might fail
        log.info("Received Booking Request: {}", request);
        String email = authentication.getName();

        Booking booking = bookingService.createBooking(request, email);

        return ResponseEntity.ok(
                BookingMapper.toResponse(booking)
        );
    }

    // ---------------- GET MY BOOKINGS ----------------
    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER','ROLE_ADMIN')")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            Authentication authentication
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                bookingService.getMyBookings(email)
                        .stream()
                        .map(BookingMapper::toResponse)
                        .toList()
        );
    }

    // ---------------- GET BOOKING BY ID ----------------
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER','ROLE_ADMIN')")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();

        Booking booking = bookingService.getBookingById(id, email);

        return ResponseEntity.ok(
                BookingMapper.toResponse(booking)
        );
    }

    // ---------------- CANCEL BOOKING ----------------
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER','ROLE_ADMIN')")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();

        bookingService.cancelBooking(id, email);
        return ResponseEntity.noContent().build();
    }

    // ---------------- CONFIRM BOOKING ----------------
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER','ROLE_ADMIN')")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable Long id,
            @RequestParam String paymentTxnId
    ) {
        Booking booking =
                bookingService.confirmBooking(id, paymentTxnId);

        return ResponseEntity.ok(
                BookingMapper.toResponse(booking)
        );
    }
    // ---------------- INTERNAL CONFIRM BOOKING ----------------
    // Inside BookingController.java (Booking Service)

    @PostMapping("/{id}/confirm/internal")
    public ResponseEntity<Void> confirmBookingInternal(
            @PathVariable Long id,
            @RequestParam String paymentTxnId,
            @RequestHeader(value = "X-Internal-Call", required = false) String internalHeader
    ) {
        // Logic to confirm the booking
        bookingService.confirmBooking(id, paymentTxnId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{bookingId}/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @PathVariable Long bookingId,
            @RequestParam String otp) {

        // This triggers the whole chain: Status Change -> Wallet Credit -> Audit Log
        String result = bookingService.verifyPickupOtp(bookingId, otp);
        return ResponseEntity.ok(result);
    }

    // ---------------- GET OWNER BOOKINGS ----------------
    @GetMapping("/owner")
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    public ResponseEntity<List<BookingResponse>> getOwnerBookings(
            Authentication authentication
    ) {
        String ownerEmail = authentication.getName(); // ✅ JWT subject

        return ResponseEntity.ok(
                bookingService.getBookingsForOwner(ownerEmail)
                        .stream()
                        .map(BookingMapper::toResponse)
                        .toList()
        );
    }

    // ---------------- ADMIN: GET ALL BOOKINGS ----------------
    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(
                bookingService.getAllBookings() // Ensure this exists in your Service
                        .stream()
                        .map(BookingMapper::toResponse)
                        .toList()
        );
    }

    // ---------------- ADMIN: PROCESS REFUND ----------------
    @PostMapping("/admin/refund/{bookingId}")
    public ResponseEntity<String> processRefund(
            @PathVariable Long bookingId,
            @RequestParam BigDecimal amount   // ✅ BigDecimal
    ) {
        bookingService.processRefund(bookingId, amount);
        return ResponseEntity.ok("Refund processed successfully");
    }


}
