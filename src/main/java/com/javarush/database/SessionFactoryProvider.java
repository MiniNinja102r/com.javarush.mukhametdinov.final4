package com.javarush.database;

import com.sun.istack.NotNull;

public interface SessionFactoryProvider {
    void load();

    @NotNull
    org.hibernate.SessionFactory getSessionFactory();

    void close();
}

