package org.nmcpye.datarun.iam.userole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObject;

import java.util.Collection;

/**
 * @author Hamza Assada
 * @since 21/03/2025
 */
@Entity
@Table(name = "role_privilege")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
public class Privilege extends JpaIdentifiableObject {
    @Size(max = 11)
    @Column(name = "uid", length = 11, updatable = false, unique = true)
    protected String uid;

    /**
     * The unique code for this object.
     */
    @Column(name = "code", unique = true)
    protected String code;

    /**
     * Read, Write, ...
     */
    @NotNull
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "privileges")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Collection<Role> roles;
}
