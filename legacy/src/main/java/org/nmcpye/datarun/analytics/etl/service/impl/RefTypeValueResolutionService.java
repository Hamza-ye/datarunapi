package org.nmcpye.datarun.analytics.etl.service.impl;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.analytics.etl.repository.DimOptionJdbcRepository;
import org.nmcpye.datarun.analytics.etl.repository.DimOrgUnitJdbcRepository;
import org.nmcpye.datarun.analytics.etl.repository.DimTeamJdbcRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefTypeValueResolutionService {

    private final DimOptionJdbcRepository dimOptionRepo;
    private final DimOrgUnitJdbcRepository dimOrgUnitRepo;
    private final DimTeamJdbcRepository dimTeamJdbcRepo;
    public final static String REF_RESOLUTION_CACHE_NAME = "refResolutionCache";

    @Cacheable(value = REF_RESOLUTION_CACHE_NAME, key = "#refType + ':' +(#optionSetUid==null?'':#optionSetUid) + ':' + #token")
    public String resolve(String token, String refType, String optionSetUid) {
        // 2) deterministic lookups
        String resolvedUid = null;
        if ("option".equalsIgnoreCase(refType)) {
            resolvedUid = resolveOption(token, optionSetUid);
        } else if ("org_unit".equalsIgnoreCase(refType) || "orgunit".equalsIgnoreCase(refType)) {
            resolvedUid = resolveOrgUnit(token);
        } else if ("team".equalsIgnoreCase(refType)) {
            resolvedUid = resolveTeam(token);
        }

        // 3) not found — persist miss
        return resolvedUid;
    }

    String resolveOption(String token, String optionSetUid) {
        Map<String, Object> opt = dimOptionRepo.findByOptionSetAndToken(optionSetUid, token).orElseGet(HashMap::new);
        return (String) opt.getOrDefault("uid", null);
    }

    String resolveOrgUnit(String token) {
        Map<String, Object> ou = dimOrgUnitRepo.findByCodeOrUidOrId(token).orElseGet(HashMap::new);
        return (String) ou.getOrDefault("uid", null);
    }

    String resolveTeam(String token) {
        Optional<Map<String, Object>> team = dimTeamJdbcRepo.findByUidOrId(token);
        String resolvedUid = null;
        if (team.isPresent()) {
            resolvedUid = (String) team.get().getOrDefault("uid", null);
        }

        return resolvedUid;
    }
}
