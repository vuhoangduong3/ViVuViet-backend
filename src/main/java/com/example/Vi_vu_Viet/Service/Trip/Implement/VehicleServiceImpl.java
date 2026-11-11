package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Trip.BusCompany;
import com.example.Vi_vu_Viet.Entity.Trip.Vehicle;
import com.example.Vi_vu_Viet.Repository.Trip.BusCompanyRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripAssignmentRepository;
import com.example.Vi_vu_Viet.Repository.Trip.VehicleRepository;
import com.example.Vi_vu_Viet.Service.Trip.VehicleService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.VehicleStatus;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateVehicleRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateVehicleRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.VehicleResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    BusCompanyRepository busCompanyRepository;
    VehicleRepository vehicleRepository;
    TripAssignmentRepository tripAssignmentRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<VehicleResponse>> getVehicles(UUID companyId, String q, VehicleStatus status) {
        getActiveCompany(companyId);
        if (q != null && q.length() > 100) throw badRequest("q must not exceed 100 characters");
        String pattern = StringUtils.hasText(q)
                ? "%" + q.trim().toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%"
                : null;
        List<VehicleResponse> vehicles = vehicleRepository.searchByCompany(companyId, status, pattern).stream()
                .map(this::toResponse)
                .toList();
        return success(vehicles);
    }

    @Override
    @Transactional
    public BaseResponse<VehicleResponse> createVehicle(UUID companyId, CreateVehicleRequest request) {
        BusCompany company = getActiveCompany(companyId);
        String plate = normalizePlate(request.getPlateNumber());
        if (vehicleRepository.existsByCompanyAndPlate(companyId, plate)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vehicle plate already exists for this company");
        }
        Vehicle vehicle = new Vehicle();
        vehicle.setCompany_id(company);
        vehicle.setPlate_normalized(plate);
        vehicle.setVehicle_model(request.getVehicleModel().trim());
        vehicle.setPassenger_capacity(request.getPassengerCapacity());
        vehicle.setStatus(VehicleStatus.ACTIVE);
        return success(toResponse(vehicleRepository.save(vehicle)));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<VehicleResponse> getVehicle(UUID companyId, UUID vehicleId) {
        getActiveCompany(companyId);
        return success(toResponse(getCompanyVehicle(companyId, vehicleId)));
    }

    @Override
    @Transactional
    public BaseResponse<VehicleResponse> updateVehicle(UUID companyId, UUID vehicleId,
                                                       UpdateVehicleRequest request) {
        getActiveCompany(companyId);
        Vehicle vehicle = getCompanyVehicle(companyId, vehicleId);
        if (!hasChanges(request)) throw badRequest("At least one vehicle field must be provided");

        if (request.getPlateNumber() != null) {
            String plate = normalizePlate(request.getPlateNumber());
            if (vehicleRepository.existsByCompanyAndPlateExcluding(companyId, plate, vehicleId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Vehicle plate already exists for this company");
            }
            vehicle.setPlate_normalized(plate);
        }
        if (request.getVehicleModel() != null) {
            if (!StringUtils.hasText(request.getVehicleModel())) throw badRequest("Vehicle model must not be blank");
            vehicle.setVehicle_model(request.getVehicleModel().trim());
        }
        if (request.getPassengerCapacity() != null) {
            vehicle.setPassenger_capacity(request.getPassengerCapacity());
        }
        if (request.getStatus() != null) vehicle.setStatus(request.getStatus());

        return success(toResponse(vehicleRepository.save(vehicle)));
    }

    @Override
    @Transactional
    public BaseResponse<Void> deleteVehicle(UUID companyId, UUID vehicleId) {
        getActiveCompany(companyId);
        Vehicle vehicle = getCompanyVehicle(companyId, vehicleId);
        if (tripAssignmentRepository.existsAssignmentForVehicle(vehicleId)) {
            vehicle.setStatus(VehicleStatus.RETIRED);
            vehicleRepository.save(vehicle);
        } else {
            vehicleRepository.delete(vehicle);
        }
        return new BaseResponse<>(ApplicationCode.SUCCESS);
    }

    private BusCompany getActiveCompany(UUID companyId) {
        return busCompanyRepository.findByIdAndStatus(companyId, BusCompanyStatus.ACTIVE)
                .orElseThrow(() -> notFound("Bus company was not found or is inactive"));
    }

    private Vehicle getCompanyVehicle(UUID companyId, UUID vehicleId) {
        return vehicleRepository.findByCompany(vehicleId, companyId)
                .orElseThrow(() -> notFound("Vehicle was not found for this company"));
    }

    private String normalizePlate(String plateNumber) {
        if (!StringUtils.hasText(plateNumber)) throw badRequest("Vehicle plate must not be blank");
        String plate = plateNumber.trim().toUpperCase(Locale.ROOT).replace(" ", "").replace("-", "");
        if (plate.isEmpty() || plate.length() > 20) throw badRequest("Vehicle plate must be 1 to 20 characters");
        return plate;
    }

    private boolean hasChanges(UpdateVehicleRequest request) {
        return request.getPlateNumber() != null || request.getVehicleModel() != null
                || request.getPassengerCapacity() != null || request.getStatus() != null;
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.setId(vehicle.getId());
        response.setCompanyId(vehicle.getCompany_id().getId());
        response.setPlateNumber(vehicle.getPlate_normalized());
        response.setVehicleModel(vehicle.getVehicle_model());
        response.setPassengerCapacity(vehicle.getPassenger_capacity());
        response.setStatus(vehicle.getStatus());
        response.setCreatedAt(vehicle.getCreated_at());
        response.setUpdatedAt(vehicle.getUpdated_at());
        return response;
    }

    private BaseResponse<VehicleResponse> success(VehicleResponse vehicle) {
        BaseResponse<VehicleResponse> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(vehicle);
        return response;
    }

    private BaseResponse<List<VehicleResponse>> success(List<VehicleResponse> vehicles) {
        BaseResponse<List<VehicleResponse>> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(vehicles);
        return response;
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
