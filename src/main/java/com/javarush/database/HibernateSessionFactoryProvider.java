package com.javarush.database;

import com.javarush.config.Config;
import com.javarush.entity.City;
import com.javarush.entity.Country;
import com.javarush.entity.CountryLanguage;
import com.sun.istack.NotNull;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Properties;

@FieldDefaults(level = AccessLevel.PRIVATE)
public final class HibernateSessionFactoryProvider implements SessionFactoryProvider {
    SessionFactory sessionFactory;

    @Override
    public void load() {
        Properties props = this.buildProperties();

        Configuration configuration = new Configuration();
        configuration.setProperties(props);

        configuration.addAnnotatedClass(City.class);
        configuration.addAnnotatedClass(Country.class);
        configuration.addAnnotatedClass(CountryLanguage.class);

        this.sessionFactory = configuration.buildSessionFactory();
    }

    @Override
    @NotNull
    public SessionFactory getSessionFactory() {
        if (sessionFactory == null)
            throw new HibernateException("SessionFactory not initialized!");
        return sessionFactory;
    }

    @Override
    public void close() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }

    @NotNull
    private Properties buildProperties() {
        final var config = Config.dbConfig;
        final Properties props = new Properties();

        props.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        props.setProperty("hibernate.connection.url", config.url());
        props.setProperty("hibernate.connection.username", config.username());
        props.setProperty("hibernate.connection.password", config.password());
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        props.setProperty("hibernate.hbm2ddl.auto", "validate");

        // Connection pool
        props.setProperty("hibernate.c3p0.min_size", String.valueOf(config.minPoolSize()));
        props.setProperty("hibernate.c3p0.max_size", String.valueOf(config.maxPoolSize()));
        props.setProperty("hibernate.c3p0.timeout", String.valueOf(config.timeout()));
        props.setProperty("hibernate.c3p0.idle_test_period", String.valueOf(config.idleTestPeriod()));

        props.setProperty("hibernate.show_sql", "true");
        props.setProperty("hibernate.format_sql", "false");

        return props;
    }
}
