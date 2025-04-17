package com.university.utms.service.impl;

import com.university.utms.model.Route;
import com.university.utms.repository.RouteRepository;
import com.university.utms.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;

    @Autowired
    public RouteServiceImpl(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Override
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    @Override
    public Optional<Route> getRouteById(Long id) {
        return routeRepository.findById(id);
    }

    @Override
    public Route createRoute(Route route) {
        return routeRepository.save(route);
    }

    @Override
    public Route updateRoute(Long id, Route route) {
        if (routeRepository.existsById(id)) {
            route.setId(id);
            return routeRepository.save(route);
        }
        return null;
    }

    @Override
    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
    }
}
