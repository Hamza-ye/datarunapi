package org.nmcpye.datarun.template.datatemplateelement.datafield;

public class Repeat extends Section {
    private String itemTitle;
    private int maxRepeats;
    private int minRepeats;

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public int getMaxRepeats() {
        return maxRepeats;
    }

    public void setMaxRepeats(int maxRepeats) {
        this.maxRepeats = maxRepeats;
    }

    public int getMinRepeats() {
        return minRepeats;
    }

    public void setMinRepeats(int minRepeats) {
        this.minRepeats = minRepeats;
    }
}
