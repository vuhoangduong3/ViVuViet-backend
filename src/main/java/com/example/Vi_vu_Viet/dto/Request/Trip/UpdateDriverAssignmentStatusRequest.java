package com.example.Vi_vu_Viet.dto.Request.Trip;

import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDriverAssignmentStatusRequest {
    @NotNull
    private DriverAssignmentStatus status;
}
