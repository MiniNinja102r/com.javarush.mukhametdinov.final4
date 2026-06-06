package com.javarush.dao;

import com.javarush.entity.City;
import com.sun.istack.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public final class CityDAO {
    final SessionFactory sessionFactory;

    public List<City> getItems(int offset, int limit) {
        Query<City> query = sessionFactory
                .getCurrentSession()
                .createQuery("select c from City c", City.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.list();
    }

    public int getTotalCount() {
        Query<Long> query = sessionFactory
                .getCurrentSession()
                .createQuery("select count(c) from City c", Long.class);
        return Math.toIntExact(query.uniqueResult());
    }

    public City getById(@NotNull Integer id) {
        Query<City> query = sessionFactory
                .getCurrentSession()
                .createQuery("select c from City c join fetch c.country where c.id = :ID", City.class);
        query.setParameter("ID", id);
        return query.getSingleResult();
    }
}
