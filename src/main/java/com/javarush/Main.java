package com.javarush;

import com.javarush.config.Config;
import com.javarush.database.ConnectionData;
import com.javarush.database.HibernateSessionFactoryProvider;
import com.javarush.database.MysqlConnectionData;
import com.javarush.database.SessionFactoryProvider;
import com.javarush.service.LiquibaseMigrationRunner;

public class Main {
    public static void main(String[] args) {
        Config.load();

        final SessionFactoryProvider sessionFactory = new HibernateSessionFactoryProvider();
        sessionFactory.load();

        final ConnectionData connectionData = new MysqlConnectionData();

        final LiquibaseMigrationRunner lmr = new LiquibaseMigrationRunner(connectionData);
        lmr.runMigrations();

        //sessionFactory.close();
    }
}
