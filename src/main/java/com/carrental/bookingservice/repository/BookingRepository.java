
package com.carrental.bookingservice.repository;

import com.carrental.bookingservice.entity.Booking;
import com.carrental.bookingservice.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByStatusAndPaidFalseAndCreatedAtBefore(BookingStatus status, LocalDateTime dateTime);

    // Get the latest booking for a car to check the 12-hour gap
    Optional<Booking> findTopByCarIdAndStatusNotOrderByEndDateDesc(Long carId, BookingStatus status);

    // Get the latest booking for a bike to check the 12-hour gap
    Optional<Booking> findTopByBikeIdAndStatusNotOrderByEndDateDesc(Long bikeId, BookingStatus status);

    List<Booking> findByOwnerEmail(String ownerEmail);




    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.carId = :carId " +
            "AND b.status IN (com.carrental.bookingservice.entity.BookingStatus.PENDING, " +
            "com.carrental.bookingservice.entity.BookingStatus.CONFIRMED) " +
            "AND (:startDate < b.endDate AND :endDate > b.startDate)")
    boolean existsOverlappingCarBooking(
            @Param("carId") Long carId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.bikeId = :bikeId " +
            "AND b.status IN (com.carrental.bookingservice.entity.BookingStatus.PENDING, " +
            "com.carrental.bookingservice.entity.BookingStatus.CONFIRMED) " +
            "AND (:startDate < b.endDate AND :endDate > b.startDate)")
    boolean existsOverlappingBikeBooking(
            @Param("bikeId") Long bikeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}