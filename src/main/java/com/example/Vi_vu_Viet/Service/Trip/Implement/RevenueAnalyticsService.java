package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Trip.Ticket;
import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Repository.Trip.DashboardRepository;
import com.example.Vi_vu_Viet.Util.Enums.PassengerTripStatus;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.dto.Response.Company.RevenueAnalyticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevenueAnalyticsService {
    private final DashboardRepository repository;

    @Transactional(readOnly = true)
    public RevenueAnalyticsResponse calculate(UUID companyId, LocalDate from, LocalDate to, UUID routeId, int limit) {
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        LocalDate previousFrom = from.minusDays(days);
        List<Trip> trips = repository.findReportTrips(companyId, from.atStartOfDay(), to.plusDays(1).atStartOfDay(), routeId);
        List<Ticket> tickets = repository.findReportTickets(companyId, from.atStartOfDay(), to.plusDays(1).atStartOfDay(), routeId);
        List<Ticket> previous = repository.findReportTickets(companyId, previousFrom.atStartOfDay(), from.atStartOfDay(), routeId);
        Map<UUID, List<Ticket>> byTrip = tickets.stream().collect(Collectors.groupingBy(t -> t.getTrip_id().getId()));
        long revenue = collected(tickets);
        long previousRevenue = collected(previous);
        long booked = tickets.stream().filter(this::booked).count();
        long paid = tickets.stream().filter(t -> t.getCollected_at() != null).count();
        long missing = trips.stream().filter(t -> t.getDriver_wage() == null).count();
        Long wages = missing == 0 ? trips.stream().mapToLong(Trip::getDriver_wage).sum() : null;
        Map<LocalDate, Long> daily = new LinkedHashMap<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) daily.put(date, 0L);
        for (Ticket ticket : tickets) {
            if (ticket.getCollected_at() != null) daily.merge(ticket.getTrip_id().getDeparture_at().toLocalDate(), ticket.getCollected_amount(), Long::sum);
        }
        List<RevenueAnalyticsResponse.Daily> dailyRows = daily.entrySet().stream()
                .map(e -> new RevenueAnalyticsResponse.Daily(e.getKey(), e.getValue())).toList();
        Map<UUID, RevenueAnalyticsResponse.Route> routes = new LinkedHashMap<>();
        for (Trip trip : trips) {
            var route = trip.getRoute_id();
            long amount = collected(byTrip.getOrDefault(trip.getId(), List.of()));
            var old = routes.get(route.getId());
            routes.put(route.getId(), new RevenueAnalyticsResponse.Route(route.getId(), route.getName(), amount + (old == null ? 0 : old.collectedRevenue())));
        }
        var routeRows = routes.values().stream().sorted((a, b) -> Long.compare(b.collectedRevenue(), a.collectedRevenue())).toList();
        List<RevenueAnalyticsResponse.TripRow> rows = new ArrayList<>();
        for (Trip trip : trips.stream().limit(limit).toList()) {
            List<Ticket> list = byTrip.getOrDefault(trip.getId(), List.of());
            long actual = collected(list);
            long expected = list.stream().filter(t -> t.getCollected_at() != null).mapToLong(Ticket::getFare_amount).sum();
            rows.add(new RevenueAnalyticsResponse.TripRow(trip.getId(), trip.getTrip_code(), trip.getRoute_id().getId(),
                    trip.getRoute_id().getName(), trip.getDeparture_at(), list.stream().filter(this::booked).count(),
                    list.stream().filter(t -> t.getCollected_at() != null).count(), expected, actual, trip.getDriver_wage(), actual - expected));
        }
        Long rowWages = rows.stream().anyMatch(r -> r.driverWage() == null) ? null : rows.stream().mapToLong(RevenueAnalyticsResponse.TripRow::driverWage).sum();
        var totals = new RevenueAnalyticsResponse.Totals(rows.stream().mapToLong(RevenueAnalyticsResponse.TripRow::bookedTickets).sum(),
                rows.stream().mapToLong(RevenueAnalyticsResponse.TripRow::collectedTickets).sum(),
                rows.stream().mapToLong(RevenueAnalyticsResponse.TripRow::collectedRevenue).sum(), rowWages);
        var reconciliation = new RevenueAnalyticsResponse.Reconciliation(rows, rows.stream().filter(r -> r.difference() != 0).count(), totals);
        BigDecimal change = previousRevenue == 0 ? (revenue == 0 ? BigDecimal.ZERO : null)
                : BigDecimal.valueOf(revenue - previousRevenue).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(previousRevenue), 2, RoundingMode.HALF_UP);
        BigDecimal rate = booked == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(paid).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(booked), 2, RoundingMode.HALF_UP);
        return new RevenueAnalyticsResponse(revenue, previousRevenue, change, booked, paid, rate,
                tickets.stream().filter(t -> t.getStatus() == TicketStatus.CANCELLED).count(),
                tickets.stream().filter(t -> t.getPassenger_trip_status() == PassengerTripStatus.NO_SHOW).count(),
                trips.stream().filter(t -> t.getStatus() == TripStatus.COMPLETED).count(),
                trips.stream().filter(t -> t.getActual_departure_at() != null).count(), wages, missing,
                wages == null ? null : revenue - wages, dailyRows, routeRows, reconciliation);
    }

    private boolean booked(Ticket ticket) {
        return ticket.getBooking_id().getConfirmed_at() != null || ticket.getStatus() == TicketStatus.CONFIRMED;
    }
    private long collected(List<Ticket> tickets) {
        return tickets.stream().filter(t -> t.getCollected_at() != null).mapToLong(t -> t.getCollected_amount() == null ? 0L : t.getCollected_amount()).sum();
    }
}
