package org.nmcpye.datarun.web.rest.v1.authenticate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nmcpye.datarun.iam.userrefreshtoken.TokenRefreshException;
import org.nmcpye.datarun.iam.userrefreshtoken.dto.RefreshTokenDto;
import org.nmcpye.datarun.iam.userrefreshtoken.repository.RefreshTokenRepository;
import org.nmcpye.datarun.iam.userrefreshtoken.service.TokenService;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.rest.legacy.jwt.TokenRefreshRequest;
import org.nmcpye.datarun.web.rest.legacy.jwt.TokenRefreshResponse;
import org.nmcpye.datarun.web.vm.LoginVM;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.bind.annotation.*;

import static org.nmcpye.datarun.web.rest.v1.authenticate.AuthenticateResource.V1;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping(V1)
@RequiredArgsConstructor
@Slf4j
public class AuthenticateResource {
    protected static final String V1 = ApiVersion.API_V1;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final TokenService tokenService;
    private final RefreshTokenRepository tokenRepository;


    @PostMapping("/authenticate")
    public ResponseEntity<?> authorize(@Valid @RequestBody LoginVM loginVM, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            loginVM.getUsername(),
            loginVM.getPassword()
        );

        // attach remote address and other web details so listeners/events can read them
        authenticationToken.setDetails(new WebAuthenticationDetails(request));


        try {
            Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);


            String accessToken = tokenService.generateAccessToken(authentication.getName());
            RefreshTokenDto refreshToken = tokenService.createRefreshToken(authentication.getName());


            return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(new TokenRefreshResponse(accessToken, refreshToken.getToken()));
        } catch (BadCredentialsException ex) {
            // safe logging: DO NOT log passwords or sensitive token data
            String ip = request.getRemoteAddr();
            log.warn("Authentication failed for username='{}' from ip='{}'", loginVM.getUsername(), ip);
            throw ex; // preserve existing exception handling pipeline (ExceptionTranslator)
        }
    }

    /**
     * {@code GET /authenticate} : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request.
     * @return the login if the user is authenticated.
     */
    @GetMapping("/authenticate")
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        final var refreshTokenOpt = tokenRepository.findByToken(request.getRefreshToken());
        if (refreshTokenOpt.isEmpty() || refreshTokenOpt.get().isExpired()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }

        // Retrieve the user. How you do this depends on your user management.
        return refreshTokenOpt
            .map(refreshToken -> {
                String username = refreshTokenOpt.get().getUser().getLogin();
                // generate another valid accessToken
                String newAccessToken = tokenService.generateAccessToken(username);

                // Revoke old refresh token/tokens (optional - implement rotation)
                // will delete all user's stored refresh tokens and create a single one
                RefreshTokenDto newRefreshToken = tokenService.rotateRefreshToken(refreshToken);

                return ResponseEntity.ok(new TokenRefreshResponse(
                    newAccessToken,
                    newRefreshToken.getToken()
                ));
            })
            .orElseThrow(() -> new TokenRefreshException("Invalid refresh token"));
    }
}
