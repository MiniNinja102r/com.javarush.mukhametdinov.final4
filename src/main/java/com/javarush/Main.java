package com.javarush;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.config.Config;
import com.javarush.dao.CityDAO;
import com.javarush.dao.CountryDAO;
import com.javarush.database.ConnectionData;
import com.javarush.database.HibernateSessionFactoryProvider;
import com.javarush.database.MysqlConnectionData;
import com.javarush.database.SessionFactoryProvider;
import com.javarush.service.LiquibaseMigrationRunner;
import io.lettuce.core.RedisClient;

public class Main {
    public static void main(String[] args) {
        Config.load();

        final SessionFactoryProvider sessionFactoryProvider = new HibernateSessionFactoryProvider();
        sessionFactoryProvider.load();

        final ConnectionData connectionData = new MysqlConnectionData();
        final LiquibaseMigrationRunner lmr = new LiquibaseMigrationRunner(connectionData);
        lmr.runMigrations();

        //final RedisClient redisClient = prepareRedisClient;
        final ObjectMapper mapper = new ObjectMapper();

        final CityDAO cityDAO = new CityDAO(sessionFactoryProvider.getSessionFactory());
        final CountryDAO countryDAO = new CountryDAO(sessionFactoryProvider.getSessionFactory());


        //sessionFactory.close();
    }
}
