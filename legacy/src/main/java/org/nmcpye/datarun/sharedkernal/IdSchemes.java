package org.nmcpye.datarun.sharedkernal;

import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.ObjectUtils;

public class IdSchemes {

    public static final IdScheme DEFAULT_ID_SCHEME = IdScheme.UID;

    private IdScheme idScheme;

    private IdScheme dataElementIdScheme;

    private IdScheme orgUnitIdScheme;

    private IdScheme orgUnitGroupIdScheme;

    private IdScheme projectIdScheme;

    private IdScheme activityIdScheme;

    private IdScheme teamIdScheme;

    private IdScheme assignmentIdScheme;

    private IdScheme dataFormIdScheme;

    private IdScheme dataFormTemplateIdScheme;

    private IdScheme dataSubmissionIdScheme;

    public IdSchemes() {
    }

    public IdScheme getScheme(IdScheme idScheme) {
        return IdScheme.from(ObjectUtils.firstNonNull(idScheme, getIdScheme()));
    }

    public IdScheme getIdScheme() {
        return IdScheme.from(ObjectUtils.firstNonNull(idScheme, DEFAULT_ID_SCHEME));
    }

    public IdSchemes setIdScheme(String idScheme) {
        this.idScheme = IdScheme.from(idScheme);
        return this;
    }

    public IdSchemes setDefaultIdScheme(IdScheme idScheme) {
        if (this.idScheme == null) {
            this.idScheme = idScheme;
        }
        return this;
    }

    // --------------------------------------------------------------------------
    // Object type id schemes
    // --------------------------------------------------------------------------

    public IdScheme getDataFormIdScheme() {
        return getScheme(dataFormIdScheme);
    }

    public IdScheme getDataFormTemplateIdScheme() {
        return getScheme(dataFormTemplateIdScheme);
    }

    public IdScheme getDataElementIdScheme() {
        return getScheme(dataElementIdScheme);
    }

    public IdSchemes setDataElementIdScheme(String idScheme) {
        this.dataElementIdScheme = IdScheme.from(idScheme);
        return this;
    }

    public IdScheme getOrgUnitIdScheme() {
        return getScheme(orgUnitIdScheme);
    }

    public IdSchemes setOrgUnitIdScheme(String idScheme) {
        this.orgUnitIdScheme = IdScheme.from(idScheme);
        return this;
    }

    public IdScheme getOrgUnitGroupIdScheme() {
        return getScheme(orgUnitGroupIdScheme);
    }

    public IdSchemes setOrgUnitGroupIdScheme(String idScheme) {
        this.orgUnitGroupIdScheme = IdScheme.from(idScheme);
        return this;
    }

    public IdSchemes setDataFormIdScheme(String idScheme) {
        this.dataFormIdScheme = IdScheme.from(idScheme);
        return this;
    }

    public IdSchemes setDataFormTemplateIdScheme(String idScheme) {
        this.dataFormTemplateIdScheme = IdScheme.from(idScheme);
        return this;
    }

    public IdScheme getDataSubmissionIdScheme() {
        return getScheme(dataSubmissionIdScheme);
    }

    public IdSchemes setDataSubmissionIdScheme(String idScheme) {
        this.dataSubmissionIdScheme = IdScheme.from(idScheme);
        return this;
    }

    public IdScheme getProjectIdScheme() {
        return getScheme(projectIdScheme);
    }

    public IdSchemes setProjectIdScheme(String idScheme) {
        this.projectIdScheme = IdScheme.from(idScheme);
        return this;
    }

    public IdScheme getActivityIdScheme() {
        return getScheme(activityIdScheme);
    }

    public IdSchemes setActivityIdScheme(String idScheme) {
        this.activityIdScheme = IdScheme.from(idScheme);
        return this;
    }

    public IdScheme getTeamIdScheme() {
        return getScheme(teamIdScheme);
    }

    public IdSchemes setTeamIdScheme(String idScheme) {
        this.teamIdScheme = IdScheme.from(teamIdScheme);
        return this;
    }

    public IdScheme getAssignmentIdScheme() {
        return getScheme(assignmentIdScheme);
    }

    public IdSchemes setAssignmentIdScheme(String idScheme) {
        this.assignmentIdScheme = IdScheme.from(assignmentIdScheme);
        return this;
    }

    // --------------------------------------------------------------------------
    // Get value methods
    // --------------------------------------------------------------------------

    public static String getValue(String uid, String code, IdentifiableProperty identifiableProperty) {
        return getValue(uid, code, IdScheme.from(identifiableProperty));
    }

    public static String getValue(String uid, String code, IdScheme idScheme) {
        boolean isId = idScheme.is(IdentifiableProperty.ID) || idScheme.is(IdentifiableProperty.UID);

        return isId ? uid : code;
    }

    public static <T> String getValue(IdentifiableObject<T> identifiableObject,
            IdentifiableProperty identifiableProperty) {
        return getValue(identifiableObject, IdScheme.from(identifiableProperty));
    }

    public static <T> String getValue(IdentifiableObject<T> identifiableObject, IdScheme idScheme) {
        boolean isId = idScheme.is(IdentifiableProperty.ID) || idScheme.is(IdentifiableProperty.UID);

        if (isId) {
            return identifiableObject.getUid();
        } else if (idScheme.is(IdentifiableProperty.CODE)) {
            return identifiableObject.getCode();
        } else if (idScheme.is(IdentifiableProperty.NAME)) {
            return identifiableObject.getName();
        }

        return null;
    }

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper(this)
                .add("idScheme", idScheme)
                .add("dataElementIdScheme", dataFormIdScheme)
                .add("dataFormTemplateIdScheme", dataFormTemplateIdScheme)
                .add("dataElementGroupIdScheme", dataSubmissionIdScheme)
                .add("orgUnitIdScheme", orgUnitIdScheme)
                .add("projrctIdScheme", projectIdScheme)
                .add("activityIdScheme", activityIdScheme)
                .add("orgUnitGroupIdScheme", orgUnitGroupIdScheme)
                .toString();
    }
}
