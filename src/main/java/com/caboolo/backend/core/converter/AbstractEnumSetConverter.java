package com.caboolo.backend.core.converter;

import jakarta.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generic converter for Set of Enums stored as a comma-separated string in the database.
 * @param <E> The Enum type
 */
public abstract class AbstractEnumSetConverter<E extends Enum<E>> implements AttributeConverter<Set<E>, String> {

    private final Class<E> enumClass;

    protected AbstractEnumSetConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String convertToDatabaseColumn(Set<E> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "";
        }
        return attribute.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

    @Override
    public Set<E> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(dbData.split(","))
                .filter(s -> !s.isBlank())
                .map(s -> Enum.valueOf(enumClass, s))
                .collect(Collectors.toSet());
    }
}
