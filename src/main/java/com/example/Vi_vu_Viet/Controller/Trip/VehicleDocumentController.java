package com.example.Vi_vu_Viet.Controller.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Trip.Vehicle;
import com.example.Vi_vu_Viet.Entity.Trip.VehicleDocument;
import com.example.Vi_vu_Viet.Entity.Trip.VehicleDocument.DocumentType;
import com.example.Vi_vu_Viet.Repository.Trip.VehicleDocumentRepository;
import com.example.Vi_vu_Viet.Repository.Trip.VehicleRepository;
import com.example.Vi_vu_Viet.Service.Trip.DriverService;
import com.example.Vi_vu_Viet.Service.Trip.VehicleService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.dto.Request.BaseRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.VehicleDocumentRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.VehicleDocumentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class VehicleDocumentController {
    private final VehicleDocumentRepository documents;
    private final VehicleRepository vehicles;
    private final DriverService drivers;
    private final VehicleService vehicleService;

    @GetMapping("/api/v1/me/driver/vehicle/documents")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public BaseResponse<List<VehicleDocumentResponse>> driverDocuments(@AuthenticationPrincipal UserAccount driver) {
        UUID vehicleId = drivers.getAssignedVehicle(driver.getId()).getWsResponse().getId();
        return success(documents.findByVehicleId(vehicleId).stream().map(VehicleDocumentResponse::from).toList());
    }

    @GetMapping("/api/v1/bus-companies/{companyId}/vehicles/{vehicleId}/documents")
    @PreAuthorize("@companyAuthorization.hasCompanyRole(authentication, #p0, 'COMPANY_ADMIN')")
    @Transactional(readOnly = true)
    public BaseResponse<List<VehicleDocumentResponse>> companyDocuments(@PathVariable UUID companyId, @PathVariable UUID vehicleId) {
        vehicleService.getVehicle(companyId, vehicleId);
        return success(documents.findByVehicleId(vehicleId).stream().map(VehicleDocumentResponse::from).toList());
    }

    @PutMapping("/api/v1/bus-companies/{companyId}/vehicles/{vehicleId}/documents/{type}")
    @PreAuthorize("@companyAuthorization.hasCompanyRole(authentication, #p0, 'COMPANY_ADMIN')")
    @Transactional
    public BaseResponse<VehicleDocumentResponse> putDocument(@PathVariable UUID companyId, @PathVariable UUID vehicleId,
            @PathVariable DocumentType type, @Valid @RequestBody BaseRequest<VehicleDocumentRequest> request) {
        vehicleService.getVehicle(companyId, vehicleId);
        Vehicle vehicle = vehicles.findByCompanyForUpdate(vehicleId, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle was not found"));
        VehicleDocumentRequest data = request.getWsRequest();
        if (data.getIssuedOn() != null && data.getExpiresOn() != null && data.getExpiresOn().isBefore(data.getIssuedOn()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry must be on or after issue date");
        VehicleDocument doc = documents.findByVehicleIdAndDocumentType(vehicleId, type).orElseGet(VehicleDocument::new);
        doc.setVehicle(vehicle);
        doc.setDocumentType(type);
        doc.setDocumentNumber(data.getDocumentNumber());
        doc.setIssuer(data.getIssuer());
        doc.setDescription(data.getDescription());
        doc.setIssuedOn(data.getIssuedOn());
        doc.setExpiresOn(data.getExpiresOn());
        if (type == DocumentType.INSPECTION) vehicle.setInspection_expires_on(data.getExpiresOn());
        return success(VehicleDocumentResponse.from(documents.saveAndFlush(doc)));
    }

    private <T> BaseResponse<T> success(T value) {
        BaseResponse<T> result = new BaseResponse<>(ApplicationCode.SUCCESS);
        result.setWsResponse(value);
        return result;
    }
}
