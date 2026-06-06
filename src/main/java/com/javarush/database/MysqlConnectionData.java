package com.javarush.database;

import com.javarush.config.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class MysqlConnectionData implements ConnectionData {

    @Override
    public Connection getConnection() {
        // connection pool
        final Config.DatabaseConfig config = Config.dbConfig;
        try {
            Connection connection = DriverManager.getConnection(
                    config.url(),
                    config.username(),
                    config.password()
            );

            createSchemaIfNotExists(connection);

            return connection;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createSchemaIfNotExists(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS world");
        } catch (SQLException e) {
            System.out.println("Произошла ошибка при попытке создать схему world: " + e.getMessage());
        }
    }
}
