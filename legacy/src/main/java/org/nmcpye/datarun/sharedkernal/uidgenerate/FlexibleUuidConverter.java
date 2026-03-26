package org.nmcpye.datarun.sharedkernal.uidgenerate;

import java.util.UUID;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class FlexibleUuidConverter implements Converter<String, UUID> {

    @Override
    public UUID convert(@NonNull String source) {
        return CodeGenerator.resolveToUuid(source);
    }
}
