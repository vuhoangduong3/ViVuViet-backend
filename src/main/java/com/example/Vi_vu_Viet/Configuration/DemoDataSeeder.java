package com.example.Vi_vu_Viet.Configuration;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Trip.Area;
import com.example.Vi_vu_Viet.Entity.Trip.Booking;
import com.example.Vi_vu_Viet.Entity.Trip.BusCompany;
import com.example.Vi_vu_Viet.Entity.Trip.CompanyMember;
import com.example.Vi_vu_Viet.Entity.Trip.CompanyMemberRole;
import com.example.Vi_vu_Viet.Entity.Trip.DriverProfile;
import com.example.Vi_vu_Viet.Entity.Trip.Route;
import com.example.Vi_vu_Viet.Entity.Trip.Ticket;
import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Entity.Trip.TripAssignment;
import com.example.Vi_vu_Viet.Entity.Trip.TripSeat;
import com.example.Vi_vu_Viet.Entity.Trip.Vehicle;
import com.example.Vi_vu_Viet.Repository.Auth.CompanyMemberRoleRepository;
import com.example.Vi_vu_Viet.Repository.Auth.UserRepository;
import com.example.Vi_vu_Viet.Repository.Trip.AreaRepository;
import com.example.Vi_vu_Viet.Repository.Trip.BookingRepository;
import com.example.Vi_vu_Viet.Repository.Trip.BusCompanyRepository;
import com.example.Vi_vu_Viet.Repository.Trip.CompanyMemberRepository;
import com.example.Vi_vu_Viet.Repository.Trip.DriverProfileRepository;
import com.example.Vi_vu_Viet.Repository.Trip.RouteRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TicketRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripAssignmentRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripSeatRepository;
import com.example.Vi_vu_Viet.Repository.Trip.VehicleRepository;
import com.example.Vi_vu_Viet.Util.Enums.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Profile({"default", "docker"})
@ConditionalOnProperty(name = "app.seed.demo-data", havingValue = "true")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements ApplicationRunner {

    private static final String COMPANY_SLUG = "vi-vu-viet-demo";
    private static final long TICKET_FARE = 450_000L;

    UserRepository userRepository;
    BusCompanyRepository busCompanyRepository;
    CompanyMemberRepository companyMemberRepository;
    CompanyMemberRoleRepository companyMemberRoleRepository;
    DriverProfileRepository driverProfileRepository;
    AreaRepository areaRepository;
    RouteRepository routeRepository;
    VehicleRepository vehicleRepository;
    TripRepository tripRepository;
    TripAssignmentRepository tripAssignmentRepository;
    TripSeatRepository tripSeatRepository;
    BookingRepository bookingRepository;
    TicketRepository ticketRepository;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (busCompanyRepository.findBySlug(COMPANY_SLUG).isPresent()) {
            log.info("Demo data already exists; skipping seed.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        BusCompany company = createCompany();
        UserAccount admin = createUser("Demo Admin", "admin@vivuviet.local", "0900000001", "Admin123!");
        UserAccount driver = createUser("Demo Driver", "driver@vivuviet.local", "0900000002", "Driver123!");
        UserAccount customer = createUser("Demo Customer", "customer@vivuviet.local", "0900000003", "Customer123!");
        UserAccount secondCustomer = createUser("Demo Passenger", "passenger@vivuviet.local", "0900000004", "Customer123!");

        CompanyMember adminMembership = createMembership(company, admin, CompanyMemberRoleType.COMPANY_ADMIN, now);
        CompanyMember driverMembership = createMembership(company, driver, CompanyMemberRoleType.DRIVER, now);
        createDriverProfile(company, driverMembership);

        Area origin = createArea("HN", "Hà Nội", "CITY");
        Area destination = createArea("DAD", "Đà Nẵng", "CITY");
        Route route = createRoute(company, origin, destination);
        Vehicle vehicle = createVehicle(company);

        LocalDateTime pastDeparture = now.minusDays(3).withHour(6).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime inProgressDeparture = now.minusHours(2);
        LocalDateTime nextDay = LocalDate.now().plusDays(1).atTime(8, 0);

        Trip completedTrip = createTrip(company, route, admin, "DEMO-TRIP-001", pastDeparture,
                TripStatus.COMPLETED, TripSalesStatus.CLOSED, pastDeparture, pastDeparture.plusMinutes(900));
        Trip inProgressTrip = createTrip(company, route, admin, "DEMO-TRIP-002", inProgressDeparture,
                TripStatus.IN_PROGRESS, TripSalesStatus.CLOSED, inProgressDeparture, null);
        Trip scheduledTrip = createTrip(company, route, admin, "DEMO-TRIP-003", nextDay,
                TripStatus.SCHEDULED, TripSalesStatus.OPEN, null, null);

        assignTrip(company, completedTrip, vehicle, driver, admin, DriverAssignmentStatus.COMPLETED);
        assignTrip(company, inProgressTrip, vehicle, driver, admin, DriverAssignmentStatus.ACCEPTED);
        assignTrip(company, scheduledTrip, vehicle, driver, admin, DriverAssignmentStatus.ASSIGNED);

        Map<String, TripSeat> completedSeats = createSeats(company, completedTrip);
        Map<String, TripSeat> inProgressSeats = createSeats(company, inProgressTrip);
        Map<String, TripSeat> scheduledSeats = createSeats(company, scheduledTrip);

        Booking completedBooking = createBooking(company, completedTrip, customer,
                "DEMO-BOOKING-001", "Nguyễn Văn Demo", "0900000003", pastDeparture.minusDays(1), 2);
        createTicket(company, completedTrip, completedBooking, completedSeats.get("1A"),
                "DEMO-TICKET-001", "Nguyễn Văn Demo", "1A", PassengerTripStatus.DROPPED_OFF,
                pastDeparture.plusMinutes(15), pastDeparture.plusMinutes(900));
        createTicket(company, completedTrip, completedBooking, completedSeats.get("1B"),
                "DEMO-TICKET-002", "Trần Thị Demo", "1B", PassengerTripStatus.DROPPED_OFF,
                pastDeparture.plusMinutes(15), pastDeparture.plusMinutes(900));

        Booking inProgressBooking = createBooking(company, inProgressTrip, secondCustomer,
                "DEMO-BOOKING-002", "Demo Passenger", "0900000004", now.minusHours(4), 1);
        createTicket(company, inProgressTrip, inProgressBooking, inProgressSeats.get("1A"),
                "DEMO-TICKET-003", "Demo Passenger", "1A", PassengerTripStatus.PICKED_UP,
                inProgressDeparture.plusMinutes(15), null);

        Booking scheduledBooking = createBooking(company, scheduledTrip, customer,
                "DEMO-BOOKING-003", "Nguyễn Văn Demo", "0900000003", now, 1);
        createTicket(company, scheduledTrip, scheduledBooking, scheduledSeats.get("1A"),
                "DEMO-TICKET-004", "Nguyễn Văn Demo", "1A", PassengerTripStatus.WAITING_PICKUP,
                null, null);

        log.info("Seeded demo data for company '{}' (admin {}, driver {}).",
                company.getSlug(), admin.getEmail(), driver.getEmail());
    }

    private BusCompany createCompany() {
        BusCompany company = new BusCompany();
        company.setName("Vi Vu Viet Demo Bus");
        company.setSlug(COMPANY_SLUG);
        company.setHotline("19001234");
        company.setTimezone("Asia/Ho_Chi_Minh");
        company.setStatus(BusCompanyStatus.ACTIVE);
        return busCompanyRepository.save(company);
    }

    private UserAccount createUser(String fullName, String email, String phone, String rawPassword) {
        return userRepository.findByLoginIdentifier(email).orElseGet(() -> {
            UserAccount user = new UserAccount();
            user.setFull_name(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            user.setPassword_hash(passwordEncoder.encode(rawPassword));
            user.setAccount_status(UserAccountStatus.ACTIVE);
            return userRepository.save(user);
        });
    }

    private CompanyMember createMembership(BusCompany company, UserAccount user,
                                           CompanyMemberRoleType roleType, LocalDateTime joinedAt) {
        CompanyMember membership = new CompanyMember();
        membership.setCompany_id(company);
        membership.setUser_id(user);
        membership.setStatus(CompanyMemberStatus.ACTIVE);
        membership.setJoined_at(joinedAt);
        membership = companyMemberRepository.save(membership);

        CompanyMemberRole role = new CompanyMemberRole();
        role.setCompany_id(company);
        role.setMembership_id(membership);
        role.setRole(roleType);
        companyMemberRoleRepository.save(role);
        return membership;
    }

    private void createDriverProfile(BusCompany company, CompanyMember membership) {
        DriverProfile profile = new DriverProfile();
        profile.setCompany_id(company);
        profile.setMembership_id(membership);
        profile.setEmployee_code("DEMO-DRIVER-001");
        profile.setLicense_number("DEMO-LICENSE-001");
        profile.setLicense_class("D");
        profile.setLicense_expires_on(LocalDate.now().plusYears(3));
        profile.setStatus(DriverProfileStatus.ACTIVE);
        driverProfileRepository.save(profile);
    }

    private Area createArea(String code, String name, String type) {
        Area area = new Area();
        area.setCode(code);
        area.setName(name);
        area.setType(type);
        area.setStatus("ACTIVE");
        return areaRepository.save(area);
    }

    private Route createRoute(BusCompany company, Area origin, Area destination) {
        Route route = new Route();
        route.setCompany_id(company);
        route.setCode("HN-DAD-DEMO");
        route.setName("Hà Nội - Đà Nẵng");
        route.setOrigin_area_id(origin);
        route.setDestination_area_id(destination);
        route.setDistance_km(new java.math.BigDecimal("770.0"));
        route.setDuration_minutes(900);
        route.setBase_fare_amount(TICKET_FARE);
        route.setStatus(RouteStatus.ACTIVE);
        return routeRepository.save(route);
    }

    private Vehicle createVehicle(BusCompany company) {
        Vehicle vehicle = new Vehicle();
        vehicle.setCompany_id(company);
        vehicle.setPlate_normalized("51B12345");
        vehicle.setVehicle_model("Thaco Mobihome 16 chỗ");
        vehicle.setPassenger_capacity(16);
        vehicle.setStatus(VehicleStatus.ACTIVE);
        return vehicleRepository.save(vehicle);
    }

    private Trip createTrip(BusCompany company, Route route, UserAccount admin, String code,
                            LocalDateTime departure, TripStatus status, TripSalesStatus salesStatus,
                            LocalDateTime actualDeparture, LocalDateTime actualArrival) {
        Trip trip = new Trip();
        trip.setCompany_id(company);
        trip.setRoute_id(route);
        trip.setTrip_code(code);
        trip.setDeparture_at(departure);
        trip.setExpected_arrival_at(departure.plusMinutes(route.getDuration_minutes()));
        trip.setActual_departure_at(actualDeparture);
        trip.setActual_arrival_at(actualArrival);
        trip.setBooking_cutoff_at(departure.minusHours(1));
        trip.setStatus(status);
        trip.setSales_status(salesStatus);
        trip.setCreated_by(admin);
        return tripRepository.save(trip);
    }

    private void assignTrip(BusCompany company, Trip trip, Vehicle vehicle, UserAccount driver,
                            UserAccount admin, DriverAssignmentStatus status) {
        LocalDateTime assignedAt = trip.getDeparture_at().minusDays(2);
        TripAssignment assignment = new TripAssignment();
        assignment.setCompany_id(company);
        assignment.setTrip_id(trip);
        assignment.setVehicle_id(vehicle);
        assignment.setDriver_user_id(driver);
        assignment.setAssigned_by(admin);
        assignment.setAssigned_at(assignedAt);
        assignment.setAssignment_status(status);
        if (status != DriverAssignmentStatus.ASSIGNED) {
            assignment.setAccepted_at(assignedAt.plusMinutes(30));
        }
        tripAssignmentRepository.save(assignment);
    }

    private Map<String, TripSeat> createSeats(BusCompany company, Trip trip) {
        Map<String, TripSeat> seats = new HashMap<>();
        for (int row = 1; row <= 4; row++) {
            for (char column = 'A'; column <= 'D'; column++) {
                String code = row + String.valueOf(column);
                TripSeat seat = new TripSeat();
                seat.setCompany_id(company);
                seat.setTrip_id(trip);
                seat.setSeat_code(code);
                seat.setSeat_class("STANDARD");
                seats.put(code, tripSeatRepository.save(seat));
            }
        }
        return seats;
    }

    private Booking createBooking(BusCompany company, Trip trip, UserAccount customer, String code,
                                  String contactName, String contactPhone, LocalDateTime confirmedAt,
                                  int passengerCount) {
        Booking booking = new Booking();
        booking.setCompany_id(company);
        booking.setTrip_id(trip);
        booking.setCustomer_user_id(customer);
        booking.setBooking_code(code);
        booking.setContact_name(contactName);
        booking.setContact_phone(contactPhone);
        booking.setContact_email(customer.getEmail());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setHold_expires_at(trip.getDeparture_at().minusHours(1));
        booking.setQuoted_total_amount(TICKET_FARE * passengerCount);
        booking.setCurrency("VND");
        booking.setConfirmed_at(confirmedAt);
        return bookingRepository.save(booking);
    }

    private void createTicket(BusCompany company, Trip trip, Booking booking, TripSeat seat,
                              String code, String passengerName, String seatLabel,
                              PassengerTripStatus passengerStatus, LocalDateTime boardedAt,
                              LocalDateTime completedAt) {
        Ticket ticket = new Ticket();
        ticket.setCompany_id(company);
        ticket.setTrip_id(trip);
        ticket.setBooking_id(booking);
        ticket.setTrip_seat_id(seat);
        ticket.setTicket_code(code);
        ticket.setPassenger_name(passengerName);
        ticket.setPassenger_phone(booking.getContact_phone());
        ticket.setPickup_address("Bến xe Mỹ Đình, Hà Nội");
        ticket.setDropoff_address("Bến xe trung tâm Đà Nẵng");
        ticket.setSeat_label_snapshot(seatLabel);
        ticket.setFare_amount(TICKET_FARE);
        ticket.setStatus(TicketStatus.CONFIRMED);
        ticket.setPassenger_trip_status(passengerStatus);
        ticket.setBoarded_at(boardedAt);
        ticket.setCompleted_at(completedAt);
        ticketRepository.save(ticket);
    }
}
