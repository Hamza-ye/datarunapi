package org.nmcpye.datarun.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.iam.user.service.LastSeenService;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class LastSeenFilter extends OncePerRequestFilter {

    private final LastSeenService lastSeenService;

    // Skip static resources to save processing
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
        ".css", ".js", ".png", ".jpg", ".gif", ".ico", ".svg", ".woff", ".woff2", ".ttf"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().toLowerCase();
        // Ignore static files
        if (path.contains(".")) {
            String ext = path.substring(path.lastIndexOf('.'));
            if (IGNORED_EXTENSIONS.contains(ext)) return true;
        }
        // Ignore actuator endpoints or heartbeats
        if (path.startsWith("/management") || path.startsWith("/actuator")) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // Check if authenticated (and not anonymous)
            if (auth != null && auth.isAuthenticated() &&
                auth.getPrincipal() instanceof CurrentUserDetails userDetails) {

                // No DB call here, just a fast map put
                lastSeenService.touch(userDetails.getId(), Instant.now());
            }
        } catch (Exception e) {
            // Fail silently so user experience isn't affected by logging logic
        }

        chain.doFilter(request, response);
    }
}
