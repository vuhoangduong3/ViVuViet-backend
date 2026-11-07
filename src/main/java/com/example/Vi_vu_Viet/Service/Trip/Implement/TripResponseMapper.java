package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Entity.Trip.TripStop;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripStopResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TripResponseMapper {

    public TripResponse toTripResponse(Trip trip, List<TripStopResponse> stops) {
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
        response.setStops(stops);
        response.setCreatedAt(trip.getCreated_at());
        response.setUpdatedAt(trip.getUpdated_at());
        return response;
    }

    public TripStopResponse toTripStopResponse(TripStop tripStop) {
        TripStopResponse response = new TripStopResponse();
        response.setId(tripStop.getId());
        response.setStopId(tripStop.getStop_id().getId());
        response.setAreaId(tripStop.getArea_id().getId());
        response.setAreaCode(tripStop.getArea_id().getCode());
        response.setAreaName(tripStop.getArea_id().getName());
        response.setSequenceNo(tripStop.getSequence_no());
        response.setName(tripStop.getName_snapshot());
        response.setAddress(tripStop.getAddress_snapshot());
        response.setPlannedArrivalAt(tripStop.getPlanned_arrival_at());
        response.setPlannedDepartureAt(tripStop.getPlanned_departure_at());
        response.setPickupAllowed(tripStop.getPickup_allowed());
        response.setDropoffAllowed(tripStop.getDropoff_allowed());
        return response;
    }
}
