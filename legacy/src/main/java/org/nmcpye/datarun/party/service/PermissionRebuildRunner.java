package org.nmcpye.datarun.party.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("run-permission-rebuild")
@RequiredArgsConstructor
@Slf4j
public class PermissionRebuildRunner implements CommandLineRunner {

    private final UserPermissionMaterializationService materializationService;

    @Override
    public void run(String... args) {
        log.info("--- Triggering User Permission Rebuild ---");
        try {
            materializationService.rebuild();
        } catch (Exception e) {
            log.error("Permission rebuild failed!", e);
        }
        log.info("--- User Permission Rebuild Finished ---");
    }
}
