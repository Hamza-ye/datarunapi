package org.nmcpye.platform.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA configuration for platform-core module.
 * Uses default SimpleJpaRepository base class (standard Spring Data).
 * Separate from legacy DatabaseConfiguration which uses BaseJpaRepositoryImpl.
 *
 * Note: @EntityScan is NOT used here — it is not additive and would override
 * the default entity scan, breaking legacy entities. Entity scanning is handled
 * at the app level via @SpringBootApplication's package scanning.
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.nmcpye.platform")
public class PlatformCoreJpaConfiguration {
}
