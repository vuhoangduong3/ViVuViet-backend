package com.example.Vi_vu_Viet.dto.Response.Company;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenuePeriodResponse {
    LocalDate date;
    Long tickets;
    Long ticketValue;
}
