package com.university.utms.service;

import com.university.utms.dto.UserRegistrationRequest;
import com.university.utms.model.User;
import java.util.List;
import java.util.Optional;


public interface UserService {
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);

    public User registerUser(UserRegistrationRequest request);
    Optional<User> getUserByEmail(String email);
}
