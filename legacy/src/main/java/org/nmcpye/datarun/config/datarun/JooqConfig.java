package org.nmcpye.datarun.config.datarun;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.SettingsTools;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author Hamza Assada 17/08/2025 (7amza.it@gmail.com)
 */
@Configuration
public class JooqConfig {
    @Bean
    public DSLContext dslContext(ObjectProvider<DataSource> dataSource) {
        var configuration = new DefaultConfiguration();
        configuration.set(dataSource.getIfUnique());
        configuration.set(SQLDialect.POSTGRES);
        final var settings = SettingsTools.defaultSettings();
        settings.setRenderQuotedNames(RenderQuotedNames.NEVER);
        configuration.set(settings);
        return DSL.using(configuration);
    }
}
