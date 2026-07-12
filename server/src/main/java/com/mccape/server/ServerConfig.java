package com.mccape.server;

import java.nio.file.Path;

public record ServerConfig(int port, Path dataDirectory, boolean insecureDevelopmentAuth, String databaseUrl) {
    public ServerConfig(int port, Path dataDirectory, boolean insecureDevelopmentAuth) {
        this(port, dataDirectory, insecureDevelopmentAuth, null);
    }

    public static ServerConfig environment() {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        Path data = Path.of(System.getenv().getOrDefault("DATA_DIR", "server-data")).toAbsolutePath();
        boolean dev = Boolean.parseBoolean(System.getenv().getOrDefault("ALLOW_INSECURE_DEV_AUTH", "false"));
        String environment = System.getenv().getOrDefault("APP_ENV", "development");
        if (dev && "production".equalsIgnoreCase(environment))
            throw new IllegalStateException("Insecure development authentication cannot run in production");
        String databaseUrl = System.getenv("DATABASE_URL");
        return new ServerConfig(port, data, dev, databaseUrl);
    }
}
