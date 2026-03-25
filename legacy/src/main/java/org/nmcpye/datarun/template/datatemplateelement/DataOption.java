package org.nmcpye.datarun.template.datatemplateelement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A DataOption.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataOption implements Serializable {
    @NotNull
    @Size(max = 11, min = 11)
    private String uid;

    @Size(max = 11)
    private String optionSetUid;

    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();
}
