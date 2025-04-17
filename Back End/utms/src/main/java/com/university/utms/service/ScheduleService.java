package com.university.utms.service;

import com.university.utms.model.Schedule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ScheduleService {
    List<Schedule> getAllSchedules();
    Optional<Schedule> getScheduleById(Long id);
    public ResponseEntity<?> createSchedule(@RequestBody Map<String, String> request);
    void deleteSchedule(Long id);
}
