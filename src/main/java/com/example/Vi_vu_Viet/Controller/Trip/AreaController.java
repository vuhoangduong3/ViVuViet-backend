package com.example.Vi_vu_Viet.Controller.Trip;

import com.example.Vi_vu_Viet.Service.Trip.AreaService;
import com.example.Vi_vu_Viet.dto.Response.Location.AreaResponse;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/areas")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AreaController {

    AreaService areaService;

    @Operation(
            summary = "Tìm kiếm địa điểm",
            description = "Tìm trong danh mục địa điểm đang hoạt động. Có thể lọc theo tên/mã, cấp địa điểm hoặc địa điểm cha."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách địa điểm thành công"),
            @ApiResponse(responseCode = "400", description = "parentId không đúng định dạng UUID")
    })
    @GetMapping
    public BaseResponse<List<AreaResponse>> getAreas(
            @Parameter(description = "Từ khóa tìm trong tên hoặc mã địa điểm")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "ID địa điểm cha để lấy các địa điểm con")
            @RequestParam(required = false) UUID parentId,
            @Parameter(description = "Loại địa điểm, ví dụ province hoặc district")
            @RequestParam(required = false) String type
    ) {
        return areaService.getAreas(keyword, parentId, type);
    }
}
