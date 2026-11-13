package com.example.Vi_vu_Viet.Repository.Trip;

import java.util.UUID;

public interface RouteTripCountProjection {
    UUID getRouteId();
    long getTripCount();
}
