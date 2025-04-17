package com.university.utms.service;

public interface AuthService {
    void registerUser(String name, String email, String password, String universityId, String role);
    void verifyEmail(String email, String code);
}
