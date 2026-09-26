package com.livo.api.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.livo.api.common.security.JwtTokenProvider;
import com.livo.api.common.security.UserPrincipal;
import com.livo.api.common.security.UserSecurityStatus;
import com.livo.api.common.security.UserSecurityStatusCache;
import com.livo.api.modules.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

/**
 * Filter that intercepts incoming HTTP requests, extracts the Bearer token,
 * verifies either HMAC JWT or Firebase ID token, and populates the SecurityContext.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserSecurityStatusCache userSecurityStatusCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractBearerToken(request);

        if (StringUtils.hasText(token)) {
            // 1. Check if it's a backend-issued JWT
            if (jwtTokenProvider.validateToken(token)) {
                // Reject refresh tokens presented as Bearer access tokens
                if (jwtTokenProvider.isRefreshToken(token)) {
                    log.warn("Rejected attempt to use refresh token as access token on endpoint: {}", request.getRequestURI());
                    filterChain.doFilter(request, response);
                    return;
                }

                try {
                    UUID userId = jwtTokenProvider.getUserIdFromToken(token);
                    Instant issuedAt = jwtTokenProvider.getIssuedAtFromToken(token);

                    UserSecurityStatus securityStatus = userSecurityStatusCache.getSecurityStatus(userId);
                    if (!securityStatus.isAllowed(issuedAt)) {
                        log.warn("Rejected request with revoked, deactivated, or deleted user token for userId: {}", userId);
                        filterChain.doFilter(request, response);
                        return;
                    }

                    String email = jwtTokenProvider.getEmailFromToken(token);
                    String firebaseUid = jwtTokenProvider.getFirebaseUidFromToken(token);

                    UserPrincipal principal = UserPrincipal.builder()
                            .id(userId)
                            .email(email)
                            .firebaseUid(firebaseUid)
                            .build();

                    setAuthentication(request, principal);
                } catch (Exception e) {
                    log.warn("Failed to set user authentication from JWT: {}", e.getMessage());
                }
            }
            // 2. Check if it's a Firebase ID Token (when FirebaseApp is initialized)
            else if (!FirebaseApp.getApps().isEmpty()) {
                try {
                    FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                    String firebaseUid = decodedToken.getUid();
                    String email = decodedToken.getEmail();
                    String name = decodedToken.getName();

                    // Lookup matching user in database if already provisioned
                    UserPrincipal.UserPrincipalBuilder principalBuilder = UserPrincipal.builder()
                            .firebaseUid(firebaseUid)
                            .email(email)
                            .fullName(name);

                    userRepository.findByFirebaseUidAndDeletedAtIsNull(firebaseUid)
                            .ifPresent(u -> principalBuilder.id(u.getId()));

                    setAuthentication(request, principalBuilder.build());
                } catch (Exception e) {
                    log.warn("Firebase token validation failed: {}", e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void setAuthentication(HttpServletRequest request, UserPrincipal principal) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
