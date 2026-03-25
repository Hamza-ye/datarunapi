package org.nmcpye.datarun.sharedkernal.translation;

import com.google.common.base.MoreObjects;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Translation implements Serializable {

    private String locale;

    private String property;

    private String value;

    /**
     * Creates a cache key.
     *
     * @param locale   the locale string, i.e. Locale.toString().
     * @param property the translation property.
     * @return a unique cache key valid for a given translated objects, or null
     * if either locale or property is null.
     */
    public static String getCacheKey(String locale, String property) {
        return locale != null && property != null ? (locale + property) : null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale, property, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Translation that = (Translation) o;
        return Objects.equals(locale, that.locale) &&
            Objects.equals(property, that.property);
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//
//        if (obj == null || getClass() != obj.getClass()) {
//            return false;
//        }
//
//        final Translation other = (Translation) obj;
//
//        return (
//            Objects.equals(this.locale, other.locale) &&
//                Objects.equals(this.property, other.property) &&
//                Objects.equals(this.value, other.value)
//        );
//    }

    // -------------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------------

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("locale", locale)
            .add("property", property)
            .add("value", value).toString();
    }
}
