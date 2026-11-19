package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Automatically converts standard cloud PostgreSQL connection strings
 * (e.g. from Neon, Supabase, Render, Railway, Heroku) into Spring Boot JDBC configuration.
 * Handles DATABASE_URL in postgres:// or postgresql:// formats, extracts credentials,
 * and ensures sslmode=require for cloud providers.
 */
public class CloudDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        Map<String, Object> props = new HashMap<>();

        if (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://")) {
            try {
                // Replace scheme with standard http for safe URI parsing
                String rawUri = databaseUrl.replaceFirst("^postgres(ql)?://", "http://");
                URI uri = URI.create(rawUri);

                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                String query = uri.getQuery();

                StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                        .append(host)
                        .append(":")
                        .append(port)
                        .append((path != null && !path.isEmpty()) ? path : "/xyz_auto_service");

                if (query != null && !query.isEmpty()) {
                    jdbcUrl.append("?").append(query);
                    if (!query.contains("sslmode=")) {
                        jdbcUrl.append("&sslmode=require");
                    }
                } else {
                    jdbcUrl.append("?sslmode=require");
                }

                props.put("spring.datasource.url", jdbcUrl.toString());

                String userInfo = uri.getUserInfo();
                if (userInfo != null && !userInfo.isEmpty()) {
                    String[] parts = userInfo.split(":", 2);
                    if (parts.length > 0 && !parts[0].isEmpty()) {
                        props.put("spring.datasource.username", parts[0]);
                    }
                    if (parts.length > 1) {
                        props.put("spring.datasource.password", parts[1]);
                    }
                }
            } catch (Exception e) {
                // Fallback: prefix jdbc: if standard URI parsing fails
                String fallback = databaseUrl.startsWith("jdbc:") ? databaseUrl : "jdbc:" + databaseUrl;
                props.put("spring.datasource.url", fallback);
            }
        } else if (!databaseUrl.startsWith("jdbc:")) {
            props.put("spring.datasource.url", "jdbc:" + databaseUrl);
        } else {
            props.put("spring.datasource.url", databaseUrl);
        }

        if (!props.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("cloudDatabaseConfiguration", props));
        }
    }
}
