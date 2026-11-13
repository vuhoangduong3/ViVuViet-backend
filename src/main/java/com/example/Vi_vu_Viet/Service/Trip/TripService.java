package com.example.Vi_vu_Viet.Service.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateTripRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.CancelTripRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.AssignTripRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateTripRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripAssignmentResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripPassengerBookingResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.CreateTripResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripDetailResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TripService {

    BaseResponse<List<TripResponse>> getCompanyTrips(UUID companyId, LocalDate date, TripStatus status,
                                                     UUID routeId, String assignmentStatus);

    BaseResponse<List<TripResponse>> getUpcomingTrips(UUID companyId);

    BaseResponse<List<TripResponse>> getInProgressTrips(UUID companyId);

    BaseResponse<List<TripResponse>> getCompletedTrips(UUID companyId);

    BaseResponse<List<TripResponse>> getCancelledTrips(UUID companyId);

    BaseResponse<List<TripPassengerBookingResponse>> getTripBookings(UUID companyId, UUID tripId);

    BaseResponse<TripAssignmentResponse> getTripAssignment(UUID companyId, UUID tripId);

    BaseResponse<TripAssignmentResponse> assignTrip(UUID companyId, UUID tripId, AssignTripRequest request,
                                                    UserAccount actor);

    BaseResponse<CreateTripResponse> createTrip(UUID companyId, CreateTripRequest request, UserAccount actor);

    BaseResponse<TripDetailResponse> getCompanyTrip(UUID companyId, UUID tripId);

    BaseResponse<TripResponse> updateTrip(UUID companyId, UUID tripId, UpdateTripRequest request);

    BaseResponse<TripResponse> publishTrip(UUID companyId, UUID tripId);

    BaseResponse<TripResponse> cancelTrip(UUID companyId, UUID tripId, CancelTripRequest request);

    BaseResponse<Void> deleteTrip(UUID companyId, UUID tripId);

}
