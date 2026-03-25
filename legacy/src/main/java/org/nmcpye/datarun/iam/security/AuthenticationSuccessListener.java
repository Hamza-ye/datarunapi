package org.nmcpye.datarun.iam.security;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.iam.user.service.LastSeenService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {
    private final LastSeenService lastSeenService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();

        if (principal instanceof CurrentUserDetails user) {
            lastSeenService.touch(user.getId(), Instant.now());
        }
    }
}
