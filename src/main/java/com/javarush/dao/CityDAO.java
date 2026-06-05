package com.javarush.dao;

import com.javarush.entity.City;
import com.javarush.util.Request;
import com.sun.istack.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public final class CityDAO {
    final SessionFactory sessionFactory;

    public List<City> getItems(@NotNull Session session, int offset, int limit) {
        Query<City> query = session.createQuery(Request.selectFromCity, City.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.list();
    }

    public int getTotalCount(@NotNull Session session) {
        Query<Long> query = session.createQuery(Request.selectCountFromCity, Long.class);
        return Math.toIntExact(query.uniqueResult());
    }
}
