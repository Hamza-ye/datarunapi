package org.nmcpye.datarun.assignment.assignment.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

import org.nmcpye.datarun.sharedkernal.enumeration.FlowStatus;

/**
 * @author Hamza Assada
 * @since 24/04/2025
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AssignmentWithAccessDto implements Serializable {
    @Size(max = 26)
    protected String id;
    protected String code;
    String activity;
    String team;
    String orgUnit;
    FlowStatus progressStatus;
    Set<AssignmentFormDto> accessibleForms;
}
