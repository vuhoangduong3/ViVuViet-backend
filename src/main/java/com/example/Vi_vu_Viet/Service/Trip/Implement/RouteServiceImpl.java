package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Trip.Area;
import com.example.Vi_vu_Viet.Entity.Trip.BusCompany;
import com.example.Vi_vu_Viet.Entity.Trip.Route;
import com.example.Vi_vu_Viet.Repository.Trip.AreaRepository;
import com.example.Vi_vu_Viet.Repository.Trip.BusCompanyRepository;
import com.example.Vi_vu_Viet.Repository.Trip.RouteRepository;
import com.example.Vi_vu_Viet.Repository.Trip.RouteTripCountProjection;
import com.example.Vi_vu_Viet.Repository.Trip.TripRepository;
import com.example.Vi_vu_Viet.Service.Trip.RouteService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.RouteStatus;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateRouteRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateRouteRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.RouteResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    BusCompanyRepository busCompanyRepository;
    AreaRepository areaRepository;
    RouteRepository routeRepository;
    TripRepository tripRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<RouteResponse>> getRoutes(UUID companyId) {
        getActiveCompany(companyId);
        List<Route> routes = routeRepository.findAllByCompanyId(companyId);
        Map<UUID, Long> tripCounts = routes.isEmpty()
                ? Map.of()
                : tripRepository.countTripsByRouteIds(
                        companyId, routes.stream().map(Route::getId).toList())
                .stream()
                .collect(Collectors.toMap(
                        RouteTripCountProjection::getRouteId,
                        RouteTripCountProjection::getTripCount));
        List<RouteResponse> responses = routes.stream()
                .map(route -> toResponse(route, tripCounts.getOrDefault(route.getId(), 0L)))
                .toList();
        BaseResponse<List<RouteResponse>> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(responses);
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
        return success(toResponse(routeRepository.save(route), 0L));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<RouteResponse> getRoute(UUID companyId, UUID routeId) {
        getActiveCompany(companyId);
        Route route = getCompanyRoute(companyId, routeId);
        return success(toResponse(route, tripRepository.countByCompanyIdAndRouteId(companyId, routeId)));
    }

    @Override
    @Transactional
    public BaseResponse<RouteResponse> updateRoute(UUID companyId, UUID routeId, UpdateRouteRequest request) {
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

        if (request.getDistanceKm() != null) route.setDistance_km(request.getDistanceKm());
        if (request.getDurationMinutes() != null) route.setDuration_minutes(request.getDurationMinutes());
        if (request.getBaseFareAmount() != null) route.setBase_fare_amount(request.getBaseFareAmount());
        if (request.getStatus() != null) route.setStatus(request.getStatus());

        Route saved = routeRepository.save(route);
        return success(toResponse(saved, tripRepository.countByCompanyIdAndRouteId(companyId, routeId)));
    }

    private BusCompany getActiveCompany(UUID companyId) {
        return busCompanyRepository.findByIdAndStatus(companyId, BusCompanyStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bus company was not found or is inactive"));
    }

    private Route getCompanyRoute(UUID companyId, UUID routeId) {
        return routeRepository.findByIdAndCompanyId(companyId, routeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Route was not found for this company"));
    }

    private Area getActiveArea(UUID areaId) {
        return areaRepository.findActiveById(areaId)
                .orElseThrow(() -> badRequest("Area was not found or is inactive"));
    }

    private void validateDifferentAreas(Area originArea, Area destinationArea) {
        if (originArea.getId().equals(destinationArea.getId())) {
            throw badRequest("Origin and destination areas must be different");
        }
    }

    private boolean hasChanges(UpdateRouteRequest request) {
        return request.getCode() != null || request.getName() != null
                || request.getOriginAreaId() != null || request.getDestinationAreaId() != null
                || request.getDistanceKm() != null || request.getDurationMinutes() != null
                || request.getBaseFareAmount() != null || request.getStatus() != null;
    }

    private String normalizeCode(String code) {
        if (!StringUtils.hasText(code)) throw badRequest("Route code must not be blank");
        return code.trim().toUpperCase();
    }

    private RouteResponse toResponse(Route route, long tripCount) {
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
        response.setTripCount(tripCount);
        response.setStatus(route.getStatus());
        response.setCreatedAt(route.getCreated_at());
        response.setUpdatedAt(route.getUpdated_at());
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
