package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.TripSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripSeatRepository extends JpaRepository<TripSeat, UUID> {

    @Modifying
    @Query("delete from TripSeat seat where seat.company_id.id = :companyId and seat.trip_id.id = :tripId")
    int deleteByCompanyAndTrip(@Param("companyId") UUID companyId, @Param("tripId") UUID tripId);

    @Query("select seat from TripSeat seat where seat.company_id.id = :companyId " +
            "and seat.trip_id.id = :tripId order by seat.row_no, seat.column_no, seat.seat_code")
    List<TripSeat> findAllByCompanyIdAndTripId(
            @Param("companyId") UUID companyId,
            @Param("tripId") UUID tripId
    );

    @Query("select seat from TripSeat seat where seat.id = :seatId " +
            "and seat.company_id.id = :companyId and seat.trip_id.id = :tripId")
    Optional<TripSeat> findByIdAndCompanyIdAndTripId(
            @Param("seatId") UUID seatId,
            @Param("companyId") UUID companyId,
            @Param("tripId") UUID tripId
    );

    @Query("select count(seat) from TripSeat seat where seat.company_id.id = :companyId " +
            "and seat.trip_id.id = :tripId")
    long countByCompanyIdAndTripId(
            @Param("companyId") UUID companyId,
            @Param("tripId") UUID tripId
    );
}
