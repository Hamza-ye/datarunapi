package org.nmcpye.datarun.party.mapper;

import org.nmcpye.datarun.party.dto.AssignmentMemberDto;
import org.nmcpye.datarun.party.entities.AssignmentMember;
import org.springframework.stereotype.Component;

@Component
public class AssignmentMemberMapper {

    public AssignmentMemberDto toDto(AssignmentMember entity) {
        if (entity == null)
            return null;
        AssignmentMemberDto dto = new AssignmentMemberDto();
        dto.setId(entity.getId());
        dto.setAssignmentId(entity.getAssignmentId());
        dto.setMemberType(entity.getMemberType());
        dto.setMemberId(entity.getMemberId());
        dto.setRole(entity.getRole());
        return dto;
    }

    public AssignmentMember toEntity(AssignmentMemberDto dto) {
        if (dto == null)
            return null;
        AssignmentMember entity = new AssignmentMember();
        entity.setId(dto.getId());
        entity.setAssignmentId(dto.getAssignmentId());
        entity.setMemberType(dto.getMemberType());
        entity.setMemberId(dto.getMemberId());
        entity.setRole(dto.getRole());
        return entity;
    }
}
