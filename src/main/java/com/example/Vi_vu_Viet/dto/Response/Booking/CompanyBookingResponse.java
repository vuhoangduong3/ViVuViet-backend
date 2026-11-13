package com.example.Vi_vu_Viet.dto.Response.Booking;

import com.example.Vi_vu_Viet.Util.Enums.BookingViewStatus;
import com.example.Vi_vu_Viet.Util.Enums.PassengerTripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Chi tiết đơn của hãng, gồm chuyến và toàn bộ vé/hành khách")
public class CompanyBookingResponse extends BookingResponse {
    UUID companyId;
    UUID customerId;
    String cancellationReason;
    @Schema(description = "ID tài khoản admin thực hiện hủy; null với dữ liệu cũ hoặc hủy qua luồng khác")
    UUID cancelledBy;
    BookingViewStatus viewStatus;
    PassengerTripStatus passengerTripStatus;
    String pickupAddress;
    String dropoffAddress;
    @Schema(description = "null nếu chưa có phân công hợp lệ hoặc phân công đã bị từ chối/thay thế")
    CompanyBookingAssignmentResponse assignment;
    LocalDateTime boardedAt;
    LocalDateTime completedAt;
    List<HistoryItem> history;
    @Schema(description = "false nếu dữ liệu cũ thiếu mốc thời gian; không suy đoán giờ không lên xe")
    boolean historyComplete;

    public record HistoryItem(String eventType, String title, LocalDateTime occurredAt, String state,
                              String description) { }
}
