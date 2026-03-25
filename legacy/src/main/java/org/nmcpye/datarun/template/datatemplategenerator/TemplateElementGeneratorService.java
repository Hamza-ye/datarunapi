package org.nmcpye.datarun.template.datatemplategenerator;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.template.datatemplate.CanonicalElement;
import org.nmcpye.datarun.template.datatemplate.DataType;
import org.nmcpye.datarun.template.datatemplate.SemanticType;
import org.nmcpye.datarun.template.datatemplate.TemplateElement;
import org.nmcpye.datarun.template.datatemplate.repository.MetadataUpsertService;
import org.nmcpye.datarun.template.datatemplate.repository.TemplateVersionRepository;
import org.nmcpye.datarun.jpa.option.OptionSet;
import org.nmcpye.datarun.jpa.option.repository.OptionSetRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateElementGeneratorService {
    private final TemplateVersionRepository versionRepository;
    private final FlatTemplateProcessor flatProcessor;
    private final TemplateElementBuilder elementBuilder;
    private final MetadataUpsertService metadataUpsertService;
    private final OptionSetRepository optionSetRepository;

    /**
     * Generates template elements and persists them via MetadataUpsertService (batch upsert).
     * This method is idempotent (uses deterministic UIDs) and safe for concurrent runs.
     */
    @Transactional
    public List<TemplateElement> generate(String templateUid, String versionUid) {
        Objects.requireNonNull(templateUid, "templateUid required");
        Objects.requireNonNull(versionUid, "versionUid required");

        var dtv = versionRepository.findByTemplateUidAndUid(templateUid, versionUid)
            .orElseThrow(() -> new IllegalArgumentException("template version not found"));

        FlatTemplateProcessor.TemplateFlatSnapshot snap = flatProcessor.process(dtv);
        MaterializedPathResolver resolver = new MaterializedPathResolver(snap.sectionByName);

        // optionSetUid -> optionSetId (bulk lookup)
        Map<String, String> optionSetIdByUidMap = optionSetRepository.findAllByUidIn(
            snap.fields.stream()
                .map(FieldTemplateElementDto::getOptionSet)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(OptionSet::getUid, OptionSet::getId));

        // canonicalUid -> CanonicalElement (in-memory, deduped)
        Map<String, CanonicalElement> canonicalByUid = new LinkedHashMap<>();

        // helper map: jsonDataPath -> repeat canonical uid (so child fields can lookup parent repeat ce)
        Map<String, String> repeatJsonPathToCanonicalUid = new HashMap<>();
        Map<String, String> repeatCanonicalUidToParentJsonPath = new HashMap<>();

        // 1) Build repeat configs and their canonical elements (one per repeatable section)
        List<TemplateElement> out = new ArrayList<>(snap.fields.size() + snap.sectionByName.size());

        for (SectionTemplateElementDto section : snap.sectionByName.values()) {
            PathMetadata sectionMeta = resolver.resolveForSection(section);
            if (!Boolean.TRUE.equals(section.getRepeatable())) continue;

            TemplateElement repeatCfg = elementBuilder.buildTemplateElementFromRepeat(section, sectionMeta, dtv);
            // compute deterministic canonical uid for this repeat (repeat semantic & ARRAY type)
            final String repeatCanonicalKey = canonicalKeys(
                repeatCfg.getTemplateUid(),
                repeatCfg.getCanonicalPath(),
                DataType.ARRAY.name(),
                SemanticType.Repeat.name()
            );
            final String repeatCanonicalUid = canonicalUidFromStringAsUuid(repeatCanonicalKey);

            // attach to the template element
            repeatCfg.setCanonicalElementId(repeatCanonicalUid);

            CanonicalElement ce = createCanonicalElement(repeatCfg, repeatCanonicalUid, null);
            canonicalByUid.putIfAbsent(repeatCanonicalUid, ce);


            // record mappings for pass B and for fields to reference
            if (repeatCfg.getJsonDataPath() != null && !repeatCfg.getJsonDataPath().isBlank()) {
                repeatJsonPathToCanonicalUid.put(repeatCfg.getJsonDataPath(), repeatCanonicalUid);
            }
            // store the parent json path (may be null)
            repeatCanonicalUidToParentJsonPath.put(repeatCanonicalUid, repeatCfg.getParentRepeatJsonDataPath());

            out.add(repeatCfg);
        }

        // PASS B: wire parentRepeatUid for repeat CEs using the map above
        for (Map.Entry<String, String> e : repeatCanonicalUidToParentJsonPath.entrySet()) {
            String canonicalUid = e.getKey();
            String parentJsonPath = e.getValue(); // may be null
            if (parentJsonPath == null || parentJsonPath.isBlank()) continue;
            String parentUid = repeatJsonPathToCanonicalUid.get(parentJsonPath);
            CanonicalElement ce = canonicalByUid.get(canonicalUid);
            if (ce != null && parentUid != null && !parentUid.equals(ce.getParentRepeatId())) {
                ce.setParentRepeatId(parentUid);
            }
        }

        // 2) Build field configs and their canonical elements
        Set<String> seenIdPath = new HashSet<>();
        for (FieldTemplateElementDto f : snap.fields) {
            PathMetadata meta = resolver.resolveForField(f);

            if (!seenIdPath.add(meta.getJsonDataIdPath())) {
                throw new IllegalStateException("Duplicate idPath detected: " + meta.getJsonDataIdPath());
            }

            TemplateElement cfg = elementBuilder.buildTemplateElementFromField(f, meta, dtv);

            // optionSetId resolution
            if (cfg.getOptionSetUid() != null) {
                cfg.setOptionSetId(optionSetIdByUidMap.get(cfg.getOptionSetUid()));
            }


            // determine parent repeat canonical uid (if any) by looking up by parentRepeatJsonDataPath
            String parentRepeatUid = null;
            String parentRepeatJsonPath = cfg.getParentRepeatJsonDataPath();
            if (parentRepeatJsonPath != null) {
                parentRepeatUid = repeatJsonPathToCanonicalUid.get(parentRepeatJsonPath);
            }

            // build canonical key and uid for this field
            final String fieldCanonicalKey = canonicalKeys(
                cfg.getTemplateUid(),
                cfg.getCanonicalPath(),
                cfg.getDataType() == null ? null : cfg.getDataType().name(),
                cfg.getSemanticType() == null ? null : cfg.getSemanticType().name()
            );

            final String fieldCanonicalUid = canonicalUidFromStringAsUuid(fieldCanonicalKey);

            // attach to the template element
            cfg.setCanonicalElementId(fieldCanonicalUid);

            // create or merge canonical element

            CanonicalElement ce = canonicalByUid.get(fieldCanonicalUid);
            if (ce == null) {
                ce = createCanonicalElement(cfg, fieldCanonicalUid, parentRepeatUid);
                canonicalByUid.put(fieldCanonicalUid, ce);
            } else {
                // ensure parentRepeatUid exists if not set (prefer existing value)
                if (ce.getParentRepeatId() == null && parentRepeatUid != null) {
                    ce.setParentRepeatId(parentRepeatUid);
                }
                // merge json data path
                ce.setJsonDataPaths(mergeJsonDataPath(ce, cfg.getJsonDataPath()));
            }

            out.add(cfg);
        }

        // 3) Prepare lists and upsert - canonical elements first
        SafeNameUtils.ensureUniqueSafeNamesBasePreferred(canonicalByUid);
        List<CanonicalElement> canonicalList = new ArrayList<>(canonicalByUid.values());

        // canonical elements upsert (this will append json_data_paths via SQL OR merge logic)
        metadataUpsertService.upsertCanonicalElements(canonicalList);

        // then template elements upsert
        metadataUpsertService.upsertTemplateElements(out);

        return out;
    }

    // ---------- helpers ----------

    /**
     * Merge jsonDataPath into ce.jsonDataPaths (deduplicating).
     */
    private static Set<String> mergeJsonDataPath(CanonicalElement ce, String path) {
        Set<String> candidates = Optional.ofNullable(ce.getJsonDataPaths()).orElse(new HashSet<>());
        if (path != null && !path.isBlank()) {
            if (!candidates.contains(path)) {
                Set<String> copy = new LinkedHashSet<>(candidates);
                copy.add(path);
                ce.setJsonDataPaths(copy);
                return copy;
            }
        }
        return candidates;
    }

    /**
     * Build a CanonicalElement instance from a TemplateElement.
     *
     * @param e               template element
     * @param canonicalUid    deterministic canonical uid (precomputed)
     * @param parentRepeatUid the parent repeat canonical uid (may be null)
     */
    private static CanonicalElement createCanonicalElement(TemplateElement e, String canonicalUid, String parentRepeatUid) {

        CanonicalElement.CanonicalElementBuilder builder = CanonicalElement.builder()
            .id(canonicalUid)
            .templateUid(e.getTemplateUid())
            .preferredName(e.getName() != null ? e.getName() : "unnamed")
            .dataType(e.getDataType())
            .semanticType(e.getSemanticType())
            .displayLabel(e.getDisplayLabel())
            .canonicalPath(e.getCanonicalPath())
            .optionSetUid(e.getOptionSetUid())
            .optionSetId(e.getOptionSetId())
            .parentRepeatId(parentRepeatUid)
            .createdDate(Instant.now());

        // initialize jsonDataPaths as a set with current element's jsonDataPath (if any)
        if (e.getJsonDataPath() != null && !e.getJsonDataPath().isBlank()) {
            builder.jsonDataPaths(Set.of(e.getJsonDataPath()));
        } else {
            builder.jsonDataPaths(Set.of());
        }

        builder.safeName(e.getName());

        return builder.build();
    }

    /**
     * Deterministic canonical UID (UUID name-based).
     */
    private static String canonicalUidFromStringAsUuid(String key) {
        if (key == null) key = "";
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString();
    }

    public static String canonicalKeys(String templateUid, String canonicalPath, String dataType,
                                       String semanticType) {
        return String.join("|",
            templateUid == null ? "" : templateUid,
            canonicalPath == null ? "" : canonicalPath,
            dataType == null ? "" : dataType,
            semanticType == null ? "" : semanticType);
    }
}
