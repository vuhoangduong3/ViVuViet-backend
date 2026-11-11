package com.example.Vi_vu_Viet;

import com.example.Vi_vu_Viet.Entity.Auth.AuthSession;
import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Trip.Area;
import com.example.Vi_vu_Viet.Entity.Trip.Booking;
import com.example.Vi_vu_Viet.Entity.Trip.BusCompany;
import com.example.Vi_vu_Viet.Entity.Trip.CompanyMember;
import com.example.Vi_vu_Viet.Entity.Trip.CompanyMemberRole;
import com.example.Vi_vu_Viet.Entity.Trip.Route;
import com.example.Vi_vu_Viet.Entity.Trip.Ticket;
import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Entity.Trip.TripAssignment;
import com.example.Vi_vu_Viet.Entity.Trip.TripSeat;
import com.example.Vi_vu_Viet.Entity.Trip.Vehicle;
import com.example.Vi_vu_Viet.Security.JwtService;
import com.example.Vi_vu_Viet.Util.Enums.BookingStatus;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberRoleType;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberStatus;
import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import com.example.Vi_vu_Viet.Util.Enums.RouteStatus;
import com.example.Vi_vu_Viet.Util.Enums.TicketStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripSalesStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus;
import com.example.Vi_vu_Viet.Util.Enums.VehicleStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DashboardApiTests {

    @Autowired MockMvc mvc;
    @Autowired EntityManager entityManager;
    @Autowired JwtService jwtService;

    private BusCompany company;
    private UserAccount admin;
    private UserAccount driver;
    private Vehicle vehicle;
    private String authorization;
    private LocalDate firstDepartureDate;
    private LocalDate secondDepartureDate;

    @BeforeEach
    void setUp() {
        company = new BusCompany();
        company.setName("Dashboard Test Company");
        company.setSlug("dashboard-test-company");
        company.setHotline("19001234");
        company.setTimezone("Asia/Ho_Chi_Minh");
        company.setStatus(BusCompanyStatus.ACTIVE);
        entityManager.persist(company);

        admin = user("dashboard-admin@example.com", "0900000001");
        CompanyMember membership = new CompanyMember();
        membership.setCompany_id(company);
        membership.setUser_id(admin);
        membership.setStatus(CompanyMemberStatus.ACTIVE);
        membership.setJoined_at(LocalDateTime.now());
        entityManager.persist(membership);
        CompanyMemberRole adminRole = new CompanyMemberRole();
        adminRole.setCompany_id(company);
        adminRole.setMembership_id(membership);
        adminRole.setRole(CompanyMemberRoleType.COMPANY_ADMIN);
        entityManager.persist(adminRole);

        AuthSession session = new AuthSession();
        session.setUser_id(admin);
        session.setRefresh_token_hash("dashboard-test-refresh-token-hash");
        session.setExpires_at(LocalDateTime.now().plusDays(1));
        entityManager.persist(session);
        entityManager.flush();
        authorization = "Bearer " + jwtService.createAccessToken(admin.getId(), session.getId());

        driver = user("dashboard-driver@example.com", "0900000002");

        vehicle = new Vehicle();
        vehicle.setCompany_id(company);
        vehicle.setPlate_normalized("51A-12345");
        vehicle.setVehicle_model("Test Bus");
        vehicle.setPassenger_capacity(30);
        vehicle.setStatus(VehicleStatus.ACTIVE);
        entityManager.persist(vehicle);

        LocalDate today = LocalDate.now();
        firstDepartureDate = today.plusDays(1);
        secondDepartureDate = today.plusDays(2);
        createTrip("DASH-SCHEDULED", firstDepartureDate, TripStatus.SCHEDULED, "BK-DASH-ONE", "TK-DASH-ONE", 250_000L);
        createTrip("DASH-COMPLETED", secondDepartureDate, TripStatus.COMPLETED, "BK-DASH-TWO", "TK-DASH-TWO", 300_000L);
        entityManager.flush();
    }

    @Test
    void dashboardAndRevenueEndpointsWorkWithAndWithoutDateFilters() throws Exception {
        mvc.perform(get(summaryPath()).header("Authorization", authorization).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.totalTrips").value(2))
                .andExpect(jsonPath("$.wsResponse.confirmedTickets").value(2))
                .andExpect(jsonPath("$.wsResponse.confirmedTicketValue").value(550_000))
                .andExpect(jsonPath("$.wsResponse.actualCollected").value(false));

        mvc.perform(get(summaryPath()).header("Authorization", authorization).param("from", firstDepartureDate.toString())
                        .param("to", firstDepartureDate.toString()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.totalTrips").value(1))
                .andExpect(jsonPath("$.wsResponse.confirmedTicketValue").value(250_000));

        mvc.perform(get(companyPath("/dashboard/trips")).header("Authorization", authorization)
                        .param("from", firstDepartureDate.toString()).param("to", secondDepartureDate.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.length()").value(2))
                .andExpect(jsonPath("$.wsResponse[0].totalTrips").value(1));

        mvc.perform(get(companyPath("/dashboard/top-drivers")).header("Authorization", authorization)
                        .param("from", firstDepartureDate.toString()).param("to", secondDepartureDate.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.length()").value(1))
                .andExpect(jsonPath("$.wsResponse[0].driverId").value(driver.getId().toString()))
                .andExpect(jsonPath("$.wsResponse[0].assignedTrips").value(2))
                .andExpect(jsonPath("$.wsResponse[0].completedTrips").value(1));

        mvc.perform(get(companyPath("/reports/revenue")).header("Authorization", authorization)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.wsResponse.tickets").value(2))
                .andExpect(jsonPath("$.wsResponse.ticketValue").value(550_000))
                .andExpect(jsonPath("$.wsResponse.currency").value("VND"))
                .andExpect(jsonPath("$.wsResponse.actualCollected").value(false));
    }

    private void createTrip(String tripCode, LocalDate departureDate, TripStatus status,
                            String bookingCode, String ticketCode, long fare) {
        Area origin = new Area();
        origin.setCode("DASH-ORIGIN-" + tripCode);
        origin.setName("Origin");
        origin.setType("PROVINCE");
        origin.setStatus("ACTIVE");
        entityManager.persist(origin);

        Area destination = new Area();
        destination.setCode("DASH-DEST-" + tripCode);
        destination.setName("Destination");
        destination.setType("PROVINCE");
        destination.setStatus("ACTIVE");
        entityManager.persist(destination);

        Route route = new Route();
        route.setCompany_id(company);
        route.setCode("ROUTE-" + tripCode);
        route.setName("Test route");
        route.setOrigin_area_id(origin);
        route.setDestination_area_id(destination);
        route.setBase_fare_amount(fare);
        route.setStatus(RouteStatus.ACTIVE);
        entityManager.persist(route);

        LocalDateTime departureAt = departureDate.atTime(8, 0);
        Trip trip = new Trip();
        trip.setCompany_id(company);
        trip.setRoute_id(route);
        trip.setTrip_code(tripCode);
        trip.setDeparture_at(departureAt);
        trip.setExpected_arrival_at(departureAt.plusHours(8));
        trip.setBooking_cutoff_at(departureAt.minusMinutes(30));
        trip.setStatus(status);
        trip.setSales_status(TripSalesStatus.OPEN);
        entityManager.persist(trip);

        TripSeat seat = new TripSeat();
        seat.setCompany_id(company);
        seat.setTrip_id(trip);
        seat.setSeat_code("A1");
        seat.setSeat_class("STANDARD");
        entityManager.persist(seat);

        Booking booking = new Booking();
        booking.setCompany_id(company);
        booking.setTrip_id(trip);
        booking.setCustomer_user_id(driver);
        booking.setBooking_code(bookingCode);
        booking.setContact_name("Test customer");
        booking.setContact_phone("0901234567");
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setHold_expires_at(LocalDateTime.now().plusDays(1));
        booking.setQuoted_total_amount(fare);
        booking.setCurrency("VND");
        booking.setConfirmed_at(LocalDateTime.now());
        entityManager.persist(booking);

        Ticket ticket = new Ticket();
        ticket.setCompany_id(company);
        ticket.setTrip_id(trip);
        ticket.setBooking_id(booking);
        ticket.setTrip_seat_id(seat);
        ticket.setTicket_code(ticketCode);
        ticket.setPassenger_name("Test passenger");
        ticket.setPickup_address("Origin address");
        ticket.setDropoff_address("Destination address");
        ticket.setSeat_label_snapshot(seat.getSeat_code());
        ticket.setFare_amount(fare);
        ticket.setStatus(TicketStatus.CONFIRMED);
        entityManager.persist(ticket);

        TripAssignment assignment = new TripAssignment();
        assignment.setCompany_id(company);
        assignment.setTrip_id(trip);
        assignment.setVehicle_id(vehicle);
        assignment.setDriver_user_id(driver);
        assignment.setAssigned_at(LocalDateTime.now());
        assignment.setAssignment_status(DriverAssignmentStatus.ACCEPTED);
        entityManager.persist(assignment);
    }

    private UserAccount user(String email, String phone) {
        UserAccount account = new UserAccount();
        account.setFull_name("Dashboard Test User");
        account.setEmail(email);
        account.setPhone(phone);
        account.setPassword_hash("test-password-hash");
        account.setAccount_status(UserAccountStatus.ACTIVE);
        entityManager.persist(account);
        return account;
    }

    private String summaryPath() {
        return companyPath("/dashboard/summary");
    }

    private String companyPath(String suffix) {
        return "/api/v1/bus-companies/" + company.getId() + suffix;
    }
}
