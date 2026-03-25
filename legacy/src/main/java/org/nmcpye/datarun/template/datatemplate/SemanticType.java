package org.nmcpye.datarun.template.datatemplate;

import org.nmcpye.datarun.template.datatemplateelement.enumeration.ValueType;

import java.util.List;

public enum SemanticType {
    OrgUnit,
    Team,
    Activity,
    Option,
    MultiSelectOption,
    Repeat,
    Name,
    Age,
    PhoneNumber,
    Email,
    URL,
    Image,
    Progress,
    Entity,
    Username,
    Coordinate,
    GeoJson;

    public static SemanticType fromValueType(ValueType valueType) {
        if (valueType.isMultiSelect()) return MultiSelectOption;
        if (valueType.isSingleSelect()) return Option;
        if (valueType == ValueType.RepeatableSection) return Repeat;
        if (valueType == ValueType.FullName) return Name;

        for (SemanticType type : SemanticType.values()) {
            if (type.name().equalsIgnoreCase(valueType.name())) {
                return type;
            }
        }

        return null;
    }

    static public List<SemanticType> refTypes() {
        return List.of(OrgUnit, Option, Activity, Team, MultiSelectOption);
    }

    public Boolean isRef() {
        return refTypes().contains(this);
    }

    public Boolean isRepeat() {
        return this == Repeat;
    }

    public Boolean isMultiSelect() {
        return this == MultiSelectOption;
    }
}
