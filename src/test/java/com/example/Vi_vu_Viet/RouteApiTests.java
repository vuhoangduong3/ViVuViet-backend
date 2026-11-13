package com.example.Vi_vu_Viet;

import com.example.Vi_vu_Viet.Entity.Auth.AuthSession;
import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Trip.Area;
import com.example.Vi_vu_Viet.Entity.Trip.BusCompany;
import com.example.Vi_vu_Viet.Entity.Trip.CompanyMember;
import com.example.Vi_vu_Viet.Entity.Trip.CompanyMemberRole;
import com.example.Vi_vu_Viet.Entity.Trip.Route;
import com.example.Vi_vu_Viet.Entity.Trip.Trip;
import com.example.Vi_vu_Viet.Repository.Trip.TripRepository;
import com.example.Vi_vu_Viet.Security.JwtService;
import com.example.Vi_vu_Viet.Service.Trip.RouteService;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberRoleType;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberStatus;
import com.example.Vi_vu_Viet.Util.Enums.RouteStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripSalesStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RouteApiTests {

    @Autowired MockMvc mvc;
    @Autowired EntityManager entityManager;
    @Autowired JwtService jwtService;
    @Autowired TripRepository tripRepository;
    @Autowired RouteService routeService;

    private BusCompany company;
    private BusCompany otherCompany;
    private Area origin;
    private Area destination;
    private Route route;
    private Route emptyRoute;
    private Route otherRoute;
    private String authorization;

    @BeforeEach
    void setUp() {
        company = company("route-test-company");
        otherCompany = company("route-test-other-company");

        UserAccount admin = new UserAccount();
        admin.setFull_name("Route Test Admin");
        admin.setEmail("route-admin@example.com");
        admin.setPhone("0900000091");
        admin.setPassword_hash("test-password-hash");
        admin.setAccount_status(UserAccountStatus.ACTIVE);
        entityManager.persist(admin);

        CompanyMember membership = new CompanyMember();
        membership.setCompany_id(company);
        membership.setUser_id(admin);
        membership.setStatus(CompanyMemberStatus.ACTIVE);
        membership.setJoined_at(LocalDateTime.now());
        entityManager.persist(membership);

        CompanyMemberRole role = new CompanyMemberRole();
        role.setCompany_id(company);
        role.setMembership_id(membership);
        role.setRole(CompanyMemberRoleType.COMPANY_ADMIN);
        entityManager.persist(role);

        AuthSession session = new AuthSession();
        session.setUser_id(admin);
        session.setRefresh_token_hash("route-test-refresh-token-hash");
        session.setExpires_at(LocalDateTime.now().plusDays(1));
        entityManager.persist(session);

        origin = area("ROUTE-ORIGIN");
        destination = area("ROUTE-DESTINATION");
        route = route(company, "ROUTE-A");
        emptyRoute = route(company, "ROUTE-B");
        otherRoute = route(otherCompany, "ROUTE-OTHER");

        // Các chuyến có đủ trạng thái, nằm cả trong quá khứ và tương lai.
        for (TripStatus status : TripStatus.values()) {
            trip(route, status, status.ordinal() * 60 - 120);
        }
        trip(otherRoute, TripStatus.SCHEDULED, 1);
        entityManager.flush();
        authorization = "Bearer " + jwtService.createAccessToken(admin.getId(), session.getId());
    }

    @Test
    void listCountsAllStatusesAndDatesAndReturnsZeroForEmptyRoute() throws Exception {
        mvc.perform(get(routesPath()).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.wsResponse.length()").value(2))
                .andExpect(jsonPath("$.wsResponse[0].id").value(route.getId().toString()))
                .andExpect(jsonPath("$.wsResponse[0].tripCount").value(6))
                .andExpect(jsonPath("$.wsResponse[1].id").value(emptyRoute.getId().toString()))
                .andExpect(jsonPath("$.wsResponse[1].tripCount").value(0));
    }

    @Test
    void detailReturnsTripCountIncludingDrafts() throws Exception {
        mvc.perform(get(routesPath() + "/" + route.getId()).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.tripCount").value(6));
    }

    @Test
    void detailReturnsZeroForRouteWithoutTrips() throws Exception {
        mvc.perform(get(routesPath() + "/" + emptyRoute.getId()).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.tripCount").value(0));
    }

    @Test
    void createReturnsZeroTripCount() throws Exception {
        String body = """
                {"wsRequest": {
                  "code": "ROUTE-NEW", "name": "Tuyến mới",
                  "originAreaId": "%s", "destinationAreaId": "%s",
                  "distanceKm": 120, "durationMinutes": 150, "baseFareAmount": 200000
                }}
                """.formatted(origin.getId(), destination.getId());
        mvc.perform(post(routesPath()).header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.wsResponse.code").value("ROUTE-NEW"))
                .andExpect(jsonPath("$.wsResponse.tripCount").value(0));
    }

    @Test
    void updateKeepsExistingTripCount() throws Exception {
        mvc.perform(patch(routesPath() + "/" + route.getId()).header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"wsRequest": {"name": "Tuyến đã cập nhật", "baseFareAmount": 250000}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse.name").value("Tuyến đã cập nhật"))
                .andExpect(jsonPath("$.wsResponse.baseFareAmount").value(250000))
                .andExpect(jsonPath("$.wsResponse.tripCount").value(6));
    }

    @Test
    void pauseAndReactivateKeepTripCountInUpdateListAndDetail() throws Exception {
        String path = routesPath() + "/" + route.getId();
        for (String routeStatus : List.of("INACTIVE", "ACTIVE")) {
            mvc.perform(patch(path).header("Authorization", authorization)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"wsRequest\":{\"status\":\"" + routeStatus + "\"}}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.wsResponse.status").value(routeStatus))
                    .andExpect(jsonPath("$.wsResponse.tripCount").value(6));
            mvc.perform(get(path).header("Authorization", authorization))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.wsResponse.tripCount").value(6));
            mvc.perform(get(routesPath()).header("Authorization", authorization))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.wsResponse[0].tripCount").value(6));
        }
    }

    @Test
    void countQueriesRespectCompanyAndRequestedRouteIds() {
        assertThat(tripRepository.countByCompanyIdAndRouteId(company.getId(), route.getId())).isEqualTo(6);
        assertThat(tripRepository.countByCompanyIdAndRouteId(otherCompany.getId(), route.getId())).isZero();
        assertThat(tripRepository.countTripsByRouteIds(otherCompany.getId(), List.of(route.getId()))).isEmpty();

        var counts = tripRepository.countTripsByRouteIds(
                company.getId(), List.of(route.getId(), emptyRoute.getId(), otherRoute.getId()));
        assertThat(counts).hasSize(1);
        assertThat(counts.get(0).getRouteId()).isEqualTo(route.getId());
        assertThat(counts.get(0).getTripCount()).isEqualTo(6);
        assertThat(tripRepository.countTripsByRouteIds(company.getId(), List.of(emptyRoute.getId()))).isEmpty();
    }

    @Test
    void cannotReadAnotherCompanyRoutesOrRouteDetail() throws Exception {
        mvc.perform(get("/api/v1/bus-companies/" + otherCompany.getId() + "/routes")
                        .header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mvc.perform(get(routesPath() + "/" + otherRoute.getId()).header("Authorization", authorization))
                .andExpect(status().isNotFound());
    }

    @Test
    void listUsesOneAggregateQueryForManyRoutes() {
        for (int i = 0; i < 12; i++) {
            route(company, "EXTRA-" + i);
        }
        entityManager.flush();
        entityManager.clear();
        Statistics statistics = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class).getStatistics();
        boolean previouslyEnabled = statistics.isStatisticsEnabled();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
        try {
            var response = routeService.getRoutes(company.getId());
            assertThat(response.getWsResponse()).hasSize(14);
            // Một query hãng, một query tuyến và một query đếm, không tăng theo số tuyến.
            assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);
        } finally {
            statistics.setStatisticsEnabled(previouslyEnabled);
        }
    }

    @Test
    void listWithoutRoutesSkipsCountQuery() throws Exception {
        // Xóa chuyến trước tuyến để không vi phạm khóa ngoại.
        entityManager.createQuery("delete from Trip trip where trip.company_id.id = :companyId")
                .setParameter("companyId", company.getId()).executeUpdate();
        entityManager.clear();
        entityManager.createQuery("delete from Route route where route.company_id.id = :companyId")
                .setParameter("companyId", company.getId()).executeUpdate();
        entityManager.flush();
        entityManager.clear();

        Statistics statistics = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class).getStatistics();
        boolean previouslyEnabled = statistics.isStatisticsEnabled();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
        try {
            assertThat(routeService.getRoutes(company.getId()).getWsResponse()).isEmpty();
            // Chỉ truy vấn hãng và danh sách tuyến; bỏ qua query đếm.
            assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
        } finally {
            statistics.setStatisticsEnabled(previouslyEnabled);
        }

        mvc.perform(get(routesPath()).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wsResponse").isEmpty());
    }

    private BusCompany company(String slug) {
        BusCompany value = new BusCompany();
        value.setName(slug);
        value.setSlug(slug);
        value.setHotline("19001234");
        value.setTimezone("Asia/Ho_Chi_Minh");
        value.setStatus(BusCompanyStatus.ACTIVE);
        entityManager.persist(value);
        return value;
    }

    private Area area(String code) {
        Area value = new Area();
        value.setCode(code);
        value.setName(code);
        value.setType("PROVINCE");
        value.setStatus("ACTIVE");
        entityManager.persist(value);
        return value;
    }

    private Route route(BusCompany owner, String code) {
        Route value = new Route();
        value.setCompany_id(owner);
        value.setCode(code);
        value.setName(code);
        value.setOrigin_area_id(origin);
        value.setDestination_area_id(destination);
        value.setDistance_km(BigDecimal.valueOf(120));
        value.setDuration_minutes(150);
        value.setBase_fare_amount(200_000L);
        value.setStatus(RouteStatus.ACTIVE);
        entityManager.persist(value);
        return value;
    }

    private void trip(Route selectedRoute, TripStatus tripStatus, int offsetDays) {
        LocalDateTime departure = LocalDateTime.now().plusDays(offsetDays);
        Trip value = new Trip();
        value.setCompany_id(selectedRoute.getCompany_id());
        value.setRoute_id(selectedRoute);
        value.setTrip_code(selectedRoute.getCode() + "-" + tripStatus);
        value.setDeparture_at(departure);
        value.setExpected_arrival_at(departure.plusMinutes(150));
        value.setBooking_cutoff_at(departure.minusMinutes(30));
        value.setStatus(tripStatus);
        value.setSales_status(tripStatus == TripStatus.DRAFT ? TripSalesStatus.CLOSED : TripSalesStatus.OPEN);
        entityManager.persist(value);
    }

    private String routesPath() {
        return "/api/v1/bus-companies/" + company.getId() + "/routes";
    }
}
