package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Repository.Trip.DashboardRepository;
import com.example.Vi_vu_Viet.Repository.Trip.BusCompanyRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripIncidentRepository;
import com.example.Vi_vu_Viet.Entity.Trip.BusCompany;
import com.example.Vi_vu_Viet.Service.Trip.DashboardService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import com.example.Vi_vu_Viet.Util.Enums.IncidentStatus;
import com.example.Vi_vu_Viet.Util.Enums.IncidentType;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardDriverResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardSummaryResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardTripPointResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.RevenuePeriodResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.RevenueReportResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardTodayTripResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardRouteResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardIncidentResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    static final LocalDate MIN_DATE = LocalDate.of(1, 1, 1);
    static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);
    static final List<DriverAssignmentStatus> EXCLUDED_ASSIGNMENTS = List.of(
            DriverAssignmentStatus.REJECTED, DriverAssignmentStatus.REPLACED);

    DashboardRepository dashboardRepository;
    BusCompanyRepository busCompanyRepository;
    TripIncidentRepository tripIncidentRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<DashboardSummaryResponse> getSummary(UUID companyId, LocalDate from, LocalDate to) {
        DateRange range = dashboardRange(from, to, companyZone(companyId));
        DateRange previous = range.previous();
        LocalDateTime fromAt = range.fromAt();
        LocalDateTime toAt = range.toAt();
        Object[] tripTotals = dashboardRepository.getTripSummary(companyId, fromAt, toAt).get(0);
        Object[] ticketTotals = dashboardRepository.getTicketValueSummary(
                companyId, fromAt, toAt, TicketStatus.CONFIRMED).get(0);
        Object[] previousTrips = dashboardRepository.getTripSummary(
                companyId, previous.fromAt(), previous.toAt()).get(0);
        Object[] previousTickets = dashboardRepository.getTicketValueSummary(
                companyId, previous.fromAt(), previous.toAt(), TicketStatus.CONFIRMED).get(0);

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setFrom(range.from());
        response.setTo(range.to());
        response.setTotalTrips(number(tripTotals, 0));
        response.setScheduledTrips(number(tripTotals, 1));
        response.setInProgressTrips(number(tripTotals, 2));
        response.setCompletedTrips(number(tripTotals, 3));
        response.setCancelledTrips(number(tripTotals, 4));
        response.setDraftTrips(number(tripTotals, 5));
        response.setTerminatedTrips(number(tripTotals, 6));
        response.setConfirmedTickets(number(ticketTotals, 0));
        response.setConfirmedTicketValue(number(ticketTotals, 1));
        response.setPreviousFrom(previous.from());
        response.setPreviousTo(previous.to());
        response.setPreviousTotalTrips(number(previousTrips, 0));
        response.setPreviousScheduledTrips(number(previousTrips, 1));
        response.setPreviousInProgressTrips(number(previousTrips, 2));
        response.setPreviousCompletedTrips(number(previousTrips, 3));
        response.setPreviousCancelledTrips(number(previousTrips, 4));
        response.setPreviousConfirmedTickets(number(previousTickets, 0));
        response.setPreviousRevenue(number(previousTickets, 1));
        response.setTotalTripsChangePct(changePct(number(tripTotals, 0), number(previousTrips, 0)));
        response.setScheduledTripsChangePct(changePct(number(tripTotals, 1), number(previousTrips, 1)));
        response.setInProgressTripsChangePct(changePct(number(tripTotals, 2), number(previousTrips, 2)));
        response.setCompletedTripsChangePct(changePct(number(tripTotals, 3), number(previousTrips, 3)));
        response.setCancelledTripsChangePct(changePct(number(tripTotals, 4), number(previousTrips, 4)));
        response.setConfirmedTicketsChangePct(changePct(number(ticketTotals, 0), number(previousTickets, 0)));
        response.setRevenueChangePct(changePct(number(ticketTotals, 1), number(previousTickets, 1)));
        response.setCurrency("VND");
        response.setActualCollected(false);
        response.setTicketValueNote("Giá trị vé đã xác nhận; hệ thống chưa có dữ liệu thanh toán hoặc tiền thực thu.");
        return success(response);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<DashboardTripPointResponse>> getTripPoints(UUID companyId,
                                                                        LocalDate from, LocalDate to) {
        DateRange range = dashboardRange(from, to, companyZone(companyId));
        validateDailyRange(range);
        List<Object[]> rows = dashboardRepository.getTripsByDepartureDate(companyId, range.fromAt(), range.toAt());
        Map<LocalDate, DashboardTripPointResponse> points = new LinkedHashMap<>();
        for (LocalDate date = range.from(); !date.isAfter(range.to()); date = date.plusDays(1)) {
            DashboardTripPointResponse point = new DashboardTripPointResponse();
            point.setDate(date);
            point.setTotalTrips(0L);
            point.setRevenue(0L);
            point.setTicketCount(0L);
            Map<TripStatus, Long> statuses = new EnumMap<>(TripStatus.class);
            for (TripStatus status : TripStatus.values()) statuses.put(status, 0L);
            point.setByStatus(statuses);
            points.put(date, point);
        }
        for (Object[] row : rows) {
            LocalDate date = toLocalDate(row, 0);
            TripStatus status = (TripStatus) row[3];
            long count = number(row[4]);
            DashboardTripPointResponse point = points.get(date);
            point.getByStatus().put(status, count);
            point.setTotalTrips(point.getTotalTrips() + count);
        }
        for (Object[] row : dashboardRepository.getRevenueByDepartureDate(
                companyId, range.fromAt(), range.toAt(), TicketStatus.CONFIRMED)) {
            DashboardTripPointResponse point = points.get(toLocalDate(row, 0));
            point.setTicketCount(number(row[3]));
            point.setRevenue(number(row[4]));
        }
        return success(new ArrayList<>(points.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<DashboardDriverResponse>> getTopDrivers(UUID companyId,
                                                                      LocalDate from, LocalDate to) {
        DateRange range = dashboardRange(from, to, companyZone(companyId));
        List<Object[]> rows = dashboardRepository.getTopDrivers(
                companyId, range.fromAt(), range.toAt(), PageRequest.of(0, 10));
        List<UUID> driverIds = rows.stream().map(row -> (UUID) row[0]).toList();
        Map<UUID, Long> revenues = new HashMap<>();
        if (!driverIds.isEmpty()) {
            for (Object[] row : dashboardRepository.getDriverRevenue(companyId, driverIds,
                    range.fromAt(), range.toAt(), TicketStatus.CONFIRMED, EXCLUDED_ASSIGNMENTS)) {
                revenues.put((UUID) row[0], number(row[1]));
            }
        }
        List<DashboardDriverResponse> drivers = rows.stream().map(row -> {
            DashboardDriverResponse response = toDriverResponse(row);
            response.setRevenue(revenues.getOrDefault(response.getDriverId(), 0L));
            response.setRating(null);
            return response;
        }).toList();
        return success(drivers);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<RevenueReportResponse> getRevenue(UUID companyId, LocalDate from, LocalDate to,
                                                           TicketStatus status) {
        DateRange range = dashboardRange(from, to, companyZone(companyId));
        validateDailyRange(range);
        DateRange previous = range.previous();
        TicketStatus selectedStatus = status == null ? TicketStatus.CONFIRMED : status;
        LocalDateTime fromAt = range.fromAt();
        LocalDateTime toAt = range.toAt();
        Object[] totals = dashboardRepository.getRevenueSummary(companyId, fromAt, toAt, selectedStatus).get(0);
        Object[] previousTotals = dashboardRepository.getRevenueSummary(
                companyId, previous.fromAt(), previous.toAt(), selectedStatus).get(0);
        Map<LocalDate, RevenuePeriodResponse> periods = new LinkedHashMap<>();
        for (LocalDate date = range.from(); !date.isAfter(range.to()); date = date.plusDays(1)) {
            RevenuePeriodResponse period = new RevenuePeriodResponse();
            period.setDate(date);
            period.setTickets(0L);
            period.setTicketValue(0L);
            periods.put(date, period);
        }
        for (Object[] row : dashboardRepository.getRevenueByDepartureDate(companyId, fromAt, toAt, selectedStatus)) {
            RevenuePeriodResponse period = toRevenuePeriod(row);
            periods.put(period.getDate(), period);
        }

        RevenueReportResponse response = new RevenueReportResponse();
        response.setFrom(range.from());
        response.setTo(range.to());
        response.setStatus(selectedStatus);
        response.setTickets(number(totals, 0));
        response.setTicketValue(number(totals, 1));
        response.setPreviousFrom(previous.from());
        response.setPreviousTo(previous.to());
        response.setPreviousRevenue(number(previousTotals, 1));
        response.setPreviousTickets(number(previousTotals, 0));
        response.setRevenueChangePct(changePct(number(totals, 1), number(previousTotals, 1)));
        response.setTicketsChangePct(changePct(number(totals, 0), number(previousTotals, 0)));
        response.setCurrency("VND");
        response.setActualCollected(false);
        response.setNote("Báo cáo giá trị vé theo trạng thái và ngày khởi hành; không thể hiện tiền thực thu vì chưa tích hợp thanh toán.");
        response.setPeriods(new ArrayList<>(periods.values()));
        return success(response);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<DashboardTodayTripResponse>> getTodayTrips(UUID companyId, int limit) {
        validateLimit(limit);
        ZoneId zone = companyZone(companyId);
        ZonedDateTime now = ZonedDateTime.now(zone);
        LocalDate today = now.toLocalDate();
        List<DashboardTodayTripResponse> trips = dashboardRepository.getTodayTrips(companyId,
                today.atStartOfDay(), today.plusDays(1).atStartOfDay(), EXCLUDED_ASSIGNMENTS,
                PageRequest.of(0, limit)).stream().map(row -> {
                    DashboardTodayTripResponse response = new DashboardTodayTripResponse();
                    response.setTripId((UUID) row[0]);
                    response.setTripCode((String) row[1]);
                    response.setRouteName((String) row[2]);
                    response.setOriginAreaName((String) row[3]);
                    response.setDestinationAreaName((String) row[4]);
                    response.setDepartureAt(offset((LocalDateTime) row[5], zone));
                    response.setActualDepartureAt(offset((LocalDateTime) row[6], zone));
                    response.setTripStatus((TripStatus) row[7]);
                    response.setVehicleId((UUID) row[8]);
                    response.setVehiclePlate((String) row[9]);
                    response.setDriverId((UUID) row[10]);
                    response.setDriverName((String) row[11]);
                    response.setAssignmentStatus((DriverAssignmentStatus) row[12]);
                    response.setLateMinutes(lateMinutes(response, now.toOffsetDateTime()));
                    return response;
                }).toList();
        return success(trips);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<DashboardRouteResponse>> getTopRoutes(UUID companyId, LocalDate from,
                                                                   LocalDate to, int limit) {
        validateLimit(limit);
        companyZone(companyId);
        // Top tuyến là ngoại lệ: thiếu cận ngày nghĩa là không giới hạn phía đó.
        DateRange range = new DateRange(from == null ? MIN_DATE : from, to == null ? MAX_DATE : to);
        validateRange(range);
        List<Object[]> rows = dashboardRepository.getTopRoutes(companyId, range.fromAt(), range.toAt(),
                TicketStatus.CONFIRMED, PageRequest.of(0, limit));
        List<UUID> routeIds = rows.stream().map(row -> (UUID) row[0]).toList();
        Map<UUID, Long> seatCounts = new HashMap<>();
        if (!routeIds.isEmpty()) {
            for (Object[] row : dashboardRepository.getRouteSeatCounts(
                    companyId, routeIds, range.fromAt(), range.toAt())) {
                seatCounts.put((UUID) row[0], number(row[1]));
            }
        }
        List<DashboardRouteResponse> routes = rows.stream().map(row -> {
            DashboardRouteResponse response = new DashboardRouteResponse();
            response.setRouteId((UUID) row[0]);
            response.setRouteCode((String) row[1]);
            response.setRouteName((String) row[2]);
            response.setTripCount(number(row[3]));
            response.setPassengerCount(number(row[4]));
            response.setRevenue(number(row[5]));
            long seats = seatCounts.getOrDefault(response.getRouteId(), 0L);
            response.setSeatCount(seats);
            response.setOccupancyRate(seats == 0 ? null : BigDecimal.valueOf(response.getPassengerCount())
                    .divide(BigDecimal.valueOf(seats), 4, RoundingMode.HALF_UP));
            return response;
        }).toList();
        return success(routes);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<DashboardIncidentResponse>> getOpenIncidents(UUID companyId, int limit) {
        validateLimit(limit);
        ZoneId zone = companyZone(companyId);
        OffsetDateTime now = ZonedDateTime.now(zone).toOffsetDateTime();
        List<DashboardIncidentResponse> incidents = tripIncidentRepository.findDashboardIncidents(companyId,
                        List.of(IncidentStatus.OPEN, IncidentStatus.IN_PROGRESS), PageRequest.of(0, limit))
                .stream().map(incident -> {
                    DashboardIncidentResponse response = new DashboardIncidentResponse();
                    response.setIncidentId(incident.getId());
                    // Mã ổn định từ UUID, không giả mã tuần tự hoặc thay đổi schema.
                    response.setIncidentCode("CN-" + incident.getId().toString()
                            .replace("-", "").toUpperCase(Locale.ROOT));
                    response.setTripId(incident.getTrip_id().getId());
                    response.setTripCode(incident.getTrip_id().getTrip_code());
                    response.setType(incident.getIncident_type());
                    response.setTypeLabel(incidentLabel(incident.getIncident_type()));
                    response.setDescription(incident.getDescription());
                    response.setDriverId(incident.getReported_by().getId());
                    response.setDriverName(incident.getReported_by().getFull_name());
                    response.setReportedAt(offset(incident.getOpened_at(), zone));
                    response.setAgeMinutes(Math.max(0L, Duration.between(response.getReportedAt(), now).toMinutes()));
                    response.setStatus(incident.getStatus());
                    return response;
                }).toList();
        return success(incidents);
    }

    private String incidentLabel(IncidentType type) {
        return switch (type) {
            case VEHICLE_BREAKDOWN -> "Hỏng xe";
            case ACCIDENT -> "Tai nạn";
            case TRAFFIC -> "Giao thông";
            case PASSENGER -> "Sự cố hành khách";
            case HEALTH -> "Sức khỏe";
            case OTHER -> "Khác";
        };
    }

    private long lateMinutes(DashboardTodayTripResponse trip, OffsetDateTime now) {
        if (trip.getTripStatus() == TripStatus.CANCELLED) return 0L;
        OffsetDateTime actual = trip.getActualDepartureAt();
        if (actual == null && trip.getTripStatus() != TripStatus.SCHEDULED) return 0L;
        return Math.max(0L, Duration.between(trip.getDepartureAt(), actual == null ? now : actual).toMinutes());
    }

    private OffsetDateTime offset(LocalDateTime time, ZoneId zone) {
        return time == null ? null : time.atZone(zone).toOffsetDateTime();
    }

    private DashboardDriverResponse toDriverResponse(Object[] row) {
        DashboardDriverResponse response = new DashboardDriverResponse();
        response.setDriverId((UUID) row[0]);
        response.setDriverName((String) row[1]);
        response.setAssignedTrips(number(row[2]));
        response.setCompletedTrips(number(row[3]));
        response.setInProgressTrips(number(row[4]));
        response.setScheduledTrips(number(row[5]));
        response.setCancelledTrips(number(row[6]));
        response.setTerminatedTrips(number(row[7]));
        return response;
    }

    private RevenuePeriodResponse toRevenuePeriod(Object[] row) {
        RevenuePeriodResponse response = new RevenuePeriodResponse();
        response.setDate(toLocalDate(row, 0));
        response.setTickets(number(row[3]));
        response.setTicketValue(number(row[4]));
        return response;
    }

    private LocalDate toLocalDate(Object[] row, int startIndex) {
        return LocalDate.of(number(row[startIndex]).intValue(),
                number(row[startIndex + 1]).intValue(), number(row[startIndex + 2]).intValue());
    }

    private Long number(Object[] values, int index) {
        return number(values[index]);
    }

    private Long number(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private ZoneId companyZone(UUID companyId) {
        BusCompany company = busCompanyRepository.findByIdAndStatus(companyId, BusCompanyStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active company was not found"));
        String timezone = company.getTimezone();
        try {
            return ZoneId.of(timezone == null || timezone.isBlank() ? "Asia/Ho_Chi_Minh" : timezone);
        } catch (DateTimeException exception) {
            throw badRequest("Company timezone is invalid");
        }
    }

    private DateRange dashboardRange(LocalDate from, LocalDate to, ZoneId zone) {
        validateRange(new DateRange(from == null ? MIN_DATE : from, to == null ? MAX_DATE : to));
        LocalDate end = to != null ? to : from != null ? from.plusDays(13) : LocalDate.now(zone);
        DateRange range = new DateRange(from == null ? end.minusDays(13) : from, end);
        validateRange(range);
        return range;
    }

    private void validateRange(DateRange range) {
        if (range.from().isAfter(range.to())) throw badRequest("from must be on or before to");
        if (range.from().isBefore(MIN_DATE) || range.to().isAfter(MAX_DATE)) {
            throw badRequest("Dates must be between 0001-01-01 and 9999-12-31");
        }
    }

    private void validateDailyRange(DateRange range) {
        if (ChronoUnit.DAYS.between(range.from(), range.to()) >= 3660) {
            throw badRequest("Daily reports support at most 3660 days per request");
        }
    }

    private void validateLimit(int limit) {
        if (limit < 1 || limit > 100) throw badRequest("limit must be between 1 and 100");
    }

    // 9.20 nghĩa là tăng 9,2%; mẫu số bằng 0 và kỳ này có dữ liệu thì trả null.
    private BigDecimal changePct(long current, long previous) {
        if (previous == 0) return current == 0 ? BigDecimal.ZERO : null;
        return BigDecimal.valueOf(current).subtract(BigDecimal.valueOf(previous))
                .multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(previous), 2, RoundingMode.HALF_UP);
    }

    private record DateRange(LocalDate from, LocalDate to) {
        LocalDateTime fromAt() { return from.atStartOfDay(); }
        LocalDateTime toAt() { return to.plusDays(1).atStartOfDay(); }

        DateRange previous() {
            long days = ChronoUnit.DAYS.between(from, to) + 1;
            LocalDate previousFrom = from.minusDays(days);
            if (previousFrom.isBefore(MIN_DATE)) throw badRequest("Date range is too early for period comparison");
            return new DateRange(previousFrom, from.minusDays(1));
        }
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private <T> BaseResponse<T> success(T value) {
        BaseResponse<T> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(value);
        return response;
    }
}
