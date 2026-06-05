package com.javarush.util;

public interface Request {
    String createSchemaWorldIfNotExists = "CREATE SCHEMA IF NOT EXISTS world";
    String selectFromCountryJoinFetchLanguage = "select c from Country c join fetch c.languages";
    String selectFromCity = "select c from City c";
    String selectCountFromCity = "select count(c) from City c";
}
