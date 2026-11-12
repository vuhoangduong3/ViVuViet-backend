package com.example.Vi_vu_Viet.Repository.Trip;

import java.util.UUID;

public interface CompanyDriverTripCountProjection {
    UUID getDriverId();
    long getTripCount();
}
