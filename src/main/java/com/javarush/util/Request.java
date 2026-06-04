package com.javarush.util;

public interface Request {
    String createSchemaWorldIfNotExists = "CREATE SCHEMA IF NOT EXISTS world";
    String selectFromCountry = "select c from Country c";
    String selectFromCity = "select c from City c";
    String selectCountFromCity = "select count(c) from City c";
}
