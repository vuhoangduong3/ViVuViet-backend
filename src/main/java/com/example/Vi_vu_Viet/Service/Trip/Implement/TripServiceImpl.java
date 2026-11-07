package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Trip.BusCompany;
import com.example.Vi_vu_Viet.Entity.Trip.Route;
import com.example.Vi_vu_Viet.Entity.Trip.RouteStop;
import com.example.Vi_vu_Viet.Entity.Trip.Stop;
import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Entity.Trip.TripStop;
import com.example.Vi_vu_Viet.Repository.Auth.UserRepository;
import com.example.Vi_vu_Viet.Repository.Trip.BusCompanyRepository;
import com.example.Vi_vu_Viet.Repository.Trip.RouteRepository;
import com.example.Vi_vu_Viet.Repository.Trip.RouteStopRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripStopRepository;
import com.example.Vi_vu_Viet.Service.Trip.TripService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.RouteStatus;
import com.example.Vi_vu_Viet.Util.Enums.StopStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripSalesStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateTripRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateTripRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripStopResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    BusCompanyRepository busCompanyRepository;
    UserRepository userRepository;
    RouteRepository routeRepository;
    RouteStopRepository routeStopRepository;
    TripRepository tripRepository;
    TripStopRepository tripStopRepository;
    TripResponseMapper tripResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<TripResponse>> getCompanyTrips(UUID companyId) {
        getActiveCompany(companyId);
        List<Trip> trips = tripRepository.findAllByCompanyId(companyId);
        Map<UUID, List<TripStopResponse>> stopsByTrip = new HashMap<>();
        if (!trips.isEmpty()) {
            List<UUID> tripIds = trips.stream().map(Trip::getId).toList();
            stopsByTrip = tripStopRepository.findAllByCompanyIdAndTripIds(companyId, tripIds)
                    .stream()
                    .collect(Collectors.groupingBy(
                            tripStop -> tripStop.getTrip_id().getId(),
                            Collectors.mapping(tripResponseMapper::toTripStopResponse, Collectors.toList())
                    ));
        }

        Map<UUID, List<TripStopResponse>> finalStopsByTrip = stopsByTrip;
        List<TripResponse> results = trips.stream()
                .map(trip -> tripResponseMapper.toTripResponse(
                        trip,
                        finalStopsByTrip.getOrDefault(trip.getId(), List.of())
                ))
                .toList();
        return success(results);
    }

    @Override
    @Transactional
    public BaseResponse<TripResponse> createTrip(UUID companyId, CreateTripRequest request, UserAccount actor) {
        BusCompany company = getActiveCompany(companyId);
        Route route = getActiveCompanyRoute(companyId, request.getRouteId());
        List<RouteStop> routeStops = routeStopRepository.findAllByCompanyIdAndRouteId(companyId, route.getId());
        validateRouteStops(routeStops);

        LocalDateTime bookingCutoffAt = request.getBookingCutoffAt() == null
                ? request.getDepartureAt().minusMinutes(30)
                : request.getBookingCutoffAt();
        validateSchedule(request.getDepartureAt(), request.getExpectedArrivalAt(), bookingCutoffAt);
        validateTripCodeAvailable(companyId, request.getTripCode());

        Trip trip = new Trip();
        trip.setCompany_id(company);
        trip.setRoute_id(route);
        trip.setTrip_code(normalizeTripCode(request.getTripCode()));
        trip.setDeparture_at(request.getDepartureAt());
        trip.setExpected_arrival_at(request.getExpectedArrivalAt());
        trip.setBooking_cutoff_at(bookingCutoffAt);
        trip.setStatus(TripStatus.DRAFT);
        trip.setSales_status(TripSalesStatus.CLOSED);
        trip.setCreated_by(actor == null
                ? null
                : userRepository.findById(actor.getId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user was not found"
                        )));

        Trip savedTrip = tripRepository.save(trip);
        List<TripStop> tripStops = createTripStopSnapshots(company, savedTrip, routeStops);
        validateTripStopTimes(savedTrip, tripStops);
        tripStopRepository.saveAll(tripStops);
        tripStopRepository.flush();

        return success(tripResponseMapper.toTripResponse(savedTrip, mapStops(companyId, savedTrip.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<TripResponse> getCompanyTrip(UUID companyId, UUID tripId) {
        getActiveCompany(companyId);
        Trip trip = getCompanyTripEntity(companyId, tripId);
        return success(tripResponseMapper.toTripResponse(trip, mapStops(companyId, tripId)));
    }

    @Override
    @Transactional
    public BaseResponse<TripResponse> updateTrip(UUID companyId, UUID tripId, UpdateTripRequest request) {
        getActiveCompany(companyId);
        Trip trip = tripRepository.findByIdAndCompanyIdForUpdate(companyId, tripId)
                .orElseThrow(() -> notFound("Trip was not found for this company"));
        if (trip.getStatus() != TripStatus.DRAFT || trip.getSales_status() != TripSalesStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only draft trips can be updated");
        }
        if (!hasChanges(request)) {
            throw badRequest("At least one trip field must be provided");
        }

        if (request.getTripCode() != null) {
            String tripCode = normalizeTripCode(request.getTripCode());
            if (tripRepository.countByCompanyIdAndTripCodeExcludingTrip(companyId, tripCode, tripId) > 0) {
                throw badRequest("Trip code already exists for this company");
            }
            trip.setTrip_code(tripCode);
        }

        if (request.getDepartureAt() != null) {
            LocalDateTime oldDepartureAt = trip.getDeparture_at();
            LocalDateTime newDepartureAt = request.getDepartureAt();
            Duration shift = Duration.between(oldDepartureAt, newDepartureAt);
            trip.setDeparture_at(newDepartureAt);
            if (request.getExpectedArrivalAt() == null) {
                trip.setExpected_arrival_at(trip.getExpected_arrival_at().plus(shift));
            }
            if (request.getBookingCutoffAt() == null) {
                trip.setBooking_cutoff_at(trip.getBooking_cutoff_at().plus(shift));
            }
            shiftTripStopTimes(companyId, tripId, shift);
        }
        if (request.getExpectedArrivalAt() != null) {
            trip.setExpected_arrival_at(request.getExpectedArrivalAt());
        }
        if (request.getBookingCutoffAt() != null) {
            trip.setBooking_cutoff_at(request.getBookingCutoffAt());
        }

        validateSchedule(trip.getDeparture_at(), trip.getExpected_arrival_at(), trip.getBooking_cutoff_at());
        validateTripStopTimes(trip, mapStopEntities(companyId, tripId));

        Trip savedTrip = tripRepository.save(trip);
        return success(tripResponseMapper.toTripResponse(savedTrip, mapStops(companyId, tripId)));
    }

    @Override
    @Transactional
    public BaseResponse<TripResponse> publishTrip(UUID companyId, UUID tripId) {
        getActiveCompany(companyId);
        Trip trip = tripRepository.findByIdAndCompanyIdForUpdate(companyId, tripId)
                .orElseThrow(() -> notFound("Trip was not found for this company"));

        if (trip.getStatus() == TripStatus.SCHEDULED && trip.getSales_status() == TripSalesStatus.OPEN) {
            return success(tripResponseMapper.toTripResponse(trip, mapStops(companyId, tripId)));
        }
        if (trip.getStatus() != TripStatus.DRAFT || trip.getSales_status() != TripSalesStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only draft trips can be published");
        }

        List<TripStop> tripStops = mapStopEntities(companyId, tripId);
        validateSchedule(trip.getDeparture_at(), trip.getExpected_arrival_at(), trip.getBooking_cutoff_at());
        validateTripStopTimes(trip, tripStops);
        if (tripStops.size() < 2) {
            throw badRequest("A trip must have at least two stops before it can be published");
        }
        validateStopsForPublishing(tripStops);
        if (trip.getBooking_cutoff_at().isBefore(LocalDateTime.now())) {
            throw badRequest("Booking cutoff must be in the future when publishing a trip");
        }
        if (trip.getRoute_id().getBase_fare_amount() == null || trip.getRoute_id().getBase_fare_amount() <= 0) {
            throw badRequest("The route must have a positive default fare before publishing");
        }

        trip.setStatus(TripStatus.SCHEDULED);
        trip.setSales_status(TripSalesStatus.OPEN);
        Trip savedTrip = tripRepository.save(trip);
        return success(tripResponseMapper.toTripResponse(savedTrip, mapStops(companyId, tripId)));
    }

    private BusCompany getActiveCompany(UUID companyId) {
        return busCompanyRepository.findByIdAndStatus(companyId, BusCompanyStatus.ACTIVE)
                .orElseThrow(() -> notFound("Bus company was not found or is inactive"));
    }

    private Route getActiveCompanyRoute(UUID companyId, UUID routeId) {
        Route route = routeRepository.findByIdAndCompanyId(companyId, routeId)
                .orElseThrow(() -> notFound("Route was not found for this company"));
        if (route.getStatus() != RouteStatus.ACTIVE) {
            throw badRequest("Route is inactive");
        }
        return route;
    }

    private Trip getCompanyTripEntity(UUID companyId, UUID tripId) {
        return tripRepository.findByIdAndCompanyId(companyId, tripId)
                .orElseThrow(() -> notFound("Trip was not found for this company"));
    }

    private void validateRouteStops(List<RouteStop> routeStops) {
        if (routeStops.size() < 2) {
            throw badRequest("The route must have at least two stops before creating a trip");
        }
        for (int i = 0; i < routeStops.size(); i++) {
            RouteStop routeStop = routeStops.get(i);
            if (routeStop.getSequence_no() == null || routeStop.getSequence_no() != i + 1) {
                throw badRequest("Route stop sequence numbers must be continuous and start at 1");
            }
            if (routeStop.getStop_id().getStatus() != StopStatus.ACTIVE) {
                throw badRequest("All route stops must be active before creating a trip");
            }
        }
    }

    private List<TripStop> createTripStopSnapshots(
            BusCompany company,
            Trip trip,
            List<RouteStop> routeStops
    ) {
        List<TripStop> snapshots = new ArrayList<>();
        for (RouteStop routeStop : routeStops) {
            Stop stop = routeStop.getStop_id();
            TripStop tripStop = new TripStop();
            tripStop.setCompany_id(company);
            tripStop.setTrip_id(trip);
            tripStop.setStop_id(stop);
            tripStop.setArea_id(stop.getArea_id());
            tripStop.setSequence_no(routeStop.getSequence_no());
            tripStop.setName_snapshot(stop.getName());
            tripStop.setAddress_snapshot(stop.getAddress());
            tripStop.setPlanned_arrival_at(toPlannedTime(
                    trip.getDeparture_at(),
                    routeStop.getArrival_offset_minutes(),
                    routeStop.getSequence_no() == 1
            ));
            tripStop.setPlanned_departure_at(toPlannedTime(
                    trip.getDeparture_at(),
                    routeStop.getDeparture_offset_minutes(),
                    routeStop.getSequence_no() == 1
            ));
            tripStop.setPickup_allowed(routeStop.getPickup_allowed());
            tripStop.setDropoff_allowed(routeStop.getDropoff_allowed());
            snapshots.add(tripStop);
        }
        return snapshots;
    }

    private LocalDateTime toPlannedTime(LocalDateTime departureAt, Integer offsetMinutes, boolean firstStop) {
        if (offsetMinutes != null) {
            return departureAt.plusMinutes(offsetMinutes);
        }
        return firstStop ? departureAt : null;
    }

    private void shiftTripStopTimes(UUID companyId, UUID tripId, Duration shift) {
        if (shift.isZero()) {
            return;
        }
        for (TripStop tripStop : mapStopEntities(companyId, tripId)) {
            if (tripStop.getPlanned_arrival_at() != null) {
                tripStop.setPlanned_arrival_at(tripStop.getPlanned_arrival_at().plus(shift));
            }
            if (tripStop.getPlanned_departure_at() != null) {
                tripStop.setPlanned_departure_at(tripStop.getPlanned_departure_at().plus(shift));
            }
        }
    }

    private void validateTripStopTimes(Trip trip, List<TripStop> tripStops) {
        for (int i = 0; i < tripStops.size(); i++) {
            TripStop tripStop = tripStops.get(i);
            validateTimeWithinTrip(tripStop.getPlanned_arrival_at(), trip);
            validateTimeWithinTrip(tripStop.getPlanned_departure_at(), trip);
            if (i > 0) {
                LocalDateTime previousDeparture = tripStops.get(i - 1).getPlanned_departure_at();
                LocalDateTime currentArrival = tripStop.getPlanned_arrival_at();
                if (previousDeparture != null && currentArrival != null
                        && currentArrival.isBefore(previousDeparture)) {
                    throw badRequest("Trip stop times must follow the stop sequence");
                }
            }
        }
    }

    private void validateStopsForPublishing(List<TripStop> tripStops) {
        for (TripStop tripStop : tripStops) {
            if (Boolean.TRUE.equals(tripStop.getPickup_allowed())
                    && tripStop.getPlanned_departure_at() == null) {
                throw badRequest("Every pickup stop needs a planned departure time");
            }
            if (Boolean.TRUE.equals(tripStop.getDropoff_allowed())
                    && tripStop.getPlanned_arrival_at() == null) {
                throw badRequest("Every dropoff stop needs a planned arrival time");
            }
        }

        boolean hasSellableSegment = tripStops.stream()
                .filter(stop -> Boolean.TRUE.equals(stop.getPickup_allowed()))
                .anyMatch(boardingStop -> tripStops.stream().anyMatch(alightingStop ->
                        Boolean.TRUE.equals(alightingStop.getDropoff_allowed())
                                && boardingStop.getSequence_no() < alightingStop.getSequence_no()
                ));
        if (!hasSellableSegment) {
            throw badRequest("The trip needs an ordered pickup and dropoff pair before publishing");
        }
    }

    private void validateTimeWithinTrip(LocalDateTime time, Trip trip) {
        if (time != null && (time.isBefore(trip.getDeparture_at())
                || time.isAfter(trip.getExpected_arrival_at()))) {
            throw badRequest("Trip stop schedule must be between departure and expected arrival");
        }
    }

    private void validateSchedule(
            LocalDateTime departureAt,
            LocalDateTime expectedArrivalAt,
            LocalDateTime bookingCutoffAt
    ) {
        if (departureAt == null || expectedArrivalAt == null || bookingCutoffAt == null) {
            throw badRequest("Departure, expected arrival, and booking cutoff are required");
        }
        if (!expectedArrivalAt.isAfter(departureAt)) {
            throw badRequest("Expected arrival must be later than departure");
        }
        if (!bookingCutoffAt.isBefore(departureAt)) {
            throw badRequest("Booking cutoff must be earlier than departure");
        }
    }

    private void validateTripCodeAvailable(UUID companyId, String tripCode) {
        String normalized = normalizeTripCode(tripCode);
        if (tripRepository.countByCompanyIdAndTripCode(companyId, normalized) > 0) {
            throw badRequest("Trip code already exists for this company");
        }
    }

    private String normalizeTripCode(String tripCode) {
        if (!StringUtils.hasText(tripCode)) {
            throw badRequest("Trip code must not be blank");
        }
        return tripCode.trim().toUpperCase(Locale.ROOT);
    }

    private boolean hasChanges(UpdateTripRequest request) {
        return request.getTripCode() != null
                || request.getDepartureAt() != null
                || request.getExpectedArrivalAt() != null
                || request.getBookingCutoffAt() != null;
    }

    private List<TripStop> mapStopEntities(UUID companyId, UUID tripId) {
        return tripStopRepository.findAllByCompanyIdAndTripId(companyId, tripId);
    }

    private List<TripStopResponse> mapStops(UUID companyId, UUID tripId) {
        return mapStopEntities(companyId, tripId).stream()
                .map(tripResponseMapper::toTripStopResponse)
                .toList();
    }

    private BaseResponse<TripResponse> success(TripResponse tripResponse) {
        BaseResponse<TripResponse> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(tripResponse);
        return response;
    }

    private BaseResponse<List<TripResponse>> success(List<TripResponse> trips) {
        BaseResponse<List<TripResponse>> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(trips);
        return response;
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
