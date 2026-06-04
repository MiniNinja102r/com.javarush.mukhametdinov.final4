package com.javarush.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(schema = "world", name = "country")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
@AllArgsConstructor
@NoArgsConstructor
public final class Country {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    Integer id;

    @Column(name = "code", length = 3, nullable = false)
    String code;

    @Column(name = "code_2", length = 2, nullable = false)
    String code2;

    @Column(name = "name", length = 52, nullable = false)
    String name;

    @Column(name = "continent")
    @Enumerated(EnumType.ORDINAL)
    Continent continent;

    @Column(name = "region", length = 26, nullable = false)
    String region;

    @Column(name = "surface_area")
    BigDecimal surfaceArea;

    @Column(name = "indep_year")
    Short independenceYear;

    @Column(name = "population", nullable = false)
    Integer population;

    @Column(name = "life_expectancy")
    BigDecimal lifeExpectancy;

    @Column(name = "gnp")
    BigDecimal gnp;

    @Column(name = "gnpo_id")
    BigDecimal gnpoId;

    @Column(name = "local_name", length = 45, nullable = false)
    String localName;

    @Column(name = "government_form", length = 45, nullable = false)
    String governmentForm;

    @Column(name = "head_of_state", length = 60, nullable = false)
    String headOfState;

    @OneToOne
    @JoinColumn(name = "capital")
    City city;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id")
    Set<CountryLanguage> languages;
}
