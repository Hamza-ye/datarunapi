package org.nmcpye.datarun.template.datatemplateelement.datafield;

import jakarta.validation.constraints.NotNull;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.ResourceTransactionType;

public class ResourceField extends DefaultField {
    @NotNull
    private String resourceUid;

    // "IN/OUT" with assignment resource mapping,
    private ResourceTransactionType transactionType;

    public String getResourceUid() {
        return resourceUid;
    }

    public void setResourceUid(String resourceUid) {
        this.resourceUid = resourceUid;
    }

    public ResourceTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(ResourceTransactionType transactionType) {
        this.transactionType = transactionType;
    }
}
