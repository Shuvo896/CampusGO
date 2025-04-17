package com.university.utms.service.impl;

import com.university.utms.model.User;
import com.university.utms.repository.UserRepository;
import com.university.utms.service.AuthService;
import com.university.utms.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void registerUser(String name, String email, String password, String universityId, String role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setUniversityId(universityId);
        user.setRole(User.Role.valueOf(role.toUpperCase())); // Assuming Role enum exists

        String verificationCode = UUID.randomUUID().toString().substring(0, 6);
        user.setVerificationCode(verificationCode);
        user.setVerified(false);

        userRepository.save(user);
        emailService.sendVerificationEmail(email, verificationCode);
    }

    @Override
    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getVerificationCode().equals(code)) {
            user.setVerified(true);
            user.setVerificationCode(null);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Invalid verification code");
        }
    }
}
