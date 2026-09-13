package com.bankjatim.jims.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "databaseUrlDatasource";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        URI uri = URI.create(databaseUrl);
        String scheme = uri.getScheme();
        if (!"postgresql".equalsIgnoreCase(scheme) && !"postgres".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("DATABASE_URL must use postgres:// or postgresql://");
        }

        Map<String, Object> datasourceProperties = new LinkedHashMap<>();
        addDatasourceProperty(environment, datasourceProperties, "SPRING_DATASOURCE_URL", "spring.datasource.url", toJdbcUrl(uri));

        String userInfo = uri.getRawUserInfo();
        if (userInfo != null && !userInfo.isBlank()) {
            String[] credentials = userInfo.split(":", 2);
            addDatasourceProperty(
                    environment,
                    datasourceProperties,
                    "SPRING_DATASOURCE_USERNAME",
                    "spring.datasource.username",
                    decode(credentials[0])
            );
            if (credentials.length > 1) {
                addDatasourceProperty(
                        environment,
                        datasourceProperties,
                        "SPRING_DATASOURCE_PASSWORD",
                        "spring.datasource.password",
                        decode(credentials[1])
                );
            }
        }

        if (!datasourceProperties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, datasourceProperties));
        }
    }

    private static void addDatasourceProperty(
            ConfigurableEnvironment environment,
            Map<String, Object> properties,
            String environmentKey,
            String key,
            String value
    ) {
        String overrideValue = environment.getProperty(environmentKey);
        String resolvedValue = overrideValue != null && !overrideValue.isBlank() ? overrideValue : value;

        if (resolvedValue != null && !resolvedValue.isBlank()) {
            properties.put(key, resolvedValue);
        }
    }

    private static String toJdbcUrl(URI uri) {
        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
        jdbcUrl.append(uri.getHost());

        if (uri.getPort() > -1) {
            jdbcUrl.append(':').append(uri.getPort());
        }

        String path = uri.getRawPath();
        if (path == null || path.isBlank()) {
            jdbcUrl.append('/');
        } else {
            jdbcUrl.append(path);
        }

        String query = uri.getRawQuery();
        if (query != null && !query.isBlank()) {
            jdbcUrl.append('?').append(query);
        }

        return jdbcUrl.toString();
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
