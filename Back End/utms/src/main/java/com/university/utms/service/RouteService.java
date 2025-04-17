package com.university.utms.service;

import com.university.utms.model.Route;

import java.util.List;
import java.util.Optional;

public interface RouteService {
    List<Route> getAllRoutes();
    Optional<Route> getRouteById(Long id);
    Route createRoute(Route route);
    Route updateRoute(Long id, Route route);
    void deleteRoute(Long id);
}
