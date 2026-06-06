package com.javarush.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.dao.CityDAO;
import com.javarush.database.SessionFactoryProvider;
import com.javarush.entity.City;
import com.javarush.entity.CountryLanguage;
import com.javarush.redis.CityCountry;
import com.javarush.redis.RedisManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.Session;

import java.util.List;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public final class DataSpeedTestService {
    final RedisManager redisManager;
    final ObjectMapper mapper;
    final SessionFactoryProvider sessionFactoryProvider;
    final CityDAO cityDAO;

    public void testRedisData(List<Integer> ids) {
        if (ids == null || ids.isEmpty())
            return;

        for (Integer id : ids) {
            String value = redisManager.sync().get(String.valueOf(id));
            try {
                mapper.readValue(value, CityCountry.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Ошибка при попытке проверить скорость redis", e);
            }
        }
    }

    public void testMysqlData(List<Integer> ids) {
        if (ids == null || ids.isEmpty())
            return;

        try (Session session = sessionFactoryProvider.getSessionFactory().getCurrentSession()) {
            session.beginTransaction();
            for (Integer id : ids) {
                City city = cityDAO.getById(id);
                Set<CountryLanguage> languages = city.getCountry().getLanguages();
            }
            session.getTransaction().commit();
        }
    }
}
