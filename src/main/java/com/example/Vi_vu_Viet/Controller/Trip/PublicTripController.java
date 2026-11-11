package com.example.Vi_vu_Viet.Controller.Trip;

import com.example.Vi_vu_Viet.Service.Trip.TripSearchService;
import com.example.Vi_vu_Viet.Service.Trip.BookingService;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.Booking.TripSeatResponse;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trips")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PublicTripController {

    TripSearchService tripSearchService;
    BookingService bookingService;

    @Operation(
            summary = "Tìm chuyến công khai",
            description = "Chỉ trả về chuyến đã công bố và đang mở bán trên tuyến có khu vực đầu/cuối khớp yêu cầu."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tìm chuyến thành công"),
            @ApiResponse(responseCode = "400", description = "Khu vực hoặc ngày đi không hợp lệ")
    })
    @GetMapping
    public BaseResponse<List<TripResponse>> searchTrips(
            @Parameter(description = "ID khu vực đi")
            @RequestParam("origin_area_id") UUID originAreaId,
            @Parameter(description = "ID khu vực đến")
            @RequestParam("destination_area_id") UUID destinationAreaId,
            @Parameter(description = "Ngày khởi hành, định dạng yyyy-MM-dd")
            @RequestParam("departure_date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate
    ) {
        return tripSearchService.searchTrips(originAreaId, destinationAreaId, departureDate);
    }

    @Operation(
            summary = "Chi tiết chuyến công khai",
            description = "Lấy chi tiết một chuyến đã công bố và đang mở bán, gồm thông tin tuyến và khu vực đầu/cuối."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết chuyến thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chuyến công khai")
    })
    @GetMapping("/{trip_id}")
    public BaseResponse<TripResponse> getPublishedTrip(
            @Parameter(description = "ID chuyến")
            @PathVariable("trip_id") UUID tripId
    ) {
        return tripSearchService.getPublishedTrip(tripId);
    }

    @Operation(summary = "Sơ đồ ghế và tình trạng còn trống của chuyến")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách ghế thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chuyến đang mở bán")
    })
    @GetMapping("/{trip_id}/seats")
    public BaseResponse<List<TripSeatResponse>> getTripSeats(
            @Parameter(description = "ID chuyến")
            @PathVariable("trip_id") UUID tripId
    ) {
        return bookingService.getAvailableSeats(tripId);
    }
}
