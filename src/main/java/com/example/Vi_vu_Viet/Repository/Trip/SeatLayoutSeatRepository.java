package com.example.Vi_vu_Viet.Repository.Trip;
import com.example.Vi_vu_Viet.Entity.Trip.SeatLayoutSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface SeatLayoutSeatRepository extends JpaRepository<SeatLayoutSeat, UUID> {
    List<SeatLayoutSeat> findAllByLayoutIdOrderByDeckAscRowNoAscColumnNoAsc(UUID layoutId);
}
