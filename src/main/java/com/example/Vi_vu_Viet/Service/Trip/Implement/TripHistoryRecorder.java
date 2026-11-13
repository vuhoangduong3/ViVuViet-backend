package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Entity.Trip.TripAssignment;
import com.example.Vi_vu_Viet.Entity.Trip.TripHistoryEvent;
import com.example.Vi_vu_Viet.Entity.Trip.TripHistoryEvent.Type;
import com.example.Vi_vu_Viet.Repository.Trip.TripHistoryEventRepository;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.dto.Response.Trip.TripDetailResponse.HistoryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TripHistoryRecorder {
    private final TripHistoryEventRepository historyRepository;

    // Gọi trong transaction của thao tác gốc: lịch sử chỉ được lưu khi thao tác thành công.
    public void record(Trip trip, Type type, String description, LocalDateTime occurredAt) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserAccount actor = authentication != null && authentication.getPrincipal() instanceof UserAccount user
                ? user : null;
        record(trip, type, description, occurredAt, actor);
    }

    public void record(Trip trip, Type type, String description, LocalDateTime occurredAt, UserAccount actor) {
        TripHistoryEvent event = new TripHistoryEvent();
        event.setTrip(trip);
        event.setType(type);
        event.setOccurredAt(occurredAt);
        event.setDescription(description);
        event.setActorId(actor == null ? null : actor.getId());
        event.setActorName(actor == null ? null : actor.getFull_name());
        historyRepository.save(event);
    }

    public void deleteForTrip(Trip trip) {
        historyRepository.deleteByCompanyAndTrip(trip.getCompany_id().getId(), trip.getId());
    }

    public History timeline(Trip trip, TripAssignment assignment) {
        List<TripHistoryEvent> events = historyRepository.findAllByCompanyAndTrip(
                trip.getCompany_id().getId(), trip.getId());
        List<HistoryItem> items = new ArrayList<>(events.stream().map(event -> new HistoryItem(
                event.getType().name(), title(event.getType()), event.getOccurredAt(), null,
                event.getActorId(), event.getActorName(), event.getDescription(), "DONE")).toList());
        boolean complete = events.stream().anyMatch(event -> event.getType() == Type.CREATED);

        // Dữ liệu cũ chỉ dựng lại những mốc đã có thời gian thật, không đoán giờ công bố/nhận lệnh.
        addKnownEvent(items, Type.CREATED, trip.getCreated_at(), trip.getCreated_by(), null);
        if (assignment != null) {
            addKnownEvent(items, Type.ASSIGNED, assignment.getAssigned_at(), assignment.getAssigned_by(),
                    assignment.getVehicle_id().getPlate_normalized() + " · "
                            + assignment.getDriver_user_id().getFull_name());
            addKnownEvent(items, Type.ACCEPTED, assignment.getAccepted_at(), assignment.getDriver_user_id(), null);
        }
        addKnownEvent(items, Type.STARTED, trip.getActual_departure_at(), null, null);
        addKnownEvent(items, Type.COMPLETED, trip.getActual_arrival_at(), null, null);
        addKnownEvent(items, Type.CANCELLED, trip.getCancelled_at(), null, trip.getCancellation_reason());
        items.sort(Comparator.comparing(HistoryItem::occurredAt));
        if (trip.getStatus() == TripStatus.SCHEDULED && trip.getActual_departure_at() == null) {
            items.add(new HistoryItem("EXPECTED_DEPARTURE", "Chờ khởi hành", null, trip.getDeparture_at(),
                    null, null, "Giờ khởi hành dự kiến", "PENDING"));
        }
        return new History(List.copyOf(items), complete);
    }

    private void addKnownEvent(List<HistoryItem> items, Type type, LocalDateTime at,
                               UserAccount actor, String description) {
        if (at == null || items.stream().anyMatch(item -> item.eventType().equals(type.name())
                && at.equals(item.occurredAt()))) return;
        items.add(new HistoryItem(type.name(), title(type), at, null,
                actor == null ? null : actor.getId(), actor == null ? null : actor.getFull_name(),
                description, "DONE"));
    }

    private String title(Type type) {
        return switch (type) {
            case CREATED -> "Đã tạo chuyến";
            case ASSIGNED -> "Đã phân công xe + tài xế";
            case PUBLISHED -> "Đã công bố bán vé";
            case ACCEPTED -> "Tài xế đã nhận lệnh";
            case REJECTED -> "Tài xế đã từ chối lệnh";
            case STARTED -> "Đã khởi hành";
            case COMPLETED -> "Đã hoàn thành chuyến";
            case CANCELLED -> "Đã hủy chuyến";
        };
    }

    public record History(List<HistoryItem> items, boolean complete) { }
}
