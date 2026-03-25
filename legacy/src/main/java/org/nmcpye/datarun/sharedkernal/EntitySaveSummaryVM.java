package org.nmcpye.datarun.sharedkernal;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class EntitySaveSummaryVM implements Serializable {
    private List<String> created = new ArrayList<>();
    private List<String> updated = new ArrayList<>();
    private Map<String, String> failed = new HashMap<>();

    public boolean hasFailed() {
        return !failed.isEmpty();
    }

    public int getFailedCount() {
        return failed.size();
    }

    public int getCreatedCount() {
        return created.size();
    }

    public int getUpdatedCount() {
        return updated.size();
    }
}
