package com.javarush.config;

import com.javarush.exception.ConfigException;
import com.javarush.util.Constant;
import com.sun.istack.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public final class Config {
    private static final String CONFIG_FILE = "config.yml";
    private static Map<String, Object> data;
    public static DatabaseConfig dbConfig;
    public static RedisConfig redisConfig;
    public static GeneralConfig generalConfig;

    public static void load() {
        Yaml yaml = new Yaml();
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null)
                throw new ConfigException("config.yml файл не был найден в resources.");

            data = yaml.load(input);
            if (data == null || data.isEmpty())
                throw new ConfigException("Произошла ошибка при чтении config.yml: файл пуст или невалиден");

            loadDatabase();
            loadRedis();
            loadGeneral();
        } catch (Exception e) {
            throw new ConfigException("Не удалось загрузить конфигурацию: " + e.getMessage());
        }
    }

    private static void loadDatabase() {
        final Map<String, Object> section = (Map<String, Object>) data.get("database");
        requireConfigSection(section, "database");

        String host = (String) section.get("host");
        String name = (String) section.get("name");
        String username = (String) section.get("username");
        String password = (String) section.get("password");
        int port = (Integer) section.get("port");
        int timeout = (Integer) section.get("timeout");
        int idleTestPeriod = (Integer) section.get("idle_test_period");
        int minPoolSize = (Integer) section.get("min_pool_size");
        int maxPoolSize = (Integer) section.get("max_pool_size");
        dbConfig = new DatabaseConfig(
                host, name, username, password, buildDbUrl(host, name, port), port,
                timeout, idleTestPeriod, minPoolSize, maxPoolSize
        );
    }

    private static void loadRedis() {
        final Map<String, Object> section = (Map<String, Object>) data.get("redis");
        requireConfigSection(section, "redis");

        String host = (String) section.get("host");
        String password = (String) section.get("password");
        int port = (Integer) section.get("port");
        int timeout = (Integer) section.get("timeout");
        int threadPoolSize = (Integer) section.get("thread_pool_size");
        int terminationAwaitSec = (Integer) section.get("termination_await_sec");
        redisConfig = new RedisConfig(
                host, password, port, timeout,
                threadPoolSize, terminationAwaitSec
        );
    }

    private static void loadGeneral() {
        final Map<String, Object> section = (Map<String, Object>) data.get("general");
        requireConfigSection(section, "general");

        int checkQuantity = (Integer) section.get("check_quantity");
        int checkPauseInSec = (Integer) section.get("check_pause_sec");
        generalConfig = new GeneralConfig(checkQuantity, checkPauseInSec);
    }

    @NotNull
    private static String buildDbUrl(@NotNull String host,
                                     @NotNull String name,
                                     int port) {
        return String.format(Constant.dbUrlFormat, host, port, name);
    }

    private static void requireConfigSection(Object section, @NotNull String name) {
        if (section == null) {
            throw new ConfigException("Missing config section: " + name);
        }
    }

    public record DatabaseConfig(@NotNull String host,
                                 @NotNull String name,
                                 @NotNull String username,
                                 @NotNull String password,
                                 @NotNull String url,
                                 int port,
                                 int timeout,
                                 int idleTestPeriod,
                                 int minPoolSize,
                                 int maxPoolSize) {
    }

    public record RedisConfig(@NotNull String host,
                              @NotNull String password,
                              int port,
                              int timeout,
                              int threadPoolSize,
                              int terminationAwaitSec) {
    }

    public record GeneralConfig(int checkQuantity, int checkPauseInSec) {
    }
}
