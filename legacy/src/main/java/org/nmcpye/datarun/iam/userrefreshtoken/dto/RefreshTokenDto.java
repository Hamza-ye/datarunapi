package org.nmcpye.datarun.iam.userrefreshtoken.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nmcpye.datarun.iam.userrefreshtoken.RefreshToken;

import java.time.Instant;
import java.util.Objects;

/**
 * @author Hamza Assada 25/04/2025 (7amza.it@gmail.com)
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenDto {
    private String token;
    private Instant expiryDate;
    private RefreshTokenUser user;

    public RefreshTokenDto(RefreshToken refreshToken) {
        this.token = refreshToken.getToken();
        this.expiryDate = refreshToken.getExpiryDate();
        final var user = refreshToken.getUser();
        this.user = new RefreshTokenUser(user.getUid(), user.getLogin());
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RefreshTokenUser {
        private String uid;
        private String login;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(getExpiryDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RefreshTokenDto that)) return false;
        return Objects.equals(getToken(), that.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getToken());
    }
}
