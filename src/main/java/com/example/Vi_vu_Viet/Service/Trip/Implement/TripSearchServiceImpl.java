package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Trip.Area;
import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Entity.Trip.TripStop;
import com.example.Vi_vu_Viet.Repository.Trip.AreaRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripStopRepository;
import com.example.Vi_vu_Viet.Service.Trip.TripSearchService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripSalesStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripSearchResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripStopResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TripSearchServiceImpl implements TripSearchService {

    AreaRepository areaRepository;
    TripRepository tripRepository;
    TripStopRepository tripStopRepository;
    TripResponseMapper tripResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<TripSearchResponse>> searchTrips(
            UUID originAreaId,
            UUID destinationAreaId,
            LocalDate departureDate
    ) {
        if (originAreaId.equals(destinationAreaId)) {
            throw badRequest("Origin and destination areas must be different");
        }
        getActiveArea(originAreaId);
        getActiveArea(destinationAreaId);

        LocalDateTime start = departureDate.atStartOfDay();
        LocalDateTime end = departureDate.plusDays(1).atStartOfDay();
        List<Object[]> rows = tripRepository.searchPublishedTrips(
                originAreaId,
                destinationAreaId,
                start,
                end,
                TripStatus.SCHEDULED,
                TripSalesStatus.OPEN,
                BusCompanyStatus.ACTIVE
        );

        List<TripSearchResponse> results = new ArrayList<>();
        for (Object[] row : rows) {
            Trip trip = (Trip) row[0];
            TripStop boardingStop = (TripStop) row[1];
            TripStop alightingStop = (TripStop) row[2];

            TripSearchResponse searchResponse = new TripSearchResponse();
            searchResponse.setTrip(tripResponseMapper.toTripResponse(trip, List.of()));
            searchResponse.setBoardingStop(tripResponseMapper.toTripStopResponse(boardingStop));
            searchResponse.setAlightingStop(tripResponseMapper.toTripStopResponse(alightingStop));
            results.add(searchResponse);
        }

        BaseResponse<List<TripSearchResponse>> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(results);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<TripResponse> getPublishedTrip(UUID tripId) {
        Trip trip = tripRepository.findPublishedTripById(
                        tripId,
                        TripStatus.SCHEDULED,
                        TripSalesStatus.OPEN,
                        BusCompanyStatus.ACTIVE
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Published trip was not found"
                ));
        List<TripStopResponse> stops = tripStopRepository
                .findAllByCompanyIdAndTripId(trip.getCompany_id().getId(), tripId)
                .stream()
                .map(tripResponseMapper::toTripStopResponse)
                .toList();

        TripResponse tripResponse = tripResponseMapper.toTripResponse(trip, stops);
        BaseResponse<TripResponse> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(tripResponse);
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
