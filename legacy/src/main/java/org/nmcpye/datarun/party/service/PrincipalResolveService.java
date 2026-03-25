package org.nmcpye.datarun.party.service;

import lombok.*;
import org.nmcpye.datarun.iam.team.repository.TeamRepository;
import org.nmcpye.datarun.iam.usegroup.repository.UserGroupRepository;
import org.nmcpye.datarun.iam.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrincipalResolveService {
    private final TeamRepository teamRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;

    /**
     * Filters holder (nullable fields ignored).
     */
    @Data
    @Builder
    public static class ResolvedPrincipal {
        public String principalId;
        public String principalUid;
        public String principalType;
    }

    public Optional<ResolvedPrincipal> resolvePrincipal(String principalType, String principalId) {
        if (principalType == null || principalId == null) {
            return Optional.empty();
        }

        return switch (principalType) {
            case "User" -> userRepository.findById(principalId).map(p -> ResolvedPrincipal.builder()
                    .principalUid(p.getUid())
                    .principalId(p.getId())
                    .principalType("User")
                    .build());
            case "Team" -> teamRepository.findById(principalId).map(p -> ResolvedPrincipal.builder()
                    .principalUid(p.getUid())
                    .principalId(p.getId())
                    .principalType("User")
                    .build());
            case "UserGroup" -> userGroupRepository.findById(principalId).map(p -> ResolvedPrincipal.builder()
                    .principalUid(p.getUid())
                    .principalId(p.getId())
                    .principalType("User")
                    .build());
            default -> throw new IllegalStateException("Unexpected principalType: " + principalType);
        };
    }
}
