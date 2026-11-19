package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class CloudDatabaseEnvironmentPostProcessorTest {

    private final CloudDatabaseEnvironmentPostProcessor processor = new CloudDatabaseEnvironmentPostProcessor();
    private final SpringApplication application = new SpringApplication(DemoApplication.class);

    @Test
    void testPostgresUrlConversionWithCredentialsAndSsl() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "postgres://testuser:secret123@ep-cool.ap-southeast-1.aws.neon.tech:5432/xyz_auto_service?sslmode=require");

        processor.postProcessEnvironment(env, application);

        assertEquals("jdbc:postgresql://ep-cool.ap-southeast-1.aws.neon.tech:5432/xyz_auto_service?sslmode=require", env.getProperty("spring.datasource.url"));
        assertEquals("testuser", env.getProperty("spring.datasource.username"));
        assertEquals("secret123", env.getProperty("spring.datasource.password"));
    }

    @Test
    void testPostgresUrlAppendsSslModeWhenMissing() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "postgresql://admin:pass@dpg-c123.oregon-postgres.render.com/xyz_auto_service");

        processor.postProcessEnvironment(env, application);

        assertEquals("jdbc:postgresql://dpg-c123.oregon-postgres.render.com:5432/xyz_auto_service?sslmode=require", env.getProperty("spring.datasource.url"));
        assertEquals("admin", env.getProperty("spring.datasource.username"));
        assertEquals("pass", env.getProperty("spring.datasource.password"));
    }

    @Test
    void testJdbcUrlPassedDirectly() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "jdbc:postgresql://localhost:5432/xyz_auto_service?sslmode=prefer");

        processor.postProcessEnvironment(env, application);

        assertEquals("jdbc:postgresql://localhost:5432/xyz_auto_service?sslmode=prefer", env.getProperty("spring.datasource.url"));
    }

    @Test
    void testNoOpWhenDatabaseUrlNotProvided() {
        MockEnvironment env = new MockEnvironment();

        processor.postProcessEnvironment(env, application);

        assertNull(env.getProperty("spring.datasource.url"));
    }
}
