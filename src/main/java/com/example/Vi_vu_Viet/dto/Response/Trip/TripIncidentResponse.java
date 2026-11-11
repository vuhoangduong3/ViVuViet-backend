package com.example.Vi_vu_Viet.dto.Response.Trip;

import com.example.Vi_vu_Viet.Util.Enums.IncidentStatus;
import com.example.Vi_vu_Viet.Util.Enums.IncidentType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripIncidentResponse {
    UUID id;
    UUID tripId;
    IncidentType type;
    String description;
    IncidentStatus status;
    LocalDateTime openedAt;
}
