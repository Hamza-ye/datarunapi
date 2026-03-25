package org.nmcpye.datarun.template.datatemplateelement.datafield;

public class ScannedCodeField extends DefaultField {
    private boolean gs1Enabled;
    private ScannedCodeProperties properties;

    public boolean getGs1Enabled() {
        return gs1Enabled;
    }

    public void setGs1Enabled(boolean gs1Enabled) {
        this.gs1Enabled = gs1Enabled;
    }

    public ScannedCodeProperties getProperties() {
        return properties;
    }

    public void setProperties(ScannedCodeProperties properties) {
        this.properties = properties;
    }
}
