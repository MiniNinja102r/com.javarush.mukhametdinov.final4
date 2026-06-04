package com.javarush.database;

import com.javarush.config.Config;
import com.javarush.util.Request;
import com.sun.istack.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class MysqlConnectionData implements ConnectionData {

    @Override
    public Connection getConnection() {
        // connection pool
        final Config.DatabaseConfig cfg = Config.dbConfig;
        try {
            Connection connection = DriverManager.getConnection(
                    buildUrl(cfg),
                    cfg.username(),
                    cfg.password()
            );

            createSchemaIfNotExists(connection);

            return connection;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildUrl(@NotNull Config.DatabaseConfig cfg) {
        return "jdbc:mysql://" + cfg.host() + ":" + cfg.port() + "/" + cfg.name();
    }

    private void createSchemaIfNotExists(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(Request.createSchemaWorldIfNotExists);
        } catch (SQLException e) {
            System.out.println("Произошла ошибка при попытке создать схему world: " + e.getMessage());
        }
    }
}
