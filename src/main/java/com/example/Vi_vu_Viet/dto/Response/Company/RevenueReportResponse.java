package com.example.Vi_vu_Viet.dto.Response.Company;

import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenueReportResponse {
    LocalDate from;
    LocalDate to;
    TicketStatus status;
    Long tickets;
    Long ticketValue;
    String currency;
    Boolean actualCollected;
    String note;
    List<RevenuePeriodResponse> periods;
}
