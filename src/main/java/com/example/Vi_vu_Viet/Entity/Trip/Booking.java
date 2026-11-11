package com.example.Vi_vu_Viet.Entity.Trip;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
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

import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "bookings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bookings_company_code",
                columnNames = {"company_id", "booking_code"}
        ),
        indexes = {
                @Index(name = "ix_bookings_customer_created", columnList = "customer_user_id, created_at"),
                @Index(name = "ix_bookings_trip_status", columnList = "trip_id, status, hold_expires_at")
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking extends BaseEntity {

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
    @JoinColumn(name = "customer_user_id", nullable = false)
    UserAccount customer_user_id;

    @Column(name = "booking_code", nullable = false, length = 24)
    String booking_code;

    @Column(name = "contact_name", nullable = false, length = 120)
    String contact_name;

    @Column(name = "contact_phone", nullable = false, length = 30)
    String contact_phone;

    @Column(name = "contact_email", length = 255)
    String contact_email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    BookingStatus status;

    @Column(name = "hold_expires_at", nullable = false)
    LocalDateTime hold_expires_at;

    @Column(name = "quoted_total_amount", nullable = false)
    Long quoted_total_amount;

    @Column(nullable = false, length = 3)
    String currency;

    @Column(name = "confirmed_at")
    LocalDateTime confirmed_at;

    @Column(name = "cancelled_at")
    LocalDateTime cancelled_at;
}
