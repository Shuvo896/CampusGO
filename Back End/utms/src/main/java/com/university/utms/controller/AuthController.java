package com.university.utms.controller;

import com.university.utms.model.User;
import com.university.utms.repository.UserRepository;
import com.university.utms.security.JwtUtil;
import com.university.utms.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            if (email == null || password == null) {
                System.err.println("Missing email or password");
                return ResponseEntity.badRequest().body("Missing email or password");
            }

            System.out.println("Checking user with email: " + email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        System.err.println("User not found for email: " + email);
                        return new RuntimeException("User not found");
                    });

            System.out.println("User found. Verifying password...");
            System.out.println("Provided password: " + password);
            System.out.println("Stored hash: " + user.getPasswordHash());

            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                String token = jwtUtil.generateToken(user.getEmail());
                System.out.println("Login successful. Generated token: " + token);
                return ResponseEntity.ok(Map.of("token", token));
            }

            System.err.println("Incorrect password");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid credentials");

        } catch (Exception e) {
            System.err.println("Login Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login failed due to an internal error");
        }
    }

    @Autowired
    private com.university.utms.service.AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");
            String password = request.get("password");
            String universityId = request.get("universityId");
            String roleStr = request.get("role");

            if (name == null || email == null || password == null || universityId == null || roleStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }

            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already in use"));
            }

            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setUniversityId(universityId);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole(User.Role.valueOf(roleStr.toLowerCase()));

            // Generate and set verification code
            String verificationCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            user.setVerificationCode(verificationCode);

            userRepository.save(user);
            emailService.sendVerificationEmail(email, verificationCode);

            return ResponseEntity.ok(Map.of("message", "User registered. Verification code sent."));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }


    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");

            authService.verifyEmail(email, code);
            return ResponseEntity.ok(Map.of("message", "Email verified successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (email == null || code == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Email and verification code are required"
            ));
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User user = userOptional.get();
        if (user.getVerificationCode() != null && user.getVerificationCode().equals(code)) {
            user.setVerified(true);
            user.setVerificationCode(null); // ✅ Clear code
            user.setEmailVerified(true);    // ✅ Mark email as verified
            userRepository.save(user);

            // ✅ Send confirmation email
            emailService.sendConfirmationEmail(email);

            return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Invalid verification code"));
    }



    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        User user = userOpt.get();
        String resetCode = UUID.randomUUID().toString().substring(0, 6); // 6-digit code
        user.setResetCode(resetCode);
        userRepository.save(user);

        emailService.sendResetCode(user.getEmail(), resetCode);
        return ResponseEntity.ok(Map.of("message", "Reset code sent to your email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String resetCode = request.get("resetCode");
        String newPassword = request.get("newPassword");

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        User user = userOpt.get();

        if (!resetCode.equals(user.getResetCode())) {
            return ResponseEntity.status(400).body(Map.of("error", "Invalid reset code"));
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetCode(null); // clear code after successful reset
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }



}
