package com.example.Vi_vu_Viet.Service.Trip.Implement;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Trip.BusCompany;
import com.example.Vi_vu_Viet.Entity.Trip.CompanyMember;
import com.example.Vi_vu_Viet.Entity.Trip.CompanyMemberRole;
import com.example.Vi_vu_Viet.Entity.Trip.DriverProfile;
import com.example.Vi_vu_Viet.Entity.Trip.TripAssignment;
import com.example.Vi_vu_Viet.Repository.Auth.AuthSessionRepository;
import com.example.Vi_vu_Viet.Repository.Auth.CompanyMemberRoleRepository;
import com.example.Vi_vu_Viet.Repository.Auth.UserRepository;
import com.example.Vi_vu_Viet.Repository.Trip.BusCompanyRepository;
import com.example.Vi_vu_Viet.Repository.Trip.CompanyDriverTripCountProjection;
import com.example.Vi_vu_Viet.Repository.Trip.CompanyMemberRepository;
import com.example.Vi_vu_Viet.Repository.Trip.DriverProfileRepository;
import com.example.Vi_vu_Viet.Repository.Trip.TripAssignmentRepository;
import com.example.Vi_vu_Viet.Service.Trip.CompanyDriverService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.BusCompanyStatus;
import com.example.Vi_vu_Viet.Util.Enums.CompanyDriverStatus;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberRoleType;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberStatus;
import com.example.Vi_vu_Viet.Util.Enums.DriverAssignmentStatus;
import com.example.Vi_vu_Viet.Util.Enums.DriverProfileStatus;
import com.example.Vi_vu_Viet.Util.Enums.TripStatus;
import com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus;
import com.example.Vi_vu_Viet.dto.Request.Trip.CreateCompanyDriverRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.ResetCompanyDriverPasswordRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateCompanyDriverRequest;
import com.example.Vi_vu_Viet.dto.Request.Trip.UpdateCompanyDriverStatusRequest;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import com.example.Vi_vu_Viet.dto.Response.PageResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.CompanyDriverCredentialResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.CompanyDriverResponse;
import com.example.Vi_vu_Viet.dto.Response.Company.CompanyDriverTripResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CompanyDriverServiceImpl implements CompanyDriverService {
    private static final SecureRandom PASSWORD_RANDOM = new SecureRandom();
    private static final List<DriverAssignmentStatus> EXCLUDED_ASSIGNMENTS =
            List.of(DriverAssignmentStatus.REJECTED, DriverAssignmentStatus.REPLACED);
    private static final List<TripStatus> COUNTED_TRIP_STATUSES =
            List.of(TripStatus.SCHEDULED, TripStatus.IN_PROGRESS, TripStatus.COMPLETED);

    BusCompanyRepository busCompanyRepository;
    UserRepository userRepository;
    CompanyMemberRepository companyMemberRepository;
    CompanyMemberRoleRepository companyMemberRoleRepository;
    DriverProfileRepository driverProfileRepository;
    TripAssignmentRepository tripAssignmentRepository;
    AuthSessionRepository authSessionRepository;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<CompanyDriverResponse>> getDrivers(UUID companyId, String search,
                                                                       CompanyDriverStatus status,
                                                                       int page, int size) {
        getActiveCompany(companyId);
        PageRequest pageable = pageRequest(page, size);
        if (search != null && search.length() > 200) throw badRequest("Search must not exceed 200 characters");
        String pattern = StringUtils.hasText(search) ? "%" + search.trim().toLowerCase(Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%" : null;
        String phonePattern = pattern == null ? null : pattern.replaceAll("[\\s()\\-]", "");
        if ("%%".equals(phonePattern)) phonePattern = null;
        Boolean active = status == null ? null : status == CompanyDriverStatus.ACTIVE;
        Page<CompanyMember> memberships = companyMemberRepository.searchCompanyDrivers(
                companyId, pattern, phonePattern, active, pageable);
        List<UUID> membershipIds = memberships.getContent().stream().map(CompanyMember::getId).toList();
        Map<UUID, DriverProfile> profiles = membershipIds.isEmpty() ? Map.of()
                : driverProfileRepository.findAllByCompanyAndMemberships(companyId, membershipIds).stream()
                    .collect(Collectors.toMap(profile -> profile.getMembership_id().getId(), profile -> profile));
        List<UUID> driverIds = memberships.getContent().stream().map(member -> member.getUser_id().getId()).toList();
        Map<UUID, Long> counts = tripCounts(companyId, driverIds);
        return success(PageResponse.from(memberships.map(member -> toResponse(member,
                profiles.get(member.getId()), counts.getOrDefault(member.getUser_id().getId(), 0L)))));
    }

    @Override
    @Transactional
    public BaseResponse<CompanyDriverCredentialResponse> createDriver(UUID companyId,
                                                                      CreateCompanyDriverRequest request) {
        BusCompany company = getActiveCompany(companyId);
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByPhone(request.getPhone())) throw conflict("Phone number is already registered");
        if (email != null && userRepository.existsByEmail(email)) throw conflict("Email is already registered");
        validateLicense(companyId, request.getLicenseNumber(), null);
        String password = generatePassword();

        UserAccount user = new UserAccount();
        user.setFull_name(request.getFullName());
        user.setPhone(request.getPhone());
        user.setEmail(email);
        user.setPassword_hash(passwordEncoder.encode(password));
        user.setAccount_status(UserAccountStatus.ACTIVE);
        user = userRepository.saveAndFlush(user);

        CompanyMember membership = new CompanyMember();
        membership.setCompany_id(company);
        membership.setUser_id(user);
        membership.setStatus(CompanyMemberStatus.ACTIVE);
        membership.setJoined_at(LocalDateTime.now());
        membership = companyMemberRepository.save(membership);

        CompanyMemberRole role = new CompanyMemberRole();
        role.setCompany_id(company);
        role.setMembership_id(membership);
        role.setRole(CompanyMemberRoleType.DRIVER);
        companyMemberRoleRepository.save(role);

        DriverProfile profile = newProfile(membership);
        profile.setLicense_number(request.getLicenseNumber());
        profile.setLicense_class(request.getLicenseClass());
        profile.setLicense_expires_on(request.getLicenseExpiresOn());
        driverProfileRepository.saveAndFlush(profile);
        return success(toCredential(user, password));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<CompanyDriverResponse> getDriver(UUID companyId, UUID driverId) {
        getActiveCompany(companyId);
        CompanyMember membership = getCompanyDriver(companyId, driverId);
        DriverProfile profile = driverProfileRepository.findByCompanyAndMembership(companyId, membership.getId())
                .orElse(null);
        return success(toResponse(membership, profile, tripCounts(companyId, List.of(driverId)).getOrDefault(driverId, 0L)));
    }

    @Override
    @Transactional
    public BaseResponse<CompanyDriverResponse> updateDriver(UUID companyId, UUID driverId,
                                                           UpdateCompanyDriverRequest request) {
        getActiveCompany(companyId);
        CompanyMember membership = getCompanyDriver(companyId, driverId);
        UserAccount user = lockUser(driverId);
        if (!hasChanges(request)) throw badRequest("At least one driver field must be provided");
        String email = normalizeEmail(request.getEmail());
        boolean identityChanged = request.getFullName() != null && !Objects.equals(request.getFullName(), user.getFull_name())
                || request.getPhone() != null && !Objects.equals(request.getPhone(), user.getPhone())
                || request.getEmail() != null && !Objects.equals(email, user.getEmail());
        if (identityChanged) requireManagedAccount(companyId, driverId);

        if (request.getPhone() != null) {
            if (userRepository.existsByPhoneExcludingUser(request.getPhone(), driverId)) {
                throw conflict("Phone number is already registered");
            }
            if (!Objects.equals(request.getPhone(), user.getPhone())) user.setPhone_verified_at(null);
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            if (email != null && userRepository.existsByEmailExcludingUser(email, driverId)) {
                throw conflict("Email is already registered");
            }
            user.setEmail(email);
        }
        if (request.getFullName() != null) user.setFull_name(request.getFullName());
        DriverProfile profile = getOrCreateProfile(membership);
        if (request.getLicenseNumber() != null) {
            validateLicense(companyId, request.getLicenseNumber(), profile.getId());
            profile.setLicense_number(request.getLicenseNumber());
        }
        if (request.getLicenseClass() != null) profile.setLicense_class(request.getLicenseClass());
        if (request.getLicenseExpiresOn() != null) profile.setLicense_expires_on(request.getLicenseExpiresOn());
        userRepository.saveAndFlush(user);
        profile = driverProfileRepository.saveAndFlush(profile);
        return success(toResponse(membership, profile, tripCounts(companyId, List.of(driverId)).getOrDefault(driverId, 0L)));
    }

    @Override
    @Transactional
    public BaseResponse<CompanyDriverResponse> updateStatus(UUID companyId, UUID driverId,
                                                           UpdateCompanyDriverStatusRequest request) {
        getActiveCompany(companyId);
        CompanyMember membership = getCompanyDriver(companyId, driverId);
        UserAccount user = lockUser(driverId);
        if (request.getStatus() == CompanyDriverStatus.ACTIVE
                && (membership.getStatus() != CompanyMemberStatus.ACTIVE
                    || user.getAccount_status() != UserAccountStatus.ACTIVE)) {
            throw conflict("The account and company membership must be active before unlocking this driver");
        }
        DriverProfile profile = getOrCreateProfile(membership);
        profile.setStatus(request.getStatus() == CompanyDriverStatus.ACTIVE
                ? DriverProfileStatus.ACTIVE : DriverProfileStatus.SUSPENDED);
        profile.setStatus_note(normalizeNote(request.getNote()));
        profile = driverProfileRepository.saveAndFlush(profile);
        return success(toResponse(membership, profile, tripCounts(companyId, List.of(driverId)).getOrDefault(driverId, 0L)));
    }

    @Override
    @Transactional
    public BaseResponse<CompanyDriverCredentialResponse> resetPassword(UUID companyId, UUID driverId,
                                                                       ResetCompanyDriverPasswordRequest request) {
        getActiveCompany(companyId);
        CompanyMember membership = getCompanyDriver(companyId, driverId);
        UserAccount user = lockUser(driverId);
        requireManagedAccount(companyId, driverId);
        if (!StringUtils.hasText(user.getPhone())) throw conflict("Driver must have a phone number for login");
        String password = generatePassword();
        user.setPassword_hash(passwordEncoder.encode(password));
        userRepository.saveAndFlush(user);
        LocalDateTime now = LocalDateTime.now();
        authSessionRepository.revokeAllByUserId(driverId, now);
        DriverProfile profile = getOrCreateProfile(membership);
        profile.setPassword_reset_note(normalizeNote(request.getNote()));
        profile.setPassword_reset_at(now);
        driverProfileRepository.saveAndFlush(profile);
        return success(toCredential(user, password));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<CompanyDriverTripResponse>> getDriverTrips(UUID companyId, UUID driverId,
                                                                              int page, int size) {
        getActiveCompany(companyId);
        getCompanyDriver(companyId, driverId);
        Page<TripAssignment> assignments = tripAssignmentRepository.findCompanyDriverTrips(
                companyId, driverId, EXCLUDED_ASSIGNMENTS, pageRequest(page, size));
        return success(PageResponse.from(assignments.map(this::toTripResponse)));
    }

    private BusCompany getActiveCompany(UUID companyId) {
        return busCompanyRepository.findByIdAndStatus(companyId, BusCompanyStatus.ACTIVE)
                .orElseThrow(() -> notFound("Bus company was not found or is inactive"));
    }

    private CompanyMember getCompanyDriver(UUID companyId, UUID driverId) {
        return companyMemberRepository.findCompanyDriver(companyId, driverId)
                .orElseThrow(() -> notFound("Driver was not found for this company"));
    }

    private UserAccount lockUser(UUID driverId) {
        return userRepository.findByIdForUpdate(driverId)
                .orElseThrow(() -> notFound("Driver account was not found"));
    }

    private void requireManagedAccount(UUID companyId, UUID driverId) {
        if (companyMemberRepository.hasSharedOrAdminAccount(companyId, driverId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Company admins cannot change the identity or password of a shared account or an admin account");
        }
    }

    private DriverProfile getOrCreateProfile(CompanyMember membership) {
        return driverProfileRepository.findByCompanyAndMembership(membership.getCompany_id().getId(), membership.getId())
                .orElseGet(() -> newProfile(membership));
    }

    private DriverProfile newProfile(CompanyMember membership) {
        DriverProfile profile = new DriverProfile();
        profile.setCompany_id(membership.getCompany_id());
        profile.setMembership_id(membership);
        profile.setStatus(DriverProfileStatus.ACTIVE);
        return profile;
    }

    private void validateLicense(UUID companyId, String licenseNumber, UUID profileId) {
        if (driverProfileRepository.existsLicenseForAnotherProfile(companyId, licenseNumber, profileId)) {
            throw conflict("License number is already registered for this company");
        }
    }

    private Map<UUID, Long> tripCounts(UUID companyId, List<UUID> driverIds) {
        if (driverIds.isEmpty()) return Map.of();
        LocalDateTime now = LocalDateTime.now();
        return tripAssignmentRepository.countCompanyDriverTrips(companyId, driverIds, now.minusDays(30), now,
                EXCLUDED_ASSIGNMENTS, COUNTED_TRIP_STATUSES).stream()
                .collect(Collectors.toMap(CompanyDriverTripCountProjection::getDriverId,
                        CompanyDriverTripCountProjection::getTripCount));
    }

    private CompanyDriverResponse toResponse(CompanyMember membership, DriverProfile profile, long tripCount) {
        UserAccount user = membership.getUser_id();
        CompanyDriverResponse response = new CompanyDriverResponse();
        response.setDriverId(user.getId());
        response.setCompanyId(membership.getCompany_id().getId());
        response.setFullName(user.getFull_name());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setCreatedAt(user.getCreated_at());
        LocalDateTime updatedAt = user.getUpdated_at();
        boolean active = membership.getStatus() == CompanyMemberStatus.ACTIVE
                && user.getAccount_status() == UserAccountStatus.ACTIVE;
        if (profile != null) {
            response.setProfileId(profile.getId());
            response.setLicenseNumber(profile.getLicense_number());
            response.setLicenseClass(profile.getLicense_class());
            response.setLicenseExpiresOn(profile.getLicense_expires_on());
            response.setStatusNote(profile.getStatus_note());
            active = active && profile.getStatus() == DriverProfileStatus.ACTIVE;
            if (profile.getUpdated_at() != null && (updatedAt == null || profile.getUpdated_at().isAfter(updatedAt))) {
                updatedAt = profile.getUpdated_at();
            }
        }
        response.setUpdatedAt(updatedAt);
        response.setStatus(active ? CompanyDriverStatus.ACTIVE : CompanyDriverStatus.LOCKED);
        response.setTripCount30Days(tripCount);
        return response;
    }

    private CompanyDriverTripResponse toTripResponse(TripAssignment assignment) {
        var trip = assignment.getTrip_id();
        CompanyDriverTripResponse response = new CompanyDriverTripResponse();
        response.setTripId(trip.getId());
        response.setTripCode(trip.getTrip_code());
        response.setRouteName(trip.getRoute_id().getName());
        response.setDepartureAt(trip.getDeparture_at());
        response.setExpectedArrivalAt(trip.getExpected_arrival_at());
        response.setStatus(trip.getStatus());
        response.setAssignmentStatus(assignment.getAssignment_status());
        response.setAssignedAt(assignment.getAssigned_at());
        return response;
    }

    private CompanyDriverCredentialResponse toCredential(UserAccount user, String password) {
        CompanyDriverCredentialResponse response = new CompanyDriverCredentialResponse();
        response.setDriverId(user.getId());
        response.setFullName(user.getFull_name());
        response.setLogin(user.getPhone());
        response.setTemporaryPassword(password);
        return response;
    }

    private PageRequest pageRequest(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw badRequest("Page must be >= 0 and size must be between 1 and 100");
        return PageRequest.of(page, size);
    }

    private String generatePassword() {
        byte[] bytes = new byte[18];
        PASSWORD_RANDOM.nextBytes(bytes);
        return "Vv1!" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalizeEmail(String email) {
        return StringUtils.hasText(email) ? email.trim().toLowerCase(Locale.ROOT) : null;
    }

    private String normalizeNote(String note) {
        return StringUtils.hasText(note) ? note.trim() : null;
    }

    private boolean hasChanges(UpdateCompanyDriverRequest request) {
        return request.getFullName() != null || request.getPhone() != null || request.getEmail() != null
                || request.getLicenseNumber() != null || request.getLicenseClass() != null
                || request.getLicenseExpiresOn() != null;
    }

    private <T> BaseResponse<T> success(T body) {
        BaseResponse<T> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(body);
        return response;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
