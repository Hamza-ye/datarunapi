package org.nmcpye.datarun.config;

import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import javax.sql.DataSource;

@Configuration
public class LiquibaseConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LiquibaseConfiguration.class);

    private final Environment env;

    public LiquibaseConfiguration(Environment env) {
        this.env = env;
    }

    @Bean
    public SpringLiquibase liquibase(
            @LiquibaseDataSource ObjectProvider<DataSource> liquibaseDataSource,
            ObjectProvider<DataSource> dataSource,
            LiquibaseProperties liquibaseProperties,
            DataSourceProperties dataSourceProperties) {
        log.debug("Configuring Liquibase");
        SpringLiquibase liquibase = new SpringLiquibase();

        // Match JHipster's logic: try liquibaseDataSource first, then standard
        // dataSource
        DataSource activeDataSource = liquibaseDataSource.getIfAvailable();
        if (activeDataSource == null) {
            activeDataSource = dataSource.getIfUnique();
        }

        // If neither are available (which shouldn't happen unless auto-config is
        // disabled incorrectly),
        // we can't initialize Liquibase properly, but setting the data source is
        // mandatory.
        if (activeDataSource != null) {
            liquibase.setDataSource(activeDataSource);
        } else {
            throw new IllegalStateException("Cannot start Liquibase: No DataSource found.");
        }

        liquibase.setChangeLog("classpath:config/liquibase/master.xml");

        if (liquibaseProperties.getContexts() != null && !liquibaseProperties.getContexts().isEmpty()) {
            liquibase.setContexts(String.join(",", liquibaseProperties.getContexts()));
        }

        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
        liquibase.setLiquibaseTablespace(liquibaseProperties.getLiquibaseTablespace());
        liquibase.setDatabaseChangeLogLockTable(liquibaseProperties.getDatabaseChangeLogLockTable());
        liquibase.setDatabaseChangeLogTable(liquibaseProperties.getDatabaseChangeLogTable());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());

        if (liquibaseProperties.getLabelFilter() != null && !liquibaseProperties.getLabelFilter().isEmpty()) {
            liquibase.setLabelFilter(String.join(",", liquibaseProperties.getLabelFilter()));
        }

        liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
        liquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
        liquibase.setTestRollbackOnUpdate(liquibaseProperties.isTestRollbackOnUpdate());

        if (env.acceptsProfiles(Profiles.of("no-liquibase"))) {
            liquibase.setShouldRun(false);
        } else {
            liquibase.setShouldRun(liquibaseProperties.isEnabled());
        }

        return liquibase;
    }
}
