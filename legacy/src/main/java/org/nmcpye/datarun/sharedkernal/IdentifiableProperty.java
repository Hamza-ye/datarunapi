package org.nmcpye.datarun.sharedkernal;

import java.util.function.Function;

public enum IdentifiableProperty {
    ID,
    UID,
    UUID,
    NAME,
    CODE,
    ATTRIBUTE;

    public static IdentifiableProperty in(IdSchemes schemes, Function<IdSchemes, IdScheme> primary) {
        IdScheme scheme = primary.apply(schemes);
        if (scheme != null && scheme.isNotNull()) {
            return scheme.getIdentifiableProperty();
        }
        scheme = schemes.getIdScheme();
        if (scheme != null && scheme.isNotNull()) {
            return scheme.getIdentifiableProperty();
        }
        return UID;
    }
}
