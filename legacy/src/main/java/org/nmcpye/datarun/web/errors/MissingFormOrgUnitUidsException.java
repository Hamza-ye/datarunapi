package org.nmcpye.datarun.web.errors;

import java.io.Serializable;
import java.util.Set;

public class MissingFormOrgUnitUidsException extends RuntimeException implements Serializable {
    String form;
    Set<String> missingOrgUnitUids;

    public MissingFormOrgUnitUidsException(String message) {
        super(message);
    }

    public MissingFormOrgUnitUidsException(String form, Set<String> missingOrgUnitUids) {
        super("The following OrgUnit UIDs were not found: " + String.join(", ", missingOrgUnitUids));
        this.missingOrgUnitUids = missingOrgUnitUids;
        this.form = form;
    }

    public MissingFormOrgUnitUidsException(Set<String> missingOrgUnitUids) {
        super("The following OrgUnit UIDs were not found: " + String.join(", ", missingOrgUnitUids));
        this.missingOrgUnitUids = missingOrgUnitUids;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public Set<String> getMissingOrgUnitUids() {
        return missingOrgUnitUids;
    }

    public void setMissingOrgUnitUids(Set<String> missingOrgUnitUids) {
        this.missingOrgUnitUids = missingOrgUnitUids;
    }
}
