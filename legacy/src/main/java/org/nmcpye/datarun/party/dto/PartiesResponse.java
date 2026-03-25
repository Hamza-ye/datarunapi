package org.nmcpye.datarun.party.dto;

import lombok.*;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartiesResponse {
    private String partySetId;
    private String resolvedFrom;
    private Integer totalEstimate;
    private Integer cacheTtlSeconds;
    private List<ResolvedParty> results;
}
