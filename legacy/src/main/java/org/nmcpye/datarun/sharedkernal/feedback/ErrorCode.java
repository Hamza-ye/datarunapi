package org.nmcpye.datarun.sharedkernal.feedback;

import lombok.Getter;

@Getter
public enum ErrorCode {

    /* General */
    E1000("API query must be specified"),
    E1001("API query contains an illegal string"),
    E1002("API version is invalid"),
    E1003("Failed to update paths: `{0}`"),
    E1004("Failed to fulfill request, entity {0}:`{1}` not found"),

    /* Basic metadata */
    E1100("Data element not found or not accessible: `{0}`:`{1}`"),
    E1101("Form element `{0}`:`{1}` parent `{2}` not found in sections"),
    E1102("Reference element `{0}`:`{1}` must specify Reference type"),
    E1103("MetaDataSchema must be specified for Reference Type element: `{0}`:`{1}`"),
    E1104("Element `{0}`:`{1}` Specified MetaDataSchema `{0}` not found"),
    E1105("Form element `{0}` and its data element value types `{1}`:`{2}` not matching"),
    E1106("Property {0}:`{1}` not found"),

    E1107("Circular reference detected for parent: `{0}`:`{1}`"),
    E1108("Duplicate `{0}` IDs found: `{1}`:`{2}`"),
    E1109("Object {0}:`{1}` not found"),

    /* Form Template Validation */
    E1110("Referenced element '{0}' does not exist in the form template"),
    E1111("Form Validation Errors: `{0}`"),
    E1112("Activity or Team is not Active: {0}"),
    E1113("Not found Form template: {0}"),
    E1114("Not found Form template version: {0}"),
    E1115("Missing required scope: {0}"),
    E1116("{0} : Expected array for multi-value scope"),
    E1117("{0} : Expected single value"),
    E1118("{0} : Invalid reference {1} for {2}"),
    E1119("RequestBody is empty"),
    E1120("Inconsistent state: FormTemplate `{0}` have no Version in the system"),
    E1121("selected Choices {0} in multi select element do not exist"),
    E1122("element: `{0}`,`{1}` is not a Categorical complex reference type"),
    E1199("System configuration Error: `{0}`"),

    /* Query */
    E2000("Query parameters cannot be null"),
    E2001("At least one data element must be specified"),

    E2014("Unable to parse filter `{0}`"),
    E2015("Unable to parse order param: `{0}`"),
    E2016("Invalid Query Format"),
    E2017("Unable to parse order param: `{0}`"),
    E2018("Value for `{0}` must be `{1}` "),
    E2019("Unsupported FilterExpression: {0}"),

    E2034("Filter not supported: `{0}`"),
    E2035("Operator not supported: `{0}`"),
    E2050("Query parsing error: `{0}`"),

    /* Security */
    E3000("No currentUser available"),
    E3001("User `{0}` is not allowed to update object `{1}`"),
    E3002("User `{0}` is not allowed to delete object `{1}`"),
    E3003("User `{0}` is not allowed to create objects of type {1}"),
    E3004("User `{0}` is not allowed to access objects of type {1}"),
    E3005("You are not allowed to access {}"),
    E3006("Team `{0}` is not available"),

    /* Data Submission Validation */
    E4110("Submission validation Error: {0}"),
    E4111("Submission Normalization Errors: `{0}`"),
    E4112("Submission `{0}` form and formVersionId must be present"),
    E4113("Team submitted the data must be present in submission: {0}"),
    E4114("your user is not part of the team {0} that created the submission: {1}"),
    E4115("updating submission's form version is not allowed: incoming `{0}` required: `{1}`"),
    E4116("Submission {} not found"),

    /* Users */
    E6201("User account not found"),
    E6202("Username is already taken");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

}
