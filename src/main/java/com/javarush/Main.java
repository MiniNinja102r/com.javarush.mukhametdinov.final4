package com.javarush;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.config.Config;
import com.javarush.dao.CityDAO;
import com.javarush.dao.CountryDAO;
import com.javarush.database.ConnectionData;
import com.javarush.database.HibernateSessionFactoryProvider;
import com.javarush.database.MysqlConnectionData;
import com.javarush.database.SessionFactoryProvider;
import com.javarush.entity.City;
import com.javarush.service.LiquibaseMigrationRunner;
import com.sun.istack.NotNull;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Main {
    final SessionFactoryProvider sessionFactoryProvider;
    //final RedisClient redisClient;
    final ObjectMapper mapper;
    final CityDAO cityDAO;
    final CountryDAO countryDAO;

    public Main() {
        Config.load();

        final ConnectionData connectionData = new MysqlConnectionData();
        final LiquibaseMigrationRunner lmr = new LiquibaseMigrationRunner(connectionData);
        lmr.runMigrations();

        this.sessionFactoryProvider = new HibernateSessionFactoryProvider();
        sessionFactoryProvider.load();

        //final RedisClient redisClient = prepareRedisClient;

        this.mapper = new ObjectMapper();

        this.cityDAO = new CityDAO(sessionFactoryProvider.getSessionFactory());
        this.countryDAO = new CountryDAO(sessionFactoryProvider.getSessionFactory());
    }

    public static void main(String[] args) {
        final Main main = new Main();
        List<City> cities = main.fetchData();
        main.shutdown();
    }

    @NotNull
    private List<City> fetchData() {
        try (Session session = sessionFactoryProvider.getSessionFactory().openSession()) {
            session.beginTransaction();
            List<City> allCities = new ArrayList<>();

            int totalCount = cityDAO.getTotalCount(session);
            int step = 500;
            for (int i = 0; i < totalCount; i += step) {
                allCities.addAll(cityDAO.getItems(session, i, step));
            }

            session.getTransaction().commit();
            return allCities;
        }
    }

    private void shutdown() {
        sessionFactoryProvider.close();
    }
}
