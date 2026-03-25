package org.nmcpye.datarun.sharedkernal;

import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

/**
 * Enum representing web API versions. The API version is exposed through the
 * API URL at <code>/api/{version}/{resource}</code>, where
 * <code>{version}</code> is a numeric value and must match a value of this
 * enum. If omitted, the <code>DEFAULT</code> value will be used. The API
 * resources can also be mapped to all versions using the <code>ALL</code>
 * value.
 * <p>
 * TODO The <code>DEFAULT</code> version must be updated for each release.
 */
@Getter
public enum DRunApiVersion {
    ALL(-1, true),
    V1(1),
    DEFAULT(V1.getVersion());

    final int version;

    final boolean ignore;

    DRunApiVersion(int version) {
        this.version = version;
        this.ignore = false;
    }

    DRunApiVersion(int version, boolean ignore) {
        this.version = version;
        this.ignore = ignore;
    }

    public String getVersionString() {
        return this == DEFAULT ? "" : String.valueOf(version);
    }

    /**
     * Indicates whether this version is equal to the given version.
     *
     * @param apiVersion the API version.
     */
    public boolean eq(DRunApiVersion apiVersion) {
        return version == apiVersion.getVersion();
    }

    /**
     * Indicates whether this version is less than the given version.
     *
     * @param apiVersion the API version.
     */
    public boolean lt(DRunApiVersion apiVersion) {
        return version < apiVersion.getVersion();
    }

    /**
     * Indicates whether this version is less than or equal to the given
     * version.
     *
     * @param apiVersion the API version.
     */
    public boolean le(DRunApiVersion apiVersion) {
        return version <= apiVersion.getVersion();
    }

    /**
     * Indicates whether this version is greater than the given version.
     *
     * @param apiVersion the API version.
     */
    public boolean gt(DRunApiVersion apiVersion) {
        return version > apiVersion.getVersion();
    }

    /**
     * Indicates whether this version is greater than or equal to the given
     * version.
     *
     * @param apiVersion the API version.
     */
    public boolean ge(DRunApiVersion apiVersion) {
        return version >= apiVersion.getVersion();
    }

    public static DRunApiVersion getVersion(int version) {
        for (int i = 0; i < DRunApiVersion.values().length; i++) {
            DRunApiVersion v = DRunApiVersion.values()[i];

            if (version == v.getVersion()) {
                return v;
            }
        }

        return DEFAULT;
    }

    private static final Pattern API_VERSION_PATTERN = Pattern.compile("/api/(?<version>[0-9]{1,2})/");

    private static final Pattern STARTS_WITH_VERSION_PATTERN = Pattern.compile("/(?<version>[0-9]{1,2})/");

    public static DRunApiVersion getVersionFromPath(String path) {
        Matcher matcher = API_VERSION_PATTERN.matcher(path);
        if (matcher.find()) {
            return getVersion(parseInt(matcher.group("version")));
        }
        matcher = STARTS_WITH_VERSION_PATTERN.matcher(path);
        if (matcher.find()) {
            return getVersion(parseInt(matcher.group("version")));
        }
        return DEFAULT;
    }
}
