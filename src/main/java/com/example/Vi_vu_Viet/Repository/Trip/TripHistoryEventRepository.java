package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.TripHistoryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TripHistoryEventRepository extends JpaRepository<TripHistoryEvent, UUID> {
    @Query("select event from TripHistoryEvent event where event.trip.company_id.id = :companyId " +
            "and event.trip.id = :tripId order by event.occurredAt, event.created_at, event.id")
    List<TripHistoryEvent> findAllByCompanyAndTrip(@Param("companyId") UUID companyId,
                                                 @Param("tripId") UUID tripId);

    @Modifying
    @Query("delete from TripHistoryEvent event where event.trip.company_id.id = :companyId " +
            "and event.trip.id = :tripId")
    int deleteByCompanyAndTrip(@Param("companyId") UUID companyId, @Param("tripId") UUID tripId);
}
