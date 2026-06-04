package com.javarush.service;

import com.javarush.config.Config;
import com.javarush.database.ConnectionData;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class LiquibaseMigrationRunner {
    private static final String CHANGELOG_FILE = "database/liquibase/changelog-master.xml";
    final ConnectionData connectionData;

    public void runMigrations() {
        Map<String, Object> config = new HashMap<>();
        try (Connection connection = connectionData.getConnection()) {
            Scope.child(config, () -> {
                Database database = DatabaseFactory
                        .getInstance()
                        .findCorrectDatabaseImplementation(new JdbcConnection(connection));

                database.setDefaultSchemaName(Config.dbConfig.name());
                database.setLiquibaseSchemaName(Config.dbConfig.name());

                Liquibase liquibase = new Liquibase(
                        CHANGELOG_FILE,
                        new ClassLoaderResourceAccessor(),
                        database
                );

                liquibase.update(new Contexts(), new LabelExpression());
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
