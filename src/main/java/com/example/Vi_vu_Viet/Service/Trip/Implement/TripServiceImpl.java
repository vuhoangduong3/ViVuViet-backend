package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Trip.TripSeat;
import com.example.Vi_vu_Viet.Repository.Trip.SeatLayoutSeatRepository;
import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Trip.BusCompany;
import com.example.Vi_vu_Viet.Entity.Trip.Route;
import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Entity.Trip.TripAssignment;
import com.example.Vi_vu_Viet.Entity.Trip.Booking;
import com.example.Vi_vu_Viet.Entity.Trip.Ticket;
import com.example.Vi_vu_Viet.Entity.Trip.Vehicle;
import com.example.Vi_vu_Viet.Repository.Auth.UserRepository;
import com.example.Vi_vu_Viet.Repository.Trip.BusCompanyRepository;
import com.example.Vi_vu_Viet.Repository.Trip.RouteRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripSeatRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripIncidentRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripAssignmentRepository;
import com.example.Vi_vu_Viet.Repository.Trip.BookingRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TicketRepository;
import com.example.Vi_vu_Viet.Repository.Trip.CompanyMemberRepository;
import com.example.Vi_vu_Viet.Repository.Trip.DriverProfileRepository;
import com.example.Vi_vu_Viet.Repository.Trip.VehicleRepository;
import com.example.Vi_vu_Viet.Service.Trip.TripService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.RouteStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripSalesStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import com.example.Vi_vu_Viet.Util.Enums.DriverProfileStatus;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberStatus;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberRoleType;
import com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus;
import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.Util.Enums.VehicleStatus;
import com.example.Vi_vu_Viet.dto.Request.Trip.AssignTripRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateTripRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.CancelTripRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateTripRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripAssignmentResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripPassengerBookingResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.TicketResponse;
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
import java.time.LocalDate;
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
    TripRepository tripRepository;
    TripSeatRepository tripSeatRepository;
    TripResponseMapper tripResponseMapper;
    TripAssignmentRepository tripAssignmentRepository;
    BookingRepository bookingRepository;
    TicketRepository ticketRepository;
    CompanyMemberRepository companyMemberRepository;
    DriverProfileRepository driverProfileRepository;
    VehicleRepository vehicleRepository;
    SeatLayoutSeatRepository seatLayoutSeatRepository;
    TripIncidentRepository tripIncidentRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<TripResponse>> getCompanyTrips(UUID companyId, LocalDate date, TripStatus status,
                                                             UUID routeId, String assignmentStatus) {
        getActiveCompany(companyId);
        if (assignmentStatus != null && !assignmentStatus.equalsIgnoreCase("ASSIGNED")
                && !assignmentStatus.equalsIgnoreCase("UNASSIGNED")) {
            throw badRequest("assignmentStatus must be ASSIGNED or UNASSIGNED");
        }
        List<TripResponse> trips = tripRepository.findAllByCompanyId(companyId).stream()
                .filter(trip -> date == null || trip.getDeparture_at().toLocalDate().equals(date))
                .filter(trip -> status == null || trip.getStatus() == status)
                .filter(trip -> routeId == null || trip.getRoute_id().getId().equals(routeId))
                .filter(trip -> assignmentStatus == null ||
                        tripAssignmentRepository.existsByCompanyAndTrip(companyId, trip.getId())
                                == assignmentStatus.equalsIgnoreCase("ASSIGNED"))
                .map(tripResponseMapper::toTripResponse)
                .toList();
        return success(trips);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<TripPassengerBookingResponse>> getTripBookings(UUID companyId, UUID tripId) {
        getActiveCompany(companyId);
        getCompanyTripEntity(companyId, tripId);
        List<Booking> bookings = bookingRepository.findAllForCompanyTrip(companyId, tripId);
        if (bookings.isEmpty()) return successBookings(List.of());
        LocalDateTime now = LocalDateTime.now();
        List<UUID> bookingIds = bookings.stream().map(Booking::getId).toList();
        Map<UUID, List<Ticket>> ticketsByBooking = ticketRepository.findAllByBookingIds(bookingIds).stream()
                .collect(Collectors.groupingBy(ticket -> ticket.getBooking_id().getId()));
        List<TripPassengerBookingResponse> results = bookings.stream().map(booking -> {
            boolean expiredHold = booking.getStatus() == BookingStatus.HOLDING
                    && !booking.getHold_expires_at().isAfter(now);
            TripPassengerBookingResponse response = new TripPassengerBookingResponse();
            response.setId(booking.getId());
            response.setBookingCode(booking.getBooking_code());
            response.setStatus(expiredHold ? BookingStatus.EXPIRED : booking.getStatus());
            response.setContactName(booking.getContact_name());
            response.setContactPhone(booking.getContact_phone());
            response.setContactEmail(booking.getContact_email());
            response.setCreatedAt(booking.getCreated_at());
            response.setPassengers(ticketsByBooking.getOrDefault(booking.getId(), List.of()).stream()
                    .map(ticket -> toPassengerResponse(ticket, expiredHold)).toList());
            return response;
        }).toList();
        return successBookings(results);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<TripAssignmentResponse> getTripAssignment(UUID companyId, UUID tripId) {
        getActiveCompany(companyId);
        getCompanyTripEntity(companyId, tripId);
        return successAssignment(tripAssignmentRepository.findByCompanyAndTrip(companyId, tripId)
                .map(this::toAssignmentResponse).orElse(null));
    }

    @Override
    @Transactional
    public BaseResponse<TripAssignmentResponse> assignTrip(UUID companyId, UUID tripId,
                                                            AssignTripRequest request, UserAccount actor) {
        BusCompany company = getActiveCompany(companyId);
        Trip trip = tripRepository.findByIdAndCompanyIdForUpdate(companyId, tripId)
                .orElseThrow(() -> notFound("Trip was not found for this company"));
        if (trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED
                || trip.getStatus() == TripStatus.TERMINATED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A finished or cancelled trip cannot be assigned");
        }
        Vehicle vehicle = vehicleRepository.findByCompanyForUpdate(request.getVehicleId(), companyId)
                .filter(v -> v.getStatus() == VehicleStatus.ACTIVE)
                .orElseThrow(() -> notFound("Active vehicle was not found for this company"));
        // Serialize assignment with company driver status updates.
        UserAccount driver = userRepository.findByIdForUpdate(request.getDriverId())
                .filter(user -> user.getAccount_status() == UserAccountStatus.ACTIVE)
                .orElseThrow(() -> notFound("Active driver account was not found"));
        if (!companyMemberRepository.hasCompanyRole(companyId, driver.getId(),
                CompanyMemberStatus.ACTIVE, CompanyMemberRoleType.DRIVER)) {
            throw badRequest("Driver must have an active DRIVER role in this company");
        }
        if (driverProfileRepository.findByCompanyAndUser(companyId, driver.getId())
                .filter(profile -> profile.getStatus() != DriverProfileStatus.ACTIVE).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Driver is locked or inactive for this company");
        }
        if (tripAssignmentRepository.hasVehicleTimeConflict(vehicle.getId(), tripId,
                trip.getDeparture_at(), trip.getExpected_arrival_at())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vehicle is already assigned during this trip");
        }
        if (tripAssignmentRepository.hasDriverTimeConflict(driver.getId(), tripId,
                trip.getDeparture_at(), trip.getExpected_arrival_at())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Driver is already assigned during this trip");
        }
        TripAssignment assignment = tripAssignmentRepository.findByCompanyAndTrip(companyId, tripId)
                .orElseGet(TripAssignment::new);
        if (assignment.getVehicle_id() == null || !assignment.getVehicle_id().getId().equals(vehicle.getId())
                || trip.getStatus() == TripStatus.DRAFT) {
            snapshotVehicleSeats(companyId, trip, vehicle);
        }
        assignment.setCompany_id(company);
        assignment.setTrip_id(trip);
        assignment.setVehicle_id(vehicle);
        assignment.setDriver_user_id(driver);
        assignment.setAssigned_by(actor == null ? null : userRepository.findById(actor.getId()).orElse(null));
        assignment.setAssigned_at(LocalDateTime.now());
        assignment.setAssignment_status(DriverAssignmentStatus.ASSIGNED);
        assignment.setAccepted_at(null);
        assignment.setRejection_reason(null);
        return successAssignment(toAssignmentResponse(tripAssignmentRepository.save(assignment)));
    }

    @Override
    @Transactional
    public BaseResponse<TripResponse> createTrip(UUID companyId, CreateTripRequest request, UserAccount actor) {
        BusCompany company = getActiveCompany(companyId);
        Route route = getActiveCompanyRoute(companyId, request.getRouteId());
        LocalDateTime bookingCutoffAt = request.getBookingCutoffAt() == null
                ? request.getDepartureAt().minusMinutes(30)
                : request.getBookingCutoffAt();
        validateSchedule(request.getDepartureAt(), request.getExpectedArrivalAt(), bookingCutoffAt);
        String tripCode = normalizeTripCode(request.getTripCode());
        validateTripCodeAvailable(companyId, tripCode);

        Trip trip = new Trip();
        trip.setCompany_id(company);
        trip.setRoute_id(route);
        trip.setTrip_code(tripCode);
        trip.setDeparture_at(request.getDepartureAt());
        trip.setExpected_arrival_at(request.getExpectedArrivalAt());
        trip.setBooking_cutoff_at(bookingCutoffAt);
        trip.setStatus(TripStatus.DRAFT);
        trip.setSales_status(TripSalesStatus.CLOSED);
        trip.setCreated_by(actor == null ? null : userRepository.findById(actor.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user was not found")));

        return success(tripResponseMapper.toTripResponse(tripRepository.save(trip)));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<TripResponse> getCompanyTrip(UUID companyId, UUID tripId) {
        getActiveCompany(companyId);
        return success(tripResponseMapper.toTripResponse(getCompanyTripEntity(companyId, tripId)));
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
        if (!hasChanges(request)) throw badRequest("At least one trip field must be provided");

        if (request.getTripCode() != null) {
            String tripCode = normalizeTripCode(request.getTripCode());
            if (tripRepository.countByCompanyIdAndTripCodeExcludingTrip(companyId, tripCode, tripId) > 0) {
                throw badRequest("Trip code already exists for this company");
            }
            trip.setTrip_code(tripCode);
        }
        if (request.getDepartureAt() != null) {
            Duration shift = Duration.between(trip.getDeparture_at(), request.getDepartureAt());
            trip.setDeparture_at(request.getDepartureAt());
            if (request.getExpectedArrivalAt() == null) {
                trip.setExpected_arrival_at(trip.getExpected_arrival_at().plus(shift));
            }
            if (request.getBookingCutoffAt() == null) {
                trip.setBooking_cutoff_at(trip.getBooking_cutoff_at().plus(shift));
            }
        }
        if (request.getExpectedArrivalAt() != null) trip.setExpected_arrival_at(request.getExpectedArrivalAt());
        if (request.getBookingCutoffAt() != null) trip.setBooking_cutoff_at(request.getBookingCutoffAt());

        validateSchedule(trip.getDeparture_at(), trip.getExpected_arrival_at(), trip.getBooking_cutoff_at());
        return success(tripResponseMapper.toTripResponse(tripRepository.save(trip)));
    }

    @Override
    @Transactional
    public BaseResponse<TripResponse> publishTrip(UUID companyId, UUID tripId) {
        getActiveCompany(companyId);
        Trip trip = tripRepository.findByIdAndCompanyIdForUpdate(companyId, tripId)
                .orElseThrow(() -> notFound("Trip was not found for this company"));
        if (trip.getStatus() == TripStatus.SCHEDULED && trip.getSales_status() == TripSalesStatus.OPEN) {
            return success(tripResponseMapper.toTripResponse(trip));
        }
        if (trip.getStatus() != TripStatus.DRAFT || trip.getSales_status() != TripSalesStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only draft trips can be published");
        }

        validateSchedule(trip.getDeparture_at(), trip.getExpected_arrival_at(), trip.getBooking_cutoff_at());
        if (trip.getBooking_cutoff_at().isBefore(LocalDateTime.now())) {
            throw badRequest("Booking cutoff must be in the future when publishing a trip");
        }
        if (trip.getRoute_id().getBase_fare_amount() == null || trip.getRoute_id().getBase_fare_amount() <= 0) {
            throw badRequest("The route must have a positive default fare before publishing");
        }
        TripAssignment assignment = tripAssignmentRepository.findByCompanyAndTrip(companyId, tripId)
                .orElseThrow(() -> badRequest("Assign a vehicle with a seat layout before publishing"));
        Vehicle vehicle = vehicleRepository.findByCompanyForUpdate(assignment.getVehicle_id().getId(), companyId)
                .filter(v -> v.getStatus() == VehicleStatus.ACTIVE)
                .orElseThrow(() -> badRequest("Assigned vehicle must be active"));
        snapshotVehicleSeats(companyId, trip, vehicle);

        trip.setStatus(TripStatus.SCHEDULED);
        trip.setSales_status(TripSalesStatus.OPEN);
        return success(tripResponseMapper.toTripResponse(tripRepository.save(trip)));
    }

    @Override
    @Transactional
    public BaseResponse<TripResponse> cancelTrip(UUID companyId, UUID tripId, CancelTripRequest request) {
        getActiveCompany(companyId);
        Trip trip = tripRepository.findByIdAndCompanyIdForUpdate(companyId, tripId)
                .orElseThrow(() -> notFound("Trip was not found for this company"));
        if (trip.getStatus() == TripStatus.CANCELLED) {
            return success(tripResponseMapper.toTripResponse(trip));
        }
        if ((trip.getStatus() != TripStatus.DRAFT && trip.getStatus() != TripStatus.SCHEDULED)
                || trip.getActual_departure_at() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only trips that have not started can be cancelled");
        }

        LocalDateTime now = LocalDateTime.now();
        // Khóa đặt chỗ để không chạy đồng thời với xác nhận hoặc hủy vé của khách.
        List<Booking> bookings = bookingRepository.findAllForCompanyTripForUpdate(companyId, tripId);
        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.HOLDING && !booking.getHold_expires_at().isAfter(now)) {
                booking.setStatus(BookingStatus.EXPIRED);
            } else if (booking.getStatus() == BookingStatus.HOLDING || booking.getStatus() == BookingStatus.CONFIRMED) {
                booking.setStatus(BookingStatus.CANCELLED);
                booking.setCancelled_at(now);
            }
        }
        if (!bookings.isEmpty()) {
            List<Ticket> tickets = ticketRepository.findAllByBookingIds(bookings.stream().map(Booking::getId).toList());
            for (Ticket ticket : tickets) {
                if (ticket.getStatus() != TicketStatus.HOLDING && ticket.getStatus() != TicketStatus.CONFIRMED) continue;
                if (ticket.getBooking_id().getStatus() == BookingStatus.EXPIRED) {
                    ticket.setStatus(TicketStatus.EXPIRED);
                } else if (ticket.getBooking_id().getStatus() == BookingStatus.CANCELLED) {
                    ticket.setStatus(TicketStatus.CANCELLED);
                }
            }
        }

        trip.setStatus(TripStatus.CANCELLED);
        trip.setSales_status(TripSalesStatus.CLOSED);
        trip.setCancelled_at(now);
        trip.setCancellation_reason(request.getReason());
        trip.setCancellation_description(request.getDescription());
        return success(tripResponseMapper.toTripResponse(tripRepository.save(trip)));
    }

    @Override
    @Transactional
    public BaseResponse<Void> deleteTrip(UUID companyId, UUID tripId) {
        getActiveCompany(companyId);
        Trip trip = tripRepository.findByIdAndCompanyIdForUpdate(companyId, tripId)
                .orElseThrow(() -> notFound("Trip was not found for this company"));
        if (trip.getStatus() != TripStatus.DRAFT || trip.getSales_status() != TripSalesStatus.CLOSED
                || trip.getActual_departure_at() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only closed draft trips can be deleted");
        }
        if (bookingRepository.countByTripId(tripId) > 0 || ticketRepository.countByTripId(tripId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Trips with booking or ticket history cannot be deleted");
        }
        if (tripIncidentRepository.countByTripId(tripId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Trips with incident history cannot be deleted");
        }
        tripAssignmentRepository.deleteByCompanyAndTrip(companyId, tripId);
        tripSeatRepository.deleteByCompanyAndTrip(companyId, tripId);
        tripRepository.delete(trip);
        tripRepository.flush();
        return new BaseResponse<>(ApplicationCode.SUCCESS);
    }

    private void snapshotVehicleSeats(UUID companyId, Trip trip, Vehicle vehicle) {
        if (vehicle.getSeat_layout() == null) throw badRequest("Configure the vehicle seat layout first");
        var configured = seatLayoutSeatRepository.findAllByLayoutIdOrderByRowNoAscColumnNoAsc(
                vehicle.getSeat_layout().getId());
        if (configured.isEmpty() || configured.size() != vehicle.getPassenger_capacity()) {
            throw badRequest("Vehicle seat layout must match passenger capacity");
        }
        var existing = tripSeatRepository.findAllByCompanyIdAndTripId(companyId, trip.getId());
        var byCode = configured.stream().collect(Collectors.toMap(s -> s.getSeatCode(), s -> s));
        boolean identical = existing.size() == configured.size() && existing.stream().allMatch(seat -> {
            var target = byCode.get(seat.getSeat_code());
            return target != null
                    && java.util.Objects.equals(seat.getRow_no(), target.getRowNo())
                    && java.util.Objects.equals(seat.getColumn_no(), target.getColumnNo());
        });
        if (identical) return; // Keep inventory IDs referenced by tickets and selected by customers.
        if (bookingRepository.countByTripId(trip.getId()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Trips with bookings require an identical seat layout when changing vehicles");
        }
        tripSeatRepository.deleteAllInBatch(existing);
        var snapshot = configured.stream().map(source -> {
            var seat = new TripSeat();
            seat.setCompany_id(trip.getCompany_id());
            seat.setTrip_id(trip);
            seat.setSeat_code(source.getSeatCode());
            seat.setRow_no(source.getRowNo());
            seat.setColumn_no(source.getColumnNo());
            return seat;
        }).toList();
        tripSeatRepository.saveAll(snapshot);
    }

    private BusCompany getActiveCompany(UUID companyId) {
        return busCompanyRepository.findByIdAndStatus(companyId, BusCompanyStatus.ACTIVE)
                .orElseThrow(() -> notFound("Bus company was not found or is inactive"));
    }

    private Route getActiveCompanyRoute(UUID companyId, UUID routeId) {
        Route route = routeRepository.findByIdAndCompanyId(companyId, routeId)
                .orElseThrow(() -> notFound("Route was not found for this company"));
        if (route.getStatus() != RouteStatus.ACTIVE) throw badRequest("Route is inactive");
        return route;
    }

    private Trip getCompanyTripEntity(UUID companyId, UUID tripId) {
        return tripRepository.findByIdAndCompanyId(companyId, tripId)
                .orElseThrow(() -> notFound("Trip was not found for this company"));
    }

    private TripAssignmentResponse toAssignmentResponse(TripAssignment assignment) {
        TripAssignmentResponse response = new TripAssignmentResponse();
        response.setTripId(assignment.getTrip_id().getId());
        response.setVehicleId(assignment.getVehicle_id().getId());
        response.setDriverId(assignment.getDriver_user_id().getId());
        response.setDriverName(assignment.getDriver_user_id().getFull_name());
        response.setDriverPhone(assignment.getDriver_user_id().getPhone());
        response.setAssignedAt(assignment.getAssigned_at());
        return response;
    }

    private TicketResponse toPassengerResponse(Ticket ticket, boolean expiredHold) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTicketCode(ticket.getTicket_code());
        response.setPassengerName(ticket.getPassenger_name());
        response.setPassengerPhone(ticket.getPassenger_phone());
        response.setPickupAddress(ticket.getPickup_address());
        response.setDropoffAddress(ticket.getDropoff_address());
        response.setSeatId(ticket.getTrip_seat_id().getId());
        response.setSeatCode(ticket.getSeat_label_snapshot());
        response.setFareAmount(ticket.getFare_amount());
        response.setStatus(expiredHold && ticket.getStatus() == TicketStatus.HOLDING
                ? TicketStatus.EXPIRED : ticket.getStatus());
        response.setPassengerTripStatus(ticket.getPassenger_trip_status());
        response.setCreatedAt(ticket.getCreated_at());
        return response;
    }

    private void validateSchedule(LocalDateTime departureAt, LocalDateTime expectedArrivalAt,
                                  LocalDateTime bookingCutoffAt) {
        if (departureAt == null || expectedArrivalAt == null || bookingCutoffAt == null) {
            throw badRequest("Departure, expected arrival, and booking cutoff are required");
        }
        if (!expectedArrivalAt.isAfter(departureAt)) throw badRequest("Expected arrival must be later than departure");
        if (!bookingCutoffAt.isBefore(departureAt)) throw badRequest("Booking cutoff must be earlier than departure");
    }

    private void validateTripCodeAvailable(UUID companyId, String tripCode) {
        if (tripRepository.countByCompanyIdAndTripCode(companyId, tripCode) > 0) {
            throw badRequest("Trip code already exists for this company");
        }
    }

    private String normalizeTripCode(String tripCode) {
        if (!StringUtils.hasText(tripCode)) throw badRequest("Trip code must not be blank");
        return tripCode.trim().toUpperCase(Locale.ROOT);
    }

    private boolean hasChanges(UpdateTripRequest request) {
        return request.getTripCode() != null || request.getDepartureAt() != null
                || request.getExpectedArrivalAt() != null || request.getBookingCutoffAt() != null;
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

    private BaseResponse<List<TripPassengerBookingResponse>> successBookings(
            List<TripPassengerBookingResponse> bookings) {
        BaseResponse<List<TripPassengerBookingResponse>> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(bookings);
        return response;
    }

    private BaseResponse<TripAssignmentResponse> successAssignment(TripAssignmentResponse assignment) {
        BaseResponse<TripAssignmentResponse> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(assignment);
        return response;
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
