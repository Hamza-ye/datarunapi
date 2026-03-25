package org.nmcpye.datarun.jpa.errorevent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nmcpye.datarun.jpa.errorevent.ErrorEvent;
import org.nmcpye.datarun.jpa.errorevent.repository.ErrorEventRepository;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ErrorEventService {
    public static final String ATTR_ATTEMPTED_USERNAME = "LOGGING_ATTEMPTED_USERNAME";

    private final ErrorEventRepository repo;
    private final ObjectMapper objectMapper;

    /**
     * Entry point for logging errors.
     * We pass the raw HttpServletRequest or NativeWebRequest.
     */
    @Async("errorLoggingExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logError(Throwable ex, NativeWebRequest request, HttpStatus status, Map<String, Object> extraContext) {
        try {
            HttpServletRequest servletRequest = null;
            if (request instanceof ServletWebRequest) {
                servletRequest = ((ServletWebRequest) request).getRequest();
            }

            ErrorEvent ev = ErrorEvent.builder()
                .occurredAt(Instant.now())
                .level("ERROR")
                .status(status != null ? status.value() : 500)
                .path(extractPath(servletRequest))
                .method(servletRequest != null ? servletRequest.getMethod() : "UNKNOWN")
                .clientIp(extractClientIp(servletRequest))
                .username(resolveUsername(servletRequest)) // The magic happens here
                .message(truncate(ex.getMessage(), 2000))
                .exception(ex.getClass().getName())
                .stacktrace(truncate(getStackTrace(ex), 4000))
                .contextJson(toJsonSafe(extraContext))
                .build();
            // persistAndFlush is rarely needed in async unless you need the ID immediately
            repo.persistAndFlush(ev);
        } catch (Exception e) {
            log.error("Failed to persist error event: {}", e.getMessage());
        }
    }

    private String extractUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? String.valueOf(auth.getName()) : "anonymous";
    }

    private String resolveUsername(HttpServletRequest request) {
        // 1. Check if the AuthenticationListener enriched the request with a failed login name
        if (request != null) {
            Object attempted = request.getAttribute(ATTR_ATTEMPTED_USERNAME);
            if (attempted != null) {
                return String.valueOf(attempted);
            }
        }

        // 2. Check Security Context (for already logged-in users)
        return SecurityUtils.getCurrentUserDetails()
            .map(CurrentUserDetails::getUsername)
            .orElse(extractUsername());
    }

    private String extractClientIp(HttpServletRequest req) {
        if (req == null) return null;
        String xf = req.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(xf)) {
            return xf.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private String extractPath(HttpServletRequest req) {
        return req != null ? req.getRequestURI() : "unknown";
    }

    private String getStackTrace(Throwable ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private String truncate(String s, int max) {
        return (s != null && s.length() > max) ? s.substring(0, max) : s;
    }

    private String toJsonSafe(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{\"error\": \"json_serialization_failed\"}";
        }
    }
//
//    @Async("errorLoggingExecutor")
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void persist(Throwable ex, NativeWebRequest request,
//                        HttpStatus status, String username, String ip, Map<String, Object> extra) {
//        try {
//            ErrorEvent ev = new ErrorEvent();
//            ev.setOccurredAt(Instant.now());
//            ev.setLevel("ERROR");
//            ev.setStatus(status == null ? null : status.value());
//            ev.setPath(request == null ? null : request.getNativeRequest(HttpServletRequest.class).getRequestURI());
//            ev.setMethod(request == null ? null : request.getNativeRequest(HttpServletRequest.class).getMethod());
//            ev.setUsername(username != null ? username : extractUsername());
//            ev.setClientIp(ip != null ? ip : extractClientIp(request));
//            ev.setMessage(truncate(ex.getMessage(), 2000));
//            ev.setException(ex.getClass().getName());
//            ev.setStacktrace(truncate(getStackTrace(ex), 4000));
//            ev.setContextJson(toJsonSafe(extra));
//            repo.persistAndFlush(ev);
//        } catch (Exception e) {
//            // Never rethrow — fallback to logging to file/stdout
//            log.error("Failed to persist error event (logging fallback): {}", e.getMessage(), e);
//        }
//    }
}
