package org.nmcpye.datarun.template.datatemplateelement.datafield;

import jakarta.validation.constraints.NotNull;

public class OptionField extends DefaultField {
    @NotNull
    private String listName;

    private String choiceFilter;

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getChoiceFilter() {
        return choiceFilter;
    }

    public void setChoiceFilter(String choiceFilter) {
        this.choiceFilter = choiceFilter;
    }
}
