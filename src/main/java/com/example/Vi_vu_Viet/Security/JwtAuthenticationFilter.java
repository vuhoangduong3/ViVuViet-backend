package com.example.Vi_vu_Viet.Security;

import com.example.Vi_vu_Viet.Entity.Auth.UserAccount;
import com.example.Vi_vu_Viet.Entity.Auth.AuthSession;
import com.example.Vi_vu_Viet.Repository.Auth.AuthSessionRepository;
import com.example.Vi_vu_Viet.Repository.Auth.BlacklistTokenRepository;
import com.example.Vi_vu_Viet.Repository.Auth.CompanyMemberRoleRepository;
import com.example.Vi_vu_Viet.Util.Enums.CompanyMemberStatus;
import com.example.Vi_vu_Viet.Util.Enums.UserAccountStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    JwtService jwtService;
    AuthSessionRepository authSessionRepository;
    BlacklistTokenRepository blacklistTokenRepository;
    CompanyMemberRoleRepository companyMemberRoleRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            jwtService.getTokenClaims(token).ifPresent(claims -> {
                if (blacklistTokenRepository.isBlacklisted(TokenUtils.hash(token))) {
                    return;
                }

                authSessionRepository.findByIdWithUser(claims.sessionId())
                        .filter(session -> isActiveSession(session, claims.userId()))
                        .map(AuthSession::getUser_id)
                        .ifPresent(this::setAuthentication);
            });
        }

        filterChain.doFilter(request, response);
    }

    private boolean isActiveSession(AuthSession session, java.util.UUID tokenUserId) {
        UserAccount user = session.getUser_id();
        return session.getRevoked_at() == null
                && session.getExpires_at().isAfter(LocalDateTime.now())
                && user.getAccount_status() == UserAccountStatus.ACTIVE
                && Objects.equals(user.getId(), tokenUserId);
    }

    private void setAuthentication(UserAccount user) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));

        companyMemberRoleRepository.findActiveCompanyRoles(user.getId(), CompanyMemberStatus.ACTIVE)
                .forEach(role -> authorities.add(new SimpleGrantedAuthority(
                        role[0].toString() + ":" + role[1].toString()
                )));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
