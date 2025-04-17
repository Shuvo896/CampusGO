package com.university.utms.service;

import com.university.utms.model.Bus;
import com.university.utms.model.Route;

import java.util.List;
import java.util.Optional;

public interface BusService {
    List<Bus> getAllBuses();
    Optional<Bus> getBusById(Long id);
    Bus createBus(Bus bus);
    Bus updateBus(Long id, Bus bus);
    void deleteBus(Long id);
    Bus assignDriverAndRoute(Long id, String driverName, Route route);


}
  