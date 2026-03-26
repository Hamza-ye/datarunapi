package org.nmcpye.platform.core.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nmcpye.platform.core.entity.EntityType;
import org.nmcpye.platform.core.entity.EntityTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/entity-types")
@RequiredArgsConstructor
public class EntityTypeController {

    private final EntityTypeRepository entityTypeRepository;

    @PostMapping
    public ResponseEntity<EntityType> create(@Valid @RequestBody EntityType entityType) {
        if (entityTypeRepository.existsByCode(entityType.getCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "EntityType with code '" + entityType.getCode() + "' already exists");
        }
        EntityType saved = entityTypeRepository.save(entityType);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<EntityType> list() {
        return entityTypeRepository.findAll();
    }

    @GetMapping("/{code}")
    public EntityType getByCode(@PathVariable String code) {
        return entityTypeRepository.findByCode(code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "EntityType not found: " + code));
    }
}
