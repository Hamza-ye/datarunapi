package org.nmcpye.datarun.config.datarun;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BasicUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    @Override
    protected String obtainUsername(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Basic ")) {
            String encodedUserPassword = authorization.replace("Basic ", "");
            String decodedUserPassword = new String(Base64.getDecoder().decode(encodedUserPassword), StandardCharsets.UTF_8);
            return decodedUserPassword.split(":")[0];
        }
        return null;
    }

    @Override
    protected String obtainPassword(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Basic ")) {
            String encodedUserPassword = authorization.replace("Basic ", "");
            String decodedUserPassword =
                new String(Base64.getDecoder()
                    .decode(encodedUserPassword),
                    StandardCharsets.UTF_8);
            return decodedUserPassword.split(":")[1];
        }
        return null;
    }
}

