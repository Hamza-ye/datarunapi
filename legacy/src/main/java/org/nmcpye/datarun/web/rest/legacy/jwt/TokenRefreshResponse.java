package org.nmcpye.datarun.web.rest.legacy.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * @author Hamza Assada 16/04/2025 (7amza.it@gmail.com)
 */
@Value
@Builder
@AllArgsConstructor
public class TokenRefreshResponse {
    String accessToken;
    String refreshToken;
}
