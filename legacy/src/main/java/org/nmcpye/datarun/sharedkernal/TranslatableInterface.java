package org.nmcpye.datarun.sharedkernal;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.nmcpye.datarun.sharedkernal.translation.Translation;
import org.springframework.data.annotation.Transient;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Hamza Assada
 * @since 20/03/2025
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface TranslatableInterface {
    /**
     * Cache for object translations, where the cache key is a combination of
     * locale and translation property, and value is the translated value.
     */
    @JsonIgnore
    @Transient
    Map<String, String> translationCache = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // Comparable implementation
    // -------------------------------------------------------------------------

    @JsonIgnore
    default Map<String, String> getTranslationCache() {
        return translationCache;
    }


    // -------------------------------------------------------------------------
    // Setters and getters
    // -------------------------------------------------------------------------
    String getName();
    Set<Translation> getTranslations();

    void setTranslations(Set<Translation> updatedTranslations);

    @JsonGetter("displayName")
    default String getDisplayName() {
        return getTranslation("name", getName());
    }

    /**
     * Returns a translated value for this object for the given property. The
     * current locale is read from the user context.
     *
     * @param translationKey the translation key.
     * @param defaultValue   the value to use if there are no translations.
     * @return a translated value.
     */
    default String getTranslation(String translationKey, String defaultValue) {
        String localeKey = SecurityUtils.getCurrentUserDetails()
            .map(CurrentUserDetails::getLangKey)
            .orElse(null);
        final String defaultTranslation = defaultValue != null ? defaultValue.trim() : null;

        if (localeKey == null || translationKey == null || CollectionUtils.isEmpty(getTranslations())) {
            return defaultValue;
        }

        return getTranslationValue(localeKey, translationKey, defaultTranslation);
    }

    default String getTranslation(String translationKey, String localeKey, String defaultValue) {
        final String defaultTranslation = defaultValue != null ? defaultValue.trim() : null;

        if (localeKey == null || translationKey == null || CollectionUtils.isEmpty(getTranslations())) {
            return defaultValue;
        }

        return getTranslationValue(localeKey, translationKey, defaultTranslation);
    }

    /**
     * Get Translation value from {@code Set<Translation>} by given locale and
     * translationKey
     *
     * @return Translation value if exists, otherwise return default value.
     */
    private String getTranslationValue(String locale, String translationKey, String defaultValue) {
        for (Translation translation : getTranslations()) {
            if (locale.equals(translation.getLocale()) && translationKey.equals(translation.getProperty()) &&
                !StringUtils.isEmpty(translation.getValue())) {
                return translation.getValue();
            }
        }

        return defaultValue;
    }

    @JsonSetter("label")
    default TranslatableInterface setLabel(Map<String, String> label) {
        // Create new name translations from the input map
        if (label != null) {
            Set<Translation> newNameTranslations = label.entrySet().stream()
                .map(entry -> Translation.builder()
                    .locale(entry.getKey())
                    .property("name")
                    .value(entry.getValue())
                    .build())
                .collect(Collectors.toSet());
            // Create updated translation set:
            // 1. Preserve existing non-name translations
            // 2. Add/replace name translations from new map
            Set<Translation> updatedTranslations = getTranslations().stream()
                .filter(t -> !"name".equals(t.getProperty())) // Keep non-name translations
                .collect(Collectors.toCollection(HashSet::new));

            updatedTranslations.addAll(newNameTranslations); // Merge new name translations

            setTranslations(updatedTranslations);
        }
        return this;
    }


    @Transient
    @JsonProperty
    default Map<String, String> getLabel() {
        if (getTranslations() == null || getTranslations().isEmpty()) {
            return null;
        }
        return Map.ofEntries(
            Map.entry("ar", getTranslation("name", "ar", getName())),
            Map.entry("en", getTranslation("name", "en", getName()))
        );
    }
}
