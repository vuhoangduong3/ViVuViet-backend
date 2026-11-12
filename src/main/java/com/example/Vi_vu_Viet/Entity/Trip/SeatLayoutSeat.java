package com.example.Vi_vu_Viet.Entity.Trip;

import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat_layout_seats", uniqueConstraints = {
    @UniqueConstraint(name = "uk_layout_seat_code", columnNames = {"seat_layout_id", "seat_code"}),
    @UniqueConstraint(name = "uk_layout_seat_position", columnNames = {"seat_layout_id", "deck", "row_no", "column_no"})
})
@Getter @Setter
public class SeatLayoutSeat extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_layout_id", nullable = false)
    private SeatLayout layout;
    @Column(name = "seat_code", nullable = false, length = 20)
    private String seatCode;
    @Column(name = "seat_class", nullable = false, length = 30)
    private String seatClass;
    @Column(nullable = false)
    private Integer deck;
    @Column(name = "row_no", nullable = false)
    private Integer rowNo;
    @Column(name = "column_no", nullable = false)
    private Integer columnNo;
}
