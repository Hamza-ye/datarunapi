package org.nmcpye.datarun.template.datatemplateelement.enumeration;

import java.util.List;

/**
 * The ValueType enumeration.
 */
public enum ValueTypeOld {
    @Deprecated(since = "Remove for Value Type rendering")
    ScannedCode,
    SelectMulti,
    SelectOne,
    Text,
    Boolean,
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
    Coordinate,

    // reference system entity ID value type
    Activity,
    OrganisationUnit,
    Team,
    Entity,
    Username;

    public static List<ValueTypeOld> numericTypes() {
        return List.of(Number,
            Integer,
            IntegerPositive,
            IntegerNegative,
            IntegerZeroOrPositive);
    }

    public static List<ValueTypeOld> booleanTypes() {
        return List.of(Boolean);
    }

    public static List<ValueTypeOld> complexReferenceTypes() {
        return List.of(/*Progress,*/
            Activity,
            OrganisationUnit,
            Team,
            Entity,
            SelectOne,
            Username);
    }

    public boolean isNumeric() {
        return numericTypes().contains(this);
    }

    public boolean isBoolean() {
        return booleanTypes().contains(this);
    }

    public boolean isComplexReference() {
        return complexReferenceTypes().contains(this);
    }

    public boolean isOptionsType() {
        return this == SelectOne || this == SelectMulti;
    }


    /**
     * Returns true if the two value types should be considered compatible:
     * - exactly the same, or
     * - both numeric
     */
    public boolean isCompatible(ValueTypeOld other) {
        if (this == other) {
            return true;
        }
        return this.isNumeric() && other.isNumeric();
    }
}
