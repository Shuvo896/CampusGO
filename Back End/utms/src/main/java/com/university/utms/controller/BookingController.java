package com.university.utms.controller;

import com.university.utms.model.Booking;
import com.university.utms.model.User;
import com.university.utms.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('student') or hasRole('faculty') or hasRole('admin')")
    public ResponseEntity<?> getBookingById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Booking> booking = bookingService.getBookingById(id);

        if (booking.isPresent()) {
            // Admins can view all, others only their own
            if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ||
                    booking.get().getUser().getEmail().equals(userDetails.getUsername())) {
                return ResponseEntity.ok(booking.get());
            }
            return ResponseEntity.status(403).body("You are not authorized to access this booking.");
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('student') or hasRole('faculty')")
    public ResponseEntity<?> createBooking(@RequestBody Booking booking, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = new User();
            user.setEmail(userDetails.getUsername());
            booking.setUser(user);
            Booking savedBooking = bookingService.createBooking(booking);
            return ResponseEntity.ok(savedBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating booking: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> updateBooking(@PathVariable Long id, @RequestBody Booking booking) {
        try {
            Booking updatedBooking = bookingService.updateBooking(id, booking);
            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating booking: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        try {
            bookingService.deleteBooking(id);
            return ResponseEntity.ok().body("Booking deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting booking: " + e.getMessage());
        }
    }

    @PostMapping("/reserve")
    @PreAuthorize("hasRole('student') or hasRole('faculty')")
    public ResponseEntity<?> reserveSeats(@RequestBody Map<String, Object> bookingRequest) {
        try {
            Object scheduleIdObj = bookingRequest.get("scheduleId");
            Object seatCountObj = bookingRequest.get("seatCount");
            Object userEmailObj = bookingRequest.get("userEmail");

            if (scheduleIdObj == null || seatCountObj == null || userEmailObj == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Missing fields",
                        "message", "Please include 'scheduleId', 'seatCount', and 'userEmail' in the request body."
                ));
            }

            Long scheduleId = Long.parseLong(scheduleIdObj.toString());
            int seatCount = Integer.parseInt(seatCountObj.toString());
            String userEmail = userEmailObj.toString();

            Booking booking = bookingService.reserveSeats(scheduleId, seatCount, userEmail);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Internal Server Error",
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/cancel/{id}")
    @PreAuthorize("hasRole('student') or hasRole('faculty')")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            bookingService.cancelBooking(id, userDetails.getUsername());
            return ResponseEntity.ok(Map.of("message", "Booking cancelled successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('student') or hasRole('faculty')")
    public ResponseEntity<?> payForBooking(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Booking booking = bookingService.markAsPaid(id, userDetails.getUsername());
            return ResponseEntity.ok(Map.of(
                    "message", "Payment successful",
                    "bookingId", booking.getId(),
                    "status", booking.getPaymentStatus(),
                    "qrCodeBase64", booking.getQrCode()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('student') or hasRole('faculty')")
    public ResponseEntity<?> getUserBookings(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String userEmail = userDetails.getUsername();
            List<Booking> bookings = bookingService.getBookingsByUserEmail(userEmail);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving bookings: " + e.getMessage());
        }
    }

    @GetMapping("/bus/{busId}")
    @PreAuthorize("hasRole('authority')")
    public ResponseEntity<?> getBookingsByBus(@PathVariable Long busId) {
        try {
            List<Booking> bookings = bookingService.getBookingsByBus(busId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching bookings: " + e.getMessage());
        }
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> getAllBookingsForAdmin(
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) Long busId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus
    ) {
        try {
            List<Booking> filtered = bookingService.getAllBookingsForAdmin(userEmail, busId, status, paymentStatus);
            return ResponseEntity.ok(filtered);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving bookings: " + e.getMessage());
        }
    }
}
