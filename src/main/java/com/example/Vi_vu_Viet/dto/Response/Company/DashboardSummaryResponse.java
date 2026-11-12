package com.example.Vi_vu_Viet.dto.Response.Company;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardSummaryResponse {
    LocalDate from;
    LocalDate to;
    Long totalTrips;
    Long scheduledTrips;
    Long inProgressTrips;
    Long completedTrips;
    Long cancelledTrips;
    Long draftTrips;
    Long terminatedTrips;
    Long confirmedTickets;
    Long confirmedTicketValue;
    LocalDate previousFrom;
    LocalDate previousTo;
    Long previousTotalTrips;
    Long previousScheduledTrips;
    Long previousInProgressTrips;
    Long previousCompletedTrips;
    Long previousCancelledTrips;
    Long previousConfirmedTickets;
    Long previousRevenue;
    BigDecimal totalTripsChangePct;
    BigDecimal scheduledTripsChangePct;
    BigDecimal inProgressTripsChangePct;
    BigDecimal completedTripsChangePct;
    BigDecimal cancelledTripsChangePct;
    BigDecimal confirmedTicketsChangePct;
    BigDecimal revenueChangePct;
    String currency;
    Boolean actualCollected;
    String ticketValueNote;
}
