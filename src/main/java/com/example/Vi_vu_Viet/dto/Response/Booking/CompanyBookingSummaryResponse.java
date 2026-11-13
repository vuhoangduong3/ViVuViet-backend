package com.example.Vi_vu_Viet.dto.Response.Booking;

import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Thông tin đơn để hiển thị danh sách quản lý toàn hãng")
public class CompanyBookingSummaryResponse {
    UUID id;
    String bookingCode;
    UUID companyId;
    UUID customerId;
    String contactName;
    String contactPhone;
    String contactEmail;
    UUID tripId;
    String tripCode;
    TripStatus tripStatus;
    LocalDateTime departureAt;
    LocalDateTime expectedArrivalAt;
    String timezone;
    UUID routeId;
    String routeName;
    String originAreaName;
    String destinationAreaName;
    @Schema(description = "Tổng số vé trong đơn, gồm cả vé đã hủy/hết hạn")
    int ticketCount;
    List<String> seatCodes;
    Long quotedTotalAmount;
    String currency;
    BookingStatus status;
    LocalDateTime holdExpiresAt;
    LocalDateTime createdAt;
    LocalDateTime confirmedAt;
    LocalDateTime cancelledAt;
    LocalDateTime updatedAt;
}
