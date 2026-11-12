package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Trip.CompanyMember;
import com.example.Vi_vu_Viet.Entity.Trip.DriverProfile;
import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Entity.Trip.TripAssignment;
import com.example.Vi_vu_Viet.Entity.Trip.TripIncident;
import com.example.Vi_vu_Viet.Entity.Trip.Ticket;
import com.example.Vi_vu_Viet.Entity.Trip.Vehicle;
import com.example.Vi_vu_Viet.Repository.Auth.UserRepository;
import com.example.Vi_vu_Viet.Repository.Trip.CompanyMemberRepository;
import com.example.Vi_vu_Viet.Repository.Trip.DriverProfileRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TicketRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripAssignmentRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripIncidentRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripRepository;
import com.example.Vi_vu_Viet.Service.Trip.DriverService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberRoleType;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberStatus;
import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import com.example.Vi_vu_Viet.Util.Enums.DriverProfileStatus;
import com.example.Vi_vu_Viet.Util.Enums.IncidentStatus;
import com.example.Vi_vu_Viet.Util.Enums.PassengerTripStatus;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.dto.Request.Trip.ReportTripIncidentRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.RejectTripRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateDriverProfileRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateTripProgressRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.TicketResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.DriverProfileResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.DriverTripResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripIncidentResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.VehicleResponse;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    CompanyMemberRepository companyMemberRepository;
    DriverProfileRepository driverProfileRepository;
    TripAssignmentRepository tripAssignmentRepository;
    TripRepository tripRepository;
    TicketRepository ticketRepository;
    TripIncidentRepository tripIncidentRepository;
    UserRepository userRepository;
    TripResponseMapper tripResponseMapper;

    @Override
    @Transactional
    public BaseResponse<List<DriverProfileResponse>> getDriverProfiles(UUID userId) {
        List<CompanyMember> memberships = companyMemberRepository.findActiveMembershipsByRole(
                userId, CompanyMemberStatus.ACTIVE, CompanyMemberRoleType.DRIVER);
        if (memberships.isEmpty()) throw forbidden("An active driver membership is required");
        List<DriverProfileResponse> profiles = memberships.stream()
                .map(membership -> driverProfileRepository.findByCompanyAndMembership(
                                membership.getCompany_id().getId(), membership.getId())
                        .map(this::toDriverProfileResponse)
                        .orElseGet(() -> emptyDriverProfile(membership)))
                .toList();
        return success(profiles);
    }

    @Override
    @Transactional
    public BaseResponse<DriverProfileResponse> updateDriverProfile(UUID userId, UpdateDriverProfileRequest request) {
        if (!hasProfileChanges(request)) throw badRequest("At least one profile field must be provided");
        // Keep self-service profile changes from overwriting an admin's lock or reset metadata.
        userRepository.findByIdForUpdate(userId).orElseThrow(() -> notFound("Driver account was not found"));
        CompanyMember membership = companyMemberRepository.findActiveMembershipsByRole(
                        userId, CompanyMemberStatus.ACTIVE, CompanyMemberRoleType.DRIVER).stream()
                .filter(item -> item.getCompany_id().getId().equals(request.getCompanyId()))
                .findFirst().orElseThrow(() -> forbidden("User is not an active driver for this company"));

        DriverProfile profile = driverProfileRepository.findByCompanyAndMembership(
                        request.getCompanyId(), membership.getId())
                .orElseGet(() -> {
                    DriverProfile created = new DriverProfile();
                    created.setCompany_id(membership.getCompany_id());
                    created.setMembership_id(membership);
                    created.setStatus(DriverProfileStatus.ACTIVE);
                    return created;
                });
        if (request.getLicenseNumber() != null && driverProfileRepository.existsLicenseForAnotherProfile(
                request.getCompanyId(), request.getLicenseNumber(), profile.getId())) {
            throw badRequest("License number already exists for this company");
        }
        if (request.getEmployeeCode() != null) profile.setEmployee_code(request.getEmployeeCode());
        if (request.getLicenseNumber() != null) profile.setLicense_number(request.getLicenseNumber());
        if (request.getLicenseClass() != null) profile.setLicense_class(request.getLicenseClass());
        if (request.getLicenseExpiresOn() != null) profile.setLicense_expires_on(request.getLicenseExpiresOn());
        return success(toDriverProfileResponse(driverProfileRepository.save(profile)));
    }

    @Override
    @Transactional
    public BaseResponse<List<DriverTripResponse>> getDriverTrips(UUID userId, LocalDate from, LocalDate to,
                                                                 TripStatus status) {
        requireDriverMembership(userId);
        if (from != null && to != null && from.isAfter(to)) throw badRequest("from must be on or before to");
        LocalDateTime fromAt = from == null ? null : from.atStartOfDay();
        LocalDateTime toAt = to == null ? null : to.plusDays(1).atStartOfDay();
        List<DriverAssignmentStatus> excluded = List.of(DriverAssignmentStatus.REJECTED,
                DriverAssignmentStatus.REPLACED);
        List<DriverTripResponse> trips = tripAssignmentRepository.findDriverTrips(
                        userId, fromAt, toAt, status, excluded).stream()
                .filter(assignment -> hasDriverMembership(userId, assignment.getCompany_id().getId()))
                .map(assignment -> toDriverTripResponse(assignment, false))
                .toList();
        return success(trips);
    }

    @Override
    @Transactional
    public BaseResponse<DriverTripResponse> getDriverTrip(UUID userId, UUID tripId) {
        TripAssignment assignment = findDriverTrip(userId, tripId);
        return success(toDriverTripResponse(assignment, true));
    }

    @Override
    @Transactional
    public BaseResponse<DriverTripResponse> acceptTrip(UUID userId, UUID tripId) {
        TripAssignment assignment = findDriverTripForUpdate(userId, tripId);
        if (assignment.getTrip_id().getStatus() != TripStatus.SCHEDULED) {
            throw conflict("Only scheduled trips can be accepted");
        }
        if (assignment.getAssignment_status() == DriverAssignmentStatus.ACCEPTED) {
            return success(toDriverTripResponse(assignment, true));
        }
        if (assignment.getAssignment_status() != DriverAssignmentStatus.ASSIGNED) {
            throw conflict("Trip assignment can no longer be accepted");
        }
        assignment.setAssignment_status(DriverAssignmentStatus.ACCEPTED);
        assignment.setAccepted_at(LocalDateTime.now());
        assignment.setRejection_reason(null);
        tripAssignmentRepository.save(assignment);
        return success(toDriverTripResponse(assignment, true));
    }

    @Override
    @Transactional
    public BaseResponse<DriverTripResponse> rejectTrip(UUID userId, UUID tripId, RejectTripRequest request) {
        TripAssignment assignment = findDriverTripForUpdate(userId, tripId);
        if (assignment.getTrip_id().getStatus() != TripStatus.SCHEDULED) {
            throw conflict("Only scheduled trips can be rejected");
        }
        if (assignment.getAssignment_status() != DriverAssignmentStatus.ASSIGNED) {
            throw conflict("Only a pending trip assignment can be rejected");
        }
        assignment.setAssignment_status(DriverAssignmentStatus.REJECTED);
        assignment.setRejection_reason(request.getReason());
        tripAssignmentRepository.save(assignment);
        return success(toDriverTripResponse(assignment, false));
    }

    @Override
    @Transactional
    public BaseResponse<DriverTripResponse> updateTripProgress(UUID userId, UUID tripId,
                                                               UpdateTripProgressRequest request) {
        TripAssignment assignment = findDriverTripForUpdate(userId, tripId);
        if (request.getStatus() == TripStatus.COMPLETED
                && assignment.getTrip_id().getStatus() == TripStatus.COMPLETED
                && assignment.getAssignment_status() == DriverAssignmentStatus.COMPLETED) {
            return success(toDriverTripResponse(assignment, true));
        }
        requireAcceptedAssignment(assignment);
        Trip trip = assignment.getTrip_id();
        TripStatus current = trip.getStatus();
        TripStatus requested = request.getStatus();
        LocalDateTime actualAt = request.getActualAt() == null ? LocalDateTime.now() : request.getActualAt();
        if (actualAt.isAfter(LocalDateTime.now().plusMinutes(1))) throw badRequest("actualAt cannot be in the future");

        if (requested == TripStatus.IN_PROGRESS && current == TripStatus.SCHEDULED) {
            trip.setStatus(TripStatus.IN_PROGRESS);
            trip.setActual_departure_at(actualAt);
        } else if (requested == TripStatus.COMPLETED && current == TripStatus.IN_PROGRESS) {
            if (trip.getActual_departure_at() != null && actualAt.isBefore(trip.getActual_departure_at())) {
                throw badRequest("Arrival time must be after actual departure");
            }
            trip.setStatus(TripStatus.COMPLETED);
            trip.setActual_arrival_at(actualAt);
            assignment.setAssignment_status(DriverAssignmentStatus.COMPLETED);
        } else if (requested != current) {
            throw conflict("Trip status transition is not allowed");
        }
        tripRepository.save(trip);
        tripAssignmentRepository.save(assignment);
        return success(toDriverTripResponse(assignment, true));
    }

    @Override
    @Transactional
    public BaseResponse<TicketResponse> updatePassengerStatus(UUID userId, UUID tripId, UUID ticketId,
                                                               PassengerTripStatus status) {
        TripAssignment assignment = findDriverTrip(userId, tripId);
        if (assignment.getTrip_id().getStatus() != TripStatus.IN_PROGRESS) {
            throw conflict("Passenger status can only be updated while the trip is in progress");
        }
        Ticket ticket = ticketRepository.findByIdAndTripId(ticketId, tripId)
                .orElseThrow(() -> notFound("Ticket was not found for this trip"));
        if (ticket.getStatus() != TicketStatus.CONFIRMED) {
            throw conflict("Only confirmed tickets can be boarded");
        }
        PassengerTripStatus current = ticket.getPassenger_trip_status();
        if (current == null) current = PassengerTripStatus.WAITING_PICKUP;
        boolean valid = (current == PassengerTripStatus.WAITING_PICKUP
                && (status == PassengerTripStatus.PICKED_UP || status == PassengerTripStatus.NO_SHOW))
                || (current == PassengerTripStatus.PICKED_UP && status == PassengerTripStatus.DROPPED_OFF)
                || current == status;
        if (!valid) throw conflict("Passenger status transition is not allowed");
        LocalDateTime now = LocalDateTime.now();
        ticket.setPassenger_trip_status(status);
        if (status == PassengerTripStatus.PICKED_UP && ticket.getBoarded_at() == null) ticket.setBoarded_at(now);
        if (status == PassengerTripStatus.DROPPED_OFF && ticket.getCompleted_at() == null) ticket.setCompleted_at(now);
        return success(toTicketResponse(ticket));
    }

    @Override
    @Transactional
    public BaseResponse<TripIncidentResponse> reportIncident(UUID userId, UUID tripId,
                                                              ReportTripIncidentRequest request) {
        TripAssignment assignment = findDriverTrip(userId, tripId);
        if (assignment.getAssignment_status() == DriverAssignmentStatus.REJECTED
                || assignment.getAssignment_status() == DriverAssignmentStatus.REPLACED) {
            throw forbidden("This driver is no longer assigned to the trip");
        }
        TripIncident incident = new TripIncident();
        incident.setCompany_id(assignment.getCompany_id());
        incident.setTrip_id(assignment.getTrip_id());
        incident.setReported_by(userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found")));
        incident.setIncident_type(request.getType());
        incident.setDescription(request.getDescription());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setOpened_at(LocalDateTime.now());
        return success(toIncidentResponse(tripIncidentRepository.save(incident)));
    }

    @Override
    @Transactional
    public BaseResponse<VehicleResponse> getAssignedVehicle(UUID userId) {
        requireDriverMembership(userId);
        List<TripAssignment> assignments = tripAssignmentRepository.findDriverVehicleAssignments(userId,
                List.of(DriverAssignmentStatus.ASSIGNED, DriverAssignmentStatus.ACCEPTED,
                        DriverAssignmentStatus.PREPARING, DriverAssignmentStatus.ARRIVED),
                List.of(TripStatus.SCHEDULED, TripStatus.IN_PROGRESS));
        Vehicle vehicle = assignments.stream()
                .filter(assignment -> hasDriverMembership(userId, assignment.getCompany_id().getId()))
                .map(TripAssignment::getVehicle_id)
                .findFirst()
                .orElseThrow(() -> notFound("No active or upcoming vehicle assignment was found"));
        return success(toVehicleResponse(vehicle));
    }

    private TripAssignment findDriverTrip(UUID userId, UUID tripId) {
        TripAssignment assignment = tripAssignmentRepository.findDriverTrip(userId, tripId)
                .orElseThrow(() -> notFound("Trip assignment was not found"));
        requireDriverMembership(userId, assignment.getCompany_id().getId());
        normalizeAssignmentStatus(assignment);
        return assignment;
    }

    private TripAssignment findDriverTripForUpdate(UUID userId, UUID tripId) {
        TripAssignment assignment = tripAssignmentRepository.findDriverTripForUpdate(userId, tripId)
                .orElseThrow(() -> notFound("Trip assignment was not found"));
        requireDriverMembership(userId, assignment.getCompany_id().getId());
        normalizeAssignmentStatus(assignment);
        return assignment;
    }

    private void requireDriverMembership(UUID userId) {
        if (companyMemberRepository.findActiveMembershipsByRole(userId, CompanyMemberStatus.ACTIVE,
                CompanyMemberRoleType.DRIVER).isEmpty()) {
            throw forbidden("An active driver membership is required");
        }
    }

    private void requireDriverMembership(UUID userId, UUID companyId) {
        if (!hasDriverMembership(userId, companyId)) {
            throw forbidden("An active driver membership is required for this company");
        }
    }

    private boolean hasDriverMembership(UUID userId, UUID companyId) {
        return companyMemberRepository.hasCompanyRole(companyId, userId, CompanyMemberStatus.ACTIVE,
                CompanyMemberRoleType.DRIVER);
    }

    private void normalizeAssignmentStatus(TripAssignment assignment) {
        if (assignment.getAssignment_status() == null) {
            assignment.setAssignment_status(DriverAssignmentStatus.ASSIGNED);
        }
    }

    private void requireAcceptedAssignment(TripAssignment assignment) {
        if (assignment.getAssignment_status() != DriverAssignmentStatus.ACCEPTED
                && assignment.getAssignment_status() != DriverAssignmentStatus.PREPARING
                && assignment.getAssignment_status() != DriverAssignmentStatus.ARRIVED) {
            throw conflict("Accept the trip assignment before updating its progress");
        }
    }

    private DriverTripResponse toDriverTripResponse(TripAssignment assignment, boolean includePassengers) {
        DriverTripResponse response = new DriverTripResponse();
        response.setTrip(tripResponseMapper.toTripResponse(assignment.getTrip_id()));
        response.setVehicle(toVehicleResponse(assignment.getVehicle_id()));
        response.setAssignmentStatus(assignment.getAssignment_status() == null
                ? DriverAssignmentStatus.ASSIGNED : assignment.getAssignment_status());
        response.setAcceptedAt(assignment.getAccepted_at());
        response.setRejectionReason(assignment.getRejection_reason());
        if (includePassengers) {
            response.setPassengers(ticketRepository.findAllActiveByTripId(assignment.getTrip_id().getId(),
                            List.of(TicketStatus.CONFIRMED)).stream()
                    .map(this::toTicketResponse).toList());
        }
        return response;
    }

    private DriverProfileResponse emptyDriverProfile(CompanyMember membership) {
        DriverProfileResponse response = new DriverProfileResponse();
        response.setCompanyId(membership.getCompany_id().getId());
        response.setCompanyName(membership.getCompany_id().getName());
        response.setUserId(membership.getUser_id().getId());
        response.setFullName(membership.getUser_id().getFull_name());
        response.setPhone(membership.getUser_id().getPhone());
        response.setStatus(DriverProfileStatus.ACTIVE);
        return response;
    }

    private DriverProfileResponse toDriverProfileResponse(DriverProfile profile) {
        DriverProfileResponse response = new DriverProfileResponse();
        response.setId(profile.getId());
        response.setCompanyId(profile.getCompany_id().getId());
        response.setCompanyName(profile.getCompany_id().getName());
        response.setUserId(profile.getMembership_id().getUser_id().getId());
        response.setFullName(profile.getMembership_id().getUser_id().getFull_name());
        response.setPhone(profile.getMembership_id().getUser_id().getPhone());
        response.setEmployeeCode(profile.getEmployee_code());
        response.setLicenseNumber(profile.getLicense_number());
        response.setLicenseClass(profile.getLicense_class());
        response.setLicenseExpiresOn(profile.getLicense_expires_on());
        response.setStatus(profile.getStatus());
        return response;
    }

    private boolean hasProfileChanges(UpdateDriverProfileRequest request) {
        return request.getEmployeeCode() != null || request.getLicenseNumber() != null
                || request.getLicenseClass() != null || request.getLicenseExpiresOn() != null;
    }

    private VehicleResponse toVehicleResponse(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.setId(vehicle.getId());
        response.setCompanyId(vehicle.getCompany_id().getId());
        response.setPlateNumber(vehicle.getPlate_normalized());
        response.setVehicleModel(vehicle.getVehicle_model());
        response.setPassengerCapacity(vehicle.getPassenger_capacity());
        response.setStatus(vehicle.getStatus());
        response.setCreatedAt(vehicle.getCreated_at());
        response.setUpdatedAt(vehicle.getUpdated_at());
        return response;
    }

    private TicketResponse toTicketResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTicketCode(ticket.getTicket_code());
        response.setPassengerName(ticket.getPassenger_name());
        response.setPassengerPhone(ticket.getPassenger_phone());
        response.setPickupAddress(ticket.getPickup_address());
        response.setDropoffAddress(ticket.getDropoff_address());
        response.setSeatId(ticket.getTrip_seat_id().getId());
        response.setSeatCode(ticket.getSeat_label_snapshot());
        response.setSeatClass(ticket.getTrip_seat_id().getSeat_class());
        response.setFareAmount(ticket.getFare_amount());
        response.setStatus(ticket.getStatus());
        response.setPassengerTripStatus(ticket.getPassenger_trip_status());
        response.setCreatedAt(ticket.getCreated_at());
        return response;
    }

    private TripIncidentResponse toIncidentResponse(TripIncident incident) {
        TripIncidentResponse response = new TripIncidentResponse();
        response.setId(incident.getId());
        response.setTripId(incident.getTrip_id().getId());
        response.setType(incident.getIncident_type());
        response.setDescription(incident.getDescription());
        response.setStatus(incident.getStatus());
        response.setOpenedAt(incident.getOpened_at());
        return response;
    }

    private <T> BaseResponse<T> success(T value) {
        BaseResponse<T> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(value);
        return response;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
