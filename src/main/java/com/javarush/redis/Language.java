package com.javarush.redis;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@Getter
@Setter
public final class Language {
    String language;

    Boolean isOfficial;

    BigDecimal percentage;
}
