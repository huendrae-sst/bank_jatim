package com.bankjatim.jims.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseUrlEnvironmentPostProcessorTest {

    @Test
    void discoversProcessorDuringSpringApplicationStartup() {
        SpringApplication application = new SpringApplication(EmptyConfiguration.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setDefaultProperties(Map.of(
                "spring.main.banner-mode", "off",
                "DATABASE_URL", "postgresql://admin:aninza@192.168.18.67:5432/bank_jatim"));

        try (ConfigurableApplicationContext context = application.run()) {
            assertThat(context.getEnvironment().getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:postgresql://192.168.18.67:5432/bank_jatim");
            assertThat(context.getEnvironment().getProperty("spring.datasource.username")).isEqualTo("admin");
            assertThat(context.getEnvironment().getProperty("spring.datasource.password")).isEqualTo("aninza");
        }
    }

    @Test
    void mapsDatabaseUrlToSpringDatasourceProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgresql://admin:aninza@192.168.18.67:5432/bank_jatim");

        new DatabaseUrlEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://192.168.18.67:5432/bank_jatim");
        assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("admin");
        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("aninza");
    }

    @Test
    void preservesExplicitSpringDatasourceProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgresql://admin:aninza@192.168.18.67:5432/bank_jatim")
                .withProperty("SPRING_DATASOURCE_URL", "jdbc:postgresql://localhost:5432/local_db")
                .withProperty("SPRING_DATASOURCE_USERNAME", "local_user")
                .withProperty("SPRING_DATASOURCE_PASSWORD", "local_password");

        new DatabaseUrlEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("spring.datasource.url")).isEqualTo("jdbc:postgresql://localhost:5432/local_db");
        assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("local_user");
        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("local_password");
    }

    @Test
    void databaseUrlOverridesYamlDefaults() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgresql://admin:aninza@192.168.18.67:5432/bank_jatim")
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/bank_jatim")
                .withProperty("spring.datasource.username", "postgres")
                .withProperty("spring.datasource.password", "secretpassword");

        new DatabaseUrlEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://192.168.18.67:5432/bank_jatim");
        assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("admin");
        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("aninza");
    }

    @Test
    void leavesDatasourceUnsetWhenDatabaseUrlIsMissing() {
        MockEnvironment environment = new MockEnvironment();

        new DatabaseUrlEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("spring.datasource.url")).isNull();
        assertThat(environment.getProperty("spring.datasource.username")).isNull();
        assertThat(environment.getProperty("spring.datasource.password")).isNull();
    }

    @Configuration(proxyBeanMethods = false)
    static class EmptyConfiguration {
    }
}
