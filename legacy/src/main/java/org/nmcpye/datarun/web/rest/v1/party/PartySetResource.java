package org.nmcpye.datarun.web.rest.v1.party;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.party.dto.PartySetDto;
import org.nmcpye.datarun.party.service.PartySetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/partySets")
@RequiredArgsConstructor
public class PartySetResource {

    private final PartySetService partySetService;

    @PostMapping
    public ResponseEntity<PartySetDto> createPartySet(@RequestBody PartySetDto partySetDto) throws URISyntaxException {
        if (partySetDto.getId() != null) {
            // Or handle as a bad request
            return ResponseEntity.badRequest().build();
        }
        PartySetDto result = partySetService.save(partySetDto);
        return ResponseEntity.created(new URI("/api/admin/partySets/" + result.getId()))
                .body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartySetDto> updatePartySet(@PathVariable String id, @RequestBody PartySetDto partySetDto) {
        partySetDto.setId(id);
        PartySetDto result = partySetService.save(partySetDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<PartySetDto>> getAllPartySets() {
        List<PartySetDto> list = partySetService.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartySetDto> getPartySet(@PathVariable String id) {
        return partySetService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePartySet(@PathVariable String id) {
        partySetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
