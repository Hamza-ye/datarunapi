package org.nmcpye.datarun.iam.team;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

import org.nmcpye.datarun.sharedkernal.enumeration.FormPermission;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class TeamFormPermissions {
    private String form;
    private Set<FormPermission> permissions;
}

