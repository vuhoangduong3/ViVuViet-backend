package com.example.Vi_vu_Viet.Service.Trip;

import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripSearchResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TripSearchService {

    BaseResponse<List<TripSearchResponse>> searchTrips(
            UUID originAreaId,
            UUID destinationAreaId,
            LocalDate departureDate
    );

    BaseResponse<TripResponse> getPublishedTrip(UUID tripId);
}
