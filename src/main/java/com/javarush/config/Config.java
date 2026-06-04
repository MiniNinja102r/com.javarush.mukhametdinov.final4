package com.javarush.config;

import com.javarush.exception.ConfigException;
import com.sun.istack.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public final class Config {
    private static final String CONFIG_FILE = "config.yml";
    private static Map<String, Object> data;
    public static DatabaseConfig dbConfig;

    public static void load() {
        Yaml yaml = new Yaml();
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null)
                throw new ConfigException("config.yml файл не был найден в resources.");

            data = yaml.load(input);
            if (data == null || data.isEmpty())
                throw new ConfigException("Произошла ошибка при чтении config.yml: файл пуст или невалиден");

            loadDatabase();
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
        dbConfig = new DatabaseConfig(host, name, username, password, port);
    }

    public static void requireConfigSection(Object section, @NotNull String name) {
        if (section == null) {
            throw new ConfigException("Missing config section: " + name);
        }
    }

    public record DatabaseConfig(@NotNull String host,
                                 @NotNull String name,
                                 @NotNull String username,
                                 @NotNull String password,
                                 int port) {
    }
}
