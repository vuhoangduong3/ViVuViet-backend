package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Trip.Area;
import com.example.Vi_vu_Viet.Repository.Trip.AreaRepository;
import com.example.Vi_vu_Viet.Service.Trip.AreaService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.dto.Response.Location.AreaResponse;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AreaServiceImpl implements AreaService {

    AreaRepository areaRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<AreaResponse>> getAreas(String keyword, UUID parentId, String type) {
        List<AreaResponse> areas = areaRepository.findActiveAreas(
                        normalize(keyword),
                        parentId,
                        normalize(type)
                )
                .stream()
                .map(this::toResponse)
                .toList();

        BaseResponse<List<AreaResponse>> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(areas);
        return response;
    }

    private AreaResponse toResponse(Area area) {
        AreaResponse response = new AreaResponse();
        response.setId(area.getId());
        response.setParentId(area.getParent_id() == null ? null : area.getParent_id().getId());
        response.setCode(area.getCode());
        response.setName(area.getName());
        response.setType(area.getType());
        return response;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
