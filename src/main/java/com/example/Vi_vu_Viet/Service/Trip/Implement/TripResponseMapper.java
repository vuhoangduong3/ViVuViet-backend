package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripResponse;
import org.springframework.stereotype.Component;


@Component
public class TripResponseMapper {

    public TripResponse toTripResponse(Trip trip) {
        TripResponse response = new TripResponse();
        response.setId(trip.getId());
        response.setCompanyId(trip.getCompany_id().getId());
        response.setCompanyName(trip.getCompany_id().getName());
        response.setRouteId(trip.getRoute_id().getId());
        response.setRouteCode(trip.getRoute_id().getCode());
        response.setRouteName(trip.getRoute_id().getName());
        response.setOriginAreaId(trip.getRoute_id().getOrigin_area_id().getId());
        response.setOriginAreaName(trip.getRoute_id().getOrigin_area_id().getName());
        response.setDestinationAreaId(trip.getRoute_id().getDestination_area_id().getId());
        response.setDestinationAreaName(trip.getRoute_id().getDestination_area_id().getName());
        response.setTripCode(trip.getTrip_code());
        response.setDepartureAt(trip.getDeparture_at());
        response.setExpectedArrivalAt(trip.getExpected_arrival_at());
        response.setActualDepartureAt(trip.getActual_departure_at());
        response.setActualArrivalAt(trip.getActual_arrival_at());
        response.setBookingCutoffAt(trip.getBooking_cutoff_at());
        response.setBaseFareAmount(trip.getRoute_id().getBase_fare_amount());
        response.setStatus(trip.getStatus());
        response.setSalesStatus(trip.getSales_status());
        response.setCancelledAt(trip.getCancelled_at());
        response.setCancellationReason(trip.getCancellation_reason());
        response.setCancellationDescription(trip.getCancellation_description());
        response.setCreatedAt(trip.getCreated_at());
        response.setUpdatedAt(trip.getUpdated_at());
        return response;
    }

}
