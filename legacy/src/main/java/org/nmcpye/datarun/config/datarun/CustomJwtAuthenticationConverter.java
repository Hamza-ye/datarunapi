package org.nmcpye.datarun.config.datarun;

import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.iam.security.DomainUserDetailsService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;

/**
 * @author Hamza Assada 21/04/2025 (7amza.it@gmail.com)
 */
public class CustomJwtAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

    private final DomainUserDetailsService userDetailsService;

    public CustomJwtAuthenticationConverter(DomainUserDetailsService uds) {
        this.userDetailsService = uds;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // 1. Extract username (subject)
        String username = jwt.getSubject();

        // 2. Load full CurrentUserDetails
        CurrentUserDetails userDetails =
            (CurrentUserDetails) userDetailsService.loadUserByUsername(username);

        // 3. Extract authorities from token claims (e.g., “auth” or “roles”)
        Collection<GrantedAuthority> authorities =
            // you may parse a space‑delimited claim or use a JwtGrantedAuthoritiesConverter
            new JwtGrantedAuthoritiesConverter().convert(jwt);

        // 4. Build an Authentication with your userDetails as principal
        return new UsernamePasswordAuthenticationToken(
            userDetails, jwt.getTokenValue(), userDetails.getAuthorities()
        );
    }
}

