package com.example.Vi_vu_Viet.dto.Response.Trip;

import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import com.example.Vi_vu_Viet.dto.Response.Booking.TicketResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Dữ liệu đầy đủ cho ngăn chi tiết chuyến của admin hãng")
public class TripDetailResponse extends TripResponse {
    String timezone;
    String currency;
    SeatSummary seats;
    Assignment assignment;
    int ticketCount;
    int activeTicketCount;
    List<Passenger> passengers;
    List<HistoryItem> history;
    @Schema(description = "false nếu chuyến có trước khi hệ thống bắt đầu lưu lịch sử sự kiện")
    boolean historyComplete;
    Actions actions;

    @Schema(description = "Số ghế tính từ inventory chụp của chuyến; occupancyRate là phần trăm 0–100")
    public record SeatSummary(long totalSeats, long confirmedSeats, long heldSeats, long occupiedSeats,
                              long availableSeats, double occupancyRate) { }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Assignment extends TripAssignmentResponse {
        DriverAssignmentStatus assignmentStatus;
        LocalDateTime acceptedAt;
        String rejectionReason;
        UUID assignedBy;
        String assignedByName;
        String plateNumber;
        String vehicleModel;
        Integer passengerCapacity;
        @Schema(description = "false nếu phân công đã bị từ chối hoặc thay thế")
        boolean active;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Passenger extends TicketResponse {
        UUID bookingId;
        String bookingCode;
        BookingStatus bookingStatus;
        String contactName;
        String contactPhone;
        @Schema(description = "null khi chưa có dữ liệu thu tiền; không suy ra từ trạng thái xác nhận vé")
        Boolean paymentCollected;
    }

    public record HistoryItem(String eventType, String title, LocalDateTime occurredAt, LocalDateTime expectedAt,
                              UUID actorId, String actorName, String description, String state) { }

    @Schema(description = "Khả năng thao tác theo trạng thái; mutation vẫn kiểm tra lịch trình, xe/ghế và phân công")
    public record Actions(boolean canEdit, boolean canAssign, boolean canPublish, boolean canCancel) { }
}
