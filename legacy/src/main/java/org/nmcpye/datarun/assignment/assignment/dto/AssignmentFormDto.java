package org.nmcpye.datarun.assignment.assignment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Builder
@Getter
@Setter
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AssignmentFormDto implements Serializable {
    private String assignment;
    private String form;
    private boolean canAddSubmissions;
    private boolean canEditSubmissions;
    private boolean canDeleteSubmissions;
}
