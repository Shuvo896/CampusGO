package com.university.utms.service.impl;

import com.university.utms.model.Bus;
import com.university.utms.model.Route;
import com.university.utms.repository.BusRepository;
import com.university.utms.service.BusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BusServiceImpl implements BusService {

    private final BusRepository busRepository;

    @Autowired
    public BusServiceImpl(BusRepository busRepository) {
        this.busRepository = busRepository;
    }

    @Override
    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }

    @Override
    public Optional<Bus> getBusById(Long id) {
        return busRepository.findById(id);
    }

    @Override
    public Bus createBus(Bus bus) {
        return busRepository.save(bus);
    }

    @Override
    public Bus updateBus(Long id, Bus bus) {
        if (busRepository.existsById(id)) {
            bus.setId(id);
            return busRepository.save(bus);
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteBus(Long id) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        if (bus.getSchedules() != null) {
            bus.getSchedules().size(); // Force initialize
        }
        bus.setSchedules(null);
        busRepository.delete(bus);
    }

    @Override
    public Bus assignDriverAndRoute(Long id, String driverName, Route route) {
        Optional<Bus> optionalBus = busRepository.findById(id);
        if (optionalBus.isPresent()) {
            Bus bus = optionalBus.get();
            bus.setDriverName(driverName);
            bus.setRoute(route);
            return busRepository.save(bus);
        } else {
            throw new RuntimeException("Bus not found with id: " + id);
        }
    }

}
