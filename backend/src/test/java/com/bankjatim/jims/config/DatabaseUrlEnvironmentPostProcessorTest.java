package com.bankjatim.jims.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseUrlEnvironmentPostProcessorTest {

    @Test
    void applicationConfigurationImportsOptionalDotenv() throws IOException {
        List<PropertySource<?>> sources = new YamlPropertySourceLoader()
                .load("application", new ClassPathResource("application.yml"));

        assertThat(sources)
                .extracting(source -> source.getProperty("spring.config.import"))
                .contains("optional:file:.env[.properties]");
    }

    @Test
    void discoversProcessorDuringSpringApplicationStartup() {
        SpringApplication application = isolatedApplication(Map.of());

        try (ConfigurableApplicationContext context = application.run(
                "--DATABASE_URL=postgresql://admin:aninza@192.168.18.67:5432/bank_jatim")) {
            assertThat(context.getEnvironment().getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:postgresql://192.168.18.67:5432/bank_jatim");
            assertThat(context.getEnvironment().getProperty("spring.datasource.username")).isEqualTo("admin");
            assertThat(context.getEnvironment().getProperty("spring.datasource.password")).isEqualTo("aninza");
        }
    }

    @Test
    void loadsDatabaseUrlFromImportedDotenv(@TempDir Path temporaryDirectory) throws IOException {
        Path dotenv = temporaryDirectory.resolve(".env");
        Files.writeString(dotenv,
                "DATABASE_URL=postgresql://dotenv_user:dotenv_password@db.example.test:5432/dotenv_db\n");

        SpringApplication application = isolatedApplication(Map.of());

        try (ConfigurableApplicationContext context = application.run(
                "--spring.config.import=optional:file:" + dotenv.toAbsolutePath() + "[.properties]")) {
            assertThat(context.getEnvironment().getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:postgresql://db.example.test:5432/dotenv_db");
            assertThat(context.getEnvironment().getProperty("spring.datasource.username")).isEqualTo("dotenv_user");
            assertThat(context.getEnvironment().getProperty("spring.datasource.password")).isEqualTo("dotenv_password");
        }
    }

    @Test
    void toleratesMissingOptionalDotenv(@TempDir Path temporaryDirectory) {
        SpringApplication application = isolatedApplication(Map.of());

        try (ConfigurableApplicationContext context = application.run(
                "--spring.config.import=optional:file:"
                        + temporaryDirectory.resolve("missing.env").toAbsolutePath()
                        + "[.properties]")) {
            assertThat(context.isActive()).isTrue();
            assertThat(context.getEnvironment().getProperty("spring.datasource.url")).isNull();
        }
    }

    @Test
    void higherPrecedenceDatabaseUrlOverridesImportedDotenv(@TempDir Path temporaryDirectory) throws IOException {
        Path dotenv = temporaryDirectory.resolve(".env");
        Files.writeString(dotenv,
                "DATABASE_URL=postgresql://dotenv_user:dotenv_password@dotenv.example.test:5432/dotenv_db\n");

        SpringApplication application = isolatedApplication(Map.of(
                "DATABASE_URL",
                "postgresql://override_user:override_password@override.example.test:5432/override_db"));

        try (ConfigurableApplicationContext context = application.run(
                "--spring.config.import=optional:file:" + dotenv.toAbsolutePath() + "[.properties]")) {
            assertThat(context.getEnvironment().getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:postgresql://override.example.test:5432/override_db");
            assertThat(context.getEnvironment().getProperty("spring.datasource.username")).isEqualTo("override_user");
            assertThat(context.getEnvironment().getProperty("spring.datasource.password")).isEqualTo("override_password");
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

    private static SpringApplication isolatedApplication(Map<String, Object> systemEnvironment) {
        SpringApplication application = new SpringApplication(EmptyConfiguration.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().replace(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new SystemEnvironmentPropertySource(
                        StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        systemEnvironment));
        environment.getPropertySources().replace(
                StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
                new MapPropertySource(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, Map.of()));
        application.setEnvironment(environment);
        application.setDefaultProperties(Map.of(
                "spring.main.banner-mode", "off",
                "spring.config.location", "optional:classpath:/isolated-test/"));
        return application;
    }

    @Configuration(proxyBeanMethods = false)
    static class EmptyConfiguration {
    }
}
