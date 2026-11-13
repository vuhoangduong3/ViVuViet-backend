package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Trip.Booking;
import com.example.Vi_vu_Viet.Entity.Trip.BusCompany;
import com.example.Vi_vu_Viet.Entity.Trip.Ticket;
import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Entity.Trip.TripSeat;
import com.example.Vi_vu_Viet.Repository.Trip.BookingRepository;
import com.example.Vi_vu_Viet.Repository.Trip.BusCompanyRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TicketRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripSeatRepository;
import com.example.Vi_vu_Viet.Service.Trip.BookingService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripSalesStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.dto.Request.Booking.CreateBookingRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.BookingResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.BookingTripResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.TicketResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.TripSeatResponse;
import com.example.Vi_vu_Viet.dto.Response.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final int HOLD_MINUTES = 5;
    private static final List<TicketStatus> OCCUPYING_TICKET_STATUSES =
            List.of(TicketStatus.HOLDING, TicketStatus.CONFIRMED);

    BusCompanyRepository busCompanyRepository;
    TripRepository tripRepository;
    TripSeatRepository tripSeatRepository;
    BookingRepository bookingRepository;
    TicketRepository ticketRepository;

    @Override
    @Transactional
    public BaseResponse<List<TripSeatResponse>> getAvailableSeats(UUID tripId) {
        Trip trip = getPublishedTrip(tripId);
        LocalDateTime now = LocalDateTime.now();
        expireHolds(tripId, now);
        Set<UUID> occupiedSeatIds = ticketRepository
                .findAllActiveByTripId(tripId, OCCUPYING_TICKET_STATUSES)
                .stream()
                .map(ticket -> ticket.getTrip_seat_id().getId())
                .collect(Collectors.toSet());
        List<TripSeat> seats = tripSeatRepository.findAllByCompanyIdAndTripId(
                trip.getCompany_id().getId(), tripId);
        return success(toSeatResponses(seats, occupiedSeatIds));
    }

    @Override
    @Transactional
    public BaseResponse<BookingResponse> createBooking(CreateBookingRequest request, UserAccount customer) {
        Trip trip = tripRepository.findPublishedTripByIdForUpdate(
                        request.getTripId(), TripStatus.SCHEDULED, TripSalesStatus.OPEN, BusCompanyStatus.ACTIVE)
                .orElseThrow(() -> notFound("Published trip was not found"));
        LocalDateTime now = LocalDateTime.now();
        expireHolds(trip.getId(), now);
        if (!trip.getBooking_cutoff_at().isAfter(now) || !trip.getDeparture_at().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking is closed for this trip");
        }
        Long fare = trip.getRoute_id().getBase_fare_amount();
        if (fare == null || fare <= 0) throw badRequest("Trip fare is not configured");

        Set<UUID> requestedSeatIds = request.getPassengers().stream()
                .map(CreateBookingRequest.PassengerInput::getSeatId)
                .collect(Collectors.toSet());
        if (requestedSeatIds.size() != request.getPassengers().size()) {
            throw badRequest("Each passenger must choose a different seat");
        }

        List<TripSeat> selectedSeats = new ArrayList<>();
        for (UUID seatId : requestedSeatIds) {
            selectedSeats.add(tripSeatRepository.findByIdAndCompanyIdAndTripId(
                            seatId, trip.getCompany_id().getId(), trip.getId())
                    .orElseThrow(() -> badRequest("A selected seat does not belong to this trip")));
        }
        Set<UUID> occupiedSeatIds = ticketRepository
                .findAllActiveByTripId(trip.getId(), OCCUPYING_TICKET_STATUSES)
                .stream()
                .map(ticket -> ticket.getTrip_seat_id().getId())
                .collect(Collectors.toSet());
        if (requestedSeatIds.stream().anyMatch(occupiedSeatIds::contains)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "One or more selected seats are unavailable");
        }

        Booking booking = new Booking();
        booking.setCompany_id(trip.getCompany_id());
        booking.setTrip_id(trip);
        booking.setCustomer_user_id(customer);
        booking.setBooking_code(generateBookingCode(trip.getCompany_id().getId()));
        booking.setContact_name(request.getContactName().trim());
        booking.setContact_phone(request.getContactPhone().trim());
        booking.setContact_email(normalize(request.getContactEmail()));
        booking.setStatus(BookingStatus.HOLDING);
        booking.setHold_expires_at(now.plusMinutes(HOLD_MINUTES));
        booking.setQuoted_total_amount(Math.multiplyExact(fare, request.getPassengers().size()));
        booking.setCurrency("VND");
        bookingRepository.saveAndFlush(booking);

        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < request.getPassengers().size(); i++) {
            CreateBookingRequest.PassengerInput passenger = request.getPassengers().get(i);
            TripSeat seat = selectedSeats.stream()
                    .filter(candidate -> candidate.getId().equals(passenger.getSeatId()))
                    .findFirst()
                    .orElseThrow();
            Ticket ticket = new Ticket();
            ticket.setCompany_id(trip.getCompany_id());
            ticket.setTrip_id(trip);
            ticket.setBooking_id(booking);
            ticket.setTrip_seat_id(seat);
            ticket.setTicket_code(generateTicketCode(trip.getCompany_id().getId()));
            ticket.setPassenger_name(passenger.getPassengerName().trim());
            ticket.setPassenger_phone(normalize(passenger.getPassengerPhone()));
            ticket.setPickup_address(passenger.getPickupAddress().trim());
            ticket.setDropoff_address(passenger.getDropoffAddress().trim());
            ticket.setSeat_label_snapshot(seat.getSeat_code());
            ticket.setFare_amount(fare);
            ticket.setStatus(TicketStatus.HOLDING);
            tickets.add(ticket);
        }
        ticketRepository.saveAll(tickets);
        return success(toBookingResponse(booking, tickets));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<BookingResponse>> getMyBookings(
            UUID customerId, BookingStatus status, UUID routeId, String q, int page, int size) {
        if (page < 0) throw badRequest("page must be greater than or equal to 0");
        if (size < 1 || size > 100) throw badRequest("size must be between 1 and 100");
        if ((long) page * size > Integer.MAX_VALUE) throw badRequest("Requested page offset is too large");
        if (q != null && q.length() > 200) throw badRequest("q must not exceed 200 characters");
        String pattern = StringUtils.hasText(q)
                ? "%" + q.trim().toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%"
                : null;
        LocalDateTime now = LocalDateTime.now();
        Page<Booking> bookings = bookingRepository.searchByCustomerId(customerId, status, routeId, pattern,
                BookingStatus.HOLDING, BookingStatus.EXPIRED, now, PageRequest.of(page, size));
        List<UUID> bookingIds = bookings.getContent().stream().map(Booking::getId).toList();
        Map<UUID, List<Ticket>> ticketsByBooking = bookingIds.isEmpty() ? Map.of()
                : ticketRepository.findAllByBookingIds(bookingIds).stream()
                    .collect(Collectors.groupingBy(ticket -> ticket.getBooking_id().getId()));
        Page<BookingResponse> results = bookings.map(booking -> toBookingResponse(
                booking, ticketsByBooking.getOrDefault(booking.getId(), List.of()), now));
        BaseResponse<PageResponse<BookingResponse>> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(PageResponse.from(results));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<BookingResponse>> getMyUpcomingBookings(UUID customerId) {
        return getMyActiveBookingsByTripStatus(customerId, TripStatus.SCHEDULED,
                List.of(BookingStatus.CONFIRMED, BookingStatus.HOLDING));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<BookingResponse>> getMyInProgressBookings(UUID customerId) {
        return getMyActiveBookingsByTripStatus(customerId, TripStatus.IN_PROGRESS, List.of(BookingStatus.CONFIRMED));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<BookingResponse>> getMyCompletedBookings(UUID customerId) {
        return getMyActiveBookingsByTripStatus(customerId, TripStatus.COMPLETED, List.of(BookingStatus.CONFIRMED));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<BookingResponse>> getMyCancelledBookings(UUID customerId) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findAllCancelledForCustomer(customerId,
                List.of(BookingStatus.CANCELLED, BookingStatus.EXPIRED), BookingStatus.HOLDING, now,
                List.of(TripStatus.CANCELLED, TripStatus.TERMINATED));
        return toMyBookingListResponse(bookings, now);
    }

    private BaseResponse<List<BookingResponse>> getMyActiveBookingsByTripStatus(
            UUID customerId, TripStatus tripStatus, List<BookingStatus> bookingStatuses) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findAllActiveForCustomerTripStatuses(customerId,
                List.of(tripStatus), bookingStatuses, BookingStatus.HOLDING, now);
        return toMyBookingListResponse(bookings, now);
    }

    private BaseResponse<List<BookingResponse>> toMyBookingListResponse(List<Booking> bookings, LocalDateTime now) {
        List<UUID> bookingIds = bookings.stream().map(Booking::getId).toList();
        Map<UUID, List<Ticket>> ticketsByBooking = bookingIds.isEmpty() ? Map.of()
                : ticketRepository.findAllByBookingIds(bookingIds).stream()
                    .collect(Collectors.groupingBy(ticket -> ticket.getBooking_id().getId()));
        BaseResponse<List<BookingResponse>> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(bookings.stream().map(booking -> toBookingResponse(
                booking, ticketsByBooking.getOrDefault(booking.getId(), List.of()), now)).toList());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<BookingResponse> getMyBooking(UUID customerId, UUID bookingId) {
        Booking booking = getOwnedBooking(customerId, bookingId);
        return success(toBookingResponse(booking, ticketRepository.findAllByBookingId(bookingId)));
    }

    @Override
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public BaseResponse<BookingResponse> confirmBooking(UUID customerId, UUID bookingId) {
        Booking booking = getOwnedBookingForUpdate(customerId, bookingId);
        LocalDateTime now = LocalDateTime.now();
        expireBookingIfNeeded(booking, now);
        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking hold has expired");
        }
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return success(toBookingResponse(booking, ticketRepository.findAllByBookingId(bookingId)));
        }
        if (booking.getStatus() != BookingStatus.HOLDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking cannot be confirmed in its current state");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmed_at(now);
        List<Ticket> tickets = ticketRepository.findAllByBookingId(bookingId);
        tickets.forEach(ticket -> ticket.setStatus(TicketStatus.CONFIRMED));
        bookingRepository.save(booking);
        ticketRepository.saveAll(tickets);
        return success(toBookingResponse(booking, tickets));
    }

    @Override
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public BaseResponse<BookingResponse> cancelBooking(UUID customerId, UUID bookingId) {
        Booking booking = getOwnedBookingForUpdate(customerId, bookingId);
        LocalDateTime now = LocalDateTime.now();
        expireBookingIfNeeded(booking, now);
        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Expired booking cannot be cancelled");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return success(toBookingResponse(booking, ticketRepository.findAllByBookingId(bookingId)));
        }
        if (booking.getStatus() != BookingStatus.HOLDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking cannot be cancelled in its current state");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelled_at(now);
        List<Ticket> tickets = ticketRepository.findAllByBookingId(bookingId);
        tickets.forEach(ticket -> ticket.setStatus(TicketStatus.CANCELLED));
        bookingRepository.save(booking);
        ticketRepository.saveAll(tickets);
        return success(toBookingResponse(booking, tickets));
    }

    private Trip getPublishedTrip(UUID tripId) {
        return tripRepository.findPublishedTripById(
                        tripId, TripStatus.SCHEDULED, TripSalesStatus.OPEN, BusCompanyStatus.ACTIVE)
                .orElseThrow(() -> notFound("Published trip was not found"));
    }

    private Booking getOwnedBooking(UUID customerId, UUID bookingId) {
        return bookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> notFound("Booking was not found"));
    }

    private Booking getOwnedBookingForUpdate(UUID customerId, UUID bookingId) {
        return bookingRepository.findByIdAndCustomerIdForUpdate(bookingId, customerId)
                .orElseThrow(() -> notFound("Booking was not found"));
    }

    private void expireHolds(UUID tripId, LocalDateTime now) {
        List<Booking> expiredBookings = bookingRepository.findExpiredHolds(tripId, BookingStatus.HOLDING, now);
        for (Booking booking : expiredBookings) expireBooking(booking);
    }

    private void expireBookingIfNeeded(Booking booking, LocalDateTime now) {
        if (booking.getStatus() == BookingStatus.HOLDING && !booking.getHold_expires_at().isAfter(now)) {
            expireBooking(booking);
        }
    }

    private void expireBooking(Booking booking) {
        booking.setStatus(BookingStatus.EXPIRED);
        bookingRepository.save(booking);
        List<Ticket> tickets = ticketRepository.findAllByBookingId(booking.getId());
        tickets.forEach(ticket -> ticket.setStatus(TicketStatus.EXPIRED));
        ticketRepository.saveAll(tickets);
    }

    private List<TripSeatResponse> toSeatResponses(List<TripSeat> seats, Set<UUID> occupiedSeatIds) {
        return seats.stream().map(seat -> {
            TripSeatResponse response = new TripSeatResponse();
            response.setId(seat.getId());
            response.setSeatCode(seat.getSeat_code());
            response.setRowNo(seat.getRow_no());
            response.setColumnNo(seat.getColumn_no());
            response.setAvailable(!occupiedSeatIds.contains(seat.getId()));
            return response;
        }).toList();
    }

    private BookingResponse toBookingResponse(Booking booking, List<Ticket> tickets) {
        return toBookingResponse(booking, tickets, LocalDateTime.now());
    }

    private BookingResponse toBookingResponse(Booking booking, List<Ticket> tickets, LocalDateTime now) {
        boolean expiredHold = booking.getStatus() == BookingStatus.HOLDING
                && !booking.getHold_expires_at().isAfter(now);
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setTripId(booking.getTrip_id().getId());
        response.setTrip(toBookingTripResponse(booking));
        response.setBookingCode(booking.getBooking_code());
        response.setContactName(booking.getContact_name());
        response.setContactPhone(booking.getContact_phone());
        response.setContactEmail(booking.getContact_email());
        response.setStatus(expiredHold ? BookingStatus.EXPIRED : booking.getStatus());
        response.setHoldExpiresAt(booking.getHold_expires_at());
        response.setQuotedTotalAmount(booking.getQuoted_total_amount());
        response.setCurrency(booking.getCurrency());
        response.setConfirmedAt(booking.getConfirmed_at());
        response.setCancelledAt(booking.getCancelled_at());
        response.setCreatedAt(booking.getCreated_at());
        response.setUpdatedAt(booking.getUpdated_at());
        response.setTickets(tickets.stream().map(ticket -> toTicketResponse(ticket, expiredHold)).toList());
        return response;
    }

    private BookingTripResponse toBookingTripResponse(Booking booking) {
        Trip trip = booking.getTrip_id();
        BusCompany company = booking.getCompany_id();
        var route = trip.getRoute_id();
        BookingTripResponse response = new BookingTripResponse();
        response.setId(trip.getId());
        response.setTripCode(trip.getTrip_code());
        response.setStatus(trip.getStatus());
        response.setDepartureAt(trip.getDeparture_at());
        response.setExpectedArrivalAt(trip.getExpected_arrival_at());
        response.setCompanyId(company.getId());
        response.setCompanyName(company.getName());
        response.setCompanyHotline(company.getHotline());
        response.setTimezone(company.getTimezone());
        response.setRouteId(route.getId());
        response.setRouteCode(route.getCode());
        response.setRouteName(route.getName());
        response.setOriginAreaId(route.getOrigin_area_id().getId());
        response.setOriginAreaName(route.getOrigin_area_id().getName());
        response.setDestinationAreaId(route.getDestination_area_id().getId());
        response.setDestinationAreaName(route.getDestination_area_id().getName());
        return response;
    }

    private TicketResponse toTicketResponse(Ticket ticket, boolean expiredHold) {
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
        response.setPaymentCollected(ticket.getCollected_at() == null ? null : Boolean.TRUE);
        response.setCollectedAmount(ticket.getCollected_amount());
        response.setCollectedAt(ticket.getCollected_at());
        response.setCollectedBy(ticket.getCollected_by() == null ? null : ticket.getCollected_by().getId());
        response.setStatus(expiredHold && ticket.getStatus() == TicketStatus.HOLDING
                ? TicketStatus.EXPIRED : ticket.getStatus());
        response.setPassengerTripStatus(ticket.getPassenger_trip_status());
        response.setCreatedAt(ticket.getCreated_at());
        return response;
    }

    private String generateBookingCode(UUID companyId) {
        String code;
        do {
            code = generateCode("BK");
        } while (bookingRepository.countByCompanyIdAndBookingCode(companyId, code) > 0);
        return code;
    }

    private String generateTicketCode(UUID companyId) {
        String code;
        do {
            code = generateCode("TK");
        } while (ticketRepository.countByCompanyIdAndTicketCode(companyId, code) > 0);
        return code;
    }

    private String generateCode(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private BaseResponse<BookingResponse> success(BookingResponse result) {
        BaseResponse<BookingResponse> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(result);
        return response;
    }

    private BaseResponse<List<TripSeatResponse>> success(List<TripSeatResponse> results) {
        BaseResponse<List<TripSeatResponse>> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(results);
        return response;
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
