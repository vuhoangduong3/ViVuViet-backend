package com.example.Vi_vu_Viet.Entity.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.Util.Enums.PassengerTripStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@DynamicUpdate
@Data
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "tickets",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_tickets_company_code",
                columnNames = {"company_id", "ticket_code"}
        ),
        indexes = {
                @Index(name = "ix_tickets_trip_status", columnList = "trip_id, status"),
                @Index(name = "ix_tickets_booking", columnList = "booking_id")
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Ticket extends BaseEntity {

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    BusCompany company_id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    Trip trip_id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    Booking booking_id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_seat_id", nullable = false)
    TripSeat trip_seat_id;

    @Column(name = "ticket_code", nullable = false, length = 24)
    String ticket_code;

    @Column(name = "passenger_name", nullable = false, length = 120)
    String passenger_name;

    @Column(name = "passenger_phone", length = 30)
    String passenger_phone;

    @Column(name = "pickup_address", nullable = false, length = 500)
    String pickup_address;

    @Column(name = "dropoff_address", nullable = false, length = 500)
    String dropoff_address;

    @Column(name = "seat_label_snapshot", nullable = false, length = 20)
    String seat_label_snapshot;

    @Column(name = "fare_amount", nullable = false)
    Long fare_amount;

    @Column(name = "collected_amount")
    Long collected_amount;

    @Column(name = "collected_at")
    LocalDateTime collected_at;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collected_by")
    UserAccount collected_by;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    TicketStatus status;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'WAITING_PICKUP'")
    @Column(name = "passenger_trip_status", nullable = false, length = 20)
    PassengerTripStatus passenger_trip_status = PassengerTripStatus.WAITING_PICKUP;

    @Column(name = "boarded_at")
    LocalDateTime boarded_at;

    @Column(name = "completed_at")
    LocalDateTime completed_at;
}
