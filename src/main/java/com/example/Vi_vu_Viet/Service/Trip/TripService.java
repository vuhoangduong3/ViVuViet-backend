package com.example.Vi_vu_Viet.Service.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateTripRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateTripRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripResponse;

import java.util.List;
import java.util.UUID;

public interface TripService {

    BaseResponse<List<TripResponse>> getCompanyTrips(UUID companyId);

    BaseResponse<TripResponse> createTrip(UUID companyId, CreateTripRequest request, UserAccount actor);

    BaseResponse<TripResponse> getCompanyTrip(UUID companyId, UUID tripId);

    BaseResponse<TripResponse> updateTrip(UUID companyId, UUID tripId, UpdateTripRequest request);

    BaseResponse<TripResponse> publishTrip(UUID companyId, UUID tripId);
}
