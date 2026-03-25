package org.nmcpye.datarun.analytics.etl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.nmcpye.datarun.template.datatemplate.CanonicalElement;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Accessors(fluent = true)
public class TemplateContext implements Serializable {
    @Builder.Default
    private Map<String, CanonicalElement> allCanonicalElementsMap = new HashMap<>();

    @Builder.Default
    private Map<String, CanonicalElement> repeatCanonicalElementsMap = new HashMap<>();
}
