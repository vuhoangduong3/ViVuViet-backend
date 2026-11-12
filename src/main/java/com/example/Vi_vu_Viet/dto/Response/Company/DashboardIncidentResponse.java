package com.example.Vi_vu_Viet.dto.Response.Company;

import com.example.Vi_vu_Viet.Util.Enums.IncidentStatus;
import com.example.Vi_vu_Viet.Util.Enums.IncidentType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardIncidentResponse {
    UUID incidentId;
    String incidentCode;
    UUID tripId;
    String tripCode;
    IncidentType type;
    String typeLabel;
    String description;
    UUID driverId;
    String driverName;
    OffsetDateTime reportedAt;
    Long ageMinutes;
    IncidentStatus status;
}
