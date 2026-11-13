package com.example.Vi_vu_Viet.dto.Request.Trip;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class VehicleDocumentRequest {
    @Size(max = 120) private String documentNumber;
    @Size(max = 120) private String issuer;
    @Size(max = 255) private String description;
    private LocalDate issuedOn;
    private LocalDate expiresOn;
}
