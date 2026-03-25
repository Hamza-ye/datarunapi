package org.nmcpye.datarun.party.resolution;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.nmcpye.datarun.party.dto.BindingResult;
import org.nmcpye.datarun.party.dto.CombineMode;
import org.nmcpye.datarun.party.dto.PartyResolutionRequest;
import org.nmcpye.datarun.party.dto.ResolvedParty;
import org.nmcpye.datarun.party.entities.PartySetKind;
import org.nmcpye.datarun.party.resolution.engine.PartyResolutionEngine;
import org.nmcpye.datarun.party.resolution.engine.BindingResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.nmcpye.datarun.jooq.public_.tables.PartySet.PARTY_SET;

@Service
@RequiredArgsConstructor
public class PartyResolutionService {

    private final BindingResolver bindingResolver;
    private final PartyResolutionEngine engine;
    private final DSLContext dsl; // Used for the simple config lookup

    @Transactional(readOnly = true)
    public List<ResolvedParty> resolveParties(PartyResolutionRequest request) {

        // 1. Resolve Bindings (The "Brain")
        // Note: You might need to look up templateFallbackPartySetId from the
        // Vocabulary ID first.
        // For MVP, passing null for fallback.
        List<BindingResult> bindings = bindingResolver.resolveBindings(
                request.getAssignmentId(),
                request.getVocabularyId(),
                request.getRole(),
                request.getUserId(),
                null // TODO: Look up data_template.party_set_ref if needed later
        );

        if (bindings.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Execute Strategies & Combine Results
        // We start with the results of the first binding, then merge subsequent ones.
        List<ResolvedParty> accumulatedParties = new ArrayList<>();
        boolean firstPass = true;

        for (BindingResult binding : bindings) {
            // A. Fetch Config (Kind + Spec)
            // In a real app, cache this lookup.
            var config = dsl.select(PARTY_SET.KIND, PARTY_SET.SPEC, PARTY_SET.IS_MATERIALIZED)
                    .from(PARTY_SET)
                    .where(PARTY_SET.ID.eq(UUID.fromString(binding.getPartySetId())))
                    .fetchOne();

            if (config == null)
                continue; // Should not happen given referential integrity

            PartySetKind kind = PartySetKind.valueOf(config.value1()); // Enum mapping
            String spec = config.value2().data(); // JSONB string

            // Default to TRUE (secure) if null, for safety
            boolean isMaterialized = config.value3() == null || config.value3();

            // B. Execute Strategy
            List<ResolvedParty> currentSetParties = engine.executeStrategy(
                    kind,
                    binding.getPartySetId(),
                    spec,
                    isMaterialized,
                    request);

            // C. Combine (Union / Intersect)
            if (firstPass) {
                accumulatedParties.addAll(currentSetParties);
                firstPass = false;
            } else {
                accumulatedParties = applyCombination(
                        accumulatedParties,
                        currentSetParties,
                        binding.getCombineMode());
            }
        }

        // 3. Final Polish (Deduplication)
        // UNION usually implies uniqueness. INTERSECT definitely does.
        // We assume ResolvedParty.equals/hashCode relies on ID.
        return accumulatedParties.stream().distinct().collect(Collectors.toList());
    }

    private List<ResolvedParty> applyCombination(
            List<ResolvedParty> existing,
            List<ResolvedParty> newParties,
            CombineMode mode) {
        if (mode == CombineMode.INTERSECT) {
            // Only keep parties present in both lists
            // O(N*M) naive, but lists are usually page-sized (small).
            // Optimizable with Sets if lists are large.
            Set<UUID> newIds = newParties.stream()
                    .map(ResolvedParty::getId)
                    .collect(Collectors.toSet());

            return existing.stream()
                    .filter(p -> newIds.contains(p.getId()))
                    .collect(Collectors.toList());
        }

        // Default: UNION
        List<ResolvedParty> result = new ArrayList<>(existing);
        result.addAll(newParties);
        return result;
    }
}
