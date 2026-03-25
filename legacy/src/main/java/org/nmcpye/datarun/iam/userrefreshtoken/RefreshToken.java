package org.nmcpye.datarun.iam.userrefreshtoken;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import org.nmcpye.datarun.iam.user.User;
import org.nmcpye.datarun.sharedkernal.IdentifiableObject;
import org.nmcpye.datarun.sharedkernal.uidgenerate.CodeGenerator;

import java.time.Instant;
import java.util.Objects;

/**
 * @author Hamza Assada 15/04/2025 (7amza.it@gmail.com)
 */
@Entity
@Table(name = "refresh_token", indexes = {
    @Index(name = "idx_refresh_token_token_unq", columnList = "token", unique = true),
})
@Getter
@Setter
public class RefreshToken {
    /**
     * ULID id (Universally Unique Lexicographically Sortable Identifier).
     * Length: 26 characters, Base32 encoded.
     */
    @Id
    @Column(name = "id", length = 26, updatable = false, nullable = false)
    private String id;


    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonSerialize(contentAs = IdentifiableObject.class)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties(value = {"teams", "password", "authorities", "translations"}, allowSetters = true)
    private User user; // Your user entity

    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }

    /**
     * Lifecycle hook to generate a ULID ID before persisting.
     */
    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = CodeGenerator.nextUlid();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RefreshToken that)) return false;
        return Objects.equals(getToken(), that.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getToken());
    }
}
