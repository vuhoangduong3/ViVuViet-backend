package com.example.Vi_vu_Viet.Entity.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "trip_assignments", uniqueConstraints = @UniqueConstraint(
        name = "uk_trip_assignments_trip", columnNames = {"trip_id"}))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    BusCompany company_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    Trip trip_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    Vehicle vehicle_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_user_id", nullable = false)
    UserAccount driver_user_id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    UserAccount assigned_by;

    @Column(name = "assigned_at", nullable = false)
    LocalDateTime assigned_at;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ASSIGNED'")
    @Column(name = "assignment_status", nullable = false, length = 20)
    DriverAssignmentStatus assignment_status = DriverAssignmentStatus.ASSIGNED;

    @Column(name = "accepted_at")
    LocalDateTime accepted_at;

    @Column(name = "rejection_reason", length = 500)
    String rejection_reason;
}
