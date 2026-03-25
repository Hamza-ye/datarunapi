package org.nmcpye.datarun.iam.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nmcpye.datarun.jpa.errorevent.service.ErrorEventService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFailureListener
    implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    private final ErrorEventService errorEventService;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        // Extract the username that FAILED to login
        Object principal = event.getAuthentication().getPrincipal();
        String attemptedUsername = principal instanceof String ? (String) principal : String.valueOf(principal);

        // Bridge the gap: Put this username into the Request Scope
        // The ExceptionTranslator will pick this up in the Service via request.getAttribute()
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            attrs.setAttribute(
                ErrorEventService.ATTR_ATTEMPTED_USERNAME,
                attemptedUsername,
                RequestAttributes.SCOPE_REQUEST
            );
        }

        // We do NOT log to DB here. We let the Exception bubble to ExceptionTranslator.
        log.warn("Login failed for user: {}", attemptedUsername);
    }
}
