package com.example.Vi_vu_Viet.dto.Request.Trip;

import com.example.Vi_vu_Viet.Util.Enums.IncidentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportTripIncidentRequest {
    @NotNull
    IncidentType type;

    @NotBlank
    @Size(max = 2000)
    String description;

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}
