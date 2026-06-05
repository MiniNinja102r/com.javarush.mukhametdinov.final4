package com.javarush.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(
        schema = "world",
        name = "city",
        indexes = {
                @Index(name = "city_ibfk_1_idx", columnList = "country_id")
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public final class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, unique = true)
    Integer id;

    @Column(name = "name", nullable = false, length = 35)
    String name;

    @ManyToOne
    @JoinColumn(name = "country_id")
    Country country;

    @Column(name = "district", length = 20, nullable = false)
    String district;

    @Column(name = "population", nullable = false)
    Integer population;
}
