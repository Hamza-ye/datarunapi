package org.nmcpye.datarun.web.rest.v1.party.resolution;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.party.dto.PartyResolutionRequest;
import org.nmcpye.datarun.party.dto.ResolvedParty;
import org.nmcpye.datarun.party.resolution.PartyResolutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/parties")
@RequiredArgsConstructor
public class PartyResolutionController {

    private final PartyResolutionService resolutionService;

    @PostMapping("/resolve")
    public ResponseEntity<List<ResolvedParty>> resolveParties(
        @RequestBody ResolveRequestDto dto,
        // Assume we extract current user ID from security context
        @RequestAttribute(name = "userId", required = false) String userId
    ) {
        // Map Web DTO to Internal Context DTO
        // If userId is missing from auth (e.g. dev mode), use the one in body or fail
        String effectiveUserId = (userId != null) ? userId : dto.getUserId();

        var request = PartyResolutionRequest.builder()
            .assignmentId(dto.getAssignmentId())
            .userId(effectiveUserId)
            .role(dto.getRole())
            .vocabularyId(dto.getVocabularyId())
            .searchQuery(dto.getQ())
            .limit(dto.getLimit() > 0 ? dto.getLimit() : 20)
            .offset(dto.getOffset())
            .contextValues(dto.getContextValues() != null ? dto.getContextValues() : Map.of())
            .build();

        List<ResolvedParty> results = resolutionService.resolveParties(request);

        return ResponseEntity.ok(results);
    }

    @Data
    public static class ResolveRequestDto {
        private String assignmentId;
        private String vocabularyId;
        private String userId; // Optional if using token auth
        private String role;
        private String q; // Search query
        private int limit;
        private int offset;
        private Map<String, Object> contextValues;
    }
}
