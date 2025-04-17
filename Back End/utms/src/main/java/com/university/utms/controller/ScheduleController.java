package com.university.utms.controller;

import com.university.utms.model.Bus;
import com.university.utms.model.Route;
import com.university.utms.model.Schedule;
import com.university.utms.repository.BusRepository;
import com.university.utms.repository.RouteRepository;
import com.university.utms.repository.ScheduleRepository;
import com.university.utms.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;
    @Autowired
    private BusRepository busRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public List<Schedule> getAllSchedules() {
        return scheduleService.getAllSchedules();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Long id) {
        Optional<Schedule> schedule = scheduleService.getScheduleById(id);
        return schedule.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('authority')")
    public ResponseEntity<?> createSchedule(@RequestBody Map<String, String> request) {
        try {
            Long busId = Long.parseLong(request.get("busId"));
            Long routeId = Long.parseLong(request.get("routeId"));
            LocalTime departure = LocalTime.parse(request.get("departureTime"));
            LocalTime arrival = LocalTime.parse(request.get("arrivalTime"));

            Bus bus = busRepository.findById(busId)
                    .orElseThrow(() -> new RuntimeException("Bus not found"));
            Route route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new RuntimeException("Route not found"));

            Schedule schedule = new Schedule();
            schedule.setBus(bus);
            schedule.setRoute(route);
            schedule.setDepartureTime(departure);
            schedule.setArrivalTime(arrival);

            Schedule saved = scheduleRepository.save(schedule);

            // Return only safe fields manually
            return ResponseEntity.ok(Map.of(
                    "id", saved.getId(),
                    "busId", saved.getBus().getId(),
                    "routeId", saved.getRoute().getId(),
                    "departureTime", saved.getDepartureTime(),
                    "arrivalTime", saved.getArrivalTime()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "Failed to create schedule",
                    "message", e.getMessage()
            ));
        }
    }



    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('authority')")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        try {
            scheduleService.deleteSchedule(id);
            return ResponseEntity.ok("Schedule deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting schedule: " + e.getMessage());
        }
    }
}
