package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Trip.Area;
import com.example.Vi_vu_Viet.Entity.Trip.BusCompany;
import com.example.Vi_vu_Viet.Entity.Trip.Stop;
import com.example.Vi_vu_Viet.Repository.Trip.AreaRepository;
import com.example.Vi_vu_Viet.Repository.Trip.BusCompanyRepository;
import com.example.Vi_vu_Viet.Repository.Trip.StopRepository;
import com.example.Vi_vu_Viet.Service.Trip.StopService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.StopStatus;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateStopRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Location.StopResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StopServiceImpl implements StopService {

    BusCompanyRepository busCompanyRepository;
    AreaRepository areaRepository;
    StopRepository stopRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<StopResponse>> getStops(UUID companyId) {
        getActiveCompany(companyId);

        List<StopResponse> stops = stopRepository.findAllByCompanyId(companyId)
                .stream()
                .map(this::toResponse)
                .toList();

        BaseResponse<List<StopResponse>> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(stops);
        return response;
    }

    @Override
    @Transactional
    public BaseResponse<StopResponse> createStop(UUID companyId, CreateStopRequest request) {
        BusCompany company = getActiveCompany(companyId);
        Area area = areaRepository.findActiveById(request.getAreaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Area was not found or is inactive"
                ));

        Stop stop = new Stop();
        stop.setCompany_id(company);
        stop.setArea_id(area);
        stop.setName(request.getName().trim());
        stop.setAddress(request.getAddress().trim());
        stop.setPickup_instructions(normalize(request.getPickupInstructions()));
        stop.setStatus(StopStatus.ACTIVE);

        Stop savedStop = stopRepository.save(stop);
        BaseResponse<StopResponse> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(toResponse(savedStop));
        return response;
    }

    private BusCompany getActiveCompany(UUID companyId) {
        return busCompanyRepository.findByIdAndStatus(companyId, BusCompanyStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Bus company was not found or is inactive"
                ));
    }

    private StopResponse toResponse(Stop stop) {
        StopResponse response = new StopResponse();
        response.setId(stop.getId());
        response.setCompanyId(stop.getCompany_id().getId());
        response.setAreaId(stop.getArea_id().getId());
        response.setAreaCode(stop.getArea_id().getCode());
        response.setAreaName(stop.getArea_id().getName());
        response.setName(stop.getName());
        response.setAddress(stop.getAddress());
        response.setPickupInstructions(stop.getPickup_instructions());
        response.setStatus(stop.getStatus());
        response.setCreatedAt(stop.getCreated_at());
        response.setUpdatedAt(stop.getUpdated_at());
        return response;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
