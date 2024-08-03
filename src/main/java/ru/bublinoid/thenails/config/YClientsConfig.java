package ru.bublinoid.thenails.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "yclients")
@Data
public class YClientsConfig {
    private String apiKey;
    private String companyId;
}
