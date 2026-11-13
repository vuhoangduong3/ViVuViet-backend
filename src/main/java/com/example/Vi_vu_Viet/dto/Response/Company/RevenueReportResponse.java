package com.example.Vi_vu_Viet.dto.Response.Company;

import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenueReportResponse {
    LocalDate from;
    LocalDate to;
    TicketStatus status;
    Long tickets;
    Long ticketValue;
    LocalDate previousFrom;
    LocalDate previousTo;
    Long previousRevenue;
    Long previousTickets;
    BigDecimal revenueChangePct;
    BigDecimal ticketsChangePct;
    String currency;
    Boolean actualCollected;
    String note;
    List<RevenuePeriodResponse> periods;
    RevenueAnalyticsResponse analytics;
}
