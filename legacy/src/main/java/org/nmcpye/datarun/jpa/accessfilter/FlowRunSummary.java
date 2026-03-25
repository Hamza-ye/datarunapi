package org.nmcpye.datarun.jpa.accessfilter;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/**
 * @author Hamza Assada
 * @since 24/04/2025
 */
public interface FlowRunSummary {
    @JsonProperty(value = "uid")
    String getUid();

    Set<String> getForms();

    RelSummary getOrgUnit();

    RelSummary getTeam();

    RelSummary getActivity();

    interface RelSummary {
        @JsonProperty(value = "uid")
        String getUid();
    }
}
