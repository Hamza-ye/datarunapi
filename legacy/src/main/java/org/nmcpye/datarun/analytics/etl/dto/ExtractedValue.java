package org.nmcpye.datarun.analytics.etl.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;


@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public final class ExtractedValue {
    final String valueText;
    final Boolean valueBool;
    final BigDecimal valueNumber;
    final String valueJson;

    public boolean isEmpty() {
        return (valueText == null || valueText.isEmpty())
            && valueNumber == null
            && (valueJson == null || valueJson.isEmpty());
    }
}
