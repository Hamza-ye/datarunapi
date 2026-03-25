package org.nmcpye.datarun.sharedkernal;

import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Contract for a generic dto to entity mapper.
 *
 * @param <DTO>    - DTO type parameter.
 * @param <ENTITY> - Entity type parameter.
 * @author Hamza Assada 07/08/2024 (7amza.it@gmail.com)
 */

public interface BaseMapper<DTO, ENTITY> {
    ENTITY toEntity(DTO dto);

    DTO toDto(ENTITY entity);

    List<DTO> toDtoList(List<ENTITY> entityList);

    ENTITY partialUpdate(@MappingTarget ENTITY entity, DTO dto);
}
