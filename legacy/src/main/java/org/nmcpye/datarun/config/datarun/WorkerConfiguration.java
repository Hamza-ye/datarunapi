package org.nmcpye.datarun.config.datarun;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Hamza Assada
 * @since 24/09/2025
 */
@Configuration
@EnableConfigurationProperties(OutboxProperties.class)
public class WorkerConfiguration {

    private final OutboxProperties outboxProperties;

    public WorkerConfiguration(OutboxProperties outboxProperties) {
        this.outboxProperties = outboxProperties;
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService outboxExecutor() {
        int threads = Math.max(2, outboxProperties.getWorkerThreads());
        return Executors.newFixedThreadPool(threads);
    }
}
