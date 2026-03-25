package org.nmcpye.datarun.config.datarun;

import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Hamza Assada
 * @since 17/09/2025
 */
@Configuration
public class JsonPathConfig {
    @Bean
    public com.jayway.jsonpath.Configuration jsonPathConfiguration() {
        return com.jayway.jsonpath.Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .build();
    }
}
