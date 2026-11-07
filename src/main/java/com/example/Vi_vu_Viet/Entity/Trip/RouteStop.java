package com.example.Vi_vu_Viet.Entity.Trip;

import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "route_stops",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_route_stops_route_sequence",
                columnNames = {"route_id", "sequence_no"}
        ),
        indexes = {
                @Index(name = "ix_route_stops_company_route_sequence", columnList = "company_id, route_id, sequence_no"),
                @Index(name = "ix_route_stops_company_stop", columnList = "company_id, stop_id")
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteStop extends BaseEntity {

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    BusCompany company_id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    Route route_id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    Stop stop_id;

    Integer sequence_no;

    Integer arrival_offset_minutes;

    Integer departure_offset_minutes;

    Boolean pickup_allowed;

    Boolean dropoff_allowed;
}
