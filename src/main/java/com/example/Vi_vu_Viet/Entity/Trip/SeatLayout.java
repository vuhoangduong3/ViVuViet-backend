package com.example.Vi_vu_Viet.Entity.Trip;

import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat_layouts")
@Getter @Setter
public class SeatLayout extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private BusCompany company;
    @Column(nullable = false, length = 120)
    private String name;
}
