package org.nmcpye.datarun.party.resolution.engine;

import org.nmcpye.datarun.party.dto.PartyResolutionRequest;
import org.nmcpye.datarun.party.dto.ResolvedParty;
import org.nmcpye.datarun.party.entities.PartySetKind;
import org.nmcpye.datarun.party.resolution.strategies.PartySetStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Hamza Assada 29/12/2025
 */
@Service
public class PartyResolutionEngine {

    private final Map<PartySetKind, PartySetStrategy> strategies;

    public PartyResolutionEngine(List<PartySetStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(PartySetStrategy::getKind, Function.identity()));
    }

    public List<ResolvedParty> executeStrategy(PartySetKind kind,
            String partySetId,
            String spec,
            boolean isMaterialized,
            PartyResolutionRequest request) {
        PartySetStrategy strategy = strategies.get(kind);

        if (strategy == null) {
            throw new UnsupportedOperationException("No strategy found for kind: " + kind);
        }

        return strategy.resolve(partySetId, spec, isMaterialized, request);
    }
}
