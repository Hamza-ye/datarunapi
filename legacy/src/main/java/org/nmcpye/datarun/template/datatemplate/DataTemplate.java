package org.nmcpye.datarun.template.datatemplate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nmcpye.datarun.sharedkernal.JpaSoftDeleteObject;
import org.nmcpye.datarun.sharedkernal.TranslatableInterface;
import org.nmcpye.datarun.sharedkernal.translation.Translation;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/// @author Hamza Assada
/// @since 27/05/2025
@Entity
@Table(name = "data_template")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DataTemplate extends JpaSoftDeleteObject implements TranslatableInterface {
    @Size(max = 11)
    @Column(name = "uid", length = 11, updatable = false, unique = true)
    protected String uid;

    /// The unique code for this object.
    @Column(name = "code", unique = true)
    protected String code;

    /// The name of this object. Required and unique.
    @Column(name = "name", nullable = false, unique = true)
    protected String name;

    /**
     * latest template version uid
     */
    @NotNull
    @Column(name = "version_uid", nullable = false, unique = true)
    private String versionUid;

    /**
     * latest version number
     */
    @NotNull
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber = 0;

    @Column(name = "deleted")
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Set of available object translation, normally filtered by locale.
     */
    @Type(JsonType.class)
    @Column(name = "translations", columnDefinition = "jsonb")
    protected Set<Translation> translations = new HashSet<>();

    // @OneToMany(fetch = FetchType.LAZY, mappedBy = "dataTemplate"/*, cascade =
    // CascadeType.PERSIST*/)
    // @JsonIgnore
    // private List<TemplateVersion> templateVersions = new LinkedList<>();

    @JsonIgnore
    @Override
    public String getId() {
        return super.getId();
    }

    public DataTemplate pumpVersion() {
        this.setVersionNumber(versionNumber + 1);
        return this;
    }

    public DataTemplate versionNumber(Integer currentVersion) {
        setVersionNumber(currentVersion);
        return this;
    }

    public DataTemplate id(String id) {
        setId(id);
        return this;
    }

    public DataTemplate uid(String uid) {
        setUid(uid);
        return this;
    }

    public DataTemplate versionUid(String versionUid) {
        this.setVersionUid(versionUid);
        return this;
    }

    // @JsonSetter(contentNulls = Nulls.SKIP)
    // public void setTemplateVersions(List<TemplateVersion> templateVersions) {
    // getTemplateVersions().clear();
    // getTemplateVersions().addAll(templateVersions);
    // for (TemplateVersion templateVersion : templateVersions) {
    // templateVersion.setDataTemplate(this);
    // }
    // }

    // @JsonIgnore
    // public Set<String> getVersionUidsAsSet() {
    // return templateVersions.stream()
    // .filter(Objects::nonNull)
    // .map(TemplateVersion::getUid)
    // .collect(Collectors.toSet());
    // }
    //
    // public TemplateVersion getLatestVersion() {
    // return templateVersions.stream()
    // .max(Comparator.comparing(TemplateVersion::getVersionNumber))
    // .orElseThrow();
    // }
    //
    // public Map<String, String> getByUidVersionPropertyMap(IdScheme idScheme) {
    // return
    // templateVersions.stream().collect(Collectors.toMap(TemplateVersion::getUid, o
    // -> o.getPropertyValue(idScheme)));
    // }
    //
    // public TemplateVersion getVersionByUid(String uid) {
    // for (TemplateVersion templateVersion : templateVersions) {
    // if (templateVersion != null && templateVersion.getUid().equals(uid)) {
    // return templateVersion;
    // }
    // }
    //
    // return null;
    // }
    //
    // public TemplateVersion getVersionByNo(Integer versionNumber) {
    // for (TemplateVersion templateVersion : templateVersions) {
    // if (templateVersion != null &&
    // templateVersion.getVersionNumber().equals(versionNumber)) {
    // return templateVersion;
    // }
    // }
    //
    // return null;
    // }
}
