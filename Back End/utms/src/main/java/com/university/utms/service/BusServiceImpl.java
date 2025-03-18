package com.university.utms.service;

import com.university.utms.model.Bus;
import com.university.utms.repository.BusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    public void deleteBus(Long id) {
        busRepository.deleteById(id);
    }
}
  