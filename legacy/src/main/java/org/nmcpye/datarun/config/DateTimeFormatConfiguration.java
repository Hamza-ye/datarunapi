package org.nmcpye.datarun.config;

import org.nmcpye.datarun.sharedkernal.uidgenerate.FlexibleUuidConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configure the converters to use the ISO format for dates by default.
 */
@Configuration
public class DateTimeFormatConfiguration implements WebMvcConfigurer {
    private final FlexibleUuidConverter flexibleUuidConverter;

    // Spring will automatically inject your @Component converter here
    public DateTimeFormatConfiguration(FlexibleUuidConverter flexibleUuidConverter) {
        this.flexibleUuidConverter = flexibleUuidConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(registry);

        // 2. New UUID/ULID Flexible Converter
        registry.addConverter(flexibleUuidConverter);
    }
}
