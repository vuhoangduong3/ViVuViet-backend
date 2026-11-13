package com.example.Vi_vu_Viet.dto.Response.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.VehicleDocument;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record VehicleDocumentResponse(UUID id, VehicleDocument.DocumentType type, String documentNumber,
        String issuer, String description, LocalDate issuedOn, LocalDate expiresOn,
        String status, LocalDateTime updatedAt) {
    public static VehicleDocumentResponse from(VehicleDocument document) {
        LocalDate expiry = document.getDocumentType() == VehicleDocument.DocumentType.INSPECTION
                ? document.getVehicle().getInspection_expires_on() : document.getExpiresOn();
        String status = document.getIssuedOn() != null && document.getIssuedOn().isAfter(LocalDate.now())
                ? "NOT_YET_VALID" : expiry == null ? "UNKNOWN" : expiry.isBefore(LocalDate.now()) ? "EXPIRED" : "VALID";
        return new VehicleDocumentResponse(document.getId(), document.getDocumentType(), document.getDocumentNumber(),
                document.getIssuer(), document.getDescription(), document.getIssuedOn(), expiry, status, document.getUpdated_at());
    }
}
