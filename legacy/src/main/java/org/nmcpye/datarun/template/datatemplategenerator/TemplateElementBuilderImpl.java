package org.nmcpye.datarun.template.datatemplategenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.template.datatemplate.DataType;
import org.nmcpye.datarun.template.datatemplate.SemanticType;
import org.nmcpye.datarun.template.datatemplate.TemplateElement;
import org.nmcpye.datarun.template.datatemplate.TemplateVersion;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * Builds TemplateElement instances. Keeps responsibility small: mapping / normalization.
 * Persistence is delegated to Publisher.
 *
 * @author Hamza Assada
 * @since 09/09/2025
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateElementBuilderImpl implements TemplateElementBuilder {

    @Override
    public TemplateElement buildTemplateElementFromField(FieldTemplateElementDto f,
                                                         PathMetadata meta,
                                                         TemplateVersion templateVersion) {
        Objects.requireNonNull(f);
        Objects.requireNonNull(meta);
        Objects.requireNonNull(templateVersion);

        final var h = hashToLong(String.join("|", templateVersion.getUid(),
            meta.getJsonDataPath(), DataType.ARRAY.name(), SemanticType.Repeat.name()));

        // synthetic dataElementUid for repeat grain
        final var schemaFingerprint = "E_" + Long.toUnsignedString(h, 36);

        var cfg = TemplateElement.builder()
//            .canonicalElementUid(schemaFingerprint) // set by caller
            .uid(schemaFingerprint)
            .templateUid(templateVersion.getTemplateUid())
            .templateVersionUid(templateVersion.getUid())
            .templateVersionNo(templateVersion.getVersionNumber())
            .name(f.getName())
            .jsonDataPath(meta.getJsonDataPath())
            .canonicalPath(meta.getCanonicalPath())
            .dataType(DataType.fromValueType(f.getType()))
            .semanticType(SemanticType.fromValueType(f.getType()))
            .parentRepeatJsonDataPath(meta.getParentRepeatIdPath())
            .parentRepeatCanonicalPath(meta.getCanonicalParentRepeatPath())
            .sortOrder(f.getOrder())
            .optionSetUid(f.getOptionSet())
            .displayLabel(f.getLabel());
        return cfg.build();
    }

    @Override
    public TemplateElement buildTemplateElementFromRepeat(SectionTemplateElementDto section,
                                                          PathMetadata meta,
                                                          TemplateVersion templateVersion) {
        Objects.requireNonNull(section);
        Objects.requireNonNull(meta);
        Objects.requireNonNull(templateVersion);

        var cfg = TemplateElement.builder()
            .templateUid(templateVersion.getTemplateUid())
            .templateVersionUid(templateVersion.getUid())
            .templateVersionNo(templateVersion.getVersionNumber())
            .dataType(DataType.ARRAY)
            .semanticType(SemanticType.Repeat);

        final var h = hashToLong(String.join("|", templateVersion.getUid(),
            meta.getJsonDataPath(), DataType.ARRAY.name(), SemanticType.Repeat.name()));

        // synthetic dataElementUid for repeat grain
        final var schemaFingerprint = "R_" + Math.abs(h);

        cfg
//            .canonicalElementUid(canonicalElementUid) // set by caller
            .uid(schemaFingerprint)
            .jsonDataPath(meta.getJsonDataPath())
            .name(section.getName())
            .canonicalPath(meta.getCanonicalPath())
            .parentRepeatJsonDataPath(meta.getParentRepeatIdPath())
            .parentRepeatCanonicalPath(meta.getCanonicalParentRepeatPath())
            .displayLabel(section.getLabel())
            .sortOrder(section.getOrder());
        return cfg.build();
    }

    // ---------- helper methods (small) ------------
    public static long hashToLong(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            ByteBuffer buf = ByteBuffer.wrap(digest, 0, Long.BYTES);
            return buf.getLong();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute hash", e);
        }
    }
}
