package com.caboolo.backend.ratingandreview.converter;

import com.caboolo.backend.ratingandreview.enums.ReviewTagType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class ReviewTagSetConverter implements AttributeConverter<Set<ReviewTagType>, String> {

    @Override
    public String convertToDatabaseColumn(Set<ReviewTagType> attribute) {
        if (attribute == null || attribute.isEmpty()) return "";
        return attribute.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

    @Override
    public Set<ReviewTagType> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return Set.of();
        return Arrays.stream(dbData.split(","))
                .map(ReviewTagType::valueOf)
                .collect(Collectors.toSet());
    }
}
