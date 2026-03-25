package org.nmcpye.datarun.template.datatemplate.service;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.analytics.etl.model.TemplateElementMap;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;

/// @author Hamza Assada
/// @since 10/08/2025
@Service
@RequiredArgsConstructor
public class TemplateElementService {
    public final static String TEMPLATE_MAP_CACHE = "templateMapCacheByTemplateAndVersion";

    private final DataTemplateInstanceService templateInstanceService;

    /// create elementMap and cache it.
    ///
    /// @param id         template id
    /// @param versionUid template version id
    /// @return elementMap cache;
    @Cacheable(cacheNames = TEMPLATE_MAP_CACHE)
    public TemplateElementMap getTemplateElementMap(String id, String versionUid) {
//        final var elementsConfMap =
//            templateConfigRepository.findAllByTemplateUidAndTemplateVersionUid(id, versionUid).stream().collect(Collectors.toMap(
//                TemplateElement::getNamePath, Function.identity()));
        return new TemplateElementMap(templateInstanceService.findByTemplateAndVersionUid(id, versionUid)
            .orElseThrow(), Map.of()/*elementsConfMap*/);
    }

    public TemplateElementMap getTemplateElementMap(String id, Integer version) {
//        final var elementsConfMap =
//            templateConfigRepository.findAllByTemplateUidAndVersionNo(id, version).stream().collect(Collectors.toMap(
//                TemplateElement::getNamePath, Function.identity()));
        return new TemplateElementMap(templateInstanceService.findByTemplateAndVersionNo(id, version)
            .orElseThrow(), Map.of()/*elementsConfMap*/);
    }
}
