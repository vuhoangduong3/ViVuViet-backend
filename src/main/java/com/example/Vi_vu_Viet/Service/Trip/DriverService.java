package com.example.Vi_vu_Viet.Service.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Util.Enums.PassengerTripStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import com.example.Vi_vu_Viet.dto.Request.Trip.ReportTripIncidentRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.CollectTicketPaymentRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.RejectTripRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateDriverProfileRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateTripProgressRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.TicketResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.DriverProfileResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.DriverTripResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripIncidentResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.VehicleResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DriverService {

    BaseResponse<List<DriverProfileResponse>> getDriverProfiles(UUID userId);

    BaseResponse<DriverProfileResponse> updateDriverProfile(UUID userId, UpdateDriverProfileRequest request);

    BaseResponse<List<DriverTripResponse>> getDriverTrips(UUID userId, LocalDate from, LocalDate to, TripStatus status);

    BaseResponse<DriverTripResponse> getDriverTrip(UUID userId, UUID tripId);

    BaseResponse<DriverTripResponse> acceptTrip(UUID userId, UUID tripId);

    BaseResponse<DriverTripResponse> rejectTrip(UUID userId, UUID tripId, RejectTripRequest request);

    BaseResponse<DriverTripResponse> updateTripProgress(UUID userId, UUID tripId, UpdateTripProgressRequest request);

    BaseResponse<DriverTripResponse> updateAssignmentStatus(UUID userId, UUID tripId, DriverAssignmentStatus status);

    BaseResponse<TicketResponse> updatePassengerStatus(UUID userId, UUID tripId, UUID ticketId,
                                                        PassengerTripStatus status);

    BaseResponse<TicketResponse> collectTicketPayment(UUID userId, UUID tripId, UUID ticketId,
                                                       CollectTicketPaymentRequest request);

    BaseResponse<TripIncidentResponse> reportIncident(UUID userId, UUID tripId,
                                                       ReportTripIncidentRequest request);

    BaseResponse<VehicleResponse> getAssignedVehicle(UUID userId);
}
