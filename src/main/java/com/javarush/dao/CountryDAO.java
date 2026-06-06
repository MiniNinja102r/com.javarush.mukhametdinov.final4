package com.javarush.dao;

import com.javarush.entity.Country;
import org.hibernate.query.Query;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.SessionFactory;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public final class CountryDAO {
    final SessionFactory sessionFactory;

    public List<Country> getAll() {
        Query<Country> query = sessionFactory
                .getCurrentSession()
                .createQuery("select c from Country c join fetch c.languages", Country.class);
        return query.list();
    }
}
