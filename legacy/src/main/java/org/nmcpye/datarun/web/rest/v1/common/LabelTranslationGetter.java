package org.nmcpye.datarun.web.rest.v1.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Named;
import org.nmcpye.datarun.sharedkernal.translation.Translation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Hamza Assada
 * @since 11/04/2026
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface LabelTranslationGetter {
    @Named("labelTranslationGetter")
    default Map<String, String> getLabel(Set<Translation> translations) {
        if (translations == null || translations.isEmpty()) {
            return null;
        }

        Map<String, String> label = new HashMap<>();

        label.put("ar", getTranslation(translations, "name", "ar", null));
        label.put("en", getTranslation(translations, "name", "en", null));
        return label;
    }


    default String getTranslation(Set<Translation> translations, String translationKey, String localeKey, String defaultValue) {
        final String defaultTranslation = defaultValue != null ? defaultValue.trim() : null;

        if (localeKey == null || translationKey == null || translations == null
            || CollectionUtils.isEmpty(translations)) {
            return defaultTranslation;
        }

        for (Translation translation : translations) {
            if (localeKey.equals(translation.getLocale()) && translationKey.equals(translation.getProperty()) &&
                !StringUtils.isEmpty(translation.getValue())) {
                return translation.getValue();
            }
        }

        return defaultTranslation;
    }
}
