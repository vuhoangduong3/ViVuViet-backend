package com.example.Vi_vu_Viet.dto.Request.Booking;

import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.Util.Enums.BookingViewStatus;
import com.example.Vi_vu_Viet.Util.Enums.PassengerTripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyBookingFilter {
    @Size(max = 200, message = "Từ khóa tối đa 200 ký tự")
    @Schema(description = "Tìm mã đơn/vé/chuyến/tuyến, tên tuyến, liên hệ hoặc hành khách")
    String q;
    BookingStatus status;
    @Schema(description = "Tab quản lý đơn; bỏ trống để lấy tất cả. HOLDING/EXPIRED dùng cho đơn chưa xác nhận.")
    BookingViewStatus viewStatus;
    @Schema(description = "Trạng thái hành trình chung của đơn, chỉ áp dụng cho đơn CONFIRMED")
    PassengerTripStatus passengerTripStatus;
    @Schema(description = "ID tài khoản tài xế đang được phân công; bỏ phân công REJECTED/REPLACED")
    UUID driverId;
    UUID tripId;
    UUID routeId;
    UUID customerId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Thời gian tạo từ (bao gồm), ISO local datetime")
    LocalDateTime createdFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Thời gian tạo đến (bao gồm), ISO local datetime")
    LocalDateTime createdTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Giờ khởi hành từ (bao gồm), theo múi giờ hãng")
    LocalDateTime departureFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Giờ khởi hành đến (bao gồm), theo múi giờ hãng")
    LocalDateTime departureTo;

    @Min(value = 0, message = "Trang phải lớn hơn hoặc bằng 0")
    @Schema(defaultValue = "0", description = "Trang bắt đầu từ 0")
    int page = 0;

    @Min(value = 1, message = "Kích thước trang tối thiểu 1")
    @Max(value = 100, message = "Kích thước trang tối đa 100")
    @Schema(defaultValue = "20")
    int size = 20;
}
