package org.nmcpye.datarun.web.rest.v1.party;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

import org.nmcpye.datarun.party.dto.AssignmentManifestDto;
import org.nmcpye.datarun.party.dto.PagedRequest;
import org.nmcpye.datarun.party.dto.ResolvedParty;
import org.nmcpye.datarun.party.service.ManifestService;
import org.nmcpye.datarun.party.service.PartySetService;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.paging.PagingConfigurator;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
public class SyncResource {

    private final PartySetService partySetService;
    private final ManifestService manifestService;

    /**
     * The Bootstrap Endpoint: Called when the app launches or syncs.
     */
    @Schema(description = "The Bootstrap Endpoint: Called when the app launches or syncs.")
    @GetMapping("/manifest")
    public ResponseEntity<PagedResponse<AssignmentManifestDto>> getManifest(
            PagedRequest pagedRequest,
            @AuthenticationPrincipal CurrentUserDetails user) {
        // Validation logic for userUid would go here
        try {
            Page<AssignmentManifestDto> processedPage = manifestService.buildManifest(user.getId(),
                    user.getUserTeamsIds(),
                    user.getUserGroupsIds(), pagedRequest);

            String next = PagingConfigurator.createNextPageLink(processedPage);

            PagedResponse<AssignmentManifestDto> response = PagingConfigurator.initPageResponse(processedPage, next,
                    "context");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/partySets/{id}/parties")
    public ResponseEntity<PagedResponse<ResolvedParty>> getPartySetMembers(
            @PathVariable String id, PagedRequest pagedRequest,
            @AuthenticationPrincipal CurrentUserDetails user) {
        try {
            Page<ResolvedParty> processedPage = partySetService
                    .findPartiesBySetId(id, pagedRequest, user);

            String next = PagingConfigurator.createNextPageLink(processedPage);

            PagedResponse<ResolvedParty> response = PagingConfigurator.initPageResponse(processedPage, next, "sync");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
