package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.Booking;
import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    String COMPANY_SEARCH_WHERE = """
            where booking.company_id.id = :companyId
              and (:tripId is null or booking.trip_id.id = :tripId)
              and (:routeId is null or booking.trip_id.route_id.id = :routeId)
              and (:customerId is null or booking.customer_user_id.id = :customerId)
              and (cast(:createdFrom as timestamp) is null or booking.created_at >= :createdFrom)
              and (cast(:createdTo as timestamp) is null or booking.created_at <= :createdTo)
              and (cast(:departureFrom as timestamp) is null or booking.trip_id.departure_at >= :departureFrom)
              and (cast(:departureTo as timestamp) is null or booking.trip_id.departure_at <= :departureTo)
              and (:status is null
                   or (booking.status = :status and
                       (:status <> :holdingStatus or booking.hold_expires_at > :now))
                   or (:status = :expiredStatus and booking.status = :holdingStatus
                       and booking.hold_expires_at <= :now))
              and (:pattern is null
                   or lower(booking.booking_code) like :pattern escape '!'
                   or lower(booking.contact_name) like :pattern escape '!'
                   or lower(booking.contact_phone) like :pattern escape '!'
                   or lower(booking.contact_email) like :pattern escape '!'
                   or lower(booking.trip_id.trip_code) like :pattern escape '!'
                   or lower(booking.trip_id.route_id.code) like :pattern escape '!'
                   or lower(booking.trip_id.route_id.name) like :pattern escape '!'
                   or exists (select ticket.id from Ticket ticket
                       where ticket.booking_id.id = booking.id
                         and (lower(ticket.ticket_code) like :pattern escape '!'
                              or lower(ticket.passenger_name) like :pattern escape '!'
                              or lower(ticket.passenger_phone) like :pattern escape '!')))
            """;

    @EntityGraph(attributePaths = {"company_id", "trip_id", "trip_id.route_id",
            "trip_id.route_id.origin_area_id", "trip_id.route_id.destination_area_id"})
    @Query(value = "select booking from Booking booking " + COMPANY_SEARCH_WHERE +
            " order by booking.created_at desc, booking.id desc",
            countQuery = "select count(booking) from Booking booking " + COMPANY_SEARCH_WHERE)
    Page<Booking> searchByCompanyId(
            @Param("companyId") UUID companyId,
            @Param("status") BookingStatus status,
            @Param("tripId") UUID tripId,
            @Param("routeId") UUID routeId,
            @Param("customerId") UUID customerId,
            @Param("createdFrom") LocalDateTime createdFrom,
            @Param("createdTo") LocalDateTime createdTo,
            @Param("departureFrom") LocalDateTime departureFrom,
            @Param("departureTo") LocalDateTime departureTo,
            @Param("pattern") String pattern,
            @Param("holdingStatus") BookingStatus holdingStatus,
            @Param("expiredStatus") BookingStatus expiredStatus,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"company_id", "trip_id", "trip_id.route_id",
            "trip_id.route_id.origin_area_id", "trip_id.route_id.destination_area_id"})
    @Query("select booking from Booking booking where booking.company_id.id = :companyId " +
            "and booking.id = :bookingId")
    Optional<Booking> findByIdAndCompanyId(@Param("companyId") UUID companyId,
                                          @Param("bookingId") UUID bookingId);

    @Query("select booking.trip_id.id from Booking booking where booking.company_id.id = :companyId " +
            "and booking.id = :bookingId")
    Optional<UUID> findTripIdByIdAndCompanyId(@Param("companyId") UUID companyId,
                                             @Param("bookingId") UUID bookingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select booking from Booking booking where booking.company_id.id = :companyId " +
            "and booking.id = :bookingId")
    Optional<Booking> findByIdAndCompanyIdForUpdate(@Param("companyId") UUID companyId,
                                                   @Param("bookingId") UUID bookingId);

    @EntityGraph(attributePaths = {"company_id", "trip_id", "trip_id.route_id",
            "trip_id.route_id.origin_area_id", "trip_id.route_id.destination_area_id"})
    @Query("select booking from Booking booking where booking.customer_user_id.id = :customerId " +
            "and booking.trip_id.status in :tripStatuses and booking.status in :bookingStatuses " +
            "and (booking.status <> :holdingStatus or booking.hold_expires_at > :now) " +
            "order by booking.created_at desc, booking.id desc")
    List<Booking> findAllActiveForCustomerTripStatuses(
            @Param("customerId") UUID customerId,
            @Param("tripStatuses") Collection<TripStatus> tripStatuses,
            @Param("bookingStatuses") Collection<BookingStatus> bookingStatuses,
            @Param("holdingStatus") BookingStatus holdingStatus,
            @Param("now") LocalDateTime now
    );

    @EntityGraph(attributePaths = {"company_id", "trip_id", "trip_id.route_id",
            "trip_id.route_id.origin_area_id", "trip_id.route_id.destination_area_id"})
    @Query("select booking from Booking booking where booking.customer_user_id.id = :customerId " +
            "and (booking.status in :cancelledBookingStatuses " +
            "or (booking.status = :holdingStatus and booking.hold_expires_at <= :now) " +
            "or booking.trip_id.status in :cancelledTripStatuses) " +
            "order by booking.created_at desc, booking.id desc")
    List<Booking> findAllCancelledForCustomer(
            @Param("customerId") UUID customerId,
            @Param("cancelledBookingStatuses") Collection<BookingStatus> cancelledBookingStatuses,
            @Param("holdingStatus") BookingStatus holdingStatus,
            @Param("now") LocalDateTime now,
            @Param("cancelledTripStatuses") Collection<TripStatus> cancelledTripStatuses
    );

    String CUSTOMER_SEARCH_WHERE = """
            where booking.customer_user_id.id = :customerId
              and (:routeId is null or booking.trip_id.route_id.id = :routeId)
              and (:status is null
                   or (booking.status = :status and
                       (:status <> :holdingStatus or booking.hold_expires_at > :now))
                   or (:status = :expiredStatus and booking.status = :holdingStatus
                       and booking.hold_expires_at <= :now))
              and (:pattern is null
                   or lower(booking.booking_code) like :pattern escape '!'
                   or lower(booking.trip_id.trip_code) like :pattern escape '!'
                   or lower(booking.trip_id.route_id.code) like :pattern escape '!'
                   or lower(booking.trip_id.route_id.name) like :pattern escape '!'
                   or lower(booking.company_id.name) like :pattern escape '!'
                   or exists (select ticket.id from Ticket ticket
                       where ticket.booking_id.id = booking.id
                         and (lower(ticket.ticket_code) like :pattern escape '!'
                              or lower(ticket.passenger_name) like :pattern escape '!')))
            """;

    @EntityGraph(attributePaths = {"company_id", "trip_id", "trip_id.route_id",
            "trip_id.route_id.origin_area_id", "trip_id.route_id.destination_area_id"})
    @Query(value = "select booking from Booking booking " + CUSTOMER_SEARCH_WHERE +
            " order by booking.created_at desc, booking.id desc",
            countQuery = "select count(booking) from Booking booking " + CUSTOMER_SEARCH_WHERE)
    Page<Booking> searchByCustomerId(
            @Param("customerId") UUID customerId,
            @Param("status") BookingStatus status,
            @Param("routeId") UUID routeId,
            @Param("pattern") String pattern,
            @Param("holdingStatus") BookingStatus holdingStatus,
            @Param("expiredStatus") BookingStatus expiredStatus,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"company_id", "trip_id", "trip_id.route_id",
            "trip_id.route_id.origin_area_id", "trip_id.route_id.destination_area_id"})
    @Query("select booking from Booking booking where booking.company_id.id = :companyId " +
            "and booking.trip_id.id = :tripId order by booking.created_at desc, booking.id desc")
    List<Booking> findAllForCompanyTrip(@Param("companyId") UUID companyId, @Param("tripId") UUID tripId);

    @Query("select customer.id as id, customer.full_name as fullName, customer.email as email, " +
            "customer.phone as phone, customer.account_status as accountStatus, " +
            "count(booking.id) as bookingCount, max(booking.created_at) as lastBookingAt " +
            "from Booking booking join booking.customer_user_id customer " +
            "where booking.company_id.id = :companyId " +
            "and (:accountStatus is null or customer.account_status = :accountStatus) " +
            "and (:bookingStatus is null or exists (select statusBooking.id from Booking statusBooking " +
            "where statusBooking.company_id.id = :companyId " +
            "and statusBooking.customer_user_id.id = customer.id " +
            "and ((statusBooking.status = :bookingStatus and " +
            "(:bookingStatus <> :holdingStatus or statusBooking.hold_expires_at > :now)) " +
            "or (:bookingStatus = :expiredStatus and statusBooking.status = :holdingStatus " +
            "and statusBooking.hold_expires_at <= :now)))) " +
            "and (:pattern is null or lower(customer.full_name) like :pattern escape '!' " +
            "or lower(customer.email) like :pattern escape '!' or lower(customer.phone) like :pattern escape '!' " +
            "or exists (select matchBooking.id from Booking matchBooking " +
            "where matchBooking.company_id.id = :companyId and matchBooking.customer_user_id.id = customer.id " +
            "and (lower(matchBooking.booking_code) like :pattern escape '!' " +
            "or exists (select ticket.id from Ticket ticket where ticket.booking_id.id = matchBooking.id " +
            "and (lower(ticket.ticket_code) like :pattern escape '!' " +
            "or lower(ticket.passenger_name) like :pattern escape '!'))))) " +
            "group by customer.id, customer.full_name, customer.email, customer.phone, customer.account_status " +
            "order by customer.full_name, customer.id")
    List<CompanyCustomerProjection> searchCompanyCustomers(
            @Param("companyId") UUID companyId,
            @Param("pattern") String pattern,
            @Param("accountStatus") UserAccountStatus accountStatus,
            @Param("bookingStatus") BookingStatus bookingStatus,
            @Param("holdingStatus") BookingStatus holdingStatus,
            @Param("expiredStatus") BookingStatus expiredStatus,
            @Param("now") LocalDateTime now);

    @Query("select case when count(booking) > 0 then true else false end from Booking booking " +
            "where booking.company_id.id = :companyId and booking.customer_user_id.id = :customerId")
    boolean existsByCompanyAndCustomer(@Param("companyId") UUID companyId,
                                       @Param("customerId") UUID customerId);

    @EntityGraph(attributePaths = {"company_id", "trip_id", "trip_id.route_id",
            "trip_id.route_id.origin_area_id", "trip_id.route_id.destination_area_id"})
    @Query("select booking from Booking booking where booking.company_id.id = :companyId " +
            "and booking.customer_user_id.id = :customerId " +
            "order by booking.created_at desc, booking.id desc")
    List<Booking> findAllForCompanyCustomer(@Param("companyId") UUID companyId,
                                            @Param("customerId") UUID customerId);

    @EntityGraph(attributePaths = {"trip_id.route_id", "trip_id.route_id.origin_area_id",
            "trip_id.route_id.destination_area_id"})
    @Query("select booking from Booking booking " +
            "join fetch booking.company_id company " +
            "join fetch booking.trip_id trip " +
            "where booking.id = :bookingId and booking.customer_user_id.id = :customerId")
    Optional<Booking> findByIdAndCustomerId(
            @Param("bookingId") UUID bookingId,
            @Param("customerId") UUID customerId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select booking from Booking booking where booking.id = :bookingId " +
            "and booking.customer_user_id.id = :customerId")
    Optional<Booking> findByIdAndCustomerIdForUpdate(
            @Param("bookingId") UUID bookingId,
            @Param("customerId") UUID customerId
    );

    @Query("select booking from Booking booking where booking.trip_id.id = :tripId " +
            "and booking.status = :status and booking.hold_expires_at <= :now")
    List<Booking> findExpiredHolds(
            @Param("tripId") UUID tripId,
            @Param("status") BookingStatus status,
            @Param("now") LocalDateTime now
    );

    @Query("select count(booking) from Booking booking where booking.trip_id.id = :tripId")
    long countByTripId(@Param("tripId") UUID tripId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select booking from Booking booking where booking.company_id.id = :companyId " +
            "and booking.trip_id.id = :tripId order by booking.id")
    List<Booking> findAllForCompanyTripForUpdate(@Param("companyId") UUID companyId,
                                                 @Param("tripId") UUID tripId);

    @Query("select count(booking) from Booking booking where booking.company_id.id = :companyId " +
            "and lower(booking.booking_code) = lower(:bookingCode)")
    long countByCompanyIdAndBookingCode(
            @Param("companyId") UUID companyId,
            @Param("bookingCode") String bookingCode
    );
}
