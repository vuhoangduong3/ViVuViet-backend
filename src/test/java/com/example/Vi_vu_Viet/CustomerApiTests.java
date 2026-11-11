package com.example.Vi_vu_Viet;

import com.example.Vi_vu_Viet.Entity.Auth.AuthSession;
import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Trip.Area;
import com.example.Vi_vu_Viet.Entity.Trip.Booking;
import com.example.Vi_vu_Viet.Entity.Trip.BusCompany;
import com.example.Vi_vu_Viet.Entity.Trip.Route;
import com.example.Vi_vu_Viet.Entity.Trip.Ticket;
import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Entity.Trip.TripSeat;
import com.example.Vi_vu_Viet.Security.JwtService;
import com.example.Vi_vu_Viet.Util.Enums.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CustomerApiTests {
    @Autowired MockMvc mvc;
    @Autowired EntityManager entityManager;
    @Autowired JwtService jwtService;

    private UserAccount customer;
    private UserAccount otherCustomer;
    private String authorization;
    private BusCompany company;
    private Trip trip;
    private Trip otherTrip;
    private final LocalDateTime now = LocalDateTime.now().withNano(0);

    @BeforeEach
    void setUp() {
        customer = user("customer@example.com", "0901234567");
        customer.setPhone_verified_at(now.minusDays(1));
        otherCustomer = user("other@example.com", "0909876543");
        AuthSession session = new AuthSession();
        session.setUser_id(customer);
        session.setRefresh_token_hash("test-refresh-token-hash");
        session.setExpires_at(now.plusDays(1));
        entityManager.persist(session);
        authorization = "Bearer " + jwtService.createAccessToken(customer.getId(), session.getId());

        company = new BusCompany();
        company.setName("Vi Vu Test");
        company.setSlug("vi-vu-test");
        company.setHotline("19001234");
        company.setTimezone("Asia/Ho_Chi_Minh");
        company.setStatus(BusCompanyStatus.ACTIVE);
        entityManager.persist(company);
        Area origin = area("HN", "Ha Noi");
        Area destination = area("DN", "Da Nang");
        trip = trip("TRIP-ONE", route("HN-DN", "Ha Noi - Da Nang", origin, destination));
        otherTrip = trip("TRIP-TWO", route("DN-HN", "Da Nang - Ha Noi", destination, origin));
        entityManager.flush();
    }

    @Test
    void customerEndpointsRequireAuthentication() throws Exception {
        mvc.perform(get("/api/v1/me/profile")).andExpect(status().isUnauthorized());
        mvc.perform(patch("/api/v1/me/profile").contentType(MediaType.APPLICATION_JSON)
                .content("{\"wsRequest\":{\"fullName\":\"New name\"}}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/me/bookings")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/me/bookings/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profileReturnsCurrentCustomerWithoutCredentials() throws Exception {
        mvc.perform(get("/api/v1/me/profile").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.id").value(customer.getId().toString()))
                .andExpect(jsonPath("$.wsResponse.email").value("customer@example.com"))
                .andExpect(jsonPath("$.wsResponse.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.wsResponse.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.wsResponse.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.wsResponse.password_hash").doesNotExist())
                .andExpect(jsonPath("$.wsResponse.passwordHash").doesNotExist());
    }

    @Test
    void partialProfileUpdateNormalizesValuesAndPreservesOtherFields() throws Exception {
        mvc.perform(patch("/api/v1/me/profile").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"wsRequest":{"fullName":"  Nguyen Van B  ","email":" CUSTOMER@EXAMPLE.COM "}}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.fullName").value("Nguyen Van B"))
                .andExpect(jsonPath("$.wsResponse.email").value("customer@example.com"))
                .andExpect(jsonPath("$.wsResponse.phone").value("0901234567"))
                .andExpect(jsonPath("$.wsResponse.phoneVerifiedAt").isNotEmpty());
        entityManager.clear();
        UserAccount saved = entityManager.find(UserAccount.class, customer.getId());
        assertThat(saved.getFull_name()).isEqualTo("Nguyen Van B");
        assertThat(saved.getPassword_hash()).isEqualTo("test-password-hash");
        assertThat(saved.getAccount_status()).isEqualTo(UserAccountStatus.ACTIVE);
        assertThat(entityManager.find(UserAccount.class, otherCustomer.getId()).getFull_name())
                .isEqualTo("Original name");
    }

    @Test
    void changingPhoneClearsVerificationButKeepingSamePhoneDoesNot() throws Exception {
        mvc.perform(patch("/api/v1/me/profile").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"wsRequest\":{\"phone\":\"(090) 123-4567\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.phoneVerifiedAt").isNotEmpty());
        mvc.perform(patch("/api/v1/me/profile").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"wsRequest\":{\"phone\":\"+84 (912) 345-678\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.phone").value("+84912345678"))
                .andExpect(jsonPath("$.wsResponse.phoneVerifiedAt").isEmpty());
        entityManager.clear();
        assertThat(entityManager.find(UserAccount.class, customer.getId()).getPhone_verified_at()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"email\":\" OTHER@EXAMPLE.COM \"}", "{\"phone\":\"090 987-6543\"}"})
    void profileRejectsAnotherCustomersContact(String fields) throws Exception {
        mvc.perform(patch("/api/v1/me/profile").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content("{\"wsRequest\":" + fields + "}"))
                .andExpect(status().isConflict());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"fullName\":null}", "{\"fullName\":\"  \"}",
            "{\"email\":\"invalid\"}", "{\"email\":\"\"}", "{\"phone\":\"123\"}"})
    void profileRejectsEmptyOrInvalidPatch(String fields) throws Exception {
        mvc.perform(patch("/api/v1/me/profile").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content("{\"wsRequest\":" + fields + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookingPagesAreOrderedAndRestrictedToCurrentCustomer() throws Exception {
        Booking older = booking(customer, trip, "BK-OLDER", BookingStatus.CONFIRMED, now.minusHours(2));
        Booking newer = booking(customer, trip, "BK-NEWER", BookingStatus.CONFIRMED, now.minusHours(1));
        Booking foreign = booking(otherCustomer, trip, "BK-FOREIGN", BookingStatus.CONFIRMED, now);
        mvc.perform(get("/api/v1/me/bookings").header("Authorization", authorization).param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.content.length()").value(1))
                .andExpect(jsonPath("$.wsResponse.content[0].id").value(newer.getId().toString()))
                .andExpect(jsonPath("$.wsResponse.totalElements").value(2))
                .andExpect(jsonPath("$.wsResponse.totalPages").value(2))
                .andExpect(jsonPath("$.wsResponse.page").value(0))
                .andExpect(jsonPath("$.wsResponse.size").value(1))
                .andExpect(jsonPath("$.wsResponse.first").value(true))
                .andExpect(jsonPath("$.wsResponse.last").value(false));
        mvc.perform(get("/api/v1/me/bookings").header("Authorization", authorization)
                .param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.content[0].id").value(older.getId().toString()))
                .andExpect(jsonPath("$.wsResponse.last").value(true));
        mvc.perform(get("/api/v1/me/bookings/{id}", foreign.getId()).header("Authorization", authorization))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/me/bookings").header("Authorization", authorization).param("q", "BK-FOREIGN"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.wsResponse.totalElements").value(0));
    }

    @Test
    void filtersIncludeExpiredHoldsBeforePaginationAndCombineWithRouteAndQuery() throws Exception {
        Booking overdue = booking(customer, trip, "BK-OVERDUE", BookingStatus.HOLDING, now.minusMinutes(1));
        ticket(overdue, "TK-OVERDUE", "Nguyen Van A");
        booking(customer, trip, "BK-EXPIRED", BookingStatus.EXPIRED, now.minusHours(1));
        Booking holding = booking(customer, trip, "BK-HOLDING", BookingStatus.HOLDING, now.plusMinutes(5));
        booking(customer, otherTrip, "BK-OTHER-ROUTE", BookingStatus.EXPIRED, now.minusHours(1));
        booking(customer, trip, "BK-CANCELLED", BookingStatus.CANCELLED, now.plusMinutes(5));
        mvc.perform(get("/api/v1/me/bookings").header("Authorization", authorization)
                .param("status", "EXPIRED").param("routeId", trip.getRoute_id().getId().toString())
                .param("q", "bk-").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.totalElements").value(2))
                .andExpect(jsonPath("$.wsResponse.totalPages").value(2))
                .andExpect(jsonPath("$.wsResponse.content[0].status").value("EXPIRED"));
        mvc.perform(get("/api/v1/me/bookings").header("Authorization", authorization).param("status", "HOLDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.totalElements").value(1))
                .andExpect(jsonPath("$.wsResponse.content[0].id").value(holding.getId().toString()));
        mvc.perform(get("/api/v1/me/bookings/{id}", overdue.getId()).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.status").value("EXPIRED"))
                .andExpect(jsonPath("$.wsResponse.tickets[0].status").value("EXPIRED"));
    }

    @ParameterizedTest
    @ValueSource(strings = {" bk-test ", "tk-test1", "TRIP-ONE", "hn-dn", "ha noi - da nang",
            "vi vu test", "nguyen van a"})
    void keywordSearchFindsBookingAndTicketFieldsWithoutDuplicateBookings(String q) throws Exception {
        Booking booking = booking(customer, trip, "BK-TEST", BookingStatus.CONFIRMED, now.minusMinutes(1));
        ticket(booking, "TK-TEST1", "Nguyen Van A");
        ticket(booking, "TK-TEST2", "Nguyen Van A");
        mvc.perform(get("/api/v1/me/bookings").header("Authorization", authorization)
                .param("q", q).param("status", "CONFIRMED").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.totalElements").value(1))
                .andExpect(jsonPath("$.wsResponse.content.length()").value(1))
                .andExpect(jsonPath("$.wsResponse.content[0].tickets.length()").value(2));
    }

    @Test
    void keywordWildcardsAreLiteralAndOutOfRangePageIsEmpty() throws Exception {
        Booking matching = booking(customer, trip, "BK-MATCH", BookingStatus.CONFIRMED, now);
        ticket(matching, "TK-MATCH", "100%_! Genuine");
        Booking other = booking(customer, trip, "BK-OTHER", BookingStatus.CONFIRMED, now);
        ticket(other, "TK-OTHER", "100 percent");
        mvc.perform(get("/api/v1/me/bookings").header("Authorization", authorization).param("q", "%_!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.totalElements").value(1))
                .andExpect(jsonPath("$.wsResponse.content[0].id").value(matching.getId().toString()));
        mvc.perform(get("/api/v1/me/bookings").header("Authorization", authorization)
                .param("page", "10").param("size", "1").param("q", " "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.content").isEmpty())
                .andExpect(jsonPath("$.wsResponse.totalElements").value(2));
    }

    @ParameterizedTest
    @CsvSource({"page,-1", "page,abc", "page,2147483647", "size,0", "size,101", "status,INVALID", "routeId,INVALID"})
    void bookingListRejectsInvalidParameters(String parameter, String value) throws Exception {
        mvc.perform(get("/api/v1/me/bookings").header("Authorization", authorization).param(parameter, value))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookingListRejectsOversizedKeyword() throws Exception {
        mvc.perform(get("/api/v1/me/bookings").header("Authorization", authorization).param("q", "a".repeat(201)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ticketDetailContainsDisplayDataAndPreservesBookedFare() throws Exception {
        Booking booking = booking(customer, trip, "BK-VIEW-TICKET", BookingStatus.CONFIRMED, now.minusMinutes(1));
        Ticket ticket = ticket(booking, "TK-VIEW-TICKET", "Nguyen Van A");
        trip.getRoute_id().setBase_fare_amount(700000L);
        entityManager.flush();
        mvc.perform(get("/api/v1/me/bookings/{id}", booking.getId()).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.bookingCode").value("BK-VIEW-TICKET"))
                .andExpect(jsonPath("$.wsResponse.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.wsResponse.confirmedAt").isNotEmpty())
                .andExpect(jsonPath("$.wsResponse.contactName").value("Contact name"))
                .andExpect(jsonPath("$.wsResponse.contactPhone").value("0901234567"))
                .andExpect(jsonPath("$.wsResponse.quotedTotalAmount").value(250000))
                .andExpect(jsonPath("$.wsResponse.currency").value("VND"))
                .andExpect(jsonPath("$.wsResponse.trip.id").value(trip.getId().toString()))
                .andExpect(jsonPath("$.wsResponse.trip.tripCode").value("TRIP-ONE"))
                .andExpect(jsonPath("$.wsResponse.trip.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.wsResponse.trip.departureAt").isNotEmpty())
                .andExpect(jsonPath("$.wsResponse.trip.expectedArrivalAt").isNotEmpty())
                .andExpect(jsonPath("$.wsResponse.trip.companyName").value("Vi Vu Test"))
                .andExpect(jsonPath("$.wsResponse.trip.companyHotline").value("19001234"))
                .andExpect(jsonPath("$.wsResponse.trip.timezone").value("Asia/Ho_Chi_Minh"))
                .andExpect(jsonPath("$.wsResponse.trip.routeId").value(trip.getRoute_id().getId().toString()))
                .andExpect(jsonPath("$.wsResponse.trip.originAreaName").value("Ha Noi"))
                .andExpect(jsonPath("$.wsResponse.trip.destinationAreaName").value("Da Nang"))
                .andExpect(jsonPath("$.wsResponse.tickets[0].ticketCode").value("TK-VIEW-TICKET"))
                .andExpect(jsonPath("$.wsResponse.tickets[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$.wsResponse.tickets[0].passengerName").value("Nguyen Van A"))
                .andExpect(jsonPath("$.wsResponse.tickets[0].seatId").value(ticket.getTrip_seat_id().getId().toString()))
                .andExpect(jsonPath("$.wsResponse.tickets[0].seatCode").value(ticket.getSeat_label_snapshot()))
                .andExpect(jsonPath("$.wsResponse.tickets[0].seatClass").value("STANDARD"))
                .andExpect(jsonPath("$.wsResponse.tickets[0].pickupAddress").value("12 Nguyen Trai, Ha Noi"))
                .andExpect(jsonPath("$.wsResponse.tickets[0].dropoffAddress").value("Ben xe Da Nang"))
                .andExpect(jsonPath("$.wsResponse.tickets[0].fareAmount").value(250000));
    }

    private UserAccount user(String email, String phone) {
        UserAccount user = new UserAccount();
        user.setFull_name("Original name");
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword_hash("test-password-hash");
        user.setAccount_status(UserAccountStatus.ACTIVE);
        entityManager.persist(user);
        return user;
    }

    private Area area(String code, String name) {
        Area area = new Area();
        area.setCode(code);
        area.setName(name);
        area.setType("PROVINCE");
        area.setStatus("ACTIVE");
        entityManager.persist(area);
        return area;
    }

    private Route route(String code, String name, Area origin, Area destination) {
        Route route = new Route();
        route.setCompany_id(company);
        route.setCode(code);
        route.setName(name);
        route.setOrigin_area_id(origin);
        route.setDestination_area_id(destination);
        route.setBase_fare_amount(250000L);
        route.setStatus(RouteStatus.ACTIVE);
        entityManager.persist(route);
        return route;
    }

    private Trip trip(String code, Route route) {
        Trip trip = new Trip();
        trip.setCompany_id(company);
        trip.setRoute_id(route);
        trip.setTrip_code(code);
        trip.setDeparture_at(now.plusDays(1));
        trip.setExpected_arrival_at(now.plusDays(1).plusHours(12));
        trip.setBooking_cutoff_at(now.plusHours(23));
        trip.setStatus(TripStatus.SCHEDULED);
        trip.setSales_status(TripSalesStatus.OPEN);
        entityManager.persist(trip);
        return trip;
    }

    private Booking booking(UserAccount owner, Trip trip, String code, BookingStatus status, LocalDateTime expiresAt) {
        Booking booking = new Booking();
        booking.setCompany_id(company);
        booking.setTrip_id(trip);
        booking.setCustomer_user_id(owner);
        booking.setBooking_code(code);
        booking.setContact_name("Contact name");
        booking.setContact_phone("0901234567");
        booking.setStatus(status);
        booking.setHold_expires_at(expiresAt);
        booking.setQuoted_total_amount(250000L);
        booking.setCurrency("VND");
        if (status == BookingStatus.CONFIRMED) booking.setConfirmed_at(now.minusMinutes(2));
        entityManager.persist(booking);
        entityManager.flush();
        return booking;
    }

    private Ticket ticket(Booking booking, String code, String passengerName) {
        TripSeat seat = new TripSeat();
        seat.setCompany_id(company);
        seat.setTrip_id(booking.getTrip_id());
        seat.setSeat_code("S" + code);
        seat.setSeat_class("STANDARD");
        entityManager.persist(seat);
        Ticket ticket = new Ticket();
        ticket.setCompany_id(company);
        ticket.setTrip_id(booking.getTrip_id());
        ticket.setBooking_id(booking);
        ticket.setTrip_seat_id(seat);
        ticket.setTicket_code(code);
        ticket.setPassenger_name(passengerName);
        ticket.setPickup_address("12 Nguyen Trai, Ha Noi");
        ticket.setDropoff_address("Ben xe Da Nang");
        ticket.setSeat_label_snapshot(seat.getSeat_code());
        ticket.setFare_amount(250000L);
        ticket.setStatus(TicketStatus.valueOf(booking.getStatus().name()));
        entityManager.persist(ticket);
        entityManager.flush();
        return ticket;
    }
}
