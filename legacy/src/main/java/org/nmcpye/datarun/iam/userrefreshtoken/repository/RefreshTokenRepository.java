package org.nmcpye.datarun.iam.userrefreshtoken.repository;

import org.nmcpye.datarun.iam.userrefreshtoken.RefreshToken;
import org.nmcpye.datarun.iam.userrefreshtoken.dto.RefreshTokenDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * @author Hamza Assada 16/04/2025 (7amza.it@gmail.com)
 */
@SuppressWarnings("unused")
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    <T> Optional<T> findByToken(String lastname, Class<T> type);

    Optional<RefreshTokenDto> findByToken(String token);

    void deleteByUserUid(String userUid);

    void deleteAllByExpiryDateBefore(Instant time);
}
