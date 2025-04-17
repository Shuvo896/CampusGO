package com.university.utms.service;

import com.university.utms.model.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingService {
    List<Booking> getAllBookings();
    Optional<Booking> getBookingById(Long id);
    Booking createBooking(Booking booking);
    Booking updateBooking(Long id, Booking booking);
    void deleteBooking(Long id);
    Booking reserveSeats(Long busId, int seatCount, String userEmail);
    void cancelBooking(Long bookingId, String userEmail);
    Booking markAsPaid(Long bookingId, String userEmail);
    List<Booking> getBookingsByUserEmail(String email);
    List<Booking> getBookingsByBus(Long busId);
    List<Booking> getAllBookingsForAdmin(String userEmail, Long busId, String status, String paymentStatus);


}
  