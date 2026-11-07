package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Trip.Area;
import com.example.Vi_vu_Viet.Entity.Trip.BusCompany;
import com.example.Vi_vu_Viet.Entity.Trip.Route;
import com.example.Vi_vu_Viet.Entity.Trip.RouteStop;
import com.example.Vi_vu_Viet.Entity.Trip.Stop;
import com.example.Vi_vu_Viet.Repository.Trip.AreaRepository;
import com.example.Vi_vu_Viet.Repository.Trip.BusCompanyRepository;
import com.example.Vi_vu_Viet.Repository.Trip.RouteRepository;
import com.example.Vi_vu_Viet.Repository.Trip.RouteStopRepository;
import com.example.Vi_vu_Viet.Repository.Trip.StopRepository;
import com.example.Vi_vu_Viet.Service.Trip.RouteService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.RouteStatus;
import com.example.Vi_vu_Viet.Util.Enums.StopStatus;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateRouteRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.ReplaceRouteStopsRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateRouteRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.RouteResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.RouteStopResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    BusCompanyRepository busCompanyRepository;
    AreaRepository areaRepository;
    StopRepository stopRepository;
    RouteRepository routeRepository;
    RouteStopRepository routeStopRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<RouteResponse>> getRoutes(UUID companyId) {
        getActiveCompany(companyId);

        List<Route> routes = routeRepository.findAllByCompanyId(companyId);
        Map<UUID, List<RouteStopResponse>> stopsByRoute = new HashMap<>();
        if (!routes.isEmpty()) {
            List<UUID> routeIds = routes.stream().map(Route::getId).toList();
            stopsByRoute = routeStopRepository.findAllByCompanyIdAndRouteIds(companyId, routeIds)
                    .stream()
                    .collect(Collectors.groupingBy(
                            routeStop -> routeStop.getRoute_id().getId(),
                            LinkedHashMap::new,
                            Collectors.mapping(this::toStopResponse, Collectors.toList())
                    ));
        }

        Map<UUID, List<RouteStopResponse>> finalStopsByRoute = stopsByRoute;
        List<RouteResponse> responseList = routes.stream()
                .map(route -> toResponse(route, finalStopsByRoute.getOrDefault(route.getId(), List.of())))
                .toList();

        BaseResponse<List<RouteResponse>> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(responseList);
        return response;
    }

    @Override
    @Transactional
    public BaseResponse<RouteResponse> createRoute(UUID companyId, CreateRouteRequest request) {
        BusCompany company = getActiveCompany(companyId);
        Area originArea = getActiveArea(request.getOriginAreaId());
        Area destinationArea = getActiveArea(request.getDestinationAreaId());
        validateDifferentAreas(originArea, destinationArea);

        String code = normalizeCode(request.getCode());
        if (routeRepository.countByCompanyIdAndCode(companyId, code) > 0) {
            throw badRequest("Route code already exists for this company");
        }

        Route route = new Route();
        route.setCompany_id(company);
        route.setCode(code);
        route.setName(request.getName().trim());
        route.setOrigin_area_id(originArea);
        route.setDestination_area_id(destinationArea);
        route.setDistance_km(request.getDistanceKm());
        route.setDuration_minutes(request.getDurationMinutes());
        route.setBase_fare_amount(request.getBaseFareAmount());
        route.setStatus(RouteStatus.ACTIVE);

        Route savedRoute = routeRepository.save(route);
        return success(toResponse(savedRoute, List.of()));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<RouteResponse> getRoute(UUID companyId, UUID routeId) {
        getActiveCompany(companyId);
        Route route = getCompanyRoute(companyId, routeId);
        List<RouteStopResponse> stops = routeStopRepository
                .findAllByCompanyIdAndRouteId(companyId, routeId)
                .stream()
                .map(this::toStopResponse)
                .toList();
        return success(toResponse(route, stops));
    }

    @Override
    @Transactional
    public BaseResponse<RouteResponse> updateRoute(
            UUID companyId,
            UUID routeId,
            UpdateRouteRequest request
    ) {
        getActiveCompany(companyId);
        Route route = getCompanyRoute(companyId, routeId);
        if (!hasChanges(request)) {
            throw badRequest("At least one route field must be provided");
        }

        if (request.getCode() != null) {
            String code = normalizeCode(request.getCode());
            if (routeRepository.countByCompanyIdAndCodeExcludingRoute(companyId, code, routeId) > 0) {
                throw badRequest("Route code already exists for this company");
            }
            route.setCode(code);
        }
        if (request.getName() != null) {
            if (!StringUtils.hasText(request.getName())) {
                throw badRequest("Route name must not be blank");
            }
            route.setName(request.getName().trim());
        }
        if (request.getOriginAreaId() != null) {
            route.setOrigin_area_id(getActiveArea(request.getOriginAreaId()));
        }
        if (request.getDestinationAreaId() != null) {
            route.setDestination_area_id(getActiveArea(request.getDestinationAreaId()));
        }
        validateDifferentAreas(route.getOrigin_area_id(), route.getDestination_area_id());

        if (request.getDistanceKm() != null) {
            route.setDistance_km(request.getDistanceKm());
        }
        if (request.getDurationMinutes() != null) {
            route.setDuration_minutes(request.getDurationMinutes());
        }
        if (request.getBaseFareAmount() != null) {
            route.setBase_fare_amount(request.getBaseFareAmount());
        }
        if (request.getStatus() != null) {
            route.setStatus(request.getStatus());
        }

        Route savedRoute = routeRepository.save(route);
        List<RouteStopResponse> stops = routeStopRepository
                .findAllByCompanyIdAndRouteId(companyId, routeId)
                .stream()
                .map(this::toStopResponse)
                .toList();
        return success(toResponse(savedRoute, stops));
    }

    @Override
    @Transactional
    public BaseResponse<RouteResponse> replaceRouteStops(
            UUID companyId,
            UUID routeId,
            ReplaceRouteStopsRequest request
    ) {
        BusCompany company = getActiveCompany(companyId);
        Route route = getCompanyRoute(companyId, routeId);
        List<ReplaceRouteStopsRequest.RouteStopInput> inputs = request.getStops();
        if (inputs.size() < 2) {
            throw badRequest("A route must have at least two stops");
        }

        Set<Integer> sequenceNumbers = new HashSet<>();
        Set<UUID> stopIds = new HashSet<>();
        for (ReplaceRouteStopsRequest.RouteStopInput input : inputs) {
            if (!sequenceNumbers.add(input.getSequenceNo())) {
                throw badRequest("Route stop sequence numbers must be unique");
            }
            if (!stopIds.add(input.getStopId())) {
                throw badRequest("A stop can only appear once in this route");
            }
            if (input.getArrivalOffsetMinutes() != null
                    && input.getDepartureOffsetMinutes() != null
                    && input.getArrivalOffsetMinutes() > input.getDepartureOffsetMinutes()) {
                throw badRequest("Arrival offset must not be later than departure offset");
            }
        }

        List<ReplaceRouteStopsRequest.RouteStopInput> orderedInputs = inputs.stream()
                .sorted(Comparator.comparing(ReplaceRouteStopsRequest.RouteStopInput::getSequenceNo))
                .toList();
        for (int i = 0; i < orderedInputs.size(); i++) {
            if (orderedInputs.get(i).getSequenceNo() != i + 1) {
                throw badRequest("Route stop sequence numbers must be continuous and start at 1");
            }
            if (i > 0) {
                Integer previousDeparture = orderedInputs.get(i - 1).getDepartureOffsetMinutes();
                Integer currentArrival = orderedInputs.get(i).getArrivalOffsetMinutes();
                if (previousDeparture != null && currentArrival != null && currentArrival < previousDeparture) {
                    throw badRequest("Stop time offsets must follow the route order");
                }
            }
        }

        Map<UUID, Stop> stopsById = stopRepository
                .findAllByCompanyIdAndIdsAndStatus(companyId, stopIds, StopStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(Stop::getId, Function.identity()));
        if (stopsById.size() != stopIds.size()) {
            throw badRequest("Every route stop must be active and belong to this company");
        }

        List<RouteStop> currentStops = routeStopRepository.findAllByCompanyIdAndRouteId(companyId, routeId)
                .stream()
                .map(routeStop -> routeStop)
                .toList();
        if (!currentStops.isEmpty()) {
            routeStopRepository.deleteAllInBatch(currentStops);
        }

        List<RouteStop> newRouteStops = new ArrayList<>();
        for (ReplaceRouteStopsRequest.RouteStopInput input : orderedInputs) {
            RouteStop routeStop = new RouteStop();
            routeStop.setCompany_id(company);
            routeStop.setRoute_id(route);
            routeStop.setStop_id(stopsById.get(input.getStopId()));
            routeStop.setSequence_no(input.getSequenceNo());
            routeStop.setArrival_offset_minutes(input.getArrivalOffsetMinutes());
            routeStop.setDeparture_offset_minutes(input.getDepartureOffsetMinutes());
            routeStop.setPickup_allowed(input.getPickupAllowed());
            routeStop.setDropoff_allowed(input.getDropoffAllowed());
            newRouteStops.add(routeStop);
        }

        routeStopRepository.saveAll(newRouteStops);
        routeStopRepository.flush();
        List<RouteStopResponse> savedStops = routeStopRepository
                .findAllByCompanyIdAndRouteId(companyId, routeId)
                .stream()
                .map(this::toStopResponse)
                .toList();
        return success(toResponse(route, savedStops));
    }

    private BusCompany getActiveCompany(UUID companyId) {
        return busCompanyRepository.findByIdAndStatus(companyId, BusCompanyStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Bus company was not found or is inactive"
                ));
    }

    private Route getCompanyRoute(UUID companyId, UUID routeId) {
        return routeRepository.findByIdAndCompanyId(companyId, routeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route was not found for this company"
                ));
    }

    private Area getActiveArea(UUID areaId) {
        return areaRepository.findActiveById(areaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Area was not found or is inactive"
                ));
    }

    private void validateDifferentAreas(Area originArea, Area destinationArea) {
        if (originArea.getId().equals(destinationArea.getId())) {
            throw badRequest("Origin and destination areas must be different");
        }
    }

    private boolean hasChanges(UpdateRouteRequest request) {
        return request.getCode() != null
                || request.getName() != null
                || request.getOriginAreaId() != null
                || request.getDestinationAreaId() != null
                || request.getDistanceKm() != null
                || request.getDurationMinutes() != null
                || request.getBaseFareAmount() != null
                || request.getStatus() != null;
    }

    private String normalizeCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw badRequest("Route code must not be blank");
        }
        return code.trim().toUpperCase();
    }

    private RouteResponse toResponse(Route route, List<RouteStopResponse> stops) {
        RouteResponse response = new RouteResponse();
        response.setId(route.getId());
        response.setCompanyId(route.getCompany_id().getId());
        response.setCode(route.getCode());
        response.setName(route.getName());
        response.setOriginAreaId(route.getOrigin_area_id().getId());
        response.setOriginAreaCode(route.getOrigin_area_id().getCode());
        response.setOriginAreaName(route.getOrigin_area_id().getName());
        response.setDestinationAreaId(route.getDestination_area_id().getId());
        response.setDestinationAreaCode(route.getDestination_area_id().getCode());
        response.setDestinationAreaName(route.getDestination_area_id().getName());
        response.setDistanceKm(route.getDistance_km());
        response.setDurationMinutes(route.getDuration_minutes());
        response.setBaseFareAmount(route.getBase_fare_amount());
        response.setStatus(route.getStatus());
        response.setStops(stops);
        response.setCreatedAt(route.getCreated_at());
        response.setUpdatedAt(route.getUpdated_at());
        return response;
    }

    private RouteStopResponse toStopResponse(RouteStop routeStop) {
        Stop stop = routeStop.getStop_id();
        RouteStopResponse response = new RouteStopResponse();
        response.setId(routeStop.getId());
        response.setStopId(stop.getId());
        response.setStopName(stop.getName());
        response.setAddress(stop.getAddress());
        response.setAreaId(stop.getArea_id().getId());
        response.setAreaName(stop.getArea_id().getName());
        response.setSequenceNo(routeStop.getSequence_no());
        response.setArrivalOffsetMinutes(routeStop.getArrival_offset_minutes());
        response.setDepartureOffsetMinutes(routeStop.getDeparture_offset_minutes());
        response.setPickupAllowed(routeStop.getPickup_allowed());
        response.setDropoffAllowed(routeStop.getDropoff_allowed());
        return response;
    }

    private BaseResponse<RouteResponse> success(RouteResponse routeResponse) {
        BaseResponse<RouteResponse> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(routeResponse);
        return response;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
