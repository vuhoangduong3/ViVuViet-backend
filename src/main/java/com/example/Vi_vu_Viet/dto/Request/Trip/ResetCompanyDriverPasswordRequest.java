package com.example.Vi_vu_Viet.dto.Request.Trip;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetCompanyDriverPasswordRequest {
    @Size(max = 500)
    String note;
}
