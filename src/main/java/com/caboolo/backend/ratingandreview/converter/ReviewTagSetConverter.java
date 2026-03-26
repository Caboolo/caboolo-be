package com.caboolo.backend.ratingandreview.converter;

import com.caboolo.backend.core.converter.AbstractEnumSetConverter;
import com.caboolo.backend.ratingandreview.enums.ReviewTagType;
import jakarta.persistence.Converter;

@Converter
public class ReviewTagSetConverter extends AbstractEnumSetConverter<ReviewTagType> {

    public ReviewTagSetConverter() {
        super(ReviewTagType.class);
    }
}
