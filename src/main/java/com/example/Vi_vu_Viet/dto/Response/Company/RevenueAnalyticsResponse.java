package com.example.Vi_vu_Viet.dto.Response.Company;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RevenueAnalyticsResponse(long collectedRevenue, long previousCollectedRevenue,
        BigDecimal collectedRevenueChangePct, long bookedTickets, long collectedTickets,
        BigDecimal collectionRate, long cancelledTickets, long noShowTickets,
        long completedTrips, long departedTrips, Long driverWages, long missingDriverWages,
        Long estimatedProfit, List<Daily> days, List<Route> routes, Reconciliation reconciliation) {
    public record Daily(LocalDate date, long collectedRevenue) { }
    public record Route(UUID routeId, String routeName, long collectedRevenue) { }
    public record TripRow(UUID tripId, String tripCode, UUID routeId, String routeName,
                          LocalDateTime departureAt, long bookedTickets, long collectedTickets,
                          long expectedRevenue, long collectedRevenue, Long driverWage, long difference) { }
    public record Totals(long bookedTickets, long collectedTickets, long collectedRevenue, Long driverWages) { }
    public record Reconciliation(List<TripRow> trips, long mismatchedTrips, Totals totals) { }
}
