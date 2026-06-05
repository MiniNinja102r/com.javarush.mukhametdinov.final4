package com.javarush.redis;

import com.javarush.entity.Continent;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public final class CityCountry {
    @EqualsAndHashCode.Include
    Integer id;

    String name;

    String district;

    Integer population;

    String countryCode;

    String alternativeCountryCode;

    String countryName;

    Continent continent;

    String countryRegion;

    BigDecimal countrySurfaceArea;

    Integer countryPopulation;

    Set<Language> languages;
}
