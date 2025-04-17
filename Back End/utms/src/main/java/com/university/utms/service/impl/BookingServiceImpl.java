package com.university.utms.service.impl;

import com.university.utms.model.Booking;
import com.university.utms.model.Bus;
import com.university.utms.model.Schedule;
import com.university.utms.model.User;
import com.university.utms.repository.BusRepository;
import com.university.utms.repository.UserRepository;
import com.university.utms.repository.ScheduleRepository;
import com.university.utms.repository.BookingRepository;
import com.university.utms.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BusRepository busRepository;
    private final UserRepository userRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    public BookingServiceImpl(
            BookingRepository bookingRepository,
            BusRepository busRepository,
            UserRepository userRepository,
            ScheduleRepository scheduleRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.busRepository = busRepository;
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    @Override
    public Booking createBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    @Override
    public Booking updateBooking(Long id, Booking booking) {
        if (bookingRepository.existsById(id)) {
            booking.setId(id);
            return bookingRepository.save(booking);
        }
        return null;
    }

    @Override
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    @Override
    public Booking reserveSeats(Long scheduleId, int seatCount, String userEmail) {
        if (seatCount < 1 || seatCount > 4) {
            throw new RuntimeException("Seat count must be between 1 and 4.");
        }

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        Bus bus = schedule.getBus();
        if (bus == null) {
            throw new RuntimeException("Bus not found for schedule");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSchedule(schedule);
        booking.setSeatCount(seatCount);
        booking.setStatus(Booking.Status.booked);
        booking.setPaymentStatus(Booking.PaymentStatus.pending);

        if (bus.getAvailableSeats() >= seatCount) {
            bus.setAvailableSeats(bus.getAvailableSeats() - seatCount);
            booking.setStanding(false);
        } else {
            long standingBookings = bookingRepository.countByScheduleIdAndStandingTrue(scheduleId);
            if (standingBookings + seatCount > bus.getStandingCapacity()) {
                throw new RuntimeException("Standing tickets sold out.");
            }
            booking.setStanding(true);
        }

        bookingRepository.save(booking);
        busRepository.save(bus);

        return booking;
    }


    @Override
    public void cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Only allow the user who made the booking to cancel
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized to cancel this booking");
        }

        // Check if already cancelled
        if (booking.getStatus() == Booking.Status.cancelled) {
            throw new RuntimeException("Booking is already cancelled");
        }

        Bus bus = booking.getSchedule().getBus();

        if (!booking.isStanding()) {
            bus.setAvailableSeats(bus.getAvailableSeats() + booking.getSeatCount());
            busRepository.save(bus);
        }

        booking.setStatus(Booking.Status.cancelled);
        bookingRepository.save(booking);
    }
    @Override
    public Booking markAsPaid(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized payment attempt.");
        }

        if (booking.getPaymentStatus() == Booking.PaymentStatus.paid) {
            throw new RuntimeException("Booking already paid.");
        }

        booking.setPaymentStatus(Booking.PaymentStatus.paid);

        // Generate QR code containing booking info
        String qrContent = "BookingID: " + booking.getId() +
                ", UserID: " + booking.getUser().getId() +
                ", BusID: " + booking.getSchedule().getBus().getId();

        try {
            String qrCodeBase64 = com.university.utms.util.QrCodeGenerator.generateQrCodeBase64(qrContent, 250, 250);
            booking.setQrCode(qrCodeBase64);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage());
        }

        return bookingRepository.save(booking);
    }

    @Override
    public List<Booking> getBookingsByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookingRepository.findAllByUser(user);
    }

    @Override
    public List<Booking> getBookingsByBus(Long busId) {
        List<Schedule> schedules = scheduleRepository.findByBusId(busId);
        List<Booking> bookings = new ArrayList<>();
        for (Schedule schedule : schedules) {
            bookings.addAll(bookingRepository.findAllBySchedule(schedule));
        }
        return bookings;
    }

    @Override
    public List<Booking> getAllBookingsForAdmin(String userEmail, Long busId, String status, String paymentStatus) {
        List<Booking> bookings = bookingRepository.findAll();

        if (userEmail != null) {
            bookings.removeIf(b -> !b.getUser().getEmail().equalsIgnoreCase(userEmail));
        }

        if (busId != null) {
            bookings.removeIf(b -> !b.getSchedule().getBus().getId().equals(busId));
        }

        if (status != null) {
            try {
                Booking.Status st = Booking.Status.valueOf(status.toLowerCase());
                bookings.removeIf(b -> b.getStatus() != st);
            } catch (Exception ignored) {}
        }

        if (paymentStatus != null) {
            try {
                Booking.PaymentStatus ps = Booking.PaymentStatus.valueOf(paymentStatus.toLowerCase());
                bookings.removeIf(b -> b.getPaymentStatus() != ps);
            } catch (Exception ignored) {}
        }

        return bookings;
    }


}
