package org.nmcpye.datarun.web.rest.v1.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentFormV1Dto implements Serializable {
    private String assignment;
    private String form;
    private boolean canAddSubmissions;
    private boolean canEditSubmissions;
    private boolean canDeleteSubmissions;
}
