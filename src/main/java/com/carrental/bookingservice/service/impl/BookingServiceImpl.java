

package com.carrental.bookingservice.service.impl;

import com.carrental.bookingservice.client.BikeClient;
import com.carrental.bookingservice.client.CarServiceClient;
import com.carrental.bookingservice.client.PaymentServiceClient;
import com.carrental.bookingservice.client.UserServiceClient;
import com.carrental.bookingservice.dto.*;
import com.carrental.bookingservice.entity.*;
import com.carrental.bookingservice.repository.BookingRepository;
import com.carrental.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserServiceClient userServiceClient;
    private final CarServiceClient carServiceClient;
    private final BikeClient bikeClient;
    private final RestTemplate restTemplate;
    private final PaymentServiceClient paymentServiceClient;

    @Value("${notification.service.url}")
    private String notificationServiceUrl;



    // ================= CREATE BOOKING =================
    @Override
    @Transactional
    public Booking createBooking(CreateBookingRequest request, String email) {

        if (request.getStartDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Start date cannot be in the past.");
        }

        UserResponse user = userServiceClient.getUserByEmail(email);

        BigDecimal calculatedAmount;
        String fetchedOwnerEmail;
        Long fetchedOwnerId;

        if (request.getBikeId() != null) {
            validateBookingGap(request.getBikeId(), request.getStartDate(), true);
            BikeDTO bike = bikeClient.getBikeById(request.getBikeId());

            if (!bike.isAvailable()) throw new RuntimeException("Bike is currently booked.");

            boolean isLocked = bookingRepository.existsOverlappingBikeBooking(
                    request.getBikeId(), request.getStartDate(), request.getEndDate());
            if (isLocked) throw new RuntimeException("Bike reserved for these dates.");

            calculatedAmount = calculateBikeAmount(request, bike);
            fetchedOwnerEmail = bike.getOwnerEmail();
            fetchedOwnerId = bike.getOwnerId(); // ✅ Get ID from the Bike Service

        } else if (request.getCarId() != null) {
            validateBookingGap(request.getCarId(), request.getStartDate(), false);

            boolean isLocked = bookingRepository.existsOverlappingCarBooking(
                    request.getCarId(), request.getStartDate(), request.getEndDate());
            if (isLocked) throw new RuntimeException("Car reserved for these dates.");

            CarResponse car = carServiceClient.getCarById(request.getCarId());
            calculatedAmount = calculateCarAmount(request, car);
            fetchedOwnerEmail = car.getOwnerEmail();
            fetchedOwnerId = car.getOwnerId(); // ✅ Get ID from the Car Service

        } else {
            throw new RuntimeException("No Vehicle ID provided.");
        }

        String pickupOtp = String.format("%06d", new Random().nextInt(999999));

        Booking booking = Booking.builder()
                .userId(user.getId())
                .customerEmail(email)
                // Use FETCHED values instead of REQUEST values for security and reliability
                .ownerId(fetchedOwnerId)
                .ownerEmail(fetchedOwnerEmail)
                .carId(request.getCarId())
                .bikeId(request.getBikeId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(BookingStatus.PENDING)
                .totalAmount(calculatedAmount) // 👈 Use the service-calculated amount
                .paid(false)
                .pickupOtp(pickupOtp)
                .bookingType(ServiceType.valueOf(request.getBookingType().toUpperCase()))
                .createdAt(LocalDateTime.now())
                .build();

        return bookingRepository.save(booking);
    }

    // ================= CONFIRM BOOKING =================
    @Override
    @Transactional
    public Booking confirmBooking(Long bookingId, String paymentTxnId) {
        log.info("Finalizing confirmation for Booking ID: {} | Payment ID: {}", bookingId, paymentTxnId);

        // 1. Fetch the booking or throw error
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 2. Update status and payment flag
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaid(true);

        // 3. Generate Pickup OTP if not already present
        if (booking.getPickupOtp() == null) {
            booking.setPickupOtp(String.format("%06d", new Random().nextInt(999999)));
        }

        // 4. Save and Flush to Database
        // This will persist ownerId and ownerEmail which are mandatory in your entity
        Booking savedBooking = bookingRepository.saveAndFlush(booking);

        // 5. Update External Service Vehicle Status
        try {
            if (savedBooking.getCarId() != null) {
                carServiceClient.updateCarStatus(savedBooking.getCarId(), "BOOKED");
            } else if (savedBooking.getBikeId() != null) {
                bikeClient.updateAvailability(savedBooking.getBikeId(), false);
            }
        } catch (Exception e) {
            // We log a warning but don't stop the flow if the remote status update fails
            log.warn("Remote vehicle status update failed: {}", e.getMessage());
        }

        // 6. Trigger existing notification flow (Customer + Owner)
        triggerNotificationFlow(savedBooking);

        return savedBooking;
    }

    // ================= NOTIFICATION =================
    private void triggerNotificationFlow(Booking booking) {

        String customerEmail = booking.getCustomerEmail();
        String customerName = "Customer";
        String ownerEmail = booking.getOwnerEmail();
        String ownerPhone = "N/A";   // 🔥 ADD THIS

        try {
            UserResponse customer = userServiceClient.getUserByEmail(customerEmail);
            customerName = customer.getName();
        } catch (Exception ignored) {}

        try {
            UserResponse owner = userServiceClient.getUserByEmail(ownerEmail);
            ownerPhone = owner.getPhone();   // 🔥 FETCH OWNER PHONE
        } catch (Exception e) {
            log.warn("Owner phone fetch failed: {}", e.getMessage());
        }

        String vehicleName = "Reserved Vehicle";
        String pickupLocation = "Pickup Location";

        try {
            if (booking.getCarId() != null) {
                CarResponse car = carServiceClient.getCarById(booking.getCarId());
                vehicleName = car.getBrand() + " " + car.getModel();
                pickupLocation = car.getLocation();
            } else if (booking.getBikeId() != null) {
                BikeDTO bike = bikeClient.getBikeById(booking.getBikeId());
                vehicleName = bike.getBrand() + " " + bike.getModel();
                pickupLocation = bike.getLocation();
            }
        } catch (Exception e) {
            log.error("Vehicle fetch failed: {}", e.getMessage());
        }

        NotificationRequest request = NotificationRequest.builder()
                .customerEmail(customerEmail)
                .customerName(customerName)
                .ownerEmail(ownerEmail)
                .ownerPhone(ownerPhone)   // 🔥 ADD THIS
                .carModel(vehicleName)
                .pickupLocation(pickupLocation)
                .pickupOtp(booking.getPickupOtp())
                .bookingType(booking.getBookingType().name())
                .build();

        restTemplate.postForObject(
                notificationServiceUrl + "/booking-success",
                request,
                String.class
        );
    }


    // ================= OTP VERIFICATION =================
    @Override
    @Transactional
    public String verifyPickupOtp(Long bookingId, String enteredOtp) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!enteredOtp.equals(booking.getPickupOtp())) {
            throw new RuntimeException("Invalid OTP.");
        }

        // 1. Mark booking as COMPLETED
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        try {
            // 2. LOG THE ATTEMPT (Check your console for this!)
            log.info("Attempting to credit owner: {} for booking: {}", booking.getOwnerEmail(), bookingId);

            // Fetch user once to ensure they exist
            UserResponse owner = userServiceClient.getUserByEmail(booking.getOwnerEmail());

            // 3. TRIGGER PAYMENT
            paymentServiceClient.creditOnOtp(
                    owner.getId(),
                    booking.getOwnerEmail(),
                    booking.getTotalAmount(),
                    booking.getId().toString()
            );

            log.info("Payment service call successful for booking: {}", bookingId);

        } catch (Exception e) {
            log.error("CRITICAL: Payment service failed for booking " + bookingId, e);
            // Throwing the exception here will let the frontend know the payment part failed
            throw new RuntimeException("OTP Verified, but Payment settlement failed: " + e.getMessage());
        }

        return "OTP verified. Booking completed and owner credited.";
    }

    // ================= OWNER BOOKINGS =================
    @Override
    @Transactional(readOnly = true)
    public List<Booking> getBookingsForOwner(String ownerEmail) {
        return bookingRepository.findByOwnerEmail(ownerEmail);
    }

    // ================= OTHER METHODS =================
    @Override
    public List<Booking> getMyBookings(String email) {
        UserResponse user = userServiceClient.getUserByEmail(email);
        return bookingRepository.findByUserId(user.getId());
    }

    @Override
    public Booking getBookingById(Long id, String email) {
        return bookingRepository.findById(id).orElseThrow();
    }

    @Override
    public void cancelBooking(Long id, String email) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Scheduled(fixedRate = 60000)
    public void autoCancelUnpaidBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        List<Booking> expired =
                bookingRepository.findByStatusAndPaidFalseAndCreatedAtBefore(
                        BookingStatus.PENDING, threshold);
        expired.forEach(b -> b.setStatus(BookingStatus.CANCELLED));
        bookingRepository.saveAll(expired);
    }

    // ================= HELPERS =================
    private void validateBookingGap(Long vehicleId, LocalDateTime newStart, boolean isBike) {
        Optional<Booking> lastBookingOpt = isBike
                ? bookingRepository.findTopByBikeIdAndStatusNotOrderByEndDateDesc(vehicleId, BookingStatus.CANCELLED)
                : bookingRepository.findTopByCarIdAndStatusNotOrderByEndDateDesc(vehicleId, BookingStatus.CANCELLED);

        lastBookingOpt.ifPresent(lastBooking -> {
            long hoursGap = ChronoUnit.HOURS.between(lastBooking.getEndDate(), newStart);
            if (hoursGap < 12) {
                throw new RuntimeException("12-hour gap required.");
            }
        });
    }

    private BigDecimal calculateCarAmount(CreateBookingRequest request, CarResponse car) {
        long hours = Math.max(1, ChronoUnit.HOURS.between(
                request.getStartDate(), request.getEndDate()));
        return car.getPricePerHour().multiply(BigDecimal.valueOf(hours));
    }

    private BigDecimal calculateBikeAmount(CreateBookingRequest request, BikeDTO bike) {
        long hours = Math.max(1, ChronoUnit.HOURS.between(
                request.getStartDate(), request.getEndDate()));
        return BigDecimal.valueOf(bike.getPricePerHour())
                .multiply(BigDecimal.valueOf(hours));
    }

    // ================= ADMIN: GET ALL BOOKINGS =================
    @Override
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        log.info("Admin fetching all system bookings");
        return bookingRepository.findAll();
    }

    // ================= ADMIN: PROCESS REFUND =================
    @Override
    @Transactional
    public void processRefund(Long bookingId, BigDecimal amount) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getPaid() == null || !booking.getPaid()) {
            throw new RuntimeException("Cannot refund an unpaid booking.");
        }

        if (booking.getStatus() == BookingStatus.REFUNDED) {
            throw new RuntimeException("Booking already refunded.");
        }

        try {
            paymentServiceClient.processRefund(
                    booking.getUserId(),
                    booking.getCustomerEmail(),
                    amount,       // ✅ BigDecimal
                    bookingId
            );

            booking.setStatus(BookingStatus.REFUNDED);
            bookingRepository.save(booking);

        } catch (Exception e) {
            throw new RuntimeException("Refund Failed: " + e.getMessage());
        }
    }

}
