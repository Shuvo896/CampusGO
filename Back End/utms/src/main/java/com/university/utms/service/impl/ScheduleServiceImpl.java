package com.university.utms.service.impl;

import com.university.utms.model.Bus;
import com.university.utms.model.Route;
import com.university.utms.model.Schedule;
import com.university.utms.repository.RouteRepository;
import com.university.utms.repository.ScheduleRepository;
import com.university.utms.repository.BusRepository;
import com.university.utms.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    @Autowired
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;

    @Autowired
    public ScheduleServiceImpl(ScheduleRepository scheduleRepository,
                               BusRepository busRepository,
                               RouteRepository routeRepository) {
        this.scheduleRepository = scheduleRepository;
        this.busRepository = busRepository;
        this.routeRepository = routeRepository;
    }

    @Override
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Override
    public Optional<Schedule> getScheduleById(Long id) {
        return scheduleRepository.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('authority')")
    public ResponseEntity<?> createSchedule(@RequestBody Map<String, String> request) {
        try {
            Long busId = Long.parseLong(request.get("busId"));
            Long routeId = Long.parseLong(request.get("routeId"));
            LocalTime departure = LocalTime.parse(request.get("departureTime"));
            LocalTime arrival = LocalTime.parse(request.get("arrivalTime"));

            // Fully fetch the Bus and Route before assigning
            Bus bus = busRepository.findById(busId)
                    .orElseThrow(() -> new RuntimeException("Bus not found"));
            Route route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new RuntimeException("Route not found"));

            Schedule schedule = new Schedule();
            schedule.setBus(bus);          // <-- Ensure managed entity is used
            schedule.setRoute(route);      // <-- Same here
            schedule.setDepartureTime(departure);
            schedule.setArrivalTime(arrival);

            Schedule saved = scheduleRepository.save(schedule);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "Failed to create schedule",
                    "message", e.getMessage()
            ));
        }
    }


    @Override
    @Transactional
    public void deleteSchedule(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + id));

        // Break references to avoid transient object errors
        schedule.setBus(null);
        schedule.setRoute(null);
        schedule.setBookings(null);  // if Booking is a child of Schedule and cascaded

        scheduleRepository.delete(schedule);
    }

}
