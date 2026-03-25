package org.nmcpye.datarun.party.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nmcpye.datarun.jpa.orgunit.OrgUnit;
import org.nmcpye.datarun.jpa.orgunit.repository.OrgUnitRepository;
import org.nmcpye.datarun.iam.team.Team;
import org.nmcpye.datarun.iam.team.repository.TeamRepository;
import org.nmcpye.datarun.iam.user.User;
import org.nmcpye.datarun.iam.user.repository.UserRepository;
import org.nmcpye.datarun.party.entities.Party.SourceType;
import org.nmcpye.datarun.party.service.PartySyncService.ToSyncParty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/// @author Hamza Assada
/// @since 24/04/2025
@Component
@Profile("run-party-migration")
@RequiredArgsConstructor
@Slf4j
public class InitPartyMigration implements CommandLineRunner {
    private static final int CHUNK_SIZE = 500;
    private final OrgUnitRepository orgUnitRepo;
    private final TeamRepository teamRepo;
    private final UserRepository userRepo;
    private final PartySyncService syncService;

    private final EntityManager em;
    private final PlatformTransactionManager txm;

    @Override
    public void run(String... args) throws Exception {
        log.info("--- Starting Initial Party Migration ---");
//        processTeamsInChunks();
//        processUsersInChunks();
        processOrgUnitsInChunks();
    }

    public void processTeamsInChunks() {
        int page = 0;
        Page<Team> chunk;
        log.info("Migrating Teams...");
        do {
            Pageable pg = PageRequest.of(page, CHUNK_SIZE);
            chunk = teamRepo.findAll(pg);
            log.info("--- migrate: {} teams ---", CHUNK_SIZE);

            if (!chunk.isEmpty()) {
                // run each chunk in its own transaction to keep EM small
                Page<Team> finalChunk = chunk;
                new TransactionTemplate(txm).execute(status -> {
                    for (Team t : finalChunk) {
                        List<String> teamTags = new ArrayList<>();
                        t.getManagedByTeams().forEach(m -> teamTags.add("managed_by_team:" + m.getUid()));
                        t.getUsers().forEach(u -> teamTags.add("has_user:" + u.getUid()));
                        final var teamActivity = t.getActivity().getUid();
                        teamTags.add("activity:" + teamActivity);

                        syncService.syncParty(
                            ToSyncParty.builder()
                                .id(t.getId())
                                .uid(t.getUid())
                                .code(t.getCode())
                                .name(t.getName())
                                .sourceType(SourceType.TEAM)
                                .parentId(null)
                                .meta(Map.of("activity", teamActivity))
                                .tags(teamTags)
                                .build());
                    }
                    log.info("Completed migration for {} teams.", finalChunk);

//                    syncService.syncParty(finalChunk.getContent());
                    // make sure we don’t accumulate managed state

                    em.flush();
                    em.clear();
                    return null;
                });
            }
            log.info("saving next {} teams", page);
            page++;
        } while (!chunk.isLast());
    }

    public void processUsersInChunks() {
        int page = 0;
        Page<User> chunk;
        log.info("Migrating Users...");
        do {
            Pageable pg = PageRequest.of(page, CHUNK_SIZE);
            chunk = userRepo.findAll(pg);
            log.info("--- migrate: {} users ---", CHUNK_SIZE);

            if (!chunk.isEmpty()) {
                // run each chunk in its own transaction to keep EM small
                Page<User> finalChunk = chunk;
                new TransactionTemplate(txm).execute(status -> {
                    for (User u : finalChunk) {
                        List<String> userTags = new ArrayList<>();
                        u.getUserGroups().forEach(ug -> userTags.add("user_group:" + ug.getUid()));
                        u.getManagedByTeams().forEach(ug -> userTags.add("managed_by_team:" + ug.getUid()));
                        u.getManagedByGroups().forEach(ug -> userTags.add("managed_by_group:" + ug.getUid()));
                        u.getTeams().forEach(t -> userTags.add("team:" + t.getUid()));
                        u.getRoles().forEach(t -> userTags.add("user_role:" + t.getName()));


                        syncService.syncParty(
                            ToSyncParty.builder()
                                .id(u.getId())
                                .uid(u.getUid())
                                .code(u.getLogin())
                                .name(u.getFirstName())
                                .label(Map.of("en", u.getFirstName()))
                                .sourceType(SourceType.USER)
                                .parentId(null)
                                .meta(Map.of())
                                .tags(userTags)
                                .build());
                    }
                    log.info("Completed migration for {} users.", finalChunk);

//                    syncService.syncParty(finalChunk.getContent());
                    // make sure we don’t accumulate managed state

                    em.flush();
                    em.clear();
                    return null;
                });
            }
            log.info("saving next {} users", page);
            page++;
        } while (!chunk.isLast());
    }

    public void processOrgUnitsInChunks() {
        int page = 0;
        Page<OrgUnit> chunk;
        log.info("Migrating OrgUnits...");
        do {
            Pageable pg = PageRequest.of(page, CHUNK_SIZE);
            chunk = orgUnitRepo.findAll(pg);
            log.info("--- migrate: {} orgUnits ---", CHUNK_SIZE);

            if (!chunk.isEmpty()) {
                // run each chunk in its own transaction to keep EM small
                Page<OrgUnit> finalChunk = chunk;
                new TransactionTemplate(txm).execute(status -> {
                    for (OrgUnit ou : finalChunk) {
                        // Parent ID is the source ID of the parent OrgUnit
                        String parentSourceId = (ou.getParent() != null) ? ou.getParent().getId() : null;

                        // Build a meaningful list of tags
                        List<String> orgUnitTags = new ArrayList<>();
                        orgUnitTags.add("level:" + ou.getHierarchyLevel());
                        ou.getOrgUnitGroups().forEach(g -> orgUnitTags.add("oug:" + g.getCode()));

                        syncService.syncParty(
                            ToSyncParty.builder()
                                .id(ou.getId())
                                .uid(ou.getUid())
                                .code(ou.getCode())
                                .name(ou.getName())
                                .label(ou.getLabel())
                                .sourceType(SourceType.ORG_UNIT)
                                .parentId(parentSourceId)
                                .meta(Map.of("level", ou.getLevel(), "path", ou.getPath()))
                                .tags(orgUnitTags)
                                .build());
                    }
                    log.info("Completed migration for {} orgUnits.", finalChunk);

//                    syncService.syncParty(finalChunk.getContent());
                    // make sure we don’t accumulate managed state

                    em.flush();
                    em.clear();
                    return null;
                });
            }
            log.info("saving next {} orgUnits", page);
            page++;
        } while (!chunk.isLast());
    }
}
