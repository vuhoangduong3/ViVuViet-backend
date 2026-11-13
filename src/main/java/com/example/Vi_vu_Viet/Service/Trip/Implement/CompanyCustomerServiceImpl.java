package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Trip.Booking;
import com.example.Vi_vu_Viet.Entity.Trip.BusCompany;
import com.example.Vi_vu_Viet.Entity.Trip.Ticket;
import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Repository.Auth.UserRepository;
import com.example.Vi_vu_Viet.Repository.Trip.BookingRepository;
import com.example.Vi_vu_Viet.Repository.Trip.BusCompanyRepository;
import com.example.Vi_vu_Viet.Repository.Trip.CompanyCustomerProjection;
import com.example.Vi_vu_Viet.Repository.Trip.TicketRepository;
import com.example.Vi_vu_Viet.Service.Trip.CompanyCustomerService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.BookingTripResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.TicketResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.CompanyCustomerBookingResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.CompanyCustomerDetailResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.CompanyCustomerResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CompanyCustomerServiceImpl implements CompanyCustomerService {

    BusCompanyRepository busCompanyRepository;
    BookingRepository bookingRepository;
    TicketRepository ticketRepository;
    UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<CompanyCustomerResponse>> getCustomers(UUID companyId, String q,
                                                                     UserAccountStatus accountStatus,
                                                                     BookingStatus bookingStatus) {
        getActiveCompany(companyId);
        if (q != null && q.length() > 200) throw badRequest("q must not exceed 200 characters");
        String pattern = StringUtils.hasText(q)
                ? "%" + q.trim().toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%"
                : null;
        List<CompanyCustomerResponse> customers = bookingRepository
                .searchCompanyCustomers(companyId, pattern, accountStatus, bookingStatus,
                        BookingStatus.HOLDING, BookingStatus.EXPIRED, LocalDateTime.now()).stream()
                .map(this::toCustomerResponse)
                .toList();
        return success(customers);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<CompanyCustomerDetailResponse> getCustomer(UUID companyId, UUID customerId) {
        getActiveCompany(companyId);
        if (!bookingRepository.existsByCompanyAndCustomer(companyId, customerId)) {
            throw notFound("Customer was not found for this company");
        }
        UserAccount customer = userRepository.findById(customerId)
                .orElseThrow(() -> notFound("Customer was not found for this company"));
        List<Booking> bookings = bookingRepository.findAllForCompanyCustomer(companyId, customerId);
        List<UUID> bookingIds = bookings.stream().map(Booking::getId).toList();
        Map<UUID, List<Ticket>> ticketsByBooking = bookingIds.isEmpty() ? Map.of()
                : ticketRepository.findAllByBookingIds(bookingIds).stream()
                    .collect(Collectors.groupingBy(ticket -> ticket.getBooking_id().getId()));
        LocalDateTime now = LocalDateTime.now();

        CompanyCustomerDetailResponse response = new CompanyCustomerDetailResponse();
        response.setId(customer.getId());
        response.setFullName(customer.getFull_name());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setAccountStatus(customer.getAccount_status());
        response.setPhoneVerifiedAt(customer.getPhone_verified_at());
        response.setCreatedAt(customer.getCreated_at());
        response.setUpdatedAt(customer.getUpdated_at());
        response.setBookings(bookings.stream().map(booking -> toBookingResponse(booking,
                ticketsByBooking.getOrDefault(booking.getId(), List.of()), now)).toList());
        BaseResponse<CompanyCustomerDetailResponse> result = new BaseResponse<>(ApplicationCode.SUCCESS);
        result.setWsResponse(response);
        return result;
    }

    private CompanyCustomerResponse toCustomerResponse(CompanyCustomerProjection projection) {
        CompanyCustomerResponse response = new CompanyCustomerResponse();
        response.setId(projection.getId());
        response.setFullName(projection.getFullName());
        response.setEmail(projection.getEmail());
        response.setPhone(projection.getPhone());
        response.setAccountStatus(projection.getAccountStatus());
        response.setBookingCount(projection.getBookingCount());
        response.setLastBookingAt(projection.getLastBookingAt());
        return response;
    }

    private CompanyCustomerBookingResponse toBookingResponse(Booking booking, List<Ticket> tickets,
                                                               LocalDateTime now) {
        boolean expiredHold = booking.getStatus() == BookingStatus.HOLDING
                && !booking.getHold_expires_at().isAfter(now);
        CompanyCustomerBookingResponse response = new CompanyCustomerBookingResponse();
        response.setId(booking.getId());
        response.setBookingCode(booking.getBooking_code());
        response.setStatus(expiredHold ? BookingStatus.EXPIRED : booking.getStatus());
        response.setContactName(booking.getContact_name());
        response.setContactPhone(booking.getContact_phone());
        response.setContactEmail(booking.getContact_email());
        response.setQuotedTotalAmount(booking.getQuoted_total_amount());
        response.setCurrency(booking.getCurrency());
        response.setHoldExpiresAt(booking.getHold_expires_at());
        response.setConfirmedAt(booking.getConfirmed_at());
        response.setCancelledAt(booking.getCancelled_at());
        response.setCreatedAt(booking.getCreated_at());
        response.setTrip(toTripResponse(booking));
        response.setTickets(tickets.stream().map(ticket -> toTicketResponse(ticket, expiredHold)).toList());
        return response;
    }

    private BookingTripResponse toTripResponse(Booking booking) {
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

    private BusCompany getActiveCompany(UUID companyId) {
        return busCompanyRepository.findByIdAndStatus(companyId, BusCompanyStatus.ACTIVE)
                .orElseThrow(() -> notFound("Bus company was not found or is inactive"));
    }

    private BaseResponse<List<CompanyCustomerResponse>> success(List<CompanyCustomerResponse> customers) {
        BaseResponse<List<CompanyCustomerResponse>> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(customers);
        return response;
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
