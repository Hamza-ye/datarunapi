package org.nmcpye.datarun.template.datatemplateelement.enumeration;

/**
 * The RuleAction enumeration.
 */
public enum RuleAction {
    Show,
    Hide,
    Error,
    Warning,
    Filter,
    // Expression must be logical (true, false)
    Mandatory,
    // Expression result must be a compatible Value with the Field type
    // i.e for default Value
    Assign,

    ErrorOnComplete, // deprecated, use Constraint
    WarningOnComplete, // deprecated, use Constraint
    DisplayText,
    DisplayKeyValuePair,

    HideOption,
    HideOptionGroup,
    ShowOptionGroup,
}
