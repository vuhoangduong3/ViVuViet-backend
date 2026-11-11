package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Repository.Trip.DashboardRepository;
import com.example.Vi_vu_Viet.Service.Trip.DashboardService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardDriverResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardSummaryResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.DashboardTripPointResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.RevenuePeriodResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.RevenueReportResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    DashboardRepository dashboardRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<DashboardSummaryResponse> getSummary(UUID companyId, LocalDate from, LocalDate to) {
        validateRange(from, to);
        LocalDateTime fromAt = fromAt(from);
        LocalDateTime toAt = toAt(to);
        Object[] tripTotals = dashboardRepository.getTripSummary(companyId, fromAt, toAt).get(0);
        Object[] ticketTotals = dashboardRepository.getTicketValueSummary(
                companyId, fromAt, toAt, TicketStatus.CONFIRMED).get(0);

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setFrom(from);
        response.setTo(to);
        response.setTotalTrips(number(tripTotals, 0));
        response.setScheduledTrips(number(tripTotals, 1));
        response.setInProgressTrips(number(tripTotals, 2));
        response.setCompletedTrips(number(tripTotals, 3));
        response.setCancelledTrips(number(tripTotals, 4));
        response.setConfirmedTickets(number(ticketTotals, 0));
        response.setConfirmedTicketValue(number(ticketTotals, 1));
        response.setCurrency("VND");
        response.setActualCollected(false);
        response.setTicketValueNote("Giá trị vé đã xác nhận; hệ thống chưa có dữ liệu thanh toán hoặc tiền thực thu.");
        return success(response);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<DashboardTripPointResponse>> getTripPoints(UUID companyId,
                                                                        LocalDate from, LocalDate to) {
        validateRange(from, to);
        List<Object[]> rows = dashboardRepository.getTripsByDepartureDate(companyId, fromAt(from), toAt(to));
        Map<LocalDate, DashboardTripPointResponse> points = new LinkedHashMap<>();
        for (Object[] row : rows) {
            LocalDate date = toLocalDate(row, 0);
            TripStatus status = (TripStatus) row[3];
            long count = number(row[4]);
            DashboardTripPointResponse point = points.computeIfAbsent(date, key -> {
                DashboardTripPointResponse created = new DashboardTripPointResponse();
                created.setDate(key);
                created.setTotalTrips(0L);
                created.setByStatus(new EnumMap<>(TripStatus.class));
                return created;
            });
            point.getByStatus().put(status, count);
            point.setTotalTrips(point.getTotalTrips() + count);
        }
        return success(new ArrayList<>(points.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<DashboardDriverResponse>> getTopDrivers(UUID companyId,
                                                                      LocalDate from, LocalDate to) {
        validateRange(from, to);
        List<DashboardDriverResponse> drivers = dashboardRepository.getTopDrivers(companyId, fromAt(from), toAt(to))
                .stream().limit(10).map(this::toDriverResponse).toList();
        return success(drivers);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<RevenueReportResponse> getRevenue(UUID companyId, LocalDate from, LocalDate to,
                                                           TicketStatus status) {
        validateRange(from, to);
        TicketStatus selectedStatus = status == null ? TicketStatus.CONFIRMED : status;
        LocalDateTime fromAt = fromAt(from);
        LocalDateTime toAt = toAt(to);
        Object[] totals = dashboardRepository.getRevenueSummary(companyId, fromAt, toAt, selectedStatus).get(0);
        List<RevenuePeriodResponse> periods = dashboardRepository.getRevenueByDepartureDate(
                        companyId, fromAt, toAt, selectedStatus).stream()
                .map(row -> toRevenuePeriod(row)).toList();

        RevenueReportResponse response = new RevenueReportResponse();
        response.setFrom(from);
        response.setTo(to);
        response.setStatus(selectedStatus);
        response.setTickets(number(totals, 0));
        response.setTicketValue(number(totals, 1));
        response.setCurrency("VND");
        response.setActualCollected(false);
        response.setNote("Báo cáo giá trị vé theo trạng thái và ngày khởi hành; không thể hiện tiền thực thu vì chưa tích hợp thanh toán.");
        response.setPeriods(periods);
        return success(response);
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

    private LocalDateTime fromAt(LocalDate from) {
        return from == null ? LocalDate.of(1, 1, 1).atStartOfDay() : from.atStartOfDay();
    }

    private LocalDateTime toAt(LocalDate to) {
        return to == null ? LocalDate.of(9999, 12, 31).plusDays(1).atStartOfDay()
                : to.plusDays(1).atStartOfDay();
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be on or before to");
        }
    }

    private <T> BaseResponse<T> success(T value) {
        BaseResponse<T> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(value);
        return response;
    }
}
