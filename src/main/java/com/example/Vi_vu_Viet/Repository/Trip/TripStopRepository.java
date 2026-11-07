package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.TripStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TripStopRepository extends JpaRepository<TripStop, UUID> {

    @Query("select tripStop from TripStop tripStop " +
            "join fetch tripStop.stop_id stop " +
            "join fetch tripStop.area_id area " +
            "where tripStop.company_id.id = :companyId and tripStop.trip_id.id = :tripId " +
            "order by tripStop.sequence_no")
    List<TripStop> findAllByCompanyIdAndTripId(
            @Param("companyId") UUID companyId,
            @Param("tripId") UUID tripId
    );

    @Query("select tripStop from TripStop tripStop " +
            "join fetch tripStop.stop_id stop " +
            "join fetch tripStop.area_id area " +
            "where tripStop.company_id.id = :companyId and tripStop.trip_id.id in :tripIds " +
            "order by tripStop.trip_id.id, tripStop.sequence_no")
    List<TripStop> findAllByCompanyIdAndTripIds(
            @Param("companyId") UUID companyId,
            @Param("tripIds") Collection<UUID> tripIds
    );
}
