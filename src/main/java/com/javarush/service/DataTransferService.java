package com.javarush.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.dao.CityDAO;
import com.javarush.database.SessionFactoryProvider;
import com.javarush.entity.City;
import com.javarush.entity.Country;
import com.javarush.entity.CountryLanguage;
import com.javarush.redis.CityCountry;
import com.javarush.redis.Language;
import com.javarush.redis.RedisManager;
import com.sun.istack.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public final class DataTransferService {
    private static final int FETCH_STEP = 500;
    final RedisManager redisManager;
    final ObjectMapper mapper;
    final SessionFactoryProvider sessionFactoryProvider;
    final CityDAO cityDAO;

    @NotNull
    public List<City> fetchData() {
        try (Session session = sessionFactoryProvider.getSessionFactory().getCurrentSession()) {
            session.beginTransaction();
            List<City> allCities = new ArrayList<>();

            int totalCount = cityDAO.getTotalCount();
            for (int i = 0; i < totalCount; i += FETCH_STEP) {
                allCities.addAll(cityDAO.getItems(i, FETCH_STEP));
            }

            session.getTransaction().commit();
            return allCities;
        }
    }

    @NotNull
    public List<CityCountry> transformData(@NotNull List<City> cities) {
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

    public void pushToRedis(List<CityCountry> data) {
        if (data == null || data.isEmpty())
            return;

        for (CityCountry cityCountry : data) {
            try {
                redisManager.sync().set(
                        String.valueOf(cityCountry.getId()),
                        mapper.writeValueAsString(cityCountry)
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error when trying push data to redis: ", e);
            }
        }
    }
}
