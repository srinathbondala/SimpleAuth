package com.authservice.simple_auth.Configure.Properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "cors")
@Data
public class CorsProperties {
    private List<String> allowedOrigins = new ArrayList<>();
}
