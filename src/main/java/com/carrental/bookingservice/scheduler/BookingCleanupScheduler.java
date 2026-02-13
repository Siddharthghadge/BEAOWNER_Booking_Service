package com.carrental.bookingservice.scheduler;

import com.carrental.bookingservice.entity.Booking;
import com.carrental.bookingservice.entity.BookingStatus;
import com.carrental.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCleanupScheduler {

    private final BookingRepository bookingRepository;

    @Value("${booking.hold.ttl-minutes:15}")
    private long bookingHoldTtlMinutes;

    @Scheduled(fixedDelayString = "${booking.cleanup.interval-ms:60000}") // Checks every minute
    public void cancelStaleBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(bookingHoldTtlMinutes);

        // Uses the corrected repository method
        List<Booking> staleBookings = bookingRepository.findByStatusAndPaidFalseAndCreatedAtBefore(
                BookingStatus.PENDING,
                cutoff
        );

        if (staleBookings.isEmpty()) return;

        log.info("Auto-cancelling {} stale bookings older than {} minutes", staleBookings.size(), bookingHoldTtlMinutes);

        for (Booking booking : staleBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
        }

        bookingRepository.saveAll(staleBookings);
    }
}