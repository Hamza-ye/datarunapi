package org.nmcpye.datarun.template.datatemplateelement.enumeration;

import java.util.List;

/**
 * The ValueType enumeration.
 */
public enum ValueType {
    ScannedCode,
    RepeatableSection,
    Section, // ui group
    SelectMulti,
    SelectOne,
    Age,
    FullName,
    Text,
    LongText,
    Letter,
    Boolean,
    TrueOnly,
    YesNo,
    Date,
    DateTime,
    Time,
    Percentage,
    Number,
    Integer,
    IntegerPositive,
    IntegerNegative,
    IntegerZeroOrPositive,
    UnitInterval,
    PhoneNumber,
    Email,
    URL,
    Image,
    Progress,
    Activity,
    OrganisationUnit,
    Team,
    Entity,
    Username,
    Reference,
    FileResource,
    Calculated,
    Coordinate,
    GeoJson;

    public static List<ValueType> SectionTypes() {
        return List.of(Section, RepeatableSection);
    }

    public static List<ValueType> numericTypes() {
        return List.of(Number,
                Integer,
                IntegerPositive,
                IntegerNegative,
                IntegerZeroOrPositive);
    }

    public static List<ValueType> dateTimeTypes() {
        return List.of(Date,
                DateTime,
                Time,
                Age);
    }

    public static List<ValueType> booleanTypes() {
        return List.of(TrueOnly,
                Boolean);
    }

    public static List<ValueType> complexReferenceTypes() {
        return List.of(/*Progress,*/
                Activity,
                OrganisationUnit,
                Team,
                Entity,
                SelectOne,
                // OptionSet
                Username);
    }

    public static List<ValueType> textualTypes() {
        return List.of(Text, LongText, Letter);
    }

    public boolean isNumeric() {
        return numericTypes().contains(this);
    }

    public boolean isBoolean() {
        return booleanTypes().contains(this);
    }

    public boolean isTextual() {
        return textualTypes().contains(this);
    }

    public boolean isSection() {
        return this == Section;
    }
    public boolean isMultiSelect() {
        return this == SelectMulti;
    }

    public boolean isSingleSelect() {
        return this == SelectOne;
    }

    public boolean isRepeat() {
        return this == RepeatableSection;
    }

    public boolean isSystemReferenceType() {
        return complexReferenceTypes().contains(this);
    }

    public boolean isReference() {
        return this == Reference;
    }

    public boolean isOptionsType() {
        return this == SelectOne || this == SelectMulti;
    }

    public boolean isDateTimeType() {
        return dateTimeTypes().contains(this);
    }


    /**
     * Returns true if the two value types should be considered compatible:
     * - exactly the same, or
     * - both numeric, or
     * - both textual.
     */
    public boolean isCompatible(ValueType other) {
        if (this == other) {
            return true;
        }
        if (this.isNumeric() && other.isNumeric()) {
            return true;
        }
        return this.isTextual() && other.isTextual();
    }
}
