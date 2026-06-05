package com.javarush;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.config.Config;
import com.javarush.dao.CityDAO;
import com.javarush.dao.CountryDAO;
import com.javarush.database.ConnectionData;
import com.javarush.database.HibernateSessionFactoryProvider;
import com.javarush.database.MysqlConnectionData;
import com.javarush.database.SessionFactoryProvider;
import com.javarush.entity.City;
import com.javarush.entity.Country;
import com.javarush.entity.CountryLanguage;
import com.javarush.redis.CityCountry;
import com.javarush.redis.Language;
import com.javarush.service.LiquibaseMigrationRunner;
import com.sun.istack.NotNull;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.Session;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Main {
    final SessionFactoryProvider sessionFactoryProvider;
    final RedisClient redisClient;
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

        this.redisClient = prepareRedisClient();

        this.mapper = new ObjectMapper();

        this.cityDAO = new CityDAO(sessionFactoryProvider.getSessionFactory());
        this.countryDAO = new CountryDAO(sessionFactoryProvider.getSessionFactory());
    }

    public static void main(String[] args) {
        final Main main = new Main();
        List<City> cities = main.fetchData();
        List<CityCountry> preparedData = main.transformData(cities);
        main.pushToRedis(preparedData);

        main.shutdown();
    }

    @NotNull
    private List<City> fetchData() {
        try (Session session = sessionFactoryProvider.getSessionFactory().getCurrentSession()) {
            session.beginTransaction();
            List<City> allCities = new ArrayList<>();

            int totalCount = cityDAO.getTotalCount();
            int step = 500;
            for (int i = 0; i < totalCount; i += step) {
                allCities.addAll(cityDAO.getItems(i, step));
            }

            session.getTransaction().commit();
            return allCities;
        }
    }

    @NotNull
    private List<CityCountry> transformData(@NotNull List<City> cities) {
        return cities.stream().map(city -> {
            CityCountry res = new CityCountry();
            res.setId(city.getId());
            res.setName(city.getName());
            res.setPopulation(city.getPopulation());
            res.setDistrict(city.getDistrict());

            Country country = city.getCountry();
            res.setAlternativeCountryCode(country.getCode2());
            res.setContinent(country.getContinent());
            res.setCountryCode(country.getCode());
            res.setCountryName(country.getName());
            res.setCountryPopulation(country.getPopulation());
            res.setCountryRegion(country.getRegion());
            res.setCountrySurfaceArea(country.getSurfaceArea());
            Set<CountryLanguage> countryLanguages = country.getLanguages();
            Set<Language> languages = countryLanguages.stream().map(cl -> {
                Language language = new Language();
                language.setLanguage(cl.getLanguage());
                language.setIsOfficial(cl.getIsOfficial());
                language.setPercentage(cl.getPercentage());
                return language;
            }).collect(Collectors.toSet());
            res.setLanguages(languages);

            return res;
        }).collect(Collectors.toList());
    }

    @NotNull
    private static RedisClient prepareRedisClient() {
        final RedisURI uri = RedisURI.Builder
                .redis(Config.redisConfig.host(), Config.redisConfig.port())
                .withPassword(Config.redisConfig.password().toCharArray())
                .withTimeout(Duration.ofMillis(Config.redisConfig.timeout()))
                .build();

        RedisClient redisClient = RedisClient.create(uri);
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            System.out.println("\nConnected to Redis\n");
        }
        return redisClient;
    }

    private void pushToRedis(List<CityCountry> data) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityCountry cityCountry : data) {
                try {
                    sync.set(String.valueOf(cityCountry.getId()), mapper.writeValueAsString(cityCountry));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void shutdown() {
        sessionFactoryProvider.close();
    }
}
