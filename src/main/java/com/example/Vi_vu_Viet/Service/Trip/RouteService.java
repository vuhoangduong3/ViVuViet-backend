package com.example.Vi_vu_Viet.Service.Trip;

import com.example.Vi_vu_Viet.dto.Request.Trip.CreateRouteRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.ReplaceRouteStopsRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateRouteRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.RouteResponse;

import java.util.List;
import java.util.UUID;

public interface RouteService {

    BaseResponse<List<RouteResponse>> getRoutes(UUID companyId);

    BaseResponse<RouteResponse> createRoute(UUID companyId, CreateRouteRequest request);

    BaseResponse<RouteResponse> getRoute(UUID companyId, UUID routeId);

    BaseResponse<RouteResponse> updateRoute(UUID companyId, UUID routeId, UpdateRouteRequest request);

    BaseResponse<RouteResponse> replaceRouteStops(
            UUID companyId,
            UUID routeId,
            ReplaceRouteStopsRequest request
    );
}
