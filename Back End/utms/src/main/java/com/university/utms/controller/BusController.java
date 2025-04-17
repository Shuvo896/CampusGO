package com.university.utms.controller;

import com.university.utms.model.Bus;
import com.university.utms.model.Route;
import com.university.utms.repository.RouteRepository;
import com.university.utms.service.BusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/buses")
public class BusController {

    private final BusService busService;
    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    public BusController(BusService busService) {
        this.busService = busService;
    }

    // Authority can view all buses
    @GetMapping
    @PreAuthorize("hasRole('authority')")
    public List<Bus> getAllBuses() {
        return busService.getAllBuses();
    }

    // Authority can view a specific bus
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('authority')")
    public ResponseEntity<Bus> getBusById(@PathVariable Long id) {
        Optional<Bus> bus = busService.getBusById(id);
        return bus.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Only authority can add new buses
    @PostMapping
    @PreAuthorize("hasRole('authority')")
    public ResponseEntity<?> createBus(@RequestBody Bus bus) {
        try {
            Bus savedBus = busService.createBus(bus);
            return ResponseEntity.ok(savedBus);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating bus: " + e.getMessage());
        }
    }

    // Only authority can update bus info
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('authority')")
    public ResponseEntity<?> updateBus(@PathVariable Long id, @RequestBody Bus bus) {
        try {
            Bus updatedBus = busService.updateBus(id, bus);
            return ResponseEntity.ok(updatedBus);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating bus: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('authority')")
    public ResponseEntity<?> deleteBus(@PathVariable Long id) {
        try {
            System.out.println("Attempting to delete bus with ID: " + id);
            busService.deleteBus(id);
            return ResponseEntity.ok().body("Bus deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace(); // <== Ensure you can see full error
            return ResponseEntity.badRequest().body("Error deleting bus: " + e.getMessage());
        }
    }



    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('authority')")
    public ResponseEntity<?> assignDriverAndRoute(@PathVariable Long id, @RequestBody Map<String, String> assignment) {
        try {
            String driverName = assignment.get("driverName");
            String routeIdStr = assignment.get("routeId");

            if (driverName == null || routeIdStr == null) {
                return ResponseEntity.badRequest().body("Both driverName and routeId are required.");
            }

            Long routeId = Long.parseLong(routeIdStr);
            Route route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new RuntimeException("Route not found"));

            Bus updatedBus = busService.assignDriverAndRoute(id, driverName, route);
            return ResponseEntity.ok(updatedBus);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error assigning driver/route: " + e.getMessage());
        }
    }

}
