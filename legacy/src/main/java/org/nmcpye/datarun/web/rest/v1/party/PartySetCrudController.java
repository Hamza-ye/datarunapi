package org.nmcpye.datarun.web.rest.v1.party;

import org.nmcpye.datarun.party.entities.PartySet;
import org.nmcpye.datarun.party.repository.PartySetRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/party-sets")
public class PartySetCrudController {

    private final PartySetRepository repo;

    public PartySetCrudController(PartySetRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<PartySet>> list() {
        return ResponseEntity.ok(repo.findAll());
    }

    @PostMapping
    public ResponseEntity<PartySet> create(@RequestBody PartySet ps) {
        PartySet saved = repo.persist(ps);
        return ResponseEntity.ok(saved);
    }


    @GetMapping("/{id}")
    public ResponseEntity<PartySet> get(@PathVariable String id) {
        return repo.findByUid(id).map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
