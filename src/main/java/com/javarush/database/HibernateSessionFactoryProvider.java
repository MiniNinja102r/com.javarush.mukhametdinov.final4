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
import org.hibernate.cfg.AvailableSettings;
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

        props.setProperty(AvailableSettings.DRIVER, "com.mysql.cj.jdbc.Driver");
        props.setProperty(AvailableSettings.URL, config.url());
        props.setProperty(AvailableSettings.USER, config.username());
        props.setProperty(AvailableSettings.PASS, config.password());
        props.setProperty(AvailableSettings.DIALECT, "org.hibernate.dialect.MySQLDialect");
        props.setProperty(AvailableSettings.HBM2DDL_AUTO, "validate");
        props.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread");

        // Connection pool
        props.setProperty(AvailableSettings.C3P0_MIN_SIZE, String.valueOf(config.minPoolSize()));
        props.setProperty(AvailableSettings.C3P0_MAX_SIZE, String.valueOf(config.maxPoolSize()));
        props.setProperty(AvailableSettings.C3P0_TIMEOUT, String.valueOf(config.timeout()));
        props.setProperty(AvailableSettings.C3P0_IDLE_TEST_PERIOD, String.valueOf(config.idleTestPeriod()));

        props.setProperty(AvailableSettings.SHOW_SQL, "true");
        props.setProperty(AvailableSettings.FORMAT_SQL, "false");

        return props;
    }
}
