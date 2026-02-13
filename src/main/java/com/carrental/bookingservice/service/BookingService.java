package com.carrental.bookingservice.service;

import com.carrental.bookingservice.dto.CreateBookingRequest;
import com.carrental.bookingservice.entity.Booking;

import java.math.BigDecimal;
import java.util.List;

public interface BookingService {

    Booking createBooking(CreateBookingRequest request, String email);

    Booking getBookingById(Long bookingId, String email);

    List<Booking> getMyBookings(String email);

    void cancelBooking(Long bookingId, String email);

    Booking confirmBooking(Long bookingId, String paymentTxnId);

    String verifyPickupOtp(Long bookingId, String enteredOtp);

    List<Booking> getBookingsForOwner(String ownerEmail);

    // --- Admin Methods ---
    List<Booking> getAllBookings(); // For the Admin Table

    void processRefund(Long bookingId, BigDecimal amount);

    // For the Refund Button

}
