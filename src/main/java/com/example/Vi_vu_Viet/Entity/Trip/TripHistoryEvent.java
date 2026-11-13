package com.example.Vi_vu_Viet.Entity.Trip;

import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "trip_history_events", indexes =
        @Index(name = "ix_trip_history_trip_time", columnList = "trip_id, occurred_at"))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripHistoryEvent extends BaseEntity {
    public enum Type { CREATED, ASSIGNED, PUBLISHED, ACCEPTED, REJECTED, STARTED, COMPLETED, CANCELLED }

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    Trip trip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    Type type;

    @Column(name = "occurred_at", nullable = false)
    LocalDateTime occurredAt;

    // Lưu tên tại thời điểm thao tác để lịch sử không đổi khi tài khoản đổi tên.
    UUID actorId;
    @Column(length = 160)
    String actorName;
    @Column(length = 1000)
    String description;
}
