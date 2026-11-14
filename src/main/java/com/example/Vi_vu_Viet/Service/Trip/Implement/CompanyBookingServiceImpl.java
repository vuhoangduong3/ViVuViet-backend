package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Trip.Booking;
import com.example.Vi_vu_Viet.Entity.Trip.BusCompany;
import com.example.Vi_vu_Viet.Entity.Trip.Ticket;
import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Repository.Trip.BookingRepository;
import com.example.Vi_vu_Viet.Repository.Trip.CompanyBookingSpecifications;
import com.example.Vi_vu_Viet.Repository.Trip.BusCompanyRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TicketRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripRepository;
import com.example.Vi_vu_Viet.Service.Trip.CompanyBookingService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.Util.Enums.BookingViewStatus;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.dto.Request.Booking.CancelCompanyBookingRequest;
import com.example.Vi_vu_Viet.dto.Request.Booking.CompanyBookingFilter;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.CompanyBookingResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.CompanyBookingSummaryResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.CompanyBookingStatisticsResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.CompanyBookingStatusCountsResponse;
import com.example.Vi_vu_Viet.dto.Response.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.DateTimeException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompanyBookingServiceImpl implements CompanyBookingService {

    BusCompanyRepository busCompanyRepository;
    BookingRepository bookingRepository;
    TicketRepository ticketRepository;
    TripRepository tripRepository;
    CompanyBookingResponseMapper responseMapper;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<CompanyBookingStatisticsResponse> getCompanyBookingStatistics(UUID companyId) {
        BusCompany company = requireActiveCompany(companyId);
        ZoneId zone;
        try {
            zone = ZoneId.of(StringUtils.hasText(company.getTimezone())
                    ? company.getTimezone() : "Asia/Ho_Chi_Minh");
        } catch (DateTimeException exception) {
            throw badRequest("Company timezone is invalid");
        }
        LocalDate today = LocalDate.now(zone);
        long todayBookings = bookingRepository.countDepartingBookings(companyId,
                today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        var totals = ticketRepository.getConfirmedTotals(companyId);
        return success(new CompanyBookingStatisticsResponse(today, zone.getId(), todayBookings,
                totals.getTicketCount(), totals.getTotalAmount(), "VND"));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<CompanyBookingStatusCountsResponse> getCompanyBookingStatusCounts(
            UUID companyId, CompanyBookingFilter filter) {
        requireActiveCompany(companyId);
        validateFilter(filter);
        LocalDateTime now = LocalDateTime.now();
        var base = CompanyBookingSpecifications.matching(companyId, filter, now, false);
        var counts = new EnumMap<BookingViewStatus, Long>(BookingViewStatus.class);
        for (BookingViewStatus view : BookingViewStatus.values()) {
            counts.put(view, bookingRepository.count(base.and(CompanyBookingSpecifications.withView(view, now))));
        }
        long all = counts.values().stream().mapToLong(Long::longValue).sum();
        return success(new CompanyBookingStatusCountsResponse(all, counts.get(BookingViewStatus.BOOKED),
                counts.get(BookingViewStatus.PICKED_UP), counts.get(BookingViewStatus.COMPLETED),
                counts.get(BookingViewStatus.NO_SHOW), counts.get(BookingViewStatus.CANCELLED),
                counts.get(BookingViewStatus.HOLDING), counts.get(BookingViewStatus.EXPIRED)));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<CompanyBookingSummaryResponse>> getCompanyBookings(
            UUID companyId, CompanyBookingFilter filter) {
        requireActiveCompany(companyId);
        validateFilter(filter);
        LocalDateTime now = LocalDateTime.now();
        var bookings = bookingRepository.findAll(
                CompanyBookingSpecifications.matching(companyId, filter, now, true),
                PageRequest.of(filter.getPage(), filter.getSize(),
                        JpaSort.unsafe(Sort.Direction.DESC, "created_at", "id")));
        List<UUID> bookingIds = bookings.getContent().stream().map(Booking::getId).toList();
        Map<UUID, List<Ticket>> ticketsByBooking = bookingIds.isEmpty() ? Map.of()
                : ticketRepository.findAllByBookingIds(bookingIds).stream()
                    .collect(Collectors.groupingBy(ticket -> ticket.getBooking_id().getId()));
        var summaries = bookings.map(booking -> responseMapper.toSummary(
                booking, ticketsByBooking.getOrDefault(booking.getId(), List.of()), now));
        return success(PageResponse.from(summaries));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<CompanyBookingResponse> getCompanyBooking(UUID companyId, UUID bookingId) {
        requireActiveCompany(companyId);
        Booking booking = bookingRepository.findByIdAndCompanyId(companyId, bookingId)
                .orElseThrow(this::bookingNotFound);
        return success(responseMapper.toDetail(booking, ticketRepository.findAllByBookingId(bookingId),
                LocalDateTime.now()));
    }

    @Override
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public BaseResponse<CompanyBookingResponse> cancelCompanyBooking(
            UUID companyId, UUID bookingId, CancelCompanyBookingRequest request, UserAccount actor) {
        requireActiveCompany(companyId);
        if (actor == null || actor.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        if (request == null || !StringUtils.hasText(request.getReason()) || request.getReason().length() > 500) {
            throw badRequest("Cancellation reason is required and must not exceed 500 characters");
        }

        // Khóa chuyến trước đơn, cùng thứ tự với tạo đơn, hủy chuyến và cập nhật hành trình.
        UUID tripId = bookingRepository.findTripIdByIdAndCompanyId(companyId, bookingId)
                .orElseThrow(this::bookingNotFound);
        Trip trip = tripRepository.findByIdAndCompanyIdForUpdate(companyId, tripId)
                .orElseThrow(this::bookingNotFound);
        Booking booking = bookingRepository.findByIdAndCompanyIdForUpdate(companyId, bookingId)
                .orElseThrow(this::bookingNotFound);
        List<Ticket> tickets = ticketRepository.findAllByBookingId(bookingId);
        LocalDateTime now = LocalDateTime.now();
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return success(responseMapper.toDetail(booking, tickets, now));
        }
        if (booking.getStatus() == BookingStatus.HOLDING && !booking.getHold_expires_at().isAfter(now)) {
            booking.setStatus(BookingStatus.EXPIRED);
            tickets.stream().filter(ticket -> ticket.getStatus() == TicketStatus.HOLDING)
                    .forEach(ticket -> ticket.setStatus(TicketStatus.EXPIRED));
            ticketRepository.saveAll(tickets);
            bookingRepository.saveAndFlush(booking);
        }
        if (booking.getStatus() != BookingStatus.HOLDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw conflict("Only unexpired holding or confirmed bookings can be cancelled");
        }
        if ((trip.getStatus() != TripStatus.DRAFT && trip.getStatus() != TripStatus.SCHEDULED)
                || trip.getActual_departure_at() != null) {
            throw conflict("Bookings can only be cancelled before the trip starts");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelled_at(now);
        booking.setCancellation_reason(request.getReason().trim());
        booking.setCancelled_by(actor);
        tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.HOLDING
                        || ticket.getStatus() == TicketStatus.CONFIRMED)
                .forEach(ticket -> ticket.setStatus(TicketStatus.CANCELLED));
        ticketRepository.saveAll(tickets);
        bookingRepository.saveAndFlush(booking);
        return success(responseMapper.toDetail(booking, tickets, now));
    }

    private void validateFilter(CompanyBookingFilter filter) {
        if (filter.getPage() < 0) throw badRequest("page must be greater than or equal to 0");
        if (filter.getSize() < 1 || filter.getSize() > 100) throw badRequest("size must be between 1 and 100");
        if ((long) filter.getPage() * filter.getSize() > Integer.MAX_VALUE) {
            throw badRequest("Requested page offset is too large");
        }
        if (filter.getQ() != null && filter.getQ().length() > 200) {
            throw badRequest("q must not exceed 200 characters");
        }
        if (filter.getCreatedFrom() != null && filter.getCreatedTo() != null
                && filter.getCreatedFrom().isAfter(filter.getCreatedTo())) {
            throw badRequest("createdFrom must not be after createdTo");
        }
        if (filter.getDepartureFrom() != null && filter.getDepartureTo() != null
                && filter.getDepartureFrom().isAfter(filter.getDepartureTo())) {
            throw badRequest("departureFrom must not be after departureTo");
        }
    }

    private BusCompany requireActiveCompany(UUID companyId) {
        return busCompanyRepository.findByIdAndStatus(companyId, BusCompanyStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Active bus company was not found"));
    }

    private <T> BaseResponse<T> success(T result) {
        BaseResponse<T> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(result);
        return response;
    }

    private ResponseStatusException bookingNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking was not found for this company");
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
