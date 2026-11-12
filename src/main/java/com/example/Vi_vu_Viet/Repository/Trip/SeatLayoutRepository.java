package com.example.Vi_vu_Viet.Repository.Trip;
import com.example.Vi_vu_Viet.Entity.Trip.SeatLayout;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface SeatLayoutRepository extends JpaRepository<SeatLayout, UUID> {}
