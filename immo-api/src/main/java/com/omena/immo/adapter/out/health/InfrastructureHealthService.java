package com.omena.immo.adapter.out.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class InfrastructureHealthService {

    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(2);

    private final JdbcTemplate jdbcTemplate;
    private final HttpClient httpClient;
    private final String n8nUrl;
    private final String n8nDbHost;
    private final int n8nDbPort;
    private final String n8nDbName;
    private final String n8nDbUser;
    private final String n8nDbPassword;
    private final String ollamaUrl;

    public InfrastructureHealthService(
            JdbcTemplate jdbcTemplate,
            @Value("${app.integrations.n8n-url:http://n8n:5678}") String n8nUrl,
            @Value("${app.integrations.n8n-db.host:n8n-db}") String n8nDbHost,
            @Value("${app.integrations.n8n-db.port:5432}") int n8nDbPort,
            @Value("${app.integrations.n8n-db.name:n8n}") String n8nDbName,
            @Value("${app.integrations.n8n-db.user:n8n}") String n8nDbUser,
            @Value("${app.integrations.n8n-db.password:n8n-local-change-me}") String n8nDbPassword,
            @Value("${app.integrations.ollama-url:http://host.docker.internal:11434}") String ollamaUrl
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(HTTP_TIMEOUT)
                .build();
        this.n8nUrl = stripTrailingSlash(n8nUrl);
        this.n8nDbHost = n8nDbHost;
        this.n8nDbPort = n8nDbPort;
        this.n8nDbName = n8nDbName;
        this.n8nDbUser = n8nDbUser;
        this.n8nDbPassword = n8nDbPassword;
        this.ollamaUrl = stripTrailingSlash(ollamaUrl);
    }

    public SystemHealth inspect() {
        List<ServiceHealth> services = new ArrayList<>();

        services.add(up(
                "immo-api",
                "API Spring Boot",
                "Application",
                0,
                "API disponible"
        ));
        services.add(checkBusinessDatabase());
        services.add(checkHttp(
                "n8n",
                "n8n",
                "Automatisation",
                n8nUrl + "/healthz",
                false
        ));
        services.add(checkN8nDatabase());
        services.add(checkHttp(
                "ollama",
                "Ollama",
                "IA locale",
                ollamaUrl + "/api/tags",
                true
        ));

        boolean allRequiredUp = services.stream()
                .filter(service -> !service.optional())
                .allMatch(service -> "UP".equals(service.status()));

        boolean allUp = services.stream()
                .allMatch(service -> "UP".equals(service.status()));

        String status = allUp ? "UP" : (allRequiredUp ? "DEGRADED" : "DOWN");

        return new SystemHealth(status, Instant.now(), services);
    }

    private ServiceHealth checkBusinessDatabase() {
        long start = System.nanoTime();

        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            if (result == null || result != 1) {
                return down(
                        "immo-db",
                        "PostgreSQL métier",
                        "Base de données",
                        elapsedMillis(start),
                        "La requête SELECT 1 n'a pas retourné le résultat attendu",
                        false
                );
            }

            return up(
                    "immo-db",
                    "PostgreSQL métier",
                    "Base de données",
                    elapsedMillis(start),
                    "Connexion SQL et SELECT 1 OK"
            );
        } catch (Exception exception) {
            return down(
                    "immo-db",
                    "PostgreSQL métier",
                    "Base de données",
                    elapsedMillis(start),
                    conciseMessage(exception),
                    false
            );
        }
    }

    private ServiceHealth checkN8nDatabase() {
        long start = System.nanoTime();
        String jdbcUrl = "jdbc:postgresql://%s:%d/%s?connectTimeout=2&socketTimeout=2"
                .formatted(n8nDbHost, n8nDbPort, n8nDbName);

        try (
                var connection = DriverManager.getConnection(jdbcUrl, n8nDbUser, n8nDbPassword);
                var statement = connection.createStatement();
                var result = statement.executeQuery("SELECT 1")
        ) {
            if (!result.next() || result.getInt(1) != 1) {
                return down(
                        "n8n-db",
                        "PostgreSQL n8n",
                        "Base de données",
                        elapsedMillis(start),
                        "La requête SELECT 1 n'a pas retourné le résultat attendu",
                        false
                );
            }

            return up(
                    "n8n-db",
                    "PostgreSQL n8n",
                    "Base de données",
                    elapsedMillis(start),
                    "Connexion SQL et SELECT 1 OK"
            );
        } catch (Exception exception) {
            return down(
                    "n8n-db",
                    "PostgreSQL n8n",
                    "Base de données",
                    elapsedMillis(start),
                    conciseMessage(exception),
                    false
            );
        }
    }

    private ServiceHealth checkHttp(
            String id,
            String name,
            String type,
            String url,
            boolean optional
    ) {
        long start = System.nanoTime();

        if (url == null || url.isBlank()) {
            return new ServiceHealth(
                    id,
                    name,
                    type,
                    "NOT_CONFIGURED",
                    optional,
                    -1,
                    "Service non configuré"
            );
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(HTTP_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<Void> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.discarding()
            );

            long latency = elapsedMillis(start);

            if (response.statusCode() >= 200 && response.statusCode() < 400) {
                return up(
                        id,
                        name,
                        type,
                        latency,
                        "HTTP " + response.statusCode()
                );
            }

            return down(
                    id,
                    name,
                    type,
                    latency,
                    "HTTP " + response.statusCode(),
                    optional
            );
        } catch (Exception exception) {
            return down(
                    id,
                    name,
                    type,
                    elapsedMillis(start),
                    conciseMessage(exception),
                    optional
            );
        }
    }

    private ServiceHealth up(String id, String name, String type, long latencyMs, String detail) {
        return new ServiceHealth(id, name, type, "UP", false, latencyMs, detail);
    }

    private ServiceHealth down(
            String id,
            String name,
            String type,
            long latencyMs,
            String detail,
            boolean optional
    ) {
        return new ServiceHealth(id, name, type, "DOWN", optional, latencyMs, detail);
    }

    private long elapsedMillis(long start) {
        return Duration.ofNanos(System.nanoTime() - start).toMillis();
    }

    private String conciseMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }

        return message.length() <= 180 ? message : message.substring(0, 177) + "...";
    }

    private String stripTrailingSlash(String value) {
        if (value == null) {
            return "";
        }

        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    public record SystemHealth(
            String status,
            Instant checkedAt,
            List<ServiceHealth> services
    ) {
    }

    public record ServiceHealth(
            String id,
            String name,
            String type,
            String status,
            boolean optional,
            long latencyMs,
            String detail
    ) {
    }
}
