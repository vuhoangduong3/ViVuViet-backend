package com.example.Vi_vu_Viet.dto.Response.Company;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

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
    Long confirmedTickets;
    Long confirmedTicketValue;
    String currency;
    Boolean actualCollected;
    String ticketValueNote;
}
