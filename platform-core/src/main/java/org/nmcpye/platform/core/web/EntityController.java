package org.nmcpye.platform.core.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nmcpye.platform.core.entity.*;
import org.nmcpye.platform.core.event.EventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entities")
@RequiredArgsConstructor
public class EntityController {

    private final PlatformEntityRepository entityRepository;
    private final EntityTypeRepository entityTypeRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @PostMapping
    @Transactional
    public ResponseEntity<PlatformEntity> create(@Valid @RequestBody EntityCreateRequest request) {
        EntityType type = entityTypeRepository.findByCode(request.typeCode())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Unknown entity type: " + request.typeCode()));

        PlatformEntity entity = new PlatformEntity();
        if (request.uid() != null && !request.uid().isBlank()) {
            if (entityRepository.existsByUid(request.uid())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Entity with uid '" + request.uid() + "' already exists");
            }
            entity.setUid(request.uid());
        }
        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setType(type);
        if (request.attributes() != null) {
            try {
                entity.setAttributes(objectMapper.writeValueAsString(request.attributes()));
            } catch (JsonProcessingException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid attributes JSON");
            }
        }

        PlatformEntity saved = entityRepository.save(entity);

        eventPublisher.publish(
            "entity.created",
            "entity",
            saved.getId(),
            null, // actor — will come from auth context later
            Map.of("uid", saved.getUid(), "typeCode", type.getCode())
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{uid}")
    public PlatformEntity getByUid(@PathVariable String uid) {
        return entityRepository.findByUid(uid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Entity not found: " + uid));
    }

    @GetMapping
    public List<PlatformEntity> list(@RequestParam(required = false) String typeCode) {
        if (typeCode != null) {
            return entityRepository.findByType_Code(typeCode);
        }
        return entityRepository.findAll();
    }

    @PatchMapping("/{uid}")
    @Transactional
    public PlatformEntity update(@PathVariable String uid,
                                 @RequestBody EntityUpdateRequest request) {
        PlatformEntity entity = entityRepository.findByUid(uid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Entity not found: " + uid));

        if (request.name() != null) {
            entity.setName(request.name());
        }
        if (request.code() != null) {
            entity.setCode(request.code());
        }
        if (request.attributes() != null) {
            try {
                entity.setAttributes(objectMapper.writeValueAsString(request.attributes()));
            } catch (JsonProcessingException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid attributes JSON");
            }
        }

        PlatformEntity saved = entityRepository.save(entity);

        eventPublisher.publish(
            "entity.updated",
            "entity",
            saved.getId(),
            null,
            Map.of("uid", saved.getUid())
        );

        return saved;
    }

    /**
     * Request body for entity creation.
     */
    public record EntityCreateRequest(
        String uid,
        String code,
        String name,
        String typeCode,
        Map<String, Object> attributes
    ) {}

    /**
     * Request body for entity update (PATCH — all fields optional).
     */
    public record EntityUpdateRequest(
        String name,
        String code,
        Map<String, Object> attributes
    ) {}
}
