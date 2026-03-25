package org.nmcpye.datarun.template.datatemplateelement.datafield;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScannedCodeProperties implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> allowedItemTypes = new ArrayList<String>();
    private List<String> gtin = new ArrayList<String>();
    private String batchLot;
    private Map<String, String> productionDate = new HashMap<String, String>();

    public List<String> getAllowedItemTypes() {
        return allowedItemTypes;
    }

    public void setAllowedItemTypes(List<String> allowedItemTypes) {
        this.allowedItemTypes = allowedItemTypes;
    }

    public List<String> getGtin() {
        return gtin;
    }

    public void setGtin(List<String> gtin) {
        this.gtin = gtin;
    }

    public String getBatchLot() {
        return batchLot;
    }

    public void setBatchLot(String batchLot) {
        this.batchLot = batchLot;
    }

    public Map<String, String> getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(Map<String, String> productionDate) {
        this.productionDate = productionDate;
    }
}
