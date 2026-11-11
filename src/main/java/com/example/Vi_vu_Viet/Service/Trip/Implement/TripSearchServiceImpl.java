package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Trip.Area;
import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Repository.Trip.AreaRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripRepository;
import com.example.Vi_vu_Viet.Service.Trip.TripSearchService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripSalesStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TripSearchServiceImpl implements TripSearchService {

    AreaRepository areaRepository;
    TripRepository tripRepository;
    TripResponseMapper tripResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<TripResponse>> searchTrips(
            UUID originAreaId, UUID destinationAreaId, LocalDate departureDate) {
        if (originAreaId.equals(destinationAreaId)) {
            throw badRequest("Origin and destination areas must be different");
        }
        getActiveArea(originAreaId);
        getActiveArea(destinationAreaId);

        LocalDateTime start = departureDate.atStartOfDay();
        LocalDateTime end = departureDate.plusDays(1).atStartOfDay();
        List<TripResponse> results = tripRepository.searchPublishedTrips(
                        originAreaId, destinationAreaId, start, end,
                        TripStatus.SCHEDULED, TripSalesStatus.OPEN, BusCompanyStatus.ACTIVE)
                .stream()
                .map(tripResponseMapper::toTripResponse)
                .toList();

        BaseResponse<List<TripResponse>> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(results);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<TripResponse> getPublishedTrip(UUID tripId) {
        Trip trip = tripRepository.findPublishedTripById(
                        tripId, TripStatus.SCHEDULED, TripSalesStatus.OPEN, BusCompanyStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Published trip was not found"));
        BaseResponse<TripResponse> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(tripResponseMapper.toTripResponse(trip));
        return response;
    }

    private Area getActiveArea(UUID areaId) {
        return areaRepository.findActiveById(areaId)
                .orElseThrow(() -> badRequest("Area was not found or is inactive"));
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
