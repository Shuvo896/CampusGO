package com.university.utms.controller;

import com.university.utms.repository.DatabaseTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class DatabaseTestController {

    @Autowired
    private DatabaseTestRepository databaseTestRepository;

    @GetMapping("/connection")
    public String testConnection() {
        return databaseTestRepository.testConnection();
    }
}