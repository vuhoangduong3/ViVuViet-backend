package com.example.Vi_vu_Viet.Service.Auth.Implement;

import com.example.Vi_vu_Viet.Entity.Auth.AuthSession;
import com.example.Vi_vu_Viet.Entity.Auth.BlacklistToken;
import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Repository.Auth.AuthSessionRepository;
import com.example.Vi_vu_Viet.Repository.Auth.BlacklistTokenRepository;
import com.example.Vi_vu_Viet.Repository.Auth.UserRepository;
import com.example.Vi_vu_Viet.Security.JwtService;
import com.example.Vi_vu_Viet.Security.TokenUtils;
import com.example.Vi_vu_Viet.Service.Auth.AuthService;
import com.example.Vi_vu_Viet.Util.ApplicationCode;
import com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus;
import com.example.Vi_vu_Viet.dto.Request.BaseRequest;
import com.example.Vi_vu_Viet.dto.Request.Auth.LoginRequest;
import com.example.Vi_vu_Viet.dto.Request.Auth.RegisterRequest;
import com.example.Vi_vu_Viet.dto.Request.Auth.RefreshRequest;
import com.example.Vi_vu_Viet.dto.Response.Auth.MeResponse;
import com.example.Vi_vu_Viet.dto.Response.Auth.TokenResponse;
import com.example.Vi_vu_Viet.dto.Response.BaseResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    AuthSessionRepository authSessionRepository;
    BlacklistTokenRepository blacklistTokenRepository;
    PasswordEncoder passwordEncoder;
    JwtService jwtService;

    @Override
    @Transactional
    public BaseResponse<MeResponse> register(BaseRequest<RegisterRequest> request) {
        RegisterRequest registerRequest = request.getWsRequest();
        String email = normalizeEmail(registerRequest.getEmail());
        String phone = normalizePhone(registerRequest.getPhone());

        if (!StringUtils.hasText(email) && !StringUtils.hasText(phone)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide an email or phone number");
        }
        if (StringUtils.hasText(email) && userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        if (StringUtils.hasText(phone) && userRepository.existsByPhone(phone)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number is already registered");
        }

        UserAccount user = new UserAccount();
        user.setFull_name(registerRequest.getFullName().trim());
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword_hash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setAccount_status(UserAccountStatus.ACTIVE);
        user = userRepository.save(user);

        MeResponse response = new MeResponse();
        response.setId(user.getId());
        response.setFullName(user.getFull_name());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setAccountStatus(user.getAccount_status());
        response.setCreatedAt(user.getCreated_at());
        response.setAuthorities(java.util.List.of("ROLE_CUSTOMER"));

        return createSuccessResponse(response);
    }

    @Override
    @Transactional
    public BaseResponse<TokenResponse> login(BaseRequest<LoginRequest> request) {
        LoginRequest loginRequest = request.getWsRequest();
        String identifier = normalizeIdentifier(loginRequest.getIdentifier());
        UserAccount user = userRepository.findByLoginIdentifier(identifier)
                .orElseThrow(this::invalidCredentials);

        if (user.getAccount_status() != UserAccountStatus.ACTIVE
                || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword_hash())) {
            throw invalidCredentials();
        }

        String refreshToken = TokenUtils.createRefreshToken();
        AuthSession session = createSession(user, refreshToken, null);

        return createSuccessResponse(createTokenResponse(user.getId(), session.getId(), refreshToken));
    }

    @Override
    @Transactional
    public BaseResponse<TokenResponse> refresh(BaseRequest<RefreshRequest> request) {
        RefreshRequest refreshRequest = request.getWsRequest();
        String oldTokenHash = TokenUtils.hash(refreshRequest.getRefreshToken());
        AuthSession oldSession = authSessionRepository.findByRefreshTokenHashForUpdate(oldTokenHash)
                .orElseThrow(this::invalidRefreshToken);

        LocalDateTime now = LocalDateTime.now();
        UserAccount user = oldSession.getUser_id();
        if (oldSession.getRevoked_at() != null
                || !oldSession.getExpires_at().isAfter(now)
                || user.getAccount_status() != UserAccountStatus.ACTIVE) {
            throw invalidRefreshToken();
        }

        oldSession.setRevoked_at(now);
        authSessionRepository.save(oldSession);

        String newRefreshToken = TokenUtils.createRefreshToken();
        AuthSession newSession = createSession(user, newRefreshToken, oldSession.getDevice_info());

        return createSuccessResponse(createTokenResponse(user.getId(), newSession.getId(), newRefreshToken));
    }

    @Override
    @Transactional
    public BaseResponse<Void> logout(String accessToken) {
        JwtService.TokenClaims claims = jwtService.getTokenClaims(accessToken)
                .orElseThrow(this::invalidAccessToken);
        String tokenHash = TokenUtils.hash(accessToken);

        if (!blacklistTokenRepository.isBlacklisted(tokenHash)) {
            BlacklistToken blacklistToken = new BlacklistToken();
            blacklistToken.setToken_hash(tokenHash);
            blacklistToken.setExpires_at(LocalDateTime.ofInstant(
                    claims.expiresAt(),
                    ZoneId.systemDefault()
            ));
            blacklistTokenRepository.save(blacklistToken);
        }

        AuthSession session = authSessionRepository.findByIdWithUserForUpdate(claims.sessionId())
                .orElseThrow(this::invalidAccessToken);
        if (session.getRevoked_at() == null) {
            session.setRevoked_at(LocalDateTime.now());
            authSessionRepository.save(session);
        }

        return createSuccessResponse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<MeResponse> getMe(UserAccount user, List<String> authorities) {
        MeResponse response = new MeResponse();
        response.setId(user.getId());
        response.setFullName(user.getFull_name());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setAccountStatus(user.getAccount_status());
        response.setCreatedAt(user.getCreated_at());
        response.setAuthorities(authorities);
        return createSuccessResponse(response);
    }

    private AuthSession createSession(UserAccount user, String refreshToken, String deviceInfo) {
        AuthSession session = new AuthSession();
        session.setUser_id(user);
        session.setRefresh_token_hash(TokenUtils.hash(refreshToken));
        session.setExpires_at(LocalDateTime.now().plusDays(30));
        session.setDevice_info(deviceInfo);
        return authSessionRepository.save(session);
    }

    private TokenResponse createTokenResponse(UUID userId, UUID sessionId, String refreshToken) {
        TokenResponse response = new TokenResponse();
        response.setTokenType("Bearer");
        response.setAccessToken(jwtService.createAccessToken(userId, sessionId));
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtService.getAccessTokenSeconds());
        return response;
    }

    private <T> BaseResponse<T> createSuccessResponse(T body) {
        BaseResponse<T> response = new BaseResponse<>(ApplicationCode.SUCCESS);
        response.setWsResponse(body);
        return response;
    }

    private String normalizeIdentifier(String identifier) {
        String value = identifier.trim();
        if (value.contains("@")) {
            return value.toLowerCase(Locale.ROOT);
        }
        return value.replaceAll("[\\s()\\-]", "");
    }

    private String normalizeEmail(String email) {
        return StringUtils.hasText(email) ? email.trim().toLowerCase(Locale.ROOT) : null;
    }

    private String normalizePhone(String phone) {
        return StringUtils.hasText(phone) ? phone.replaceAll("[\\s()\\-]", "") : null;
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email, phone, or password");
    }

    private ResponseStatusException invalidRefreshToken() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired");
    }

    private ResponseStatusException invalidAccessToken() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access token is invalid or revoked");
    }
}
