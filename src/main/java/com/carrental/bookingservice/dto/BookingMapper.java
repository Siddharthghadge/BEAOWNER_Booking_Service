


package com.carrental.bookingservice.dto;

import com.carrental.bookingservice.entity.Booking;

public class BookingMapper {

    public static BookingResponse toResponse(Booking booking) {
        if (booking == null) return null;

        BookingResponse res = new BookingResponse();
        res.setId(booking.getId());
        res.setCarId(booking.getCarId());
        res.setBikeId(booking.getBikeId()); // ✅ Added: Map Bike ID
        res.setStatus(booking.getStatus());
        res.setTotalAmount(booking.getTotalAmount());
        res.setPaid(booking.getPaid());
        res.setStartDate(booking.getStartDate());
        res.setEndDate(booking.getEndDate());
        res.setPickupOtp(booking.getPickupOtp());// ✅ Added: Transfer OTP to Response
        res.setCustomerEmail(booking.getCustomerEmail());
        res.setOwnerEmail(booking.getOwnerEmail());

        if (booking.getBookingType() != null) {
            res.setBookingType(booking.getBookingType().name());
        }

        return res;
    }
}